package com.sicaproject.sica.iam.application.service;

import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.iam.application.port.out.RolRepository;
import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import com.sicaproject.sica.iam.domain.Rol;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.personas.domain.Persona;
import java.util.List;
import java.util.Optional;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final RbacService rbacService;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                          RbacService rbacService, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.rbacService = rbacService;
        this.auditoriaService = auditoriaService;
    }

    public Usuario crearUsuario(String username, String passwordPlano, String nombreRol,
                                Persona personaAsociada, Usuario usuarioSolicitante) {
        if (usuarioSolicitante != null && rbacService != null) {
            rbacService.verificarPermiso(usuarioSolicitante, "gestionar_usuarios");
        }

        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con el username: " + username);
        }

        Rol rol = rolRepository.porNombre(nombreRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + nombreRol));

        Usuario nuevo = new Usuario();
        nuevo.setUsername(username);
        nuevo.setPasswordHash(AuthService.hashPassword(passwordPlano));
        nuevo.setActivo(true);
        nuevo.setRol(rol);
        nuevo.setPersona(personaAsociada);

        Usuario guardado = usuarioRepository.save(nuevo);

        if (auditoriaService != null) {
            Long adminId = usuarioSolicitante != null ? usuarioSolicitante.getId() : null;
            auditoriaService.registrar(adminId, "USUARIO_CREADO", "USUARIO", guardado.getId(),
                    "Usuario creado: " + username + " con rol " + nombreRol, "EXITOSO");
        }

        return guardado;
    }

    public Usuario cambiarEstado(long usuarioId, boolean activo, Usuario usuarioSolicitante) {
        if (usuarioSolicitante != null && rbacService != null) {
            rbacService.verificarPermiso(usuarioSolicitante, "gestionar_usuarios");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));

        usuario.setActivo(activo);
        Usuario guardado = usuarioRepository.save(usuario);

        if (auditoriaService != null) {
            Long adminId = usuarioSolicitante != null ? usuarioSolicitante.getId() : null;
            auditoriaService.registrar(adminId, "USUARIO_ESTADO_CAMBIADO", "USUARIO", guardado.getId(),
                    "Estado de usuario " + usuario.getUsername() + " cambiado a " + (activo ? "ACTIVO" : "INACTIVO"), "EXITOSO");
        }

        return guardado;
    }

    public Optional<Usuario> porId(long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> porUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarPorRol(String nombreRol) {
        return usuarioRepository.findByRol(nombreRol);
    }
}
