package com.sicaproject.sica.incidentes.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.empresas.infrastructure.adapter.out.persistence.EmpresaEntity;
import com.sicaproject.sica.iam.infrastructure.adapter.out.persistence.UsuarioEntity;
import com.sicaproject.sica.personas.infrastructure.adapter.out.persistence.PersonaEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidente")
public class IncidenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String severidad;

    @Column(nullable = false, length = 20)
    private String estado = "ABIERTO";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id")
    private PersonaEntity persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private EmpresaEntity empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportado_por_id", nullable = false)
    private UsuarioEntity reportadoPor;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    public IncidenteEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getSeveridad() { return severidad; }
    public void setSeveridad(String severidad) { this.severidad = severidad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public PersonaEntity getPersona() { return persona; }
    public void setPersona(PersonaEntity persona) { this.persona = persona; }

    public EmpresaEntity getEmpresa() { return empresa; }
    public void setEmpresa(EmpresaEntity empresa) { this.empresa = empresa; }

    public UsuarioEntity getReportadoPor() { return reportadoPor; }
    public void setReportadoPor(UsuarioEntity reportadoPor) { this.reportadoPor = reportadoPor; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
