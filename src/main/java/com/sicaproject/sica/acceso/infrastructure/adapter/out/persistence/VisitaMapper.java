package com.sicaproject.sica.acceso.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.empresas.infrastructure.adapter.out.persistence.EmpresaMapper;
import com.sicaproject.sica.iam.infrastructure.adapter.out.persistence.UsuarioMapper;
import com.sicaproject.sica.personas.infrastructure.adapter.out.persistence.PersonaMapper;

public final class VisitaMapper {

    private VisitaMapper() {}

    public static Visita toDomain(VisitaEntity entity) {
        if (entity == null) return null;
        Visita visita = new Visita();
        visita.setId(entity.getId());
        visita.setPersona(PersonaMapper.toDomain(entity.getPersona()));
        visita.setEmpresaDestino(EmpresaMapper.toDomain(entity.getEmpresaDestino()));
        if (entity.getFuncionarioAnfitrion() != null) {
            visita.setFuncionarioAnfitrion(UsuarioMapper.toDomain(entity.getFuncionarioAnfitrion()));
        }
        visita.setGuarda(UsuarioMapper.toDomain(entity.getGuarda()));
        visita.setEstado(EstadoVisita.valueOf(entity.getEstado()));
        visita.setPaseTemporal(entity.isPasetemporal());
        visita.setFechaHoraRegistro(entity.getFechaHoraRegistro());
        visita.setFechaHoraEntrada(entity.getFechaHoraEntrada());
        visita.setFechaHoraSalida(entity.getFechaHoraSalida());
        visita.setObservaciones(entity.getObservaciones());
        return visita;
    }

    public static void copyToEntity(Visita visita, VisitaEntity entity) {
        entity.setEstado(visita.getEstado().name());
        entity.setPasetemporal(visita.isPaseTemporal());
        entity.setFechaHoraRegistro(visita.getFechaHoraRegistro());
        entity.setFechaHoraEntrada(visita.getFechaHoraEntrada());
        entity.setFechaHoraSalida(visita.getFechaHoraSalida());
        entity.setObservaciones(visita.getObservaciones());
        // Las relaciones (persona, empresa, guarda, funcionario) se resuelven en el adaptador
    }
}
