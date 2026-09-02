package com.sicaproject.sica.auditoria.domain;

import java.time.LocalDateTime;

/**
 * Entidad de Dominio que modela una entrada inmutable en la bitácora de auditoría.
 * Registra cada evento crítico del sistema proporcionando trazabilidad forense:
 * quién realizó la acción, qué entidad fue afectada, cuál fue el resultado y en qué momento exacto.
 */
public class BitacoraAuditoria {
    private long id;
    private Long usuarioId; // Identificador del usuario emisor (null si es intento anónimo)
    private String accion; // Tipo de evento (ej. 'LOGIN_EXITOSO', 'VISITA_CHECK_IN', 'PERSONA_BLOQUEADA')
    private String entidadAfectada; // Recurso impactado ('USUARIO', 'VISITA', 'PERSONA', 'INCIDENTE')
    private long entidadId; // ID del registro impactado
    private String detalle; // Explicación contextual del suceso
    private String resultado; // Resultado de la operación: 'EXITOSO', 'FALLIDO', 'DENEGADO'
    private LocalDateTime fechaHora; // Timestamp inmutable de la ocurrencia

    public BitacoraAuditoria() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getEntidadAfectada() { return entidadAfectada; }
    public void setEntidadAfectada(String entidadAfectada) { this.entidadAfectada = entidadAfectada; }

    public String getRecurso() { return entidadAfectada; }
    public void setRecurso(String recurso) { this.entidadAfectada = recurso; }

    public long getEntidadId() { return entidadId; }
    public void setEntidadId(long entidadId) { this.entidadId = entidadId; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}