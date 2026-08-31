-- Schema SQL para SICA (Sistema Integrado de Control de Acceso)
-- Generado a partir de entidades JPA para PostgreSQL

CREATE TABLE rol (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE permiso (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
);

CREATE TABLE rol_permiso (
    rol_id BIGINT NOT NULL REFERENCES rol(id) ON DELETE CASCADE,
    permiso_id BIGINT NOT NULL REFERENCES permiso(id) ON DELETE CASCADE,
    PRIMARY KEY (rol_id, permiso_id)
);

CREATE TABLE empresa (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    nit VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE persona (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(20),
    tipo_documento VARCHAR(10),
    nombre VARCHAR(200) NOT NULL,
    documento VARCHAR(100),
    foto_url VARCHAR(500),
    empresa_id BIGINT,
    bloqueado BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_bloqueo VARCHAR(500),
    total_visitas INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    persona_id BIGINT,
    rol_id BIGINT NOT NULL REFERENCES rol(id)
);

ALTER TABLE persona ADD CONSTRAINT fk_persona_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id);
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_persona FOREIGN KEY (persona_id) REFERENCES persona(id);

CREATE TABLE visita (
    id BIGSERIAL PRIMARY KEY,
    persona_id BIGINT NOT NULL REFERENCES persona(id),
    empresa_destino_id BIGINT NOT NULL REFERENCES empresa(id),
    funcionario_anfitrion_id BIGINT REFERENCES usuario(id),
    guarda_id BIGINT REFERENCES usuario(id),
    estado VARCHAR(50) NOT NULL,
    es_pase_temporal BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_hora_registro TIMESTAMP NOT NULL,
    fecha_hora_entrada TIMESTAMP,
    fecha_hora_salida TIMESTAMP,
    observaciones TEXT
);

CREATE TABLE bitacora_auditoria (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT,
    accion VARCHAR(100) NOT NULL,
    entidad_afectada VARCHAR(100) NOT NULL,
    entidad_id BIGINT NOT NULL,
    detalle TEXT,
    resultado VARCHAR(20),
    fecha_hora TIMESTAMP NOT NULL
);
ALTER TABLE bitacora_auditoria ADD CONSTRAINT fk_bitacora_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id);

CREATE TABLE incidente (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    severidad VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
    persona_id BIGINT REFERENCES persona(id),
    empresa_id BIGINT REFERENCES empresa(id),
    reportado_por_id BIGINT NOT NULL REFERENCES usuario(id),
    fecha_hora TIMESTAMP NOT NULL
);

CREATE INDEX idx_visita_persona ON visita(persona_id);
CREATE INDEX idx_visita_estado ON visita(estado);
CREATE INDEX idx_visita_fechahora ON visita(fecha_hora_registro);
CREATE INDEX idx_bitacora_usuario ON bitacora_auditoria(usuario_id);
CREATE INDEX idx_bitacora_fecha ON bitacora_auditoria(fecha_hora);
CREATE INDEX idx_usuario_rol ON usuario(rol_id);
CREATE INDEX idx_persona_empresa ON persona(empresa_id);
CREATE INDEX idx_incidente_fecha ON incidente(fecha_hora);
CREATE INDEX idx_incidente_severidad ON incidente(severidad);