package com.sicaproject.sica.personas.application.service;

import com.sicaproject.sica.personas.domain.Persona;
import com.sicaproject.sica.personas.application.port.out.PersonaRepository;
import java.util.Optional;

public class PersonaService {

    private final PersonaRepository personaRepository;

    public PersonaService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    public Persona crearPersona(Persona persona) {
        return personaRepository.guardar(persona);
    }

    public Persona guardar(Persona persona) {
        return personaRepository.guardar(persona);
    }

    public Optional<Persona> porDocumento(String documento) {
        return personaRepository.buscarPorDocumento(documento);
    }

    public Optional<Persona> porId(Long id) {
        return personaRepository.porId(id);
    }

    public java.util.List<Persona> listarTodas() {
        return personaRepository.listarTodas();
    }

    public void bloquearPersona(Persona persona, String motivo) {
        persona.setBloqueado(true);
        persona.setMotivoBloqueo(motivo);
        personaRepository.actualizar(persona);
    }
}