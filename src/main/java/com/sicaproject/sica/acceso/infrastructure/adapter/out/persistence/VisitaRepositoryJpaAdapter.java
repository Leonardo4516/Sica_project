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
            entity.setEmpresaDestino(em.getReference(EmpresaEntity.class, visita.getEmpresaDestino().getId()));
            entity.setGuarda(em.getReference(UsuarioEntity.class, visita.getGuarda().getId()));
            if (visita.getFuncionarioAnfitrion() != null) {
                entity.setFuncionarioAnfitrion(em.getReference(UsuarioEntity.class, visita.getFuncionarioAnfitrion().getId()));
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
            VisitaEntity entity = em.find(VisitaEntity.class, id);
            return Optional.ofNullable(VisitaMapper.toDomain(entity));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Visita> porPersonaYEstado(long personaId, EstadoVisita estado) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<VisitaEntity> query = em.createQuery(
                    "SELECT v FROM VisitaEntity v WHERE v.persona.id = :personaId AND v.estado = :estado",
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
                    "SELECT v FROM VisitaEntity v WHERE v.funcionarioAnfitrion.id = :funcionarioId " +
                    "AND v.estado = 'PENDIENTE_APROBACION'", VisitaEntity.class);
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
                    "SELECT v FROM VisitaEntity v ORDER BY v.fechaHoraRegistro DESC", VisitaEntity.class);
            return query.getResultList().stream().map(VisitaMapper::toDomain).toList();
        } finally {
            em.close();
        }
    }
}
