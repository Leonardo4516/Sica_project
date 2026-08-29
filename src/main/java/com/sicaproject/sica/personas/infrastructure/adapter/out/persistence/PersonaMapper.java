package com.sicaproject.sica.personas.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.personas.domain.Persona;

public final class PersonaMapper {

    private PersonaMapper() {}

    public static Persona toDomain(PersonaEntity entity) {
        if (entity == null) return null;
        Persona persona = new Persona();
        persona.setId(entity.getId());
        persona.setTipo(entity.getTipo());
        persona.setTipoDocumento(entity.getTipoDocumento());
        persona.setNombre(entity.getNombre());
        persona.setDocumento(entity.getDocumento());
        persona.setFotoUrl(entity.getFotoUrl());
        persona.setEmpresaId(entity.getEmpresa() != null ? entity.getEmpresa().getId() : null);
        persona.setBloqueado(entity.isBloqueado());
        persona.setMotivoBloqueo(entity.getMotivoBloqueo());
        return persona;
    }

public static void copyToEntity(Persona persona, PersonaEntity entity) {
        entity.setTipo(persona.getTipo());
        entity.setTipoDocumento(persona.getTipoDocumento());
        entity.setNombre(persona.getNombre());
        entity.setDocumento(persona.getDocumento());
        entity.setFotoUrl(persona.getFotoUrl());
        entity.setBloqueado(persona.isBloqueado());
        entity.setMotivoBloqueo(persona.getMotivoBloqueo());
    }
}
