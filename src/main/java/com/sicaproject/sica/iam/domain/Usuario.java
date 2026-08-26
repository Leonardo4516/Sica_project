package com.sicaproject.sica.iam.domain;

import jakarta.persistence.*;
import lombok.*;
import com.sicaproject.sica.personas.domain.Persona;

@Entity
@Table(name = "usuario")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @OneToOne
    @JoinColumn(name = "persona_id", nullable = true)
    private Persona persona;

    @Column(nullable = false)
    private boolean activo;
}