package com.sicaproject.sica.acceso.application.service;

import com.sicaproject.sica.acceso.application.port.out.VisitaRepository;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.service.RbacService;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.personas.domain.Persona;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Reescrito desde cero. Cambios respecto a la versión anterior:
 *  - Ya no guarda nada en un Map estático: todo pasa por VisitaRepository (JPA real).
 *  - Cada operación crítica verifica el permiso RBAC correspondiente ANTES de ejecutarse
 *    (antes RbacService existía pero nunca se llamaba desde aquí).
 *  - checkOut() estaba invertido (hacía APROBADA -> DENTRO y lo llamaba "check-out").
 *    Ahora hace correctamente DENTRO -> CERRADA.
 *  - Se agregó regularizarIngreso() para el flujo 4 del documento ("salida olvidada"),
 *    que no existía en absoluto antes.
 *  - Cada método termina registrando en AuditoriaService (persistente), no en una lista
 *    estática en memoria.
 */
public class VisitaService {

    private final VisitaRepository visitaRepository;
    private final RbacService rbacService;
    private final AuditoriaService auditoriaService;

    public VisitaService(VisitaRepository visitaRepository, RbacService rbacService,
                          AuditoriaService auditoriaService) {
        this.visitaRepository = visitaRepository;
        this.rbacService = rbacService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Flujo 1: invitado pre-registrado.
     * El FUNCIONARIO ya lo registró antes con estado APROBADA (ver registrarVisitaPreaprobada).
     * Este método es lo que hace el GUARDA al momento de la llegada física: mueve
     * la visita de APROBADA -> DENTRO.
     */
    public Visita checkIn(long visitaId, Usuario guarda) {
        rbacService.verificarPermiso(guarda, "registrar_visita");

        Visita visita = visitaRepository.porId(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada: " + visitaId));

        // Flujo 4: si la persona tiene otra visita abierta (DENTRO) sin check-out, se regulariza primero
        regularizarSiCorresponde(visita.getPersona().getId(), guarda);

        visita.cambiarEstado(EstadoVisita.DENTRO);
        visita.setFechaHoraEntrada(LocalDateTime.now());
        visitaRepository.guardar(visita);

        auditoriaService.registrar(guarda.getId(), "VISITA_CHECK_IN", "VISITA", visita.getId(),
                "Check-in realizado por guarda " + guarda.getUsername(), "EXITOSO");
        return visita;
    }

    /**
     * El FUNCIONARIO pre-registra un invitado antes de que llegue.
     */
    public Visita registrarVisitaPreaprobada(Persona persona, Empresa empresaDestino,
                                              Usuario funcionario, Usuario guardaAsignado) {
        rbacService.verificarPermiso(funcionario, "aprobar_visita");

        Visita visita = new Visita();
        visita.setPersona(persona);
        visita.setEmpresaDestino(empresaDestino);
        visita.setFuncionarioAnfitrion(funcionario);
        visita.setGuarda(guardaAsignado);
        visita.setEstado(EstadoVisita.APROBADA);
        visita.setFechaHoraRegistro(LocalDateTime.now());
        visitaRepository.guardar(visita);

        auditoriaService.registrar(funcionario.getId(), "VISITA_PREAPROBADA", "VISITA", visita.getId(),
                "Visita pre-aprobada para " + persona.getNombre(), "EXITOSO");
        return visita;
    }

    /**
     * Flujos 2 y 3: invitado no anunciado, o trabajador con carnet olvidado
     * (usar paseTemporal=true para el segundo caso).
     */
    public Visita solicitarAcceso(Persona persona, Empresa empresaDestino,
                                   Usuario funcionarioAnfitrion, Usuario guarda, boolean paseTemporal) {
        rbacService.verificarPermiso(guarda, "registrar_visita");

        regularizarSiCorresponde(persona.getId(), guarda);

        Visita visita = new Visita();
        visita.setPersona(persona);
        visita.setEmpresaDestino(empresaDestino);
        visita.setFuncionarioAnfitrion(funcionarioAnfitrion);
        visita.setGuarda(guarda);
        visita.setEstado(EstadoVisita.PENDIENTE_APROBACION);
        visita.setPaseTemporal(paseTemporal);
        visita.setFechaHoraRegistro(LocalDateTime.now());
        visitaRepository.guardar(visita);

        auditoriaService.registrar(guarda.getId(), "VISITA_SOLICITUD", "VISITA", visita.getId(),
                "Solicitud de acceso para " + persona.getNombre() + " en " + empresaDestino.getNombre()
                        + (paseTemporal ? " (pase temporal por olvido de carnet)" : ""),
                "EXITOSO");
        return visita;
    }

    public Visita aprobarVisita(long visitaId, Usuario funcionario) {
        rbacService.verificarPermiso(funcionario, "aprobar_visita");

        Visita visita = visitaRepository.porId(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada: " + visitaId));
        visita.cambiarEstado(EstadoVisita.APROBADA);
        visitaRepository.guardar(visita);

        auditoriaService.registrar(funcionario.getId(), "VISITA_APROBADA", "VISITA", visita.getId(),
                "Visita aprobada por funcionario " + funcionario.getUsername(), "EXITOSO");
        return visita;
    }

    public Visita rechazarVisita(long visitaId, Usuario funcionario) {
        rbacService.verificarPermiso(funcionario, "rechazar_visita");

        Visita visita = visitaRepository.porId(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada: " + visitaId));
        visita.cambiarEstado(EstadoVisita.RECHAZADA);
        visitaRepository.guardar(visita);

        auditoriaService.registrar(funcionario.getId(), "VISITA_RECHAZADA", "VISITA", visita.getId(),
                "Visita rechazada por funcionario " + funcionario.getUsername(), "EXITOSO");
        return visita;
    }

    /**
     * CORRECCIÓN DEL BUG: antes esto hacía APROBADA -> DENTRO (era un check-in disfrazado).
     * Ahora hace correctamente DENTRO -> CERRADA.
     */
    public Visita checkOut(long visitaId, Usuario guarda) {
        rbacService.verificarPermiso(guarda, "registrar_salida");

        Visita visita = visitaRepository.porId(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada: " + visitaId));
        visita.cambiarEstado(EstadoVisita.CERRADA);
        visita.setFechaHoraSalida(LocalDateTime.now());
        visitaRepository.guardar(visita);

        auditoriaService.registrar(guarda.getId(), "VISITA_CHECK_OUT", "VISITA", visita.getId(),
                "Check-out de visita #" + visitaId, "EXITOSO");
        return visita;
    }

    /**
     * Flujo 4: salida olvidada. Si la persona tiene una visita abierta (DENTRO),
     * se cierra automáticamente como CERRADA_POR_SISTEMA. No implementado antes.
     * No bloquea el nuevo ingreso, solo deja constancia para auditoría.
     */
    private void regularizarSiCorresponde(long personaId, Usuario guarda) {
        List<Visita> abiertas = visitaRepository.porPersonaYEstado(personaId, EstadoVisita.DENTRO);
        for (Visita anterior : abiertas) {
            anterior.cambiarEstado(EstadoVisita.CERRADA_POR_SISTEMA);
            anterior.setFechaHoraSalida(LocalDateTime.now());
            visitaRepository.guardar(anterior);

            auditoriaService.registrar(guarda.getId(), "SALIDA_OLVIDADA", "VISITA", anterior.getId(),
                    "Salida olvidada detectada al reingresar; visita anterior cerrada por sistema",
                    "EXITOSO");
        }
    }

    public Optional<Visita> obtenerVisita(long visitaId) {
        return visitaRepository.porId(visitaId);
    }

    public boolean tieneVisitaActiva(long personaId) {
        return !visitaRepository.porPersonaYEstado(personaId, EstadoVisita.DENTRO).isEmpty();
    }

    public List<Visita> listarPendientesPorFuncionario(long funcionarioId) {
        return visitaRepository.listarPendientesPorFuncionario(funcionarioId);
    }

    public List<Visita> listarTodas() {
        return visitaRepository.listarTodas();
    }
}
