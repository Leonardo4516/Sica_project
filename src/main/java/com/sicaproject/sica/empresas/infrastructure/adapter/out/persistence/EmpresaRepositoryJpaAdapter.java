package com.sicaproject.sica.empresas.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.empresas.application.port.out.EmpresaRepository;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class EmpresaRepositoryJpaAdapter implements EmpresaRepository {

    @Override
    public Optional<Empresa> porId(long id) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            EmpresaEntity entity = em.find(EmpresaEntity.class, id);
            return Optional.ofNullable(EmpresaMapper.toDomain(entity));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Empresa> porNit(String nit) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<EmpresaEntity> query = em.createQuery(
                    "SELECT e FROM EmpresaEntity e WHERE e.nit = :nit", EmpresaEntity.class);
            query.setParameter("nit", nit);
            return query.getResultStream().findFirst().map(EmpresaMapper::toDomain);
        } finally {
            em.close();
        }
    }

    @Override
    public Empresa guardar(Empresa empresa) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            em.getTransaction().begin();
            EmpresaEntity entity = new EmpresaEntity();
            EmpresaMapper.copyToEntity(empresa, entity);
            em.persist(entity);
            em.getTransaction().commit();
            empresa.setId(entity.getId());
            return empresa;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Empresa> listar() {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<EmpresaEntity> query = em.createQuery(
                    "SELECT e FROM EmpresaEntity e ORDER BY e.nombre", EmpresaEntity.class);
            return query.getResultList().stream().map(EmpresaMapper::toDomain).toList();
        } finally {
            em.close();
        }
    }
}
