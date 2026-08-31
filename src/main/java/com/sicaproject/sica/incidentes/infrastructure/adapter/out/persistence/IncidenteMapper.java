package com.sicaproject.sica.incidentes.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.empresas.infrastructure.adapter.out.persistence.EmpresaMapper;
import com.sicaproject.sica.iam.infrastructure.adapter.out.persistence.UsuarioMapper;
import com.sicaproject.sica.incidentes.domain.Incidente;
import com.sicaproject.sica.incidentes.domain.SeveridadIncidente;
import com.sicaproject.sica.personas.infrastructure.adapter.out.persistence.PersonaMapper;

public class IncidenteMapper {

    public static Incidente toDomain(IncidenteEntity entity) {
        if (entity == null) return null;
        Incidente domain = new Incidente();
        domain.setId(entity.getId() != null ? entity.getId() : 0L);
        domain.setTitulo(entity.getTitulo());
        domain.setDescripcion(entity.getDescripcion());
        try {
            domain.setSeveridad(SeveridadIncidente.valueOf(entity.getSeveridad()));
        } catch (Exception e) {
            domain.setSeveridad(SeveridadIncidente.MEDIA);
        }
        domain.setEstado(entity.getEstado());
        if (entity.getPersona() != null) {
            domain.setPersona(PersonaMapper.toDomain(entity.getPersona()));
        }
        if (entity.getEmpresa() != null) {
            domain.setEmpresa(EmpresaMapper.toDomain(entity.getEmpresa()));
        }
        if (entity.getReportadoPor() != null) {
            domain.setReportadoPor(UsuarioMapper.toDomain(entity.getReportadoPor()));
        }
        domain.setFechaHora(entity.getFechaHora());
        return domain;
    }

    public static void copyToEntity(Incidente domain, IncidenteEntity entity) {
        entity.setTitulo(domain.getTitulo());
        entity.setDescripcion(domain.getDescripcion());
        entity.setSeveridad(domain.getSeveridad() != null ? domain.getSeveridad().name() : "MEDIA");
        entity.setEstado(domain.getEstado() != null ? domain.getEstado() : "ABIERTO");
        entity.setFechaHora(domain.getFechaHora());
    }
}
