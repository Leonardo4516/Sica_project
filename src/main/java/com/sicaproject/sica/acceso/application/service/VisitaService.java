package com.sicaproject.sica.acceso.application.service;

import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.personas.domain.Persona;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.domain.Usuario;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VisitaService {

    private static Map<Long, Visita> visitas = new HashMap<>();
    private static long nextVisitaId = 1;

    // Referencias a otros servicios
    private final com.sicaproject.sica.iam.application.service.AuthService authService;
    private final com.sicaproject.sica.personas.application.service.PersonaService personaService;
    private final com.sicaproject.sica.empresas.application.service.EmpresaService empresaService;

    public VisitaService(
            com.sicaproject.sica.iam.application.service.AuthService authService,
            com.sicaproject.sica.personas.application.service.PersonaService personaService,
            com.sicaproject.sica.empresas.application.service.EmpresaService empresaService) {
        this.authService = authService;
        this.personaService = personaService;
        this.empresaService = empresaService;
    }

    // Registrar check-in para un visitante pre-registrado
    public com.sicaproject.sica.acceso.domain.Visita checkInPreRegistrado(
            String username, com.sicaproject.sica.empresas.domain.Empresa empresaDestino) {
        var usuario = authService.login(username, "visitante123");
        var visita = new com.sicaproject.sica.acceso.domain.Visita();
        visita.setId(nextVisitaId++);
        visita.setPersona(usuario.getPersona());
        visita.setEmpresaDestino(empresaDestino);
        visita.setGuarda(usuario);
        visita.setEstado(EstadoVisita.APROBADA);
        visita.setFechaHoraRegistro(LocalDateTime.now());
        visitas.put(visita.getId(), visita);
        return visita;
    }

    // Registrar solicitud de acceso (invitado no anunciado)
    public com.sicaproject.sica.acceso.domain.Visita solicitarAcceso(
            com.sicaproject.sica.personas.domain.Persona persona,
            com.sicaproject.sica.empresas.domain.Empresa empresaDestino,
            com.sicaproject.sica.iam.domain.Usuario funcionarioAnfitrion) {
        var visita = new com.sicaproject.sica.acceso.domain.Visita();
        visita.setId(nextVisitaId++);
        visita.setPersona(persona);
        visita.setEmpresaDestino(empresaDestino);
        visita.setFuncionarioAnfitrion(funcionarioAnfitrion);
        visita.setEstado(EstadoVisita.PENDIENTE_APROBACION);
        visita.setFechaHoraRegistro(LocalDateTime.now());
        visitas.put(visita.getId(), visita);
        return visita;
    }

    // Aprobar una visita pendiente
    public boolean aprobarVisita(long visitaId) {
        var visita = visitas.get(visitaId);
        if (visita == null) return false;
        
        if (visita.getEstado().puedeTransicionarA(EstadoVisita.APROBADA)) {
            visita.setEstado(EstadoVisita.APROBADA);
            return true;
        }
        return false;
    }

    // Realizar check-out
    public boolean checkOut(long visitaId) {
        var visita = visitas.get(visitaId);
        if (visita == null) return false;
        
        if (visita.getEstado() == EstadoVisita.APROBADA) {
            visita.setEstado(EstadoVisita.DENTRO);
            visita.setFechaHoraSalida(LocalDateTime.now());
            return true;
        }
        return false;
    }

    // Marcar salida olvidada (para el flujo de "Salida Olvidada")
    public void marcarSalidaOlvidada(long visitaId) {
        var visita = visitas.get(visitaId);
        if (visita != null) {
            // La visita que estaba abierta se marca como CERRADA_POR_SISTEMA
            visita.setEstado(EstadoVisita.CERRADA_POR_SISTEMA);
            visita.setFechaHoraSalida(LocalDateTime.now());
            // Se crea un nuevo registro para el nuevo ingreso
            // (handled elsewhere)
        }
    }

    // Obtener visita por ID
    public Optional<Visita> obtenerVisita(long visitaId) {
        return Optional.ofNullable(visitas.get(visitaId));
    }

    // Verificar si una persona ya tiene una visita activa (dentro del complejo)
    public boolean tieneVisitaActiva(long personaId) {
        return visitas.values().stream()
                .anyMatch(v -> v.getPersona().getId() == personaId && 
                       v.getEstado() == EstadoVisita.DENTRO);
    }
}