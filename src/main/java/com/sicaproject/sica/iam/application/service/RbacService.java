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
     * Comprueba si un usuario tiene un permiso específico.
     * 
     * [LÓGICA DEL NEGOCIO]:
     * - Si usuario es null, no tiene rol o está inactivo -> retorna false.
     * - Consulta a usuario.getRol().tienePermiso(permisoCodigo), que a su vez
     *   recorre la colección de permisos traídos desde la tabla 'rol_permiso' de PostgreSQL.
     * 
     * [PISTAS PARA EL DEBUG / EVALUACIÓN]:
     * - Si te borran !usuario.isActivo(): usuarios desactivados podrán hacer operaciones.
     * - Si te borran usuario.getRol().tienePermiso(permisoCodigo): nadie tendrá permisos o todos los tendrán.
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
     * Lanza excepción de dominio PermisoDenegadoException si el usuario no tiene el permiso.
     * 
     * [PISTAS PARA EL DEBUG]:
     * - Todos los servicios (VisitaService, PersonaService, etc.) llaman a este método ANTES
     *   de ejecutar cualquier operación sensible.
     * - Si te borran el 'throw new PermisoDenegadoException(usuario, permisoCodigo);',
     *   las pruebas de seguridad RBAC fallarán.
     */
    public void verificarPermiso(Usuario usuario, String permisoCodigo) {
        if (!tienePermiso(usuario, permisoCodigo)) {
            throw new PermisoDenegadoException(usuario, permisoCodigo);
        }
    }
}
