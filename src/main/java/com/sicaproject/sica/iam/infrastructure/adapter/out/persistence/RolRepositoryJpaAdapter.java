package com.sicaproject.sica.iam.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.iam.application.port.out.RolRepository;
import com.sicaproject.sica.iam.domain.Rol;
import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

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
}
