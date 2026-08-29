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
-- hash generado y verificado con AuthService.hashPassword("123456"): $2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6

-- Usuario GUARDA
INSERT INTO usuario (id, username, password_hash, activo, rol_id)
VALUES (1, 'guarda1', '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, 1);

-- Usuario FUNCIONARIO
INSERT INTO usuario (id, username, password_hash, activo, rol_id)
VALUES (2, 'funcionario1', '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, 2);

-- Usuario ADMIN
INSERT INTO usuario (id, username, password_hash, activo, rol_id)
VALUES (3, 'admin1', '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, 3);

-- ========== EMPRESAS DE PRUEBA ==========
INSERT INTO empresa (id, nombre, nit) VALUES
(1, 'Empresa Demo S.A.', '900123456-1'),
(2, 'Constructora Los Andes', '900654321-2'),
(3, 'Tecnología Avanzada Ltda', '900789123-3');

-- ========== PERSONAS DE PRUEBA ==========
INSERT INTO persona (id, tipo, tipo_documento, nombre, documento, empresa_id, bloqueado) VALUES
(1, 'TRABAJADOR', 'CC', 'Carlos Pérez', '12345678', 1, FALSE),
(2, 'TRABAJADOR', 'CC', 'María González', '87654321', 2, FALSE),
(3, 'INVITADO', 'CC', 'Juan Rodríguez', '11223344', NULL, FALSE),
(4, 'INVITADO', 'PASAPORTE', 'Ana Martínez', '55667788', NULL, FALSE);

-- Sincronizar secuencias para inserciones dinámicas de Hibernate
SELECT setval(pg_get_serial_sequence('persona', 'id'), coalesce(max(id), 1)) FROM persona;
SELECT setval(pg_get_serial_sequence('empresa', 'id'), coalesce(max(id), 1)) FROM empresa;
SELECT setval(pg_get_serial_sequence('usuario', 'id'), coalesce(max(id), 1)) FROM usuario;
SELECT setval(pg_get_serial_sequence('rol', 'id'), coalesce(max(id), 1)) FROM rol;
SELECT setval(pg_get_serial_sequence('permiso', 'id'), coalesce(max(id), 1)) FROM permiso;
SELECT setval(pg_get_serial_sequence('visita', 'id'), coalesce(max(id), 1)) FROM visita;
SELECT setval(pg_get_serial_sequence('bitacora_auditoria', 'id'), coalesce(max(id), 1)) FROM bitacora_auditoria;