package com.sicaproject.sica.auditoria.application;

import com.sicaproject.sica.auditoria.application.port.out.BitacoraAuditoriaRepository;
import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reemplaza al antiguo AuditorService (métodos estáticos + lista en memoria).
 * Ahora persiste de verdad en bitacora_auditoria vía el puerto de repositorio,
 * e inyecta la dependencia en vez de acoplarse a un estado global estático.
 */
public class AuditoriaService {

    private final BitacoraAuditoriaRepository repository;

    public AuditoriaService(BitacoraAuditoriaRepository repository) {
        this.repository = repository;
    }

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

    public List<BitacoraAuditoria> obtenerBitacora() {
        return repository.listarTodas();
    }

    public List<BitacoraAuditoria> listarTodas() {
        return repository.listarTodas();
    }
}
