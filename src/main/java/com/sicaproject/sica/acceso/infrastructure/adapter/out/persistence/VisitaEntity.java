package com.sicaproject.sica.acceso.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.empresas.infrastructure.adapter.out.persistence.EmpresaEntity;
import com.sicaproject.sica.iam.infrastructure.adapter.out.persistence.UsuarioEntity;
import com.sicaproject.sica.personas.infrastructure.adapter.out.persistence.PersonaEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visita")
public class VisitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private PersonaEntity persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_destino_id", nullable = false)
    private EmpresaEntity empresaDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_anfitrion_id")
    private UsuarioEntity funcionarioAnfitrion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guarda_id", nullable = false)
    private UsuarioEntity guarda;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(name = "es_pase_temporal", nullable = false)
    private boolean pasetemporal;

    @Column(name = "fecha_hora_registro", nullable = false)
    private LocalDateTime fechaHoraRegistro;

    @Column(name = "fecha_hora_entrada")
    private LocalDateTime fechaHoraEntrada;

    @Column(name = "fecha_hora_salida")
    private LocalDateTime fechaHoraSalida;

    @Column(length = 500)
    private String observaciones;

    public VisitaEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PersonaEntity getPersona() { return persona; }
    public void setPersona(PersonaEntity persona) { this.persona = persona; }

    public EmpresaEntity getEmpresaDestino() { return empresaDestino; }
    public void setEmpresaDestino(EmpresaEntity empresaDestino) { this.empresaDestino = empresaDestino; }

    public UsuarioEntity getFuncionarioAnfitrion() { return funcionarioAnfitrion; }
    public void setFuncionarioAnfitrion(UsuarioEntity funcionarioAnfitrion) { this.funcionarioAnfitrion = funcionarioAnfitrion; }

    public UsuarioEntity getGuarda() { return guarda; }
    public void setGuarda(UsuarioEntity guarda) { this.guarda = guarda; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isPasetemporal() { return pasetemporal; }
    public void setPasetemporal(boolean pasetemporal) { this.pasetemporal = pasetemporal; }

    public LocalDateTime getFechaHoraRegistro() { return fechaHoraRegistro; }
    public void setFechaHoraRegistro(LocalDateTime fechaHoraRegistro) { this.fechaHoraRegistro = fechaHoraRegistro; }

    public LocalDateTime getFechaHoraEntrada() { return fechaHoraEntrada; }
    public void setFechaHoraEntrada(LocalDateTime fechaHoraEntrada) { this.fechaHoraEntrada = fechaHoraEntrada; }

    public LocalDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
