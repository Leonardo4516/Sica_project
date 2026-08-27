-- Datos iniciales para SICA
-- Roles, permisos y usuarios de prueba

-- ========== PERMISOS ==========
INSERT INTO permiso (codigo, descripcion) VALUES
-- Accesos
('registrar_visita', 'Registrar ingreso de visitantes'),
('aprobar_visita', 'Aprobar solicitudes de acceso'),
('rechazar_visita', 'Rechazar solicitudes de acceso'),
('registrar_salida', 'Registrar salida de visitantes'),
-- Administración
('gestionar_usuarios', 'Crear/editar/eliminar usuarios'),
('gestionar_roles', 'Gestionar roles y permisos'),
('gestionar_empresas', 'Gestionar empresas'),
('ver_bitacora', 'Ver bitácora de auditoría'),
('ver_dashboard', 'Ver dashboard administrativo');

-- ========== ROLES ==========
INSERT INTO rol (id, nombre) VALUES
(1, 'GUARDA'),
(2, 'FUNCIONARIO'),
(3, 'ADMIN');

-- ========== ASIGNACIÓN DE PERMISOS A ROLES ==========
-- GUARDA: registrar_visita, registrar_salida
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r, permiso p
WHERE r.nombre = 'GUARDA' AND p.codigo IN ('registrar_visita', 'registrar_salida');

-- FUNCIONARIO: aprobar_visita, rechazar_visita
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r, permiso p
WHERE r.nombre = 'FUNCIONARIO' AND p.codigo IN ('aprobar_visita', 'rechazar_visita');

-- ADMIN: todos los permisos
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r, permiso p
WHERE r.nombre = 'ADMIN';

-- ========== USUARIOS DE PRUEBA ==========
-- Contraseña: "123456" (hasheada con BCrypt)
-- hash generado con BCrypt.gensalt(10): $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiS5FQ5e6ePPFR.bE6GhNBTu9u

-- Usuario GUARDA
INSERT INTO usuario (id, username, password_hash, activo, rol_id)
VALUES (1, 'guarda1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiS5FQ5e6ePPFR.bE6GhNBTu9u', TRUE, 1);

-- Usuario FUNCIONARIO
INSERT INTO usuario (id, username, password_hash, activo, rol_id)
VALUES (2, 'funcionario1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiS5FQ5e6ePPFR.bE6GhNBTu9u', TRUE, 2);

-- Usuario ADMIN
INSERT INTO usuario (id, username, password_hash, activo, rol_id)
VALUES (3, 'admin1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiS5FQ5e6ePPFR.bE6GhNBTu9u', TRUE, 3);

-- ========== EMPRESAS DE PRUEBA ==========
INSERT INTO empresa (id, nombre, nit) VALUES
(1, 'Empresa Demo S.A.', '900123456-1'),
(2, 'Constructora Los Andes', '900654321-2'),
(3, 'Tecnología Avanzada Ltda', '900789123-3');

-- ========== PERSONAS DE PRUEBA ==========
INSERT INTO persona (id, tipo, nombre, documento, empresa_id, bloqueado) VALUES
(1, 'TRABAJADOR', 'Carlos Pérez', 'CC12345678', 1, FALSE),
(2, 'TRABAJADOR', 'María González', 'CC87654321', 2, FALSE),
(3, 'INVITADO', 'Juan Rodríguez', 'CC11223344', NULL, FALSE),
(4, 'INVITADO', 'Ana Martínez', 'CC55667788', NULL, FALSE);