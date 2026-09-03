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
    // FLUJO 1: INVITADO PRE-REGISTRADO (CHECK-IN EN PORTERÍA)
    // =========================================================================
    /**
     * Procesa el ingreso físico (Check-In) de una persona autorizada en portería.
     * 
     * Secuencia de ejecución:
     * 1. Validación de seguridad RBAC: exige el permiso 'registrar_visita' al operador.
     * 2. Búsqueda y validación de existencia del registro de visita en persistencia.
     * 3. Control perimetral: valida que la persona no posea bloqueo activo en lista negra.
     * 4. Regularización automática: si posee una visita anterior en estado DENTRO, se cierra por sistema.
     * 5. Transición de estado: cambia a DENTRO, registra marca temporal de entrada y asigna el guarda.
     * 6. Registro de auditoría inmutable de la operación.
     * 
     * @param visitaId Identificador único de la visita pre-aprobada.
     * @param guarda Usuario operador autenticado que efectúa el check-in.
     * @return Entidad Visita actualizada y persistida en estado DENTRO.
     * @throws PermisoDenegadoException Si el guarda no tiene el permiso 'registrar_visita'.
     * @throws IllegalArgumentException Si no existe una visita con el ID especificado.
     * @throws IllegalStateException Si la persona asociada se encuentra bloqueada por seguridad.
     */
    public Visita checkIn(long visitaId, Usuario guarda) {
        // Validación de permisos de acceso del operador según matriz RBAC
        rbacService.verificarPermiso(guarda, "registrar_visita");

        // Recuperar registro de visita desde la capa de persistencia JPA
        Visita visita = visitaRepository.porId(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada: " + visitaId));

        // Control perimetral: verificar si la persona tiene restricción de acceso activa
        if (visita.getPersona() != null && visita.getPersona().isBloqueado()) {
            auditoriaService.registrar(guarda.getId(), "ACCESO_DENEGADO_BLOQUEO", "PERSONA", visita.getPersona().getId(),
                    "Intento de check-in denegado: La persona '" + visita.getPersona().getNombre() + "' se encuentra bloqueada (" +
                            (visita.getPersona().getMotivoBloqueo() != null ? visita.getPersona().getMotivoBloqueo() : "Sin motivo especificado") + ")", "DENEGADO");
            throw new IllegalStateException("Acceso denegado: la persona '" + visita.getPersona().getNombre() + "' se encuentra bloqueada por seguridad (" +
                    (visita.getPersona().getMotivoBloqueo() != null ? visita.getPersona().getMotivoBloqueo() : "Restricción de acceso activa") + ")");
        }

        // Flujo 4: Regularizar salida previa si la persona quedó registrada como DENTRO
        regularizarSiCorresponde(visita.getPersona().getId(), guarda);

        // Actualizar estado de ciclo de vida, timestamp de entrada y operador responsable
        visita.cambiarEstado(EstadoVisita.DENTRO);
        visita.setFechaHoraEntrada(LocalDateTime.now());
        visita.setGuarda(guarda);
        visitaRepository.guardar(visita);

        // Trazabilidad inmutable en bitácora de auditoría
        auditoriaService.registrar(guarda.getId(), "VISITA_CHECK_IN", "VISITA", visita.getId(),
                "Check-in realizado por guarda " + guarda.getUsername(), "EXITOSO");
        return visita;
    }

    // =========================================================================
    // FLUJO 1: PRE-REGISTRO DE INVITADO POR FUNCIONARIO
    // =========================================================================
    /**
     * Registra previamente una visita autorizada por parte de un funcionario anfitrión.
     * La visita nace directamente con estado APROBADA para su posterior ingreso en portería.
     * 
     * @param persona Datos del visitante o trabajador a autorizar.
     * @param empresaDestino Empresa residente receptora de la visita.
     * @param funcionario Usuario anfitrión autenticado que autoriza el ingreso.
     * @param guardaAsignado Guarda opcional asignado en el registro.
     * @return Entidad Visita persistida en estado APROBADA.
     */
    public Visita registrarVisitaPreaprobada(Persona persona, Empresa empresaDestino,
                                              Usuario funcionario, Usuario guardaAsignado) {
        // Verificar que el funcionario cuente con el permiso de aprobación
        rbacService.verificarPermiso(funcionario, "aprobar_visita");

        // Validar que no se autorice a personas con restricción perimetral
        if (persona != null && persona.isBloqueado()) {
            auditoriaService.registrar(funcionario.getId(), "PREAPROBACION_BLOQUEADA", "PERSONA", persona.getId(),
                    "Intento de pre-aprobación para persona bloqueada: " + persona.getNombre(), "DENEGADO");
            throw new IllegalStateException("No se puede pre-aprobar visita: la persona '" + persona.getNombre() + "' se encuentra bloqueada en el sistema.");
        }

        // Construir instancia de visita autorizada en estado APROBADA
        Visita visita = new Visita();
        visita.setPersona(persona);
        visita.setEmpresaDestino(empresaDestino);
        visita.setFuncionarioAnfitrion(funcionario);
        visita.setGuarda(guardaAsignado);
        visita.setEstado(EstadoVisita.APROBADA);
        visita.setFechaHoraRegistro(LocalDateTime.now());
        visitaRepository.guardar(visita);

        // Registrar evento de pre-aprobación en bitácora
        auditoriaService.registrar(funcionario.getId(), "VISITA_PREAPROBADA", "VISITA", visita.getId(),
                "Visita pre-aprobada para " + persona.getNombre(), "EXITOSO");
        return visita;
    }

    private com.sicaproject.sica.acceso.application.port.out.WhatsAppPort whatsAppPort;

    public void setWhatsAppPort(com.sicaproject.sica.acceso.application.port.out.WhatsAppPort whatsAppPort) {
        this.whatsAppPort = whatsAppPort;
    }

    // =========================================================================
    // FLUJOS 2 Y 3: SOLICITUD EN TIEMPO REAL (NO ANUNCIADO O CARNET OLVIDADO)
    // =========================================================================
    /**
     * Genera una solicitud de acceso desde portería para un visitante no anunciado (Flujo 2)
     * o un trabajador con carnet olvidado (Flujo 3).
     * La visita se inicializa con estado PENDIENTE_APROBACION a la espera de validación del anfitrión.
     * 
     * @param persona Persona que solicita el ingreso.
     * @param empresaDestino Empresa a la que se dirige.
     * @param funcionarioAnfitrion Funcionario que debe aprobar o rechazar la solicitud.
     * @param guarda Guarda que recepciona y emite la solicitud en portería.
     * @param paseTemporal Indica si corresponde a pase temporal por carnet olvidado.
     * @return Entidad Visita en estado PENDIENTE_APROBACION.
     */
    public Visita solicitarAcceso(Persona persona, Empresa empresaDestino,
                                   Usuario funcionarioAnfitrion, Usuario guarda, boolean paseTemporal) {
        // Validar permiso de registro en portería
        rbacService.verificarPermiso(guarda, "registrar_visita");

        // Validar si la persona presenta restricción perimetral
        if (persona != null && persona.isBloqueado()) {
            auditoriaService.registrar(guarda.getId(), "SOLICITUD_ACCESO_BLOQUEADO", "PERSONA", persona.getId(),
                    "Intento de solicitud de acceso para persona bloqueada: " + persona.getNombre(), "DENEGADO");
            throw new IllegalStateException("No se puede solicitar acceso: la persona '" + persona.getNombre() + "' se encuentra bloqueada en el sistema.");
        }

        // Regularizar visitas previas abiertas en caso de existir
        regularizarSiCorresponde(persona.getId(), guarda);

        // Instanciar solicitud de acceso con indicador de pase temporal
        Visita visita = new Visita();
        visita.setPersona(persona);
        visita.setEmpresaDestino(empresaDestino);
        visita.setFuncionarioAnfitrion(funcionarioAnfitrion);
        visita.setGuarda(guarda);
        visita.setEstado(EstadoVisita.PENDIENTE_APROBACION);
        visita.setPaseTemporal(paseTemporal);
        visita.setFechaHoraRegistro(LocalDateTime.now());
        visitaRepository.guardar(visita);

        // Registrar solicitud en auditoría
        auditoriaService.registrar(guarda.getId(), "VISITA_SOLICITUD", "VISITA", visita.getId(),
                "Solicitud de acceso para " + persona.getNombre() + " en " + empresaDestino.getNombre()
                        + (paseTemporal ? " (pase temporal por olvido de carnet)" : ""),
                "EXITOSO");

        // ESCALAMIENTO ASÍNCRONO (Evolution API)
        if (whatsAppPort != null) {
            long idGuardado = visita.getId();
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                try {
                    visitaRepository.porId(idGuardado).ifPresent(v -> {
                        if (v.getEstado() == EstadoVisita.PENDIENTE_APROBACION) {
                            whatsAppPort.enviarRecordatorioAnfitrion(v);
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Error en el hilo de WhatsApp: " + e.getMessage());
                }
            }, 1, java.util.concurrent.TimeUnit.MINUTES);
        }

        return visita;
    }

    // =========================================================================
    // GESTIÓN DE SOLICITUDES: APROBACIÓN Y RECHAZO (PANEL FUNCIONARIO)
    // =========================================================================
    /**
     * Aprueba una solicitud de acceso pendiente, dejándola habilitada para check-in.
     * 
     * @param visitaId Identificador de la visita pendiente.
     * @param funcionario Usuario anfitrión que otorga la autorización.
     * @return Visita actualizada en estado APROBADA.
     */
    public Visita aprobarVisita(long visitaId, Usuario funcionario) {
        // Validación de permisos de aprobación
        rbacService.verificarPermiso(funcionario, "aprobar_visita");

        // Cargar entidad y realizar transición de estado a APROBADA
        Visita visita = visitaRepository.porId(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada: " + visitaId));
        visita.cambiarEstado(EstadoVisita.APROBADA);
        visitaRepository.guardar(visita);

        // Registro de auditoría
        auditoriaService.registrar(funcionario.getId(), "VISITA_APROBADA", "VISITA", visita.getId(),
                "Visita aprobada por funcionario " + funcionario.getUsername(), "EXITOSO");
        return visita;
    }

    /**
     * Rechaza una solicitud de acceso pendiente.
     * 
     * @param visitaId Identificador de la visita pendiente.
     * @param funcionario Usuario anfitrión que deniega el ingreso.
     * @return Visita actualizada en estado RECHAZADA.
     */
    public Visita rechazarVisita(long visitaId, Usuario funcionario) {
        // Validación de permisos de rechazo
        rbacService.verificarPermiso(funcionario, "rechazar_visita");

        // Cargar entidad y realizar transición de estado a RECHAZADA
        Visita visita = visitaRepository.porId(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada: " + visitaId));
        visita.cambiarEstado(EstadoVisita.RECHAZADA);
        visitaRepository.guardar(visita);

        // Registro de auditoría
        auditoriaService.registrar(funcionario.getId(), "VISITA_RECHAZADA", "VISITA", visita.getId(),
                "Visita rechazada por funcionario " + funcionario.getUsername(), "EXITOSO");
        return visita;
    }

    // =========================================================================
    // SALIDA FÍSICA: CHECK-OUT EN PORTERÍA
    // =========================================================================
    /**
     * Registra la salida física (Check-Out) de una persona actualmente dentro del complejo.
     * Realiza la transición de estado DENTRO -> CERRADA y registra el timestamp de salida.
     * 
     * @param visitaId Identificador de la visita activa.
     * @param guarda Guarda que registra la salida física.
     * @return Visita finalizada en estado CERRADA.
     */
    public Visita checkOut(long visitaId, Usuario guarda) {
        // Validar permiso de registro de salidas
        rbacService.verificarPermiso(guarda, "registrar_salida");

        // Recuperar registro de visita activa
        Visita visita = visitaRepository.porId(visitaId)
                .orElseThrow(() -> new IllegalArgumentException("Visita no encontrada: " + visitaId));
        
        // Efectuar transición a CERRADA y registrar fecha/hora de egreso
        visita.cambiarEstado(EstadoVisita.CERRADA);
        visita.setFechaHoraSalida(LocalDateTime.now());
        visitaRepository.guardar(visita);

        // Auditoría de salida
        auditoriaService.registrar(guarda.getId(), "VISITA_CHECK_OUT", "VISITA", visita.getId(),
                "Check-out de visita #" + visitaId, "EXITOSO");
        return visita;
    }

    // =========================================================================
    // FLUJO 4: REGULARIZACIÓN AUTOMÁTICA DE SALIDA OLVIDADA
    // =========================================================================
    /**
     * Mecanismo de regularización: detecta si la persona presenta registros previos
     * que hayan quedado abiertos en estado DENTRO al intentar un nuevo ingreso.
     * Cierra automáticamente las visitas abiertas previas como CERRADA_POR_SISTEMA
     * y registra la novedad en la bitácora sin bloquear el nuevo ingreso.
     * 
     * @param personaId Identificador de la persona evaluada.
     * @param guarda Operador que procesa el nuevo ingreso.
     */
    private void regularizarSiCorresponde(long personaId, Usuario guarda) {
        // Consultar visitas abiertas (DENTRO) vinculadas a la persona
        List<Visita> abiertas = visitaRepository.porPersonaYEstado(personaId, EstadoVisita.DENTRO);
        for (Visita anterior : abiertas) {
            // Regularizar registro anterior con estado de cierre forzado por sistema
            anterior.cambiarEstado(EstadoVisita.CERRADA_POR_SISTEMA);
            anterior.setFechaHoraSalida(LocalDateTime.now());
            visitaRepository.guardar(anterior);

            // Dejar constancia de auditoría de salida olvidada
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
