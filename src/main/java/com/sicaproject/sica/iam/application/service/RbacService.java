package com.sicaproject.sica.iam.application.service;

import com.sicaproject.sica.iam.application.port.in.RbacUseCase;
import com.sicaproject.sica.iam.domain.Permiso;
import com.sicaproject.sica.iam.domain.Rol;
import com.sicaproject.sica.iam.domain.Usuario;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Antes: los permisos vivían hardcodeados en un Map estático dentro de esta clase.
 * Ahora: el Rol trae sus Permiso reales desde la base de datos (tabla rol_permiso),
 * cargados por RolMapper/RolEntity. Esta clase solo pregunta al dominio, no decide.
 * Esto es justo lo que pide el documento: "los permisos no están definidos en el
 * código, sino en la base de datos".
 */
public class RbacService implements RbacUseCase {

    // =========================================================================
    // VERIFICACIÓN DE PERMISOS DINÁMICOS EN BASE DE DATOS (RBAC)
    // =========================================================================
    /**
     * Evalúa si un usuario cuenta con un permiso específico consultando la matriz
     * de roles y permisos persistida en base de datos.
     * 
     * @param usuario Usuario a evaluar.
     * @param permisoCodigo Código único del permiso (ej. 'registrar_visita', 'bloquear_persona').
     * @return true si el usuario está activo y su rol contiene el permiso; false en caso contrario.
     */
    @Override
    public boolean tienePermiso(Usuario usuario, String permisoCodigo) {
        if (usuario == null || usuario.getRol() == null || !usuario.isActivo()) {
            return false;
        }
        return usuario.getRol().tienePermiso(permisoCodigo);
    }

    @Override
    public Set<String> obtenerPermisos(Rol rol) {
        if (rol == null) return Set.of();
        return rol.getPermisos().stream().map(Permiso::getCodigo).collect(Collectors.toSet());
    }

    /**
     * Valida de manera estricta que el usuario posea el permiso requerido para una acción sensible.
     * Si no lo posee, interrumpe el flujo lanzando una excepción de dominio.
     * 
     * @param usuario Usuario solicitante.
     * @param permisoCodigo Código del permiso exigido por la operación.
     * @throws PermisoDenegadoException Si el usuario no cuenta con la autorización necesaria.
     */
    public void verificarPermiso(Usuario usuario, String permisoCodigo) {
        if (!tienePermiso(usuario, permisoCodigo)) {
            throw new PermisoDenegadoException(usuario, permisoCodigo);
        }
    }
}
