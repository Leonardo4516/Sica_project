package com.sicaproject.sica.iam.application.service;

import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import java.util.Optional;

public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario login(String username, String password) {
        Optional<Usuario> optional = usuarioRepository.findByUsername(username);
        if (optional.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        Usuario usuario = optional.get();
        // En producción verificar con BCrypt
        if (!usuario.getPasswordHash().equals(password)) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        if (!usuario.isActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }
        return usuario;
    }
}