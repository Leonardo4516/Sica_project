package com.sicaproject.sica.personas.application.port.out;

import com.sicaproject.sica.personas.domain.Persona;
import java.util.Optional;

public interface PersonaRepository {
    Optional<Persona> buscarPorDocumento(String documento);
    Persona guardar(Persona persona);
    void actualizar(Persona persona);
}