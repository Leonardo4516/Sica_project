package com.sicaproject.sica.iam.application.service;

import com.sicaproject.sica.iam.domain.Usuario;

public class PermisoDenegadoException extends RuntimeException {
    public PermisoDenegadoException(Usuario usuario, String permisoCodigo) {
        super(String.format(
            "El usuario '%s' (rol %s) no tiene el permiso requerido: '%s'",
            usuario != null ? usuario.getUsername() : "desconocido",
            usuario != null && usuario.getRol() != null ? usuario.getRol().getNombre() : "N/A",
            permisoCodigo
        ));
    }
}
