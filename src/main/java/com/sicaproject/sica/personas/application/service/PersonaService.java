package com.sicaproject.sica.personas.application.service;

import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.iam.application.service.RbacService;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.personas.application.port.out.PersonaRepository;
import com.sicaproject.sica.personas.domain.Persona;
import java.util.List;
import java.util.Optional;

public class PersonaService {

    private final PersonaRepository personaRepository;
    private final RbacService rbacService;
    private final AuditoriaService auditoriaService;

    public PersonaService(PersonaRepository personaRepository) {
        this(personaRepository, null, null);
    }

    public PersonaService(PersonaRepository personaRepository, RbacService rbacService, AuditoriaService auditoriaService) {
        this.personaRepository = personaRepository;
        this.rbacService = rbacService;
        this.auditoriaService = auditoriaService;
    }

    public Persona crearPersona(Persona persona) {
        return guardar(persona, null);
    }

    public Persona guardar(Persona persona) {
        return guardar(persona, null);
    }

    // =========================================================================
    // CREACIÓN Y ACTUALIZACIÓN DE PERSONAS CON AUDITORÍA
    // =========================================================================
    /**
     * Guarda una persona en PostgreSQL (creación si id == 0, actualización si id > 0).
     * 
     * [PISTAS PARA EL DEBUG / EVALUACIÓN]:
     * - boolean esNueva = (persona.getId() == 0L);
     * - Si persona.getId() == 0, el repositorio hace persist(entity); si no, hace merge(entity).
     * - Audita PERSONA_CREADA o PERSONA_ACTUALIZADA según corresponda.
     */
    public Persona guardar(Persona persona, Usuario usuarioSolicitante) {
        boolean esNueva = (persona.getId() == 0L);
        Persona guardada = personaRepository.guardar(persona);
        if (auditoriaService != null) {
            Long usuarioId = usuarioSolicitante != null ? usuarioSolicitante.getId() : null;
            String accion = esNueva ? "PERSONA_CREADA" : "PERSONA_ACTUALIZADA";
            auditoriaService.registrar(usuarioId, accion, "PERSONA", guardada.getId(),
                    (esNueva ? "Creación de persona: " : "Actualización de datos de persona: ") +
                            guardada.getNombre() + " (" + guardada.getTipoDocumento() + " " + guardada.getDocumento() + ")",
                    "EXITOSO");
        }
        return guardada;
    }

    public Optional<Persona> porDocumento(String documento) {
        return personaRepository.buscarPorDocumento(documento);
    }

    public Optional<Persona> porTipoYDocumento(String tipoDocumento, String documento) {
        return personaRepository.buscarPorTipoYDocumento(tipoDocumento, documento);
    }

    public Persona incrementarVisitas(Persona persona) {
        persona.setTotalVisitas(persona.getTotalVisitas() + 1);
        return personaRepository.guardar(persona);
    }

    public Optional<Persona> porId(Long id) {
        return personaRepository.porId(id);
    }

    public List<Persona> listarTodas() {
        return personaRepository.listarTodas();
    }

    // =========================================================================
    // RESTRICCIÓN DE ACCESO: BLOQUEO (LISTA NEGRA)
    // =========================================================================
    /**
     * Bloquea a una persona para que no pueda ingresar a Zona Acme bajo ninguna circunstancia.
     * 
     * [PISTAS PARA EL DEBUG / EVALUACIÓN]:
     * - Requiere permiso 'bloquear_persona'.
     * - Fija persona.setBloqueado(true);
     * - Fija persona.setMotivoBloqueo(motivo);
     * - Audita acción 'PERSONA_BLOQUEADA'.
     * - Si borran el llamado a personaRepository.actualizar(persona), el cambio no se guardará en PostgreSQL.
     */
    public void bloquearPersona(Persona persona, String motivo) {
        bloquearPersona(persona, motivo, null);
    }

    public void bloquearPersona(Persona persona, String motivo, Usuario usuarioSolicitante) {
        if (usuarioSolicitante != null && rbacService != null) {
            rbacService.verificarPermiso(usuarioSolicitante, "bloquear_persona");
        }
        persona.setBloqueado(true);
        persona.setMotivoBloqueo(motivo);
        personaRepository.actualizar(persona);

        if (auditoriaService != null) {
            Long usuarioId = usuarioSolicitante != null ? usuarioSolicitante.getId() : null;
            String detalle = "Persona '" + persona.getNombre() + "' bloqueada. Motivo: " + (motivo != null ? motivo : "No especificado");
            auditoriaService.registrar(usuarioId, "PERSONA_BLOQUEADA", "PERSONA", persona.getId(), detalle, "EXITOSO");
        }
    }

    // =========================================================================
    // RESTRICCIÓN DE ACCESO: DESBLOQUEO
    // =========================================================================
    /**
     * Levanta el bloqueo de acceso a una persona.
     * 
     * [PISTAS PARA EL DEBUG]:
     * - persona.setBloqueado(false);
     * - persona.setMotivoBloqueo(null);
     * - Audita 'PERSONA_DESBLOQUEADA'.
     */
    public void desbloquearPersona(Persona persona) {
        desbloquearPersona(persona, null);
    }

    public void desbloquearPersona(Persona persona, Usuario usuarioSolicitante) {
        if (usuarioSolicitante != null && rbacService != null) {
            rbacService.verificarPermiso(usuarioSolicitante, "bloquear_persona");
        }
        persona.setBloqueado(false);
        persona.setMotivoBloqueo(null);
        personaRepository.actualizar(persona);

        if (auditoriaService != null) {
            Long usuarioId = usuarioSolicitante != null ? usuarioSolicitante.getId() : null;
            String detalle = "Persona '" + persona.getNombre() + "' desbloqueada en el sistema";
            auditoriaService.registrar(usuarioId, "PERSONA_DESBLOQUEADA", "PERSONA", persona.getId(), detalle, "EXITOSO");
        }
    }

    // =========================================================================
    // ELIMINACIÓN DE PERSONAS (EXCLUSIVO ADMINISTRADOR)
    // =========================================================================
    /**
     * Elimina físicamente a una persona del sistema.
     * 
     * [PISTAS PARA EL DEBUG]:
     * - Verifica que el usuario tenga permiso de administrador ('ver_panel_admin').
     * - Llama a personaRepository.eliminar(id);
     * - Audita 'PERSONA_ELIMINADA'.
     * - Si la persona tiene llaves foráneas activas (visitas previas), JPA lanzará una excepción
     *   que el controlador captura para proteger la integridad referencial.
     */
    public void eliminarPersona(Long id, Usuario usuarioSolicitante) {
        if (usuarioSolicitante != null && rbacService != null) {
            rbacService.verificarPermiso(usuarioSolicitante, "ver_panel_admin");
        }
        Optional<Persona> personaOpt = personaRepository.porId(id);
        personaRepository.eliminar(id);

        if (auditoriaService != null && personaOpt.isPresent()) {
            Persona p = personaOpt.get();
            Long usuarioId = usuarioSolicitante != null ? usuarioSolicitante.getId() : null;
            String detalle = "Eliminación de persona del sistema: " + p.getNombre() + " (" + p.getTipoDocumento() + " " + p.getDocumento() + ")";
            auditoriaService.registrar(usuarioId, "PERSONA_ELIMINADA", "PERSONA", id, detalle, "EXITOSO");
        }
    }
}