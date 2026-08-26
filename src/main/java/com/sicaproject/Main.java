package com.sicaproject;

import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;

/**
 * Smoke test de la capa de persistencia (alternativa headless).
 * Para iniciar la UI con JavaFX, ejecutar com.sicaproject.SicaApplication.
 */
public class Main {
    public static void main(String[] args) {
        try {
            var empresas = CompositionRoot.getInstance().empresaService().listar();
            System.out.println("Conexión a BD exitosa. Empresas encontradas: " + empresas.size());
            for (Empresa e : empresas) {
                System.out.println(" - " + e.getNombre() + " (" + e.getNit() + ")");
            }
        } catch (Exception e) {
            System.err.println("Error conectando a la base de datos: " + e.getMessage());
        } finally {
            JpaConfig.close();
        }
    }
}
