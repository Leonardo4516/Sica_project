package com.sicaproject.sica.iam.application.port.out;

import com.sicaproject.sica.iam.domain.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findById(Long id);
    List<Usuario> findAll();
    List<Usuario> findByRol(String rolNombre);
    Usuario save(Usuario usuario);
}