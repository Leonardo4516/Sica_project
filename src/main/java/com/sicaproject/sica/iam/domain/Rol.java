package com.sicaproject.sica.iam.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String nombre; // GUARDA, FUNCIONARIO, ADMIN

    @OneToOne(mappedBy = "rol")
    private Usuario usuario;
}