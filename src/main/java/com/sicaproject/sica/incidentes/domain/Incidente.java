package com.sicaproject.sica.incidentes.domain;

import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.personas.domain.Persona;
import java.time.LocalDateTime;

/**
 * Entidad de Dominio que modela un evento anómalo o incidente de seguridad en el complejo.
 * Almacena el título, descripción, clasificación de riesgo (severidad), estado de resolución,
 * personas o empresas involucradas, usuario reportante y marca temporal.
 */
public class Incidente {
    private long id;
    private String titulo;
    private String descripcion;
    private SeveridadIncidente severidad; // Nivel de criticidad: BAJA, MEDIA, ALTA, CRITICA
    private String estado; // Estado de ciclo de vida: ABIERTO, EN_PROCESO, RESUELTO, CERRADO
    private Persona persona; // Persona involucrada en la anomalía (opcional)
    private Empresa empresa; // Empresa residente vinculada (opcional)
    private Usuario reportadoPor; // Usuario operador o funcionario que generó la alerta
    private LocalDateTime fechaHora; // Timestamp exacto del reporte

    public Incidente() {
        this.severidad = SeveridadIncidente.MEDIA;
        this.estado = "ABIERTO";
        this.fechaHora = LocalDateTime.now();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public SeveridadIncidente getSeveridad() { return severidad; }
    public void setSeveridad(SeveridadIncidente severidad) { this.severidad = severidad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public Usuario getReportadoPor() { return reportadoPor; }
    public void setReportadoPor(Usuario reportadoPor) { this.reportadoPor = reportadoPor; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
