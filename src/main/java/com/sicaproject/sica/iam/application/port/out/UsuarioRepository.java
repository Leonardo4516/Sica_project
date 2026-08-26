package com.sicaproject.sica.iam.application.port.out;

import com.sicaproject.sica.iam.domain.Usuario;
import java.util.Optional;

public interface UsuarioRepository {
    Optional<Usuario> findByUsername(String username);
    Usuario save(Usuario usuario);
}