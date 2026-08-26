package com.sicaproject.sica.iam.application.port.in;

import com.sicaproject.sica.iam.domain.Rol;
import com.sicaproject.sica.iam.domain.Usuario;
import java.util.Set;

/**
 * Puertos de entrada para operaciones RBAC.
 * Estos son los casos de uso que el cliente (UI) invoca.
 */
public interface RbacUseCase {

    /**
     * Verifica si el usuario tiene permiso para ejecutar una acción.
     * @param usuario El usuario que intenta la acción
     * @param permisoCodigo El código del permiso requerido (ej. "registrar_visita")
     * @return true si tiene permiso, false en caso contrario
     */
    boolean tienePermiso(Usuario usuario, String permisoCodigo);

    /**
     * Obtiene todos los permisos asociados a un rol.
     * @param rol El rol del usuario
     * @return Set de códigos de permiso
     */
    java.util.Set<String> obtenerPermisos(Rol rol);
}