package com.sicaproject.sica.shared.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    @Test
    void debe_rechazar_documentos_cc_invalidos() {
        // Documentos con letras no permitidas en CC (como 'aaaaaa')
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarDocumento("CC", "aaaaaa"));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarDocumento("CC", "10203a50"));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarDocumento("CC", "123")); // Muy corto
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarDocumento("CC", "1234567890123")); // Muy largo
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarDocumento("CC", "0000000000")); // Repetidos
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarDocumento("CC", null));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarDocumento("CC", "   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1020304050", "52145896", "80123456", "1098765432", "79854123"})
    void debe_aceptar_documentos_cc_validos(String doc) {
        assertDoesNotThrow(() -> InputValidator.validarDocumento("CC", doc));
    }

    @Test
    void debe_validar_ce_y_pasaporte() {
        assertDoesNotThrow(() -> InputValidator.validarDocumento("CE", "E1234567"));
        assertDoesNotThrow(() -> InputValidator.validarDocumento("CE", "98765432"));
        assertDoesNotThrow(() -> InputValidator.validarDocumento("PASAPORTE", "PA-987654"));

        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarDocumento("CE", "ab")); // Corto
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarDocumento("PASAPORTE", "123")); // Corto
    }

    @Test
    void debe_rechazar_nombres_de_persona_invalidos() {
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarNombrePersona("123456"));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarNombrePersona("Carlos 123"));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarNombrePersona("a"));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarNombrePersona("aaaaaa"));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarNombrePersona(null));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarNombrePersona("   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Carlos Andrés Pérez", "María José Gómez", "Leonardo Hernández", "John O'Connor", "Ana Sofía"})
    void debe_aceptar_nombres_de_persona_validos(String nombre) {
        assertDoesNotThrow(() -> InputValidator.validarNombrePersona(nombre));
    }

    @Test
    void debe_validar_nit_empresas() {
        assertDoesNotThrow(() -> InputValidator.validarNit("900123456-1"));
        assertDoesNotThrow(() -> InputValidator.validarNit("800234567"));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarNit("abc"));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarNit("123"));
    }

    @Test
    void debe_validar_username_y_password() {
        assertDoesNotThrow(() -> InputValidator.validarUsername("guarda1"));
        assertDoesNotThrow(() -> InputValidator.validarUsername("admin_sec"));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarUsername("a"));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarUsername("user@bad!"));

        assertDoesNotThrow(() -> InputValidator.validarPassword("123456"));
        assertThrows(IllegalArgumentException.class, () -> InputValidator.validarPassword("12"));
    }
}
