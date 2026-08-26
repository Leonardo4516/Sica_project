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
                    "SELECT p FROM PersonaEntity p WHERE p.documento = :documento", PersonaEntity.class);
            query.setParameter("documento", documento);
            return query.getResultStream().findFirst().map(PersonaMapper::toDomain);
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
