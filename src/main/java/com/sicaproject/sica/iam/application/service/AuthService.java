package com.sicaproject.sica.iam.application.service;

import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import com.sicaproject.sica.iam.domain.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;

public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Intenta autenticar. Nunca lanza excepción con detalle específico de
     * "usuario no existe" vs "contraseña incorrecta" hacia afuera de este método,
     * para no filtrar información útil a un atacante — solo Optional.empty()
     * si algo no coincide.
     */
    public Optional<Usuario> login(String username, String passwordPlano) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        Usuario usuario = usuarioOpt.get();
        if (!usuario.isActivo()) {
            return Optional.empty();
        }
        if (!BCrypt.checkpw(passwordPlano, usuario.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(usuario);
    }

    public static String hashPassword(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(10));
    }
}
