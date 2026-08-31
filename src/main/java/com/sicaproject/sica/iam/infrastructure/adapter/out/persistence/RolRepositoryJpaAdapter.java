package com.sicaproject.sica.iam.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.iam.application.port.out.RolRepository;
import com.sicaproject.sica.iam.domain.Rol;
import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class RolRepositoryJpaAdapter implements RolRepository {

    @Override
    public List<Rol> listarTodos() {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<RolEntity> query = em.createQuery(
                    "SELECT DISTINCT r FROM RolEntity r LEFT JOIN FETCH r.permisos ORDER BY r.nombre",
                    RolEntity.class);
            return query.getResultList().stream().map(RolMapper::toDomain).toList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Rol> porNombre(String nombre) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<RolEntity> query = em.createQuery(
                    "SELECT DISTINCT r FROM RolEntity r LEFT JOIN FETCH r.permisos WHERE UPPER(r.nombre) = UPPER(:nombre)",
                    RolEntity.class);
            query.setParameter("nombre", nombre);
            List<RolEntity> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(RolMapper.toDomain(results.get(0)));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Rol> porId(long id) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<RolEntity> query = em.createQuery(
                    "SELECT DISTINCT r FROM RolEntity r LEFT JOIN FETCH r.permisos WHERE r.id = :id",
                    RolEntity.class);
            query.setParameter("id", id);
            List<RolEntity> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(RolMapper.toDomain(results.get(0)));
        } finally {
            em.close();
        }
    }
}
