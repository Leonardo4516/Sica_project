package com.sicaproject.sica.iam.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.iam.domain.Permiso;
import com.sicaproject.sica.iam.domain.Rol;
import java.util.stream.Collectors;

public final class RolMapper {

    private RolMapper() {}

    public static Rol toDomain(RolEntity entity) {
        if (entity == null) return null;
        Rol rol = new Rol();
        rol.setId(entity.getId());
        rol.setNombre(entity.getNombre());
        rol.setPermisos(entity.getPermisos().stream()
                .map(RolMapper::permisoToDomain)
                .collect(Collectors.toSet()));
        return rol;
    }

    private static Permiso permisoToDomain(PermisoEntity p) {
        return new Permiso(p.getId(), p.getCodigo(), p.getDescripcion());
    }
}
