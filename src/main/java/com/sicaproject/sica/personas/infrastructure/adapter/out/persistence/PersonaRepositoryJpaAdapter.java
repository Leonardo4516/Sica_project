package com.sicaproject.sica.personas.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.empresas.infrastructure.adapter.out.persistence.EmpresaEntity;
import com.sicaproject.sica.personas.application.port.out.PersonaRepository;
import com.sicaproject.sica.personas.domain.Persona;
import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de Infraestructura JPA para el puerto de salida PersonaRepository.
 * Gestiona el acceso a datos de trabajadores e invitados en PostgreSQL:
 * consultas por documento/tipo de documento, guardado idempotente (persist/merge),
 * actualización de restricciones perimetrales y eliminación física.
 */
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
    public Optional<Persona> buscarPorTipoYDocumento(String tipoDocumento, String documento) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<PersonaEntity> query = em.createQuery(
                    "SELECT p FROM PersonaEntity p WHERE p.tipoDocumento = :tipo AND p.documento = :doc", PersonaEntity.class);
            query.setParameter("tipo", tipoDocumento);
            query.setParameter("doc", documento);
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
            PersonaEntity entity;
            if (persona.getId() != 0) {
                entity = em.find(PersonaEntity.class, persona.getId());
                if (entity == null) {
                    throw new IllegalArgumentException("Persona no encontrada: " + persona.getId());
                }
                PersonaMapper.copyToEntity(persona, entity);
                if (persona.getEmpresaId() != null) {
                    entity.setEmpresa(em.getReference(EmpresaEntity.class, persona.getEmpresaId()));
                } else {
                    entity.setEmpresa(null);
                }
                entity = em.merge(entity);
            } else {
                entity = new PersonaEntity();
                PersonaMapper.copyToEntity(persona, entity);
                if (persona.getEmpresaId() != null) {
                    entity.setEmpresa(em.getReference(EmpresaEntity.class, persona.getEmpresaId()));
                }
                em.persist(entity);
            }
            em.getTransaction().commit();
            persona.setId(entity.getId());
            persona.setTotalVisitas(entity.getTotalVisitas());
            return persona;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Persona> porId(Long id) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            PersonaEntity entity = em.find(PersonaEntity.class, id);
            return Optional.ofNullable(entity).map(PersonaMapper::toDomain);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Persona> listarTodas() {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<PersonaEntity> query = em.createQuery("SELECT p FROM PersonaEntity p", PersonaEntity.class);
            return query.getResultStream().map(PersonaMapper::toDomain).toList();
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

    @Override
    public void eliminar(Long id) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            em.getTransaction().begin();
            // 1. Desvincular de cuentas de usuario del sistema (persona_id -> null)
            em.createQuery("UPDATE UsuarioEntity u SET u.persona = null WHERE u.persona.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            // 2. Desvincular de incidentes de seguridad (persona_id -> null) preservando el registro de la anomalía
            em.createQuery("UPDATE IncidenteEntity i SET i.persona = null WHERE i.persona.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            // 3. Eliminar visitas asociadas a la persona
            em.createQuery("DELETE FROM VisitaEntity v WHERE v.persona.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            // 4. Eliminar físicamente el registro de la persona
            PersonaEntity entity = em.find(PersonaEntity.class, id);
            if (entity != null) {
                em.remove(entity);
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