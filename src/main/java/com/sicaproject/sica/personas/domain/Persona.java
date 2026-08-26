package com.sicaproject.sica.personas.domain;

import jakarta.persistence.*;
import lombok.*;
import com.sicaproject.sica.empresas.domain.Empresa;

@Entity
@Table(name = "persona")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String tipo; // TRABAJADOR / INVITADO

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(unique = true, nullable = false, length = 20)
    private String documento;

    @Column(length = 255)
    private String fotoUrl;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Column(name = "bloqueado", nullable = false)
    private boolean bloqueado;

    @Column(length = 255)
    private String motivoBloqueo;
}