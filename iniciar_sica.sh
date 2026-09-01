#!/usr/bin/env bash
# ==============================================================================
# SICA — Script Ejecutable de Escritorio
# Complejo Empresarial Zona Acme
# ==============================================================================
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

# Verificar si PostgreSQL está listo, si no, levantar contenedor si docker está instalado
if ! pg_isready -h localhost -p 5432 -U sica_user -d sica_db >/dev/null 2>&1; then
    echo "Verificando base de datos PostgreSQL..."
    if command -v docker &>/dev/null; then
        docker compose -f docker/docker-compose.yml up -d >/dev/null 2>&1 || docker compose up -d >/dev/null 2>&1 || true
        sleep 2
    fi
fi

# Ejecutar la aplicación JavaFX
mvn javafx:run
