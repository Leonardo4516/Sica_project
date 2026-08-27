package com.sicaproject.sica.acceso.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.acceso.application.port.out.VisitaRepository;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.empresas.infrastructure.adapter.out.persistence.EmpresaEntity;
import com.sicaproject.sica.iam.infrastructure.adapter.out.persistence.UsuarioEntity;
import com.sicaproject.sica.personas.infrastructure.adapter.out.persistence.PersonaEntity;
import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class VisitaRepositoryJpaAdapter implements VisitaRepository {

    private static final String FETCH_GRAPH =
            "SELECT DISTINCT v FROM VisitaEntity v "
                    + "LEFT JOIN FETCH v.persona p "
                    + "LEFT JOIN FETCH p.empresa "
                    + "LEFT JOIN FETCH v.empresaDestino "
                    + "LEFT JOIN FETCH v.guarda "
                    + "LEFT JOIN FETCH v.funcionarioAnfitrion ";

    @Override
    public Visita guardar(Visita visita) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            em.getTransaction().begin();
            VisitaEntity entity;
            if (visita.getId() != 0L) {
                entity = em.find(VisitaEntity.class, visita.getId());
            } else {
                entity = new VisitaEntity();
            }
            VisitaMapper.copyToEntity(visita, entity);
            entity.setPersona(em.getReference(PersonaEntity.class, visita.getPersona().getId()));
            if (visita.getEmpresaDestino() != null) {
                entity.setEmpresaDestino(em.getReference(EmpresaEntity.class, visita.getEmpresaDestino().getId()));
            }
            if (visita.getGuarda() != null) {
                entity.setGuarda(em.getReference(UsuarioEntity.class, visita.getGuarda().getId()));
            } else {
                entity.setGuarda(null);
            }
            if (visita.getFuncionarioAnfitrion() != null) {
                entity.setFuncionarioAnfitrion(em.getReference(UsuarioEntity.class, visita.getFuncionarioAnfitrion().getId()));
            } else {
                entity.setFuncionarioAnfitrion(null);
            }
            if (entity.getId() == null) {
                em.persist(entity);
            }
            em.getTransaction().commit();
            visita.setId(entity.getId());
            return visita;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Visita> porId(long id) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<VisitaEntity> query = em.createQuery(
                    FETCH_GRAPH + "WHERE v.id = :id", VisitaEntity.class);
            query.setParameter("id", id);
            return query.getResultList().stream().findFirst().map(VisitaMapper::toDomain);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Visita> porPersonaYEstado(long personaId, EstadoVisita estado) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<VisitaEntity> query = em.createQuery(
                    FETCH_GRAPH + "WHERE p.id = :personaId AND v.estado = :estado",
                    VisitaEntity.class);
            query.setParameter("personaId", personaId);
            query.setParameter("estado", estado.name());
            return query.getResultList().stream().map(VisitaMapper::toDomain).toList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Visita> listarPendientesPorFuncionario(long funcionarioId) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<VisitaEntity> query = em.createQuery(
                    FETCH_GRAPH + "WHERE v.funcionarioAnfitrion.id = :funcionarioId "
                            + "AND v.estado = 'PENDIENTE_APROBACION'",
                    VisitaEntity.class);
            query.setParameter("funcionarioId", funcionarioId);
            return query.getResultList().stream().map(VisitaMapper::toDomain).toList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Visita> listarTodas() {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<VisitaEntity> query = em.createQuery(
                    FETCH_GRAPH + "ORDER BY v.fechaHoraRegistro DESC", VisitaEntity.class);
            return query.getResultList().stream().map(VisitaMapper::toDomain).toList();
        } finally {
            em.close();
        }
    }
}
