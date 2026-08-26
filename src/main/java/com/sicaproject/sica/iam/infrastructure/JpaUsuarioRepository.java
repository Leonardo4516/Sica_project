package com.sicaproject.sica.iam.infrastructure;

import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Optional;

public class JpaUsuarioRepository implements UsuarioRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Usuario> findByUsername(String username) {
        javax.persistence.Query query = entityManager.createQuery(
                "SELECT u FROM Usuario u WHERE u.username = :username", Usuario.class);
        query.setParameter("username", username);
        return Optional.ofNullable((Usuario) query.getSingleResult());
    }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        entityManager.persist(usuario);
        return usuario;
    }
}