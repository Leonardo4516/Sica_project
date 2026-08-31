-- Migración: agregar columna total_visitas a tabla persona
-- Ejecutar este script en la base de datos existente

ALTER TABLE persona ADD COLUMN IF NOT EXISTS total_visitas INTEGER NOT NULL DEFAULT 0;
