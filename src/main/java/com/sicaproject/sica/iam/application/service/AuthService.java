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

    // =========================================================================
    // AUTENTICACIÓN CENTRAL Y AUDITORÍA DE INICIOS DE SESIÓN
    // =========================================================================
    /**
     * Autentica credenciales de usuario mediante verificación criptográfica BCrypt
     * y genera trazabilidad inmutable de cada intento (exitoso o fallido).
     * 
     * Flujo de validación:
     * 1. Consulta el registro de usuario por su nombre de usuario en persistencia.
     * 2. Si no existe: audita 'LOGIN_FALLIDO' y retorna Optional.empty().
     * 3. Si la cuenta está desactivada: audita 'LOGIN_FALLIDO' y retorna Optional.empty().
     * 4. Valida la contraseña mediante BCrypt.checkpw (contraseña en texto plano vs hash con salt).
     * 5. Si es correcta: audita 'LOGIN_EXITOSO' y retorna Optional.of(usuario).
     * 
     * @param username Nombre de usuario provisto en el login.
     * @param passwordPlano Contraseña en texto plano ingresada en la interfaz.
     * @return Optional con el Usuario autenticado, o Optional.empty() si las credenciales son inválidas.
     */
    public Optional<Usuario> login(String username, String passwordPlano) {
        // Consulta del usuario por nombre de usuario en repositorio JPA
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            registrarAuditoria(null, "LOGIN_FALLIDO", 0L,
                    "Intento de login fallido: usuario '" + username + "' no encontrado", "FALLIDO");
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();

        // Validar estado de activación de la cuenta
        if (!usuario.isActivo()) {
            registrarAuditoria(usuario.getId(), "LOGIN_FALLIDO", usuario.getId(),
                    "Intento de login fallido: usuario '" + username + "' se encuentra inactivo", "FALLIDO");
            return Optional.empty();
        }

        // 3. Verificación criptográfica con BCrypt (algoritmo hash seguro con salt)
        if (!BCrypt.checkpw(passwordPlano, usuario.getPasswordHash())) {
            registrarAuditoria(usuario.getId(), "LOGIN_FALLIDO", usuario.getId(),
                    "Intento de login fallido: contraseña incorrecta para usuario '" + username + "'", "FALLIDO");
            return Optional.empty();
        }

        // 4. Registro inmutable del login exitoso en bitacora_auditoria
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
