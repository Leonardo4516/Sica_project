package com.sicaproject.sica.iam.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Optional;

public class UsuarioRepositoryJpaAdapter implements UsuarioRepository {

    @Override
    public Optional<Usuario> findByUsername(String username) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            TypedQuery<UsuarioEntity> query = em.createQuery(
                    "SELECT u FROM UsuarioEntity u WHERE u.username = :username", UsuarioEntity.class);
            query.setParameter("username", username);
            return query.getResultStream().findFirst().map(UsuarioMapper::toDomain);
        } finally {
            em.close();
        }
    }

    @Override
    public Usuario save(Usuario usuario) {
        EntityManager em = JpaConfig.newEntityManager();
        try {
            em.getTransaction().begin();
            UsuarioEntity entity;
            if (usuario.getId() != null) {
                entity = em.find(UsuarioEntity.class, usuario.getId());
            } else {
                entity = new UsuarioEntity();
            }
            entity.setUsername(usuario.getUsername());
            entity.setPasswordHash(usuario.getPasswordHash());
            entity.setActivo(usuario.isActivo());
            entity.setRol(em.getReference(RolEntity.class, usuario.getRol().getId()));
            if (usuario.getPersona() != null) {
                entity.setPersona(em.getReference(
                        com.sicaproject.sica.personas.infrastructure.adapter.out.persistence.PersonaEntity.class,
                        usuario.getPersona().getId()));
            }
            if (entity.getId() == null) {
                em.persist(entity);
            }
            em.getTransaction().commit();
            usuario.setId(entity.getId());
            return usuario;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
