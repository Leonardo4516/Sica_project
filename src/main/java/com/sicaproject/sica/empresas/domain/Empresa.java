package com.sicaproject.sica.empresas.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresa")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(unique = true, nullable = false, length = 20)
    private String nit;
}