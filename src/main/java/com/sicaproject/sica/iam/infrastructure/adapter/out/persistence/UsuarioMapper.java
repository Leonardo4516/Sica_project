package com.sicaproject.sica.iam.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.personas.infrastructure.adapter.out.persistence.PersonaEntity;
import com.sicaproject.sica.personas.infrastructure.adapter.out.persistence.PersonaMapper;

public final class UsuarioMapper {

    private UsuarioMapper() {}

    public static Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) return null;
        Usuario usuario = new Usuario();
        usuario.setId(entity.getId());
        usuario.setUsername(entity.getUsername());
        usuario.setPasswordHash(entity.getPasswordHash());
        usuario.setActivo(entity.isActivo());
        usuario.setRol(RolMapper.toDomain(entity.getRol()));
        if (entity.getPersona() != null) {
            usuario.setPersona(PersonaMapper.toDomain(entity.getPersona()));
        }
        return usuario;
    }
}
