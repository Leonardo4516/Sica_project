package com.sicaproject.sica.acceso.application.port.out;

import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import java.util.List;
import java.util.Optional;

public interface VisitaRepository {
    Visita guardar(Visita visita);
    Optional<Visita> porId(long id);
    List<Visita> porPersonaYEstado(long personaId, EstadoVisita estado);
    List<Visita> listarPendientesPorFuncionario(long funcionarioId);
    List<Visita> listarTodas();
}
