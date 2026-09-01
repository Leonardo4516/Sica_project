package com.sicaproject.sica.personas.application.port.out;

import com.sicaproject.sica.personas.domain.Persona;
import java.util.List;
import java.util.Optional;

public interface PersonaRepository {
    Optional<Persona> buscarPorDocumento(String documento);
    Optional<Persona> buscarPorTipoYDocumento(String tipoDocumento, String documento);
    Optional<Persona> porId(Long id);
    List<Persona> listarTodas();
    Persona guardar(Persona persona);
    void actualizar(Persona persona);
    void eliminar(Long id);
}