package com.sicaproject.sica.acceso.domain;

import jakarta.persistence.*;
import lombok.*;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.personas.domain.Persona;

@Entity
@Table(name = "visita")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "empresa_destino_id", nullable = false)
    private Empresa empresaDestino;

    @ManyToOne
    @JoinColumn(name = "funcionario_anfitrion_id", nullable = true)
    private Usuario funcionarioAnfitrion;

    @ManyToOne
    @JoinColumn(name = "guarda_id", nullable = false)
    private Usuario guarda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVisita estado;

    @Column(name = "es_pase_temporal", nullable = false)
    private boolean paseTemporal;

    @Column(name = "fecha_hora_registro", nullable = false)
    private java.time.LocalDateTime fechaHoraRegistro;

    @Column(name = "fecha_hora_entrada")
    private java.time.LocalDateTime fechaHoraEntrada;

    @Column(name = "fecha_hora_salida")
    private java.time.LocalDateTime fechaHoraSalida;

    @Column(length = 500)
    private String observaciones;
}