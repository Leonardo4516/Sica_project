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

    // =========================================================================
    // FLUJO 1: INVITADO PRE-REGISTRADO (EL FLUJO IDEAL) - CHECK-IN FÍSICO
    // =========================================================================
    /**
     * Realiza el Check-In físico de una visita en portería cuando la persona llega.
     * 
     * [LÓGICA DEL NEGOCIO]:
     * 1. Verifica permiso RBAC 'registrar_visita' en el usuario guarda.
     * 2. Obtiene la visita por su ID o lanza error si no existe.
     * 3. Valida si la persona está BLOQUEADA (Lista Negra). Si lo está, audita y deniega el paso.
     * 4. Regulariza si la persona tenía una visita abierta anterior (Flujo 4).
     * 5. Cambia el estado a DENTRO, fija fecha/hora de entrada actual y asocia el guarda.
     * 6. Registra el evento en la bitácora inmutable de auditoría.
     * 
     * [PISTAS PARA EL DEBUG / EVALUACIÓN]:
     * - Si te borran la validación RBAC: falta rbacService.verificarPermiso(guarda, "registrar_visita");
     * - Si te borran el chequeo de bloqueo: el guarda podría dejar entrar a personas vetadas.
     * - Si te borran regularizarSiCorresponde: el sistema fallará en el Flujo 4 (salida olvidada).
     * - Si te borran visita.cambiarEstado(EstadoVisita.DENTRO): la visita nunca pasará a estado activo.
     * - Si te borran fechaHoraEntrada: la visita no tendrá registro temporal de cuándo ingresó.
     */
    public Visita checkIn(long visitaId, Usuario guarda) {
        // 1. Verificación de Seguridad RBAC en Base de Datos
        rbacService.verificarPermiso(guarda, "registrar_visita");

        // 2. Recuperar la visita existente desde el repositorio JPA
        Visita visita = visitaRepository.porId(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada: " + visitaId));

        // 3. Verificación de Restricción Perimetral (Lista Negra / Bloqueo)
        if (visita.getPersona() != null && visita.getPersona().isBloqueado()) {
            auditoriaService.registrar(guarda.getId(), "ACCESO_DENEGADO_BLOQUEO", "PERSONA", visita.getPersona().getId(),
                    "Intento de check-in denegado: La persona '" + visita.getPersona().getNombre() + "' se encuentra bloqueada (" +
                            (visita.getPersona().getMotivoBloqueo() != null ? visita.getPersona().getMotivoBloqueo() : "Sin motivo especificado") + ")", "DENEGADO");
            throw new IllegalStateException("Acceso denegado: la persona '" + visita.getPersona().getNombre() + "' se encuentra bloqueada por seguridad (" +
                    (visita.getPersona().getMotivoBloqueo() != null ? visita.getPersona().getMotivoBloqueo() : "Restricción de acceso activa") + ")");
        }

        // 4. Flujo 4: si la persona tiene otra visita abierta (DENTRO) sin check-out, se regulariza automáticamente
        regularizarSiCorresponde(visita.getPersona().getId(), guarda);

        // 5. Transición de Estado de Dominio y Registro Temporal
        visita.cambiarEstado(EstadoVisita.DENTRO);
        visita.setFechaHoraEntrada(LocalDateTime.now());
        visita.setGuarda(guarda);
        visitaRepository.guardar(visita);

        // 6. Auditoría inmutable en tabla bitacora_auditoria
        auditoriaService.registrar(guarda.getId(), "VISITA_CHECK_IN", "VISITA", visita.getId(),
                "Check-in realizado por guarda " + guarda.getUsername(), "EXITOSO");
        return visita;
    }

    // =========================================================================
    // FLUJO 1: PRE-REGISTRO DE INVITADO POR FUNCIONARIO
    // =========================================================================
    /**
     * El Funcionario pre-registra a un invitado antes de que llegue físicamente a Zona Acme.
     * La visita nace directamente en estado APROBADA.
     * 
     * [PISTAS PARA EL DEBUG]:
     * - Requiere permiso 'aprobar_visita'.
     * - Valida que la persona no esté bloqueada.
     * - El estado inicial DEBE ser EstadoVisita.APROBADA.
     */
    public Visita registrarVisitaPreaprobada(Persona persona, Empresa empresaDestino,
                                              Usuario funcionario, Usuario guardaAsignado) {
        // 1. Verificación de permiso RBAC del funcionario
        rbacService.verificarPermiso(funcionario, "aprobar_visita");

        // 2. Comprobar que no se pre-apruebe a personas con restricción de acceso
        if (persona != null && persona.isBloqueado()) {
            auditoriaService.registrar(funcionario.getId(), "PREAPROBACION_BLOQUEADA", "PERSONA", persona.getId(),
                    "Intento de pre-aprobación para persona bloqueada: " + persona.getNombre(), "DENEGADO");
            throw new IllegalStateException("No se puede pre-aprobar visita: la persona '" + persona.getNombre() + "' se encuentra bloqueada en el sistema.");
        }

        // 3. Crear entidad de visita con estado APROBADA
        Visita visita = new Visita();
        visita.setPersona(persona);
        visita.setEmpresaDestino(empresaDestino);
        visita.setFuncionarioAnfitrion(funcionario);
        visita.setGuarda(guardaAsignado);
        visita.setEstado(EstadoVisita.APROBADA);
        visita.setFechaHoraRegistro(LocalDateTime.now());
        visitaRepository.guardar(visita);

        // 4. Auditoría de creación
        auditoriaService.registrar(funcionario.getId(), "VISITA_PREAPROBADA", "VISITA", visita.getId(),
                "Visita pre-aprobada para " + persona.getNombre(), "EXITOSO");
        return visita;
    }

    // =========================================================================
    // FLUJOS 2 Y 3: INVITADO NO ANUNCIADO O CARNET OLVIDADO (PASE TEMPORAL)
    // =========================================================================
    /**
     * El Guarda genera una solicitud de ingreso desde portería.
     * Si paseTemporal es true -> Flujo 3 (Trabajador sin carnet).
     * Si paseTemporal es false -> Flujo 2 (Invitado no anunciado).
     * En ambos casos, la visita nace con estado PENDIENTE_APROBACION.
     * 
     * [PISTAS PARA EL DEBUG]:
     * - Guarda requiere permiso 'registrar_visita'.
     * - La visita queda en PENDIENTE_APROBACION esperando que el funcionario la apruebe.
     */
    public Visita solicitarAcceso(Persona persona, Empresa empresaDestino,
                                   Usuario funcionarioAnfitrion, Usuario guarda, boolean paseTemporal) {
        rbacService.verificarPermiso(guarda, "registrar_visita");

        if (persona != null && persona.isBloqueado()) {
            auditoriaService.registrar(guarda.getId(), "SOLICITUD_ACCESO_BLOQUEADO", "PERSONA", persona.getId(),
                    "Intento de solicitud de acceso para persona bloqueada: " + persona.getNombre(), "DENEGADO");
            throw new IllegalStateException("No se puede solicitar acceso: la persona '" + persona.getNombre() + "' se encuentra bloqueada en el sistema.");
        }

        // Regularizar si tenía visita abierta previa antes de pedir nuevo ingreso
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

    // =========================================================================
    // APROBACIÓN Y RECHAZO DE SOLICITUDES (PANEL FUNCIONARIO)
    // =========================================================================
    /**
     * El Funcionario aprueba una visita que estaba PENDIENTE_APROBACION.
     * Mueve el estado a APROBADA para que el Guarda pueda hacer check-in.
     */
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

    /**
     * El Funcionario rechaza una visita que estaba PENDIENTE_APROBACION.
     * Mueve el estado a RECHAZADA.
     */
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

    // =========================================================================
    // SALIDA FÍSICA: CHECK-OUT (GUARDA EN PORTERÍA)
    // =========================================================================
    /**
     * Registra la salida física de una persona que está actualmente DENTRO.
     * Mueve el estado de DENTRO -> CERRADA y fija la fecha/hora de salida.
     * 
     * [PISTAS PARA EL DEBUG]:
     * - Guarda requiere permiso 'registrar_salida'.
     * - El estado final debe ser EstadoVisita.CERRADA.
     * - Se debe asignar visita.setFechaHoraSalida(LocalDateTime.now()).
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

    // =========================================================================
    // FLUJO 4: SALIDA OLVIDADA (REGULARIZACIÓN AUTOMÁTICA POR SISTEMA)
    // =========================================================================
    /**
     * [REQUERIMIENTO CLAVE DEL PROYECTO]:
     * Si una persona salió sin registrar su salida, su visita anterior quedó como DENTRO.
     * Al intentar ingresar nuevamente, el sistema no le bloquea el paso, sino que:
     * 1. Cierra automáticamente la visita anterior con estado CERRADA_POR_SISTEMA.
     * 2. Registra la fecha/hora de salida del cierre forzado.
     * 3. Audita la novedad con acción 'SALIDA_OLVIDADA' para que quede registro histórico.
     * 
     * [PISTAS PARA EL DEBUG]:
     * - Busca visitas con EstadoVisita.DENTRO para esa persona.
     * - Las actualiza a EstadoVisita.CERRADA_POR_SISTEMA.
     * - Fija fechaHoraSalida con LocalDateTime.now().
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

    public List<Visita> listarUltimasVisitasPorPersona(long personaId, int limite) {
        return visitaRepository.listarUltimasPorPersona(personaId, limite);
    }
}
