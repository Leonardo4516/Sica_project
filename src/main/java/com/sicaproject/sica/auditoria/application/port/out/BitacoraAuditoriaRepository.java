package com.sicaproject.sica.auditoria.application.port.out;

import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
import java.util.List;

public interface BitacoraAuditoriaRepository {
    BitacoraAuditoria guardar(BitacoraAuditoria entrada);
    List<BitacoraAuditoria> listarTodas();
}
