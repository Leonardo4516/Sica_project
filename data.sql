-- Datos iniciales y maestros para SICA (Sistema Integrado de Control de Acceso)
-- Complejo Empresarial Zona Acme

-- ========== PERMISOS ==========
INSERT INTO permiso (codigo, descripcion) VALUES
-- Accesos
('registrar_visita', 'Registrar ingreso de visitantes'),
('aprobar_visita', 'Aprobar solicitudes de acceso'),
('rechazar_visita', 'Rechazar solicitudes de acceso'),
('registrar_salida', 'Registrar salida de visitantes'),
-- Incidentes y Auditoría
('reportar_incidente', 'Reportar incidente de seguridad'),
('gestionar_incidentes', 'Gestionar y cerrar incidentes'),
('generar_reporte', 'Generar reportes y métricas de seguridad'),
-- Administración
('gestionar_usuarios', 'Crear/editar/eliminar usuarios'),
('gestionar_roles', 'Gestionar roles y permisos'),
('gestionar_empresas', 'Gestionar empresas'),
('ver_bitacora', 'Ver bitácora de auditoría'),
('ver_dashboard', 'Ver dashboard administrativo'),
('bloquear_persona', 'Bloquear/desbloquear personas');

-- ========== ROLES ==========
INSERT INTO rol (id, nombre) VALUES
(1, 'GUARDA'),
(2, 'FUNCIONARIO'),
(3, 'ADMIN');

-- ========== ASIGNACIÓN DE PERMISOS A ROLES ==========
-- GUARDA: registrar_visita, registrar_salida, reportar_incidente
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r, permiso p
WHERE r.nombre = 'GUARDA' AND p.codigo IN ('registrar_visita', 'registrar_salida', 'reportar_incidente');

-- FUNCIONARIO: aprobar_visita, rechazar_visita, registrar_visita, bloquear_persona, reportar_incidente
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r, permiso p
WHERE r.nombre = 'FUNCIONARIO' AND p.codigo IN ('aprobar_visita', 'rechazar_visita', 'registrar_visita', 'bloquear_persona', 'reportar_incidente');

-- ADMIN: todos los permisos
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r, permiso p
WHERE r.nombre = 'ADMIN';

-- ========== EMPRESAS DEL COMPLEJO ZONA ACME ==========
INSERT INTO empresa (id, nombre, nit) VALUES
(1, 'Bavaria S.A.', '860005224-6'),
(2, 'Grupo Nutresa S.A.S.', '890900050-1'),
(3, 'Bancolombia S.A.', '890903938-8'),
(4, 'Ecopetrol S.A.', '899999068-1'),
(5, 'Celsia Energía S.A.', '900276962-4'),
(6, 'Sodimac Corona Colombia', '800242106-2');

-- ========== PERSONAS REALES DEL PADRÓN ==========
INSERT INTO persona (id, tipo, tipo_documento, nombre, documento, foto_url, empresa_id, bloqueado, motivo_bloqueo, total_visitas) VALUES
-- Trabajadores
(1, 'TRABAJADOR', 'CC', 'Carlos Andrés Pérez Morales', '1017123456', 'https://i.pravatar.cc/150?u=carlos', 1, FALSE, NULL, 15),
(2, 'TRABAJADOR', 'CC', 'María Alejandra González Ruiz', '1020456789', 'https://i.pravatar.cc/150?u=maria', 1, FALSE, NULL, 22),
(3, 'TRABAJADOR', 'CE', 'José Alejandro Restrepo Gómez', '2345678', 'https://i.pravatar.cc/150?u=jose', 1, FALSE, NULL, 8),
(4, 'TRABAJADOR', 'CC', 'Laura Sofía Torres Castro', '1035678901', 'https://i.pravatar.cc/150?u=laura', 2, FALSE, NULL, 12),
(5, 'TRABAJADOR', 'CC', 'Roberto Carlos Díaz Valencia', '1028345671', 'https://i.pravatar.cc/150?u=roberto', 2, FALSE, NULL, 19),
(6, 'TRABAJADOR', 'CC', 'Andrés Felipe López Zapata', '1014567890', 'https://i.pravatar.cc/150?u=andres', 3, FALSE, NULL, 30),
(7, 'TRABAJADOR', 'CC', 'Valentina Mendoza Herrera', '1019876543', 'https://i.pravatar.cc/150?u=valentina', 3, FALSE, NULL, 14),
(8, 'TRABAJADOR', 'CC', 'Javier Eduardo Silva Pineda', '1015678234', 'https://i.pravatar.cc/150?u=javier', 4, FALSE, NULL, 9),
(9, 'TRABAJADOR', 'CC', 'Diana Marcela Osorio Cano', '1022334455', 'https://i.pravatar.cc/150?u=diana', 4, FALSE, NULL, 17),

-- Invitados y Contratistas
(10, 'INVITADO', 'CC', 'Leonardo Hernández', '1100955643', 'https://i.pravatar.cc/150?u=leo', NULL, FALSE, NULL, 6),
(11, 'INVITADO', 'CC', 'Juan Camilo Rodríguez Vélez', '1018234567', 'https://i.pravatar.cc/150?u=juan', NULL, FALSE, NULL, 4),
(12, 'INVITADO', 'CC', 'Ana María Martínez Duarte', '1025678912', 'https://i.pravatar.cc/150?u=ana', NULL, FALSE, NULL, 3),
(13, 'INVITADO', 'PASAPORTE', 'Emily Watson', 'AB782910', 'https://i.pravatar.cc/150?u=emily', NULL, FALSE, NULL, 2),
(14, 'INVITADO', 'CC', 'Pedro Nel Hernández Botero', '1032123456', 'https://i.pravatar.cc/150?u=pedro', NULL, FALSE, NULL, 5),
(15, 'INVITADO', 'CC', 'Sofía Vargas Quintana', '1024567812', 'https://i.pravatar.cc/150?u=sofia', NULL, FALSE, NULL, 1),
(16, 'INVITADO', 'CE', 'Marco Rossi', '45678901', 'https://i.pravatar.cc/150?u=marco', NULL, FALSE, NULL, 3),
(17, 'INVITADO', 'PASAPORTE', 'Isabella Moreau', 'CD901234', 'https://i.pravatar.cc/150?u=isabella', NULL, FALSE, NULL, 1),
(18, 'INVITADO', 'CC', 'Diego Alejandro Morales Londoño', '1013456789', 'https://i.pravatar.cc/150?u=diego', NULL, FALSE, NULL, 4),
(19, 'INVITADO', 'CC', 'Claudia Patricia Ríos Montoya', '1029876543', 'https://i.pravatar.cc/150?u=claudia', NULL, FALSE, NULL, 2),
(20, 'INVITADO', 'CC', 'Héctor Fabio Ramírez Arias', '1016789234', 'https://i.pravatar.cc/150?u=hector', NULL, TRUE, 'Incumplimiento reiterado de normas de seguridad industrial en planta', 7);

-- ========== USUARIOS DEL SISTEMA ==========
-- Contraseña estándar para todos: "123456" (BCrypt)
-- Hash: $2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6

INSERT INTO usuario (id, username, password_hash, activo, persona_id, rol_id) VALUES
(1, 'guarda1', '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, NULL, 1),
(2, 'funcionario1', '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, 1, 2),
(3, 'funcionario2', '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, 4, 2),
(4, 'funcionario3', '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, 6, 2),
(5, 'admin1', '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, NULL, 3);

-- ========== VISITAS REALISTAS INICIALES ==========
-- 1. Visita DENTRO (Carlos Andrés Pérez en Bavaria)
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (1, 1, 1, 2, 1, 'DENTRO', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '2 hours 50 minutes', NULL, 'Ingreso a jornada laboral matutina', FALSE);

-- 2. Visita DENTRO (Leonardo Hernández en Bavaria)
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (2, 10, 1, 2, 1, 'DENTRO', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour 45 minutes', NULL, 'Reunión de coordinación de proyectos', FALSE);

-- 3. Visita DENTRO (Laura Sofía Torres en Nutresa)
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (3, 4, 2, 3, 1, 'DENTRO', NOW() - INTERVAL '4 hours', NOW() - INTERVAL '3 hours 55 minutes', NULL, 'Ingreso operativo de auditoría', FALSE);

-- 4. Visita APROBADA (Lista para check-in de Juan Camilo Rodríguez)
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (4, 11, 1, 2, 1, 'APROBADA', NOW() - INTERVAL '30 minutes', NULL, NULL, 'Visita técnica autorizada', FALSE);

-- 5. Visita PENDIENTE DE APROBACIÓN (Ana María Martínez solicitó en portería)
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (5, 12, 1, 2, 1, 'PENDIENTE_APROBACION', NOW() - INTERVAL '10 minutes', NULL, NULL, 'Solicitud en portería para entrevista laboral', FALSE);

-- 6. Visita CERRADA HOY (María Alejandra González)
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (6, 2, 1, 2, 1, 'CERRADA', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '5 hours 50 minutes', NOW() - INTERVAL '1 hour', 'Entrega de informes completada', FALSE);

-- ========== INCIDENTES REALISTAS INICIALES ==========
INSERT INTO incidente (id, titulo, descripcion, severidad, estado, persona_id, empresa_id, reportado_por_id, fecha_hora)
VALUES 
(1, 'Vehículo estacionado sin autorización en bahía de carga', 'Se detectó camión bloqueando la salida de emergencia de la bahía 2 sin conductor a la vista.', 'MEDIA', 'ABIERTO', NULL, 1, 1, NOW() - INTERVAL '1 hour 20 minutes'),
(2, 'Intento de ingreso con credencial no vigente', 'Se rechazó intento de acceso de visitante con carnet expirado en portería peatonal norte.', 'BAJA', 'RESUELTO', 20, NULL, 1, NOW() - INTERVAL '4 hours');

-- ========== BITÁCORA DE AUDITORÍA INICIAL ==========
INSERT INTO bitacora_auditoria (id, usuario_id, accion, entidad_afectada, entidad_id, detalle, fecha_hora, resultado)
VALUES
(1, 1, 'LOGIN_EXITOSO', 'Usuario', 1, 'Inicio de sesión exitoso desde estación de portería principal', NOW() - INTERVAL '4 hours', 'EXITOSO'),
(2, 2, 'LOGIN_EXITOSO', 'Usuario', 2, 'Inicio de sesión exitoso de funcionario anfitrión', NOW() - INTERVAL '3 hours 50 minutes', 'EXITOSO'),
(3, 1, 'CHECK_IN', 'Visita', 1, 'Ingreso registrado para Carlos Andrés Pérez Morales', NOW() - INTERVAL '2 hours 50 minutes', 'EXITOSO'),
(4, 1, 'CHECK_IN', 'Visita', 2, 'Ingreso registrado para Leonardo Hernández', NOW() - INTERVAL '1 hour 45 minutes', 'EXITOSO'),
(5, 1, 'REGISTRO_INCIDENTE', 'Incidente', 1, 'Reporte de vehículo en bahía de carga con severidad MEDIA', NOW() - INTERVAL '1 hour 20 minutes', 'EXITOSO');

-- Sincronizar secuencias para inserciones dinámicas de Hibernate
SELECT setval(pg_get_serial_sequence('empresa', 'id'), coalesce(max(id), 1)) FROM empresa;
SELECT setval(pg_get_serial_sequence('persona', 'id'), coalesce(max(id), 1)) FROM persona;
SELECT setval(pg_get_serial_sequence('usuario', 'id'), coalesce(max(id), 1)) FROM usuario;
SELECT setval(pg_get_serial_sequence('rol', 'id'), coalesce(max(id), 1)) FROM rol;
SELECT setval(pg_get_serial_sequence('permiso', 'id'), coalesce(max(id), 1)) FROM permiso;
SELECT setval(pg_get_serial_sequence('visita', 'id'), coalesce(max(id), 1)) FROM visita;
SELECT setval(pg_get_serial_sequence('bitacora_auditoria', 'id'), coalesce(max(id), 1)) FROM bitacora_auditoria;
SELECT setval(pg_get_serial_sequence('incidente', 'id'), coalesce(max(id), 1)) FROM incidente;