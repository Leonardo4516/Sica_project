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
     * Autentica a un usuario comparando su contraseña en texto plano contra el hash BCrypt en BD.
     * 
     * [LÓGICA DEL NEGOCIO]:
     * 1. Busca el usuario en PostgreSQL por su username exacto.
     * 2. Si no existe: audita LOGIN_FALLIDO y retorna Optional.empty().
     * 3. Si existe pero está inactivo: audita LOGIN_FALLIDO y retorna Optional.empty().
     * 4. Valida la contraseña usando BCrypt.checkpw(passwordPlano, usuario.getPasswordHash()).
     *    ¡NUNCA uses .equals() para comparar hashes de BCrypt porque BCrypt usa salt aleatorio!
     * 5. Si coincide: audita LOGIN_EXITOSO y retorna Optional.of(usuario).
     * 
     * [PISTAS PARA EL DEBUG / EVALUACIÓN]:
     * - Si te borran el chequeo BCrypt.checkpw: la contraseña no se validará o fallará.
     *   Línea clave: if (!BCrypt.checkpw(passwordPlano, usuario.getPasswordHash())) return Optional.empty();
     * - Si te cambian checkpw por equals(): NUNCA dará true porque el salt de BCrypt es diferente cada vez.
     * - Si te borran !usuario.isActivo(): un usuario desactivado por el administrador podrá entrar.
     * - Si te borran la auditoría: los tests de auditoría (AuthServiceAuditTest) fallarán.
     */
    public Optional<Usuario> login(String username, String passwordPlano) {
        // 1. Consulta del usuario por nombre de usuario
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            registrarAuditoria(null, "LOGIN_FALLIDO", 0L,
                    "Intento de login fallido: usuario '" + username + "' no encontrado", "FALLIDO");
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();

        // 2. Comprobar que la cuenta no haya sido desactivada por el Admin
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
