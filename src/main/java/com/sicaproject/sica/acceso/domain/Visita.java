package com.sicaproject.sica.acceso.domain;

/**
 * Entidad de Dominio que representa un evento de visita o acceso físico en Zona Acme.
 * Encapsula la persona visitante/trabajador, la empresa receptora, el funcionario anfitrión,
 * el operador en portería, los timestamps de entrada/salida y las reglas de transición de estado.
 */
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
     * Punto centralizado para la mutación de estado de la visita.
     * Valida de manera estricta contra la máquina de estados EstadoVisita.puedeTransicionarA
     * impidiendo inconsistencias en la base de datos (por ejemplo, check-out sin previo check-in).
     * 
     * @param nuevoEstado Estado al que se desea mover la visita.
     * @throws IllegalStateException Si la transición solicitada viola las reglas de negocio.
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