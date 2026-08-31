package com.sicaproject.sica.incidentes.application.port.out;

import com.sicaproject.sica.incidentes.domain.Incidente;
import java.util.List;
import java.util.Optional;

public interface IncidenteRepository {
    Incidente guardar(Incidente incidente);
    Optional<Incidente> porId(long id);
    List<Incidente> listarTodos();
    List<Incidente> listarPorPersona(long personaId);
}
