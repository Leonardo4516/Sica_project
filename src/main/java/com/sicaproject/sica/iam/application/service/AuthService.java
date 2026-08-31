package com.sicaproject.sica.iam.application.service;

import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import com.sicaproject.sica.iam.domain.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;

public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public AuthService(UsuarioRepository usuarioRepository) {
        this(usuarioRepository, null);
    }

    public AuthService(UsuarioRepository usuarioRepository, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Intenta autenticar. Nunca lanza excepción con detalle específico de
     * "usuario no existe" vs "contraseña incorrecta" hacia afuera de este método,
     * para no filtrar información útil a un atacante — solo Optional.empty()
     * si algo no coincide.
     * Audita tanto los intentos exitosos como los fallidos según los requerimientos de SICA.
     */
    public Optional<Usuario> login(String username, String passwordPlano) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            registrarAuditoria(null, "LOGIN_FALLIDO", 0L,
                    "Intento de login fallido: usuario '" + username + "' no encontrado", "FALLIDO");
            return Optional.empty();
        }
        Usuario usuario = usuarioOpt.get();
        if (!usuario.isActivo()) {
            registrarAuditoria(usuario.getId(), "LOGIN_FALLIDO", usuario.getId(),
                    "Intento de login fallido: usuario '" + username + "' se encuentra inactivo", "FALLIDO");
            return Optional.empty();
        }
        if (!BCrypt.checkpw(passwordPlano, usuario.getPasswordHash())) {
            registrarAuditoria(usuario.getId(), "LOGIN_FALLIDO", usuario.getId(),
                    "Intento de login fallido: contraseña incorrecta para usuario '" + username + "'", "FALLIDO");
            return Optional.empty();
        }

        registrarAuditoria(usuario.getId(), "LOGIN_EXITOSO", usuario.getId(),
                "Inicio de sesión exitoso para usuario '" + username + "' con rol " +
                        (usuario.getRol() != null ? usuario.getRol().getNombre() : "N/A"), "EXITOSO");

        return Optional.of(usuario);
    }

    private void registrarAuditoria(Long usuarioId, String accion, long entidadId, String detalle, String resultado) {
        if (auditoriaService != null) {
            try {
                auditoriaService.registrar(usuarioId, accion, "USUARIO", entidadId, detalle, resultado);
            } catch (Exception e) {
                System.err.println("Error al auditar login: " + e.getMessage());
            }
        }
    }

    public static String hashPassword(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(10));
    }
}
