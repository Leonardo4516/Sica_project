-- Migración: agregar columna tipo_documento a tabla persona
-- Ejecutar este script en la base de datos existente antes de usar las nuevas funcionalidades

ALTER TABLE persona ADD COLUMN IF NOT EXISTS tipo_documento VARCHAR(10);

-- Actualizar personas existentes con valores por defecto
UPDATE persona SET tipo_documento = 'CC' WHERE tipo_documento IS NULL AND tipo = 'TRABAJADOR';
UPDATE persona SET tipo_documento = 'CC' WHERE tipo_documento IS NULL AND tipo = 'INVITADO';
