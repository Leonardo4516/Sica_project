package com.sicaproject.sica.auditoria.application;

import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
import java.time.LocalDateTime;

public class AuditorService {

    // En producción esto escribiría en una base de datos o archivo immutable
    // Por ahora almacenamos en memoria estática para demostración
    private static java.util.List<BitacoraAuditoria> bitacora = 
        new java.util.ArrayList<>();

    public static void registrar(String usuarioId, String accion, 
                                 String entidadAfectada, long entidadId,
                                 String detalle, String resultado) {
        var entry = new BitacoraAuditoria();
        entry.setUsuarioId(usuarioId);
        entry.setAccion(accion);
        entry.setEntidadAfectada(entidadAfectada);
        entry.setEntidadId(entidadId);
        entry.setDetalle(detalle);
        entry.setResultado(resultado);
        entry.setFechaHora(LocalDateTime.now());
        bitacora.add(entry);
    }

    public static java.util.List<BitacoraAuditoria> obtenerBitacora() {
        return java.util.List.copyOf(bitacora);
    }

    public static int obtenerTotal() {
        return bitacora.size();
    }
}