package com.sicaproject.sica.iam.application.service;

import com.sicaproject.sica.iam.domain.Usuario;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private static Map<String, Usuario> usuarios = new HashMap<>();

    public AuthService() {
        // Inicializar con usuarios de prueba
        if (usuarios.isEmpty()) {
            var adminRol = new com.sicaproject.sica.iam.domain.Rol();
            adminRol.setId(1);
            adminRol.setNombre("ADMIN");

            var admin = new Usuario();
            admin.setId(1L);
            admin.setUsername("admin");
            admin.setPasswordHash("admin123"); // En producción: BCrypt
            admin.setActivo("true");
            admin.setRol(adminRol);
            usuarios.put("admin", admin);

            var guardaRol = new com.sicaproject.sica.iam.domain.Rol();
            guardaRol.setId(2);
            guardaRol.setNombre("GUARDA");

            var guarda = new Usuario();
            guarda.setId(2L);
            guarda.setUsername("guarda");
            guarda.setPasswordHash("guarda123");
            guarda.setActivo("true");
            guarda.setRol(guardaRol);
            usuarios.put("guarda", guarda);

            var funcionarioRol = new com.sicaproject.sica.iam.domain.Rol();
            funcionarioRol.setId(3);
            funcionarioRol.setNombre("FUNCIONARIO");

            var funcionario = new Usuario();
            funcionario.setId(3L);
            funcionario.setUsername("funcionario");
            funcionario.setPasswordHash("funcionario123");
            funcionario.setActivo("true");
            funcionario.setRol(funcionarioRol);
            usuarios.put("funcionario", funcionario);
        }
    }

    public Usuario login(String username, String password) {
        var usuario = usuarios.get(username);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        if (!usuario.getPasswordHash().equals(password)) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        if (!"true".equals(usuario.getActivo())) {
            throw new RuntimeException("Usuario inactivo");
        }
        return usuario;
    }
}