package com.sicaproject.sica.personas.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.empresas.infrastructure.adapter.out.persistence.EmpresaEntity;
import com.sicaproject.sica.personas.application.port.out.PersonaRepository;
import com.sicaproject.sica.personas.domain.Persona;
import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Optional;

public class PersonaRepositoryJpaAdapter implements PersonaRepository {

    @Override
    public Optional<Persona> buscarPorDocumento(String documento) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<PersonaEntity> query = em.createQuery(
                    "SELECT p FROM PersonaEntity p LEFT JOIN FETCH p.empresa WHERE p.documento = :documento",
                    PersonaEntity.class);
            query.setParameter("documento", documento);
            return query.getResultList().stream().findFirst().map(PersonaMapper::toDomain);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Persona> porId(Long id) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<PersonaEntity> query = em.createQuery(
                    "SELECT p FROM PersonaEntity p LEFT JOIN FETCH p.empresa WHERE p.id = :id",
                    PersonaEntity.class);
            query.setParameter("id", id);
            return query.getResultList().stream().findFirst().map(PersonaMapper::toDomain);
        } finally {
            em.close();
        }
    }

    @Override
    public java.util.List<Persona> listarTodas() {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<PersonaEntity> query = em.createQuery(
                    "SELECT p FROM PersonaEntity p LEFT JOIN FETCH p.empresa ORDER BY p.nombre",
                    PersonaEntity.class);
            return query.getResultList().stream().map(PersonaMapper::toDomain).toList();
        } finally {
            em.close();
        }
    }

    @Override
    public Persona guardar(Persona persona) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            em.getTransaction().begin();
            PersonaEntity entity = new PersonaEntity();
            PersonaMapper.copyToEntity(persona, entity);
            if (persona.getEmpresaId() != null) {
                entity.setEmpresa(em.getReference(EmpresaEntity.class, persona.getEmpresaId()));
            }
            em.persist(entity);
            em.getTransaction().commit();
            persona.setId(entity.getId());
            return persona;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void actualizar(Persona persona) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            em.getTransaction().begin();
            PersonaEntity entity = em.find(PersonaEntity.class, persona.getId());
            if (entity == null) {
                throw new IllegalArgumentException("Persona no encontrada: " + persona.getId());
            }
            PersonaMapper.copyToEntity(persona, entity);
            if (persona.getEmpresaId() != null) {
                entity.setEmpresa(em.getReference(EmpresaEntity.class, persona.getEmpresaId()));
            } else {
                entity.setEmpresa(null);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
