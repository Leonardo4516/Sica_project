package com.sicaproject.sica.incidentes.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.empresas.infrastructure.adapter.out.persistence.EmpresaEntity;
import com.sicaproject.sica.iam.infrastructure.adapter.out.persistence.UsuarioEntity;
import com.sicaproject.sica.incidentes.application.port.out.IncidenteRepository;
import com.sicaproject.sica.incidentes.domain.Incidente;
import com.sicaproject.sica.personas.infrastructure.adapter.out.persistence.PersonaEntity;
import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class IncidenteRepositoryJpaAdapter implements IncidenteRepository {

    private static final String FETCH_GRAPH =
            "SELECT DISTINCT i FROM IncidenteEntity i "
                    + "LEFT JOIN FETCH i.persona p "
                    + "LEFT JOIN FETCH p.empresa "
                    + "LEFT JOIN FETCH i.empresa "
                    + "LEFT JOIN FETCH i.reportadoPor u "
                    + "LEFT JOIN FETCH u.rol ";

    @Override
    public Incidente guardar(Incidente incidente) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            em.getTransaction().begin();
            IncidenteEntity entity;
            if (incidente.getId() != 0L) {
                entity = em.find(IncidenteEntity.class, incidente.getId());
            } else {
                entity = new IncidenteEntity();
            }
            IncidenteMapper.copyToEntity(incidente, entity);

            if (incidente.getPersona() != null && incidente.getPersona().getId() != 0L) {
                entity.setPersona(em.getReference(PersonaEntity.class, incidente.getPersona().getId()));
            } else {
                entity.setPersona(null);
            }

            if (incidente.getEmpresa() != null && incidente.getEmpresa().getId() != 0L) {
                entity.setEmpresa(em.getReference(EmpresaEntity.class, incidente.getEmpresa().getId()));
            } else {
                entity.setEmpresa(null);
            }

            if (incidente.getReportadoPor() != null && incidente.getReportadoPor().getId() != null) {
                entity.setReportadoPor(em.getReference(UsuarioEntity.class, incidente.getReportadoPor().getId()));
            }

            if (entity.getId() == null) {
                em.persist(entity);
            }
            em.getTransaction().commit();
            incidente.setId(entity.getId());
            return incidente;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Incidente> porId(long id) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<IncidenteEntity> query = em.createQuery(
                    FETCH_GRAPH + "WHERE i.id = :id", IncidenteEntity.class);
            query.setParameter("id", id);
            List<IncidenteEntity> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(IncidenteMapper.toDomain(results.get(0)));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Incidente> listarTodos() {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<IncidenteEntity> query = em.createQuery(
                    FETCH_GRAPH + "ORDER BY i.fechaHora DESC", IncidenteEntity.class);
            return query.getResultList().stream().map(IncidenteMapper::toDomain).toList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Incidente> listarPorPersona(long personaId) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<IncidenteEntity> query = em.createQuery(
                    FETCH_GRAPH + "WHERE i.persona.id = :personaId ORDER BY i.fechaHora DESC", IncidenteEntity.class);
            query.setParameter("personaId", personaId);
            return query.getResultList().stream().map(IncidenteMapper::toDomain).toList();
        } finally {
            em.close();
        }
    }
}
