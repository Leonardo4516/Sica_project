package com.sicaproject.sica.shared.util;

/**
 * Validador transversal de integridad de entradas para el sistema SICA.
 * Centraliza las reglas de negocio y restricciones de formato para documentos de identidad,
 * nombres de personas, NITs empresariales, credenciales de usuario y reportes de seguridad.
 */
public final class InputValidator {

    private InputValidator() {}

    /**
     * Valida el formato y la coherencia del documento de identidad según su tipo.
     * 
     * Reglas aplicadas:
     * - CC (Cédula de Ciudadanía): Exclusivamente numérico de 6 a 10 dígitos (ej. 1020304050).
     * - CE (Cédula de Extranjería): Alfanumérico de 6 a 12 caracteres.
     * - PASAPORTE: Alfanumérico de 6 a 15 caracteres.
     * - No permite secuencias compuestas por un único carácter repetido (ej. "000000", "aaaaaa").
     * 
     * @param tipoDocumento Tipo de documento (CC, CE, PASAPORTE).
     * @param documento Número de identificación a validar.
     * @throws IllegalArgumentException Si el documento no cumple con las restricciones de formato.
     */
    public static void validarDocumento(String tipoDocumento, String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de documento de identidad es obligatorio.");
        }

        String doc = documento.trim();
        String tipo = (tipoDocumento != null && !tipoDocumento.trim().isEmpty())
                ? tipoDocumento.trim().toUpperCase()
                : "CC";

        // Rechazar valores triviales repetidos (ej. "000000", "aaaaaa")
        if (doc.matches("^(.)\\1{4,}$")) {
            throw new IllegalArgumentException("El número de documento no es válido (contiene caracteres repetidos no admisibles: '" + doc + "').");
        }

        switch (tipo) {
            case "CC" -> {
                if (!doc.matches("^[0-9]{6,10}$")) {
                    throw new IllegalArgumentException("La Cédula de Ciudadanía (CC) debe contener únicamente dígitos numéricos (entre 6 y 10 dígitos). Valor ingresado: '" + doc + "'");
                }
            }
            case "CE" -> {
                if (!doc.matches("^[a-zA-Z0-9]{6,12}$")) {
                    throw new IllegalArgumentException("La Cédula de Extranjería (CE) debe ser alfanumérica y tener entre 6 y 12 caracteres. Valor ingresado: '" + doc + "'");
                }
            }
            case "PASAPORTE" -> {
                if (!doc.matches("^[a-zA-Z0-9-]{6,15}$")) {
                    throw new IllegalArgumentException("El Pasaporte debe ser alfanumérico y tener entre 6 y 15 caracteres. Valor ingresado: '" + doc + "'");
                }
            }
            default -> {
                if (doc.length() < 5 || doc.length() > 20) {
                    throw new IllegalArgumentException("El documento debe tener entre 5 y 20 caracteres.");
                }
            }
        }
    }

    /**
     * Valida el nombre completo de una persona (trabajador o visitante).
     * 
     * Reglas aplicadas:
     * - Mínimo 3 caracteres, máximo 100 caracteres.
     * - Solo admite letras, tildes, diéresis, espacios y caracteres ortográficos estándar (., '-).
     * - No admite números ni símbolos especiales.
     * 
     * @param nombre Nombre completo a validar.
     * @throws IllegalArgumentException Si el nombre no cumple con el formato permitido.
     */
    public static void validarNombrePersona(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la persona es obligatorio.");
        }

        String nom = nombre.trim();
        if (nom.length() < 3 || nom.length() > 100) {
            throw new IllegalArgumentException("El nombre de la persona debe tener entre 3 y 100 caracteres.");
        }

        // Rechazar caracteres numéricos o caracteres especiales no admisibles en nombres
        if (!nom.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s.'-]+$")) {
            throw new IllegalArgumentException("El nombre de la persona solo puede contener letras y espacios válidos (sin números ni símbolos extraños).");
        }

        // Rechazar repeticiones triviales
        if (nom.matches("^(.)\\1{3,}$")) {
            throw new IllegalArgumentException("El nombre ingresado no es válido.");
        }
    }

    /**
     * Valida el Número de Identificación Tributaria (NIT) de una empresa residente.
     * 
     * @param nit NIT de la empresa a validar.
     * @throws IllegalArgumentException Si el NIT no coincide con el estándar numérico.
     */
    public static void validarNit(String nit) {
        if (nit == null || nit.trim().isEmpty()) {
            throw new IllegalArgumentException("El NIT de la empresa es obligatorio.");
        }

        String nitLimpio = nit.trim();
        if (!nitLimpio.matches("^[0-9]{6,12}(-[0-9kK])?$")) {
            throw new IllegalArgumentException("El formato del NIT es inválido (debe tener entre 6 y 12 dígitos, opcionalmente seguido de guión y dígito de verificación). Valor: '" + nitLimpio + "'");
        }
    }

    /**
     * Valida el nombre de usuario para el inicio de sesión.
     * 
     * @param username Nombre de usuario a evaluar.
     * @throws IllegalArgumentException Si el username es demasiado corto o posee caracteres inválidos.
     */
    public static void validarUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }

        String user = username.trim();
        if (!user.matches("^[a-zA-Z0-9_.]{3,30}$")) {
            throw new IllegalArgumentException("El nombre de usuario debe ser alfanumérico (entre 3 y 30 caracteres, permitiendo puntos y guiones bajos).");
        }
    }

    /**
     * Valida la longitud y consistencia de una contraseña en texto plano antes de encriptarla.
     */
    public static void validarPassword(String password) {
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres.");
        }
    }

    /**
     * Valida los campos requeridos para la emisión de un reporte de incidente.
     */
    public static void validarIncidente(String titulo, String descripcion) {
        if (titulo == null || titulo.trim().length() < 5) {
            throw new IllegalArgumentException("El título del incidente debe tener al menos 5 caracteres.");
        }
        if (descripcion == null || descripcion.trim().length() < 10) {
            throw new IllegalArgumentException("La descripción del incidente debe ser detallada (mínimo 10 caracteres).");
        }
    }

    /**
     * Valida que el motivo de restricción perimetral esté debidamente justificado.
     */
    public static void validarMotivoBloqueo(String motivo) {
        if (motivo == null || motivo.trim().length() < 5) {
            throw new IllegalArgumentException("Debe especificar una justificación clara para el bloqueo perimetral (mínimo 5 caracteres).");
        }
    }
}
