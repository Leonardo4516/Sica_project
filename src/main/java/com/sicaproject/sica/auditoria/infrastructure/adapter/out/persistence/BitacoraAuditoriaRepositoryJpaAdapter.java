package com.sicaproject.sica.auditoria.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.auditoria.application.port.out.BitacoraAuditoriaRepository;
import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
import com.sicaproject.sica.iam.infrastructure.adapter.out.persistence.UsuarioEntity;
import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class BitacoraAuditoriaRepositoryJpaAdapter implements BitacoraAuditoriaRepository {

    @Override
    public BitacoraAuditoria guardar(BitacoraAuditoria entrada) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            em.getTransaction().begin();
            BitacoraAuditoriaEntity entity = new BitacoraAuditoriaEntity();
            if (entrada.getUsuarioId() != null) {
                entity.setUsuario(em.getReference(UsuarioEntity.class, entrada.getUsuarioId()));
            }
            entity.setAccion(entrada.getAccion());
            entity.setEntidadAfectada(entrada.getEntidadAfectada());
            entity.setEntidadId(entrada.getEntidadId());
            entity.setDetalle(entrada.getDetalle());
            entity.setResultado(entrada.getResultado());
            entity.setFechaHora(entrada.getFechaHora());
            em.persist(entity);
            em.getTransaction().commit();
            entrada.setId(entity.getId());
            return entrada;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public List<BitacoraAuditoria> listarTodas() {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<BitacoraAuditoriaEntity> query = em.createQuery(
                    "SELECT b FROM BitacoraAuditoriaEntity b ORDER BY b.fechaHora DESC", BitacoraAuditoriaEntity.class);
            return query.getResultList().stream().map(BitacoraAuditoriaMapper::toDomain).toList();
        } finally {
            em.close();
        }
    }
}
