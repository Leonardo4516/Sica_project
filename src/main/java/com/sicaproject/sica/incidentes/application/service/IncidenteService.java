package com.sicaproject.sica.incidentes.application.service;

import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.service.RbacService;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.incidentes.application.port.out.IncidenteRepository;
import com.sicaproject.sica.incidentes.domain.Incidente;
import com.sicaproject.sica.incidentes.domain.SeveridadIncidente;
import com.sicaproject.sica.personas.domain.Persona;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class IncidenteService {

    private final IncidenteRepository incidenteRepository;
    private final RbacService rbacService;
    private final AuditoriaService auditoriaService;

    public IncidenteService(IncidenteRepository incidenteRepository, RbacService rbacService, AuditoriaService auditoriaService) {
        this.incidenteRepository = incidenteRepository;
        this.rbacService = rbacService;
        this.auditoriaService = auditoriaService;
    }

    // =========================================================================
    // REPORTE DE INCIDENTES CON CLASIFICACIÓN DE SEVERIDAD Y AUDITORÍA
    // =========================================================================
    /**
     * Registra un nuevo incidente o novedad de seguridad perimetral en las instalaciones,
     * asociándolo opcionalmente a personas o empresas involucradas.
     * 
     * @param titulo Resumen descriptivo del incidente.
     * @param descripcion Detalle pormenorizado de los hechos.
     * @param severidad Nivel de riesgo asignado (BAJA, MEDIA, ALTA, CRITICA).
     * @param personaInvolucrada Persona relacionada con el evento (opcional).
     * @param empresaInvolucrada Empresa residente afectada (opcional).
     * @param reportadoPor Usuario operador o funcionario que emite el reporte.
     * @return Entidad Incidente persistida en estado inicial ABIERTO.
     */
    public Incidente reportarIncidente(String titulo, String descripcion, SeveridadIncidente severidad,
                                       Persona personaInvolucrada, Empresa empresaInvolucrada,
                                       Usuario reportadoPor) {
        if (reportadoPor != null && rbacService != null) {
            rbacService.verificarPermiso(reportadoPor, "reportar_incidente");
        }

        Incidente incidente = new Incidente();
        incidente.setTitulo(titulo);
        incidente.setDescripcion(descripcion);
        incidente.setSeveridad(severidad != null ? severidad : SeveridadIncidente.MEDIA);
        incidente.setPersona(personaInvolucrada);
        incidente.setEmpresa(empresaInvolucrada);
        incidente.setReportadoPor(reportadoPor);
        incidente.setFechaHora(LocalDateTime.now());
        incidente.setEstado("ABIERTO");

        Incidente guardado = incidenteRepository.guardar(incidente);

        if (auditoriaService != null) {
            Long usuarioId = reportadoPor != null ? reportadoPor.getId() : null;
            String detalle = "Incidente reportado [" + guardado.getSeveridad() + "]: " + titulo +
                    (personaInvolucrada != null ? " (Involucra a: " + personaInvolucrada.getNombre() + ")" : "");
            auditoriaService.registrar(usuarioId, "REGISTRO_INCIDENTE", "INCIDENTE", guardado.getId(), detalle, "EXITOSO");
        }

        return guardado;
    }

    // =========================================================================
    // GESTIÓN Y RESOLUCIÓN DE INCIDENTES (ABIERTO -> RESUELTO / EN_INVESTIGACION)
    // =========================================================================
    /**
     * Actualiza el estado operativo de un incidente registrado.
     * 
     * @param incidenteId Identificador único del incidente.
     * @param nuevoEstado Nuevo estado asignado (ej. 'RESUELTO', 'EN_PROCESO').
     * @param usuarioSolicitante Usuario que ejecuta la actualización.
     * @return Entidad Incidente con estado actualizado y registrado en auditoría.
     */
    public Incidente cambiarEstado(long incidenteId, String nuevoEstado, Usuario usuarioSolicitante) {
        if (usuarioSolicitante != null && rbacService != null) {
            rbacService.verificarPermiso(usuarioSolicitante, "gestionar_incidentes");
        }

        Incidente incidente = incidenteRepository.porId(incidenteId)
                .orElseThrow(() -> new IllegalArgumentException("Incidente no encontrado: " + incidenteId));

        incidente.setEstado(nuevoEstado);
        Incidente guardado = incidenteRepository.guardar(incidente);

        if (auditoriaService != null) {
            Long usuarioId = usuarioSolicitante != null ? usuarioSolicitante.getId() : null;
            auditoriaService.registrar(usuarioId, "INCIDENTE_ESTADO_CAMBIADO", "INCIDENTE", guardado.getId(),
                    "Estado de incidente #" + incidenteId + " cambiado a " + nuevoEstado, "EXITOSO");
        }

        return guardado;
    }

    public Optional<Incidente> porId(long id) {
        return incidenteRepository.porId(id);
    }

    public List<Incidente> listarTodos() {
        return incidenteRepository.listarTodos();
    }

    public List<Incidente> listarPorPersona(long personaId) {
        return incidenteRepository.listarPorPersona(personaId);
    }
}
