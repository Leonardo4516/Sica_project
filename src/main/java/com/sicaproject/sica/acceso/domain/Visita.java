package com.sicaproject.sica.acceso.domain;

public class Visita {
    private long id;
    private com.sicaproject.sica.personas.domain.Persona persona;
    private com.sicaproject.sica.empresas.domain.Empresa empresaDestino;
    private com.sicaproject.sica.iam.domain.Usuario funcionarioAnfitrion;
    private com.sicaproject.sica.iam.domain.Usuario guarda;
    private EstadoVisita estado;
    private boolean paseTemporal;
    private java.time.LocalDateTime fechaHoraRegistro;
    private java.time.LocalDateTime fechaHoraEntrada;
    private java.time.LocalDateTime fechaHoraSalida;
    private String observaciones;

    public Visita() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public com.sicaproject.sica.personas.domain.Persona getPersona() { return persona; }
    public void setPersona(com.sicaproject.sica.personas.domain.Persona persona) { this.persona = persona; }

    public com.sicaproject.sica.empresas.domain.Empresa getEmpresaDestino() { return empresaDestino; }
    public void setEmpresaDestino(com.sicaproject.sica.empresas.domain.Empresa empresaDestino) { this.empresaDestino = empresaDestino; }

    public com.sicaproject.sica.iam.domain.Usuario getFuncionarioAnfitrion() { return funcionarioAnfitrion; }
    public void setFuncionarioAnfitrion(com.sicaproject.sica.iam.domain.Usuario funcionarioAnfitrion) { this.funcionarioAnfitrion = funcionarioAnfitrion; }

    public com.sicaproject.sica.iam.domain.Usuario getGuarda() { return guarda; }
    public void setGuarda(com.sicaproject.sica.iam.domain.Usuario guarda) { this.guarda = guarda; }

    public EstadoVisita getEstado() { return estado; }
    public void setEstado(EstadoVisita estado) { this.estado = estado; }

    /**
     * Punto único para cambiar de estado. Valida contra EstadoVisita.puedeTransicionarA
     * antes de aplicar el cambio -- evita que el sistema quede en un estado inconsistente
     * (ej. intentar hacer check-out de una visita que nunca hizo check-in).
     */
    public void cambiarEstado(EstadoVisita nuevoEstado) {
        if (this.estado != null && !this.estado.puedeTransicionarA(nuevoEstado)) {
            throw new IllegalStateException(
                "Transición de estado inválida: " + this.estado + " -> " + nuevoEstado);
        }
        this.estado = nuevoEstado;
    }

    public boolean isPaseTemporal() { return paseTemporal; }
    public void setPaseTemporal(boolean paseTemporal) { this.paseTemporal = paseTemporal; }

    public java.time.LocalDateTime getFechaHoraRegistro() { return fechaHoraRegistro; }
    public void setFechaHoraRegistro(java.time.LocalDateTime fechaHoraRegistro) { this.fechaHoraRegistro = fechaHoraRegistro; }

    public java.time.LocalDateTime getFechaHoraEntrada() { return fechaHoraEntrada; }
    public void setFechaHoraEntrada(java.time.LocalDateTime fechaHoraEntrada) { this.fechaHoraEntrada = fechaHoraEntrada; }

    public java.time.LocalDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(java.time.LocalDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}