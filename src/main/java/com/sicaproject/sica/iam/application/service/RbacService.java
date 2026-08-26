package com.sicaproject.sica.iam.application.service;

import com.sicaproject.sica.iam.domain.Rol;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.iam.application.port.in.RbacUseCase;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementación concreta del patrón Strategy para autorización RBAC.
 * Los permisos están definidos en esta clase, no en el código de negocio.
 */
public class RbacService implements RbacUseCase {

    // Definición de permisos por rol (configurable, no hardcodeado en lógica de negocio)
    // Estructura: rol -> set de permisos
    private static final java.util.Map<String, Set<String>> PERMISOS_POR_ROL = new java.util.HashMap<>();

    static {
        // Permisos para el rol GUARDA
        var guardaPermisos = new HashSet<String>();
        guardaPermisos.add("registrar_visita");
        guardaPermisos.add("registrar_salida");
        guardaPermisos.add("ver_visitantes");
        PERMISOS_POR_ROL.put("GUARDA", guardaPermisos);

        // Permisos para el rol FUNCIONARIO
        var funcionarioPermisos = new HashSet<String>();
        funcionarioPermisos.add("aprobar_visita");
        funcionarioPermisos.add("rechazar_visita");
        funcionarioPermisos.add("ver_solicitudes");
        PERMISOS_POR_ROL.put("FUNCIONARIO", funcionarioPermisos);

        // Permisos para el rol ADMIN
        var adminPermisos = new HashSet<String>();
        adminPermisos.add("registrar_visita");
        adminPermisos.add("registrar_salida");
        adminPermisos.add("aprobar_visita");
        adminPermisos.add("rechazar_visita");
        adminPermisos.add("generar_reporte");
        adminPermisos.add("bloquear_persona");
        adminPermisos.add("desbloquear_persona");
        adminPermisos.add("gestionar_usuarios");
        adminPermisos.add("ver_todas_las_visitas");
        PERMISOS_POR_ROL.put("ADMIN", adminPermisos);
    }

    @Override
    public boolean tienePermiso(Usuario usuario, String permisoCodigo) {
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }
        
        var permisos = PERMISOS_POR_ROL.get(usuario.getRol().getNombre());
        if (permisos == null) {
            return false;
        }
        
        return permisos.contains(permisoCodigo);
    }

    @Override
    public java.util.Set<String> obtenerPermisos(Rol rol) {
        var permisos = PERMISOS_POR_ROL.get(rol.getNombre());
        if (permisos != null) {
            return new HashSet<>(permisos);
        }
        return new HashSet<>();
    }
}