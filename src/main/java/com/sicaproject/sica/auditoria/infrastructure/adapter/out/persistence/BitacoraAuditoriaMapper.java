package com.sicaproject.sica.auditoria.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;

public final class BitacoraAuditoriaMapper {

    private BitacoraAuditoriaMapper() {}

    public static BitacoraAuditoria toDomain(BitacoraAuditoriaEntity entity) {
        BitacoraAuditoria b = new BitacoraAuditoria();
        b.setId(entity.getId());
        b.setUsuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null);
        b.setAccion(entity.getAccion());
        b.setEntidadAfectada(entity.getEntidadAfectada());
        b.setEntidadId(entity.getEntidadId() != null ? entity.getEntidadId() : 0L);
        b.setDetalle(entity.getDetalle());
        b.setResultado(entity.getResultado());
        b.setFechaHora(entity.getFechaHora());
        return b;
    }
}
