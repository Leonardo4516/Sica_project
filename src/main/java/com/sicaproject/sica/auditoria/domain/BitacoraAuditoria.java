package com.sicaproject.sica.auditoria.domain;

import java.time.LocalDateTime;

public class BitacoraAuditoria {
    private long id;
    private Long usuarioId;
    private String accion;
    private String entidadAfectada;
    private long entidadId;
    private String detalle;
    private String resultado; // EXITOSO / FALLIDO
    private LocalDateTime fechaHora;

    public BitacoraAuditoria() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getEntidadAfectada() { return entidadAfectada; }
    public void setEntidadAfectada(String entidadAfectada) { this.entidadAfectada = entidadAfectada; }

    public long getEntidadId() { return entidadId; }
    public void setEntidadId(long entidadId) { this.entidadId = entidadId; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}