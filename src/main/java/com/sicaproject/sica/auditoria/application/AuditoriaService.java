package com.sicaproject.sica.auditoria.application;

import com.sicaproject.sica.auditoria.application.port.out.BitacoraAuditoriaRepository;
import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de aplicación para la gestión de la bitácora inmutable de auditoría.
 * Registra cada evento crítico del sistema (inicios de sesión, check-in, bloqueos, etc.)
 * garantizando la trazabilidad histórica de acuerdo con los requerimientos de seguridad.
 */
public class AuditoriaService {

    private final BitacoraAuditoriaRepository repository;

    public AuditoriaService(BitacoraAuditoriaRepository repository) {
        this.repository = repository;
    }

    /**
     * Inserta un nuevo registro inmutable en la bitácora de auditoría.
     * 
     * @param usuarioId ID del usuario que originó el evento (puede ser null para intentos de login no autenticados).
     * @param accion Código de la acción ejecutada (ej. 'LOGIN_EXITOSO', 'VISITA_CHECK_IN', 'PERSONA_BLOQUEADA').
     * @param entidadAfectada Nombre de la entidad impactada (ej. 'USUARIO', 'VISITA', 'PERSONA', 'INCIDENTE').
     * @param entidadId ID del registro impactado.
     * @param detalle Descripción explicativa contextual del evento.
     * @param resultado Resultado de la operación ('EXITOSO', 'FALLIDO', 'DENEGADO').
     */
    public void registrar(Long usuarioId, String accion, String entidadAfectada,
                           long entidadId, String detalle, String resultado) {
        BitacoraAuditoria entrada = new BitacoraAuditoria();
        entrada.setUsuarioId(usuarioId);
        entrada.setAccion(accion);
        entrada.setEntidadAfectada(entidadAfectada);
        entrada.setEntidadId(entidadId);
        entrada.setDetalle(detalle);
        entrada.setResultado(resultado);
        entrada.setFechaHora(LocalDateTime.now());
        repository.guardar(entrada);
    }

    /**
     * Retorna el listado completo de registros históricos de auditoría ordenados temporalmente.
     */
    public List<BitacoraAuditoria> obtenerBitacora() {
        return repository.listarTodas();
    }

    public List<BitacoraAuditoria> listarTodas() {
        return repository.listarTodas();
    }
}
