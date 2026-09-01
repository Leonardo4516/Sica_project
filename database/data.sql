-- ==============================================================================
-- SICA - Sistema Integrado de Control de Acceso
-- Dataset de Inicialización Limpio y Realista
-- Complejo Empresarial Zona Acme (Bogotá / Medellín, Colombia)
-- ==============================================================================

-- Limpieza preventiva
TRUNCATE TABLE bitacora_auditoria, incidente, visita, usuario, persona, empresa, rol_permiso, permiso, rol CASCADE;

-- ========== ROLES DEL SISTEMA ==========
INSERT INTO rol (id, nombre) VALUES
(1, 'GUARDA'),
(2, 'FUNCIONARIO'),
(3, 'ADMIN');

-- ========== PERMISOS RBAC ==========
INSERT INTO permiso (id, codigo, descripcion) VALUES
(1, 'ver_panel_guarda', 'Acceso a la consola operativa de portería'),
(2, 'ver_panel_funcionario', 'Acceso a la consola de aprobación y gestión de visitas'),
(3, 'ver_panel_admin', 'Acceso completo a la consola de administración del sistema'),
(4, 'registrar_visita', 'Registrar ingreso físico de visitantes y trabajadores'),
(5, 'registrar_salida', 'Registrar salida física de personas del complejo'),
(6, 'aprobar_visita', 'Aprobar solicitudes de ingreso pre-registradas'),
(7, 'rechazar_visita', 'Rechazar solicitudes de ingreso'),
(8, 'gestionar_usuarios', 'Crear, editar, activar y desactivar cuentas de usuario'),
(9, 'gestionar_empresas', 'Crear y administrar empresas residentes del complejo'),
(10, 'reportar_incidente', 'Registrar novedades y anomalías de seguridad'),
(11, 'gestionar_incidentes', 'Actualizar estado y resolución de incidentes'),
(12, 'consultar_auditoria', 'Ver bitácora inmutable de eventos del sistema'),
(13, 'bloquear_persona', 'Bloquear o desbloquear personas en lista negra');

-- ========== ASIGNACIÓN DE PERMISOS A ROLES ==========
-- GUARDA
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
(1, 1), (1, 4), (1, 5), (1, 10), (1, 13);

-- FUNCIONARIO
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
(2, 2), (2, 6), (2, 7), (2, 10), (2, 13);

-- ADMIN (Todos los permisos)
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 12), (3, 13);

-- ========== EMPRESAS RESIDENTES REALES ==========
INSERT INTO empresa (id, nombre, nit) VALUES
(1, 'Bavaria & Cía S.C.A.', '860005224-6'),
(2, 'Grupo Nutresa S.A.', '890900050-1'),
(3, 'Grupo Argos S.A.', '890900266-3'),
(4, 'Postobón S.A.', '890903939-5');

-- ========== PERSONAS REALISTAS (10 Personas: 5 con Foto, 5 sin Foto) ==========
-- 1. Carlos Andrés Pérez Morales (Trabajador Bavaria - Con Foto)
-- 2. María Alejandra González Ruiz (Trabajador Bavaria - Con Foto)
-- 3. Andrés Felipe López Zapata (Trabajador Grupo Argos - Con Foto)
-- 4. Laura Sofía Torres Castro (Trabajador Grupo Nutresa - Con Foto)
-- 5. Leonardo Hernández (Invitado - Con Foto)
-- 6. Valentina Mendoza Herrera (Trabajador Grupo Argos - Sin Foto)
-- 7. Javier Eduardo Silva Pineda (Trabajador Postobón - Sin Foto)
-- 8. Juan Camilo Rodríguez Vélez (Invitado - Sin Foto)
-- 9. Ana María Martínez Duarte (Invitado - Sin Foto)
-- 10. Héctor Fabio Ramírez Arias (Invitado Bloqueado - Sin Foto)

INSERT INTO persona (id, tipo, tipo_documento, nombre, documento, foto_url, empresa_id, bloqueado, motivo_bloqueo, total_visitas) VALUES
(1, 'TRABAJADOR', 'CC', 'Carlos Andrés Pérez Morales', '1020304050', 'photos/carlos_perez.jpg', 1, FALSE, NULL, 15),
(2, 'TRABAJADOR', 'CC', 'María Alejandra González Ruiz', '1020456789', 'photos/maria_gonzalez.jpg', 1, FALSE, NULL, 22),
(3, 'TRABAJADOR', 'CC', 'Andrés Felipe López Zapata', '1014567890', 'photos/andres_lopez.jpg', 3, FALSE, NULL, 30),
(4, 'TRABAJADOR', 'CC', 'Laura Sofía Torres Castro', '1035678901', 'photos/laura_torres.jpg', 2, FALSE, NULL, 12),
(5, 'INVITADO',   'CC', 'Leonardo Hernández',           '1100955643', 'photos/leonardo_hernandez.jpg', NULL, FALSE, NULL, 6),
(6, 'TRABAJADOR', 'CC', 'Valentina Mendoza Herrera',     '1019876543', NULL, 3, FALSE, NULL, 14),
(7, 'TRABAJADOR', 'CC', 'Javier Eduardo Silva Pineda',   '1015678234', NULL, 4, FALSE, NULL, 9),
(8, 'INVITADO',   'CC', 'Juan Camilo Rodríguez Vélez',   '1018234567', NULL, NULL, FALSE, NULL, 4),
(9, 'INVITADO',   'CC', 'Ana María Martínez Duarte',     '1025678912', NULL, NULL, FALSE, NULL, 3),
(10, 'INVITADO',  'CC', 'Héctor Fabio Ramírez Arias',    '1016789234', NULL, NULL, TRUE, 'Incumplimiento reiterado de normas de seguridad industrial en planta', 7);

-- ========== USUARIOS DEL SISTEMA ==========
-- Contraseña estándar para todos: "123456" (BCrypt)
-- Hash: $2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6

INSERT INTO usuario (id, username, password_hash, activo, persona_id, rol_id) VALUES
(1, 'guarda1',      '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, NULL, 1),
(2, 'funcionario1', '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, 1,    2),
(3, 'funcionario2', '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, 4,    2),
(4, 'funcionario3', '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, 3,    2),
(5, 'admin1',       '$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6', TRUE, NULL, 3);

-- ========== VISITAS INICIALES REALISTAS ==========
-- 1. Carlos Andrés Pérez dentro de Bavaria (Trabajador con foto)
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (1, 1, 1, 2, 1, 'DENTRO', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '2 hours 50 minutes', NULL, 'Ingreso a jornada laboral matutina', FALSE);

-- 2. Leonardo Hernández dentro de Bavaria (Invitado con foto)
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (2, 5, 1, 2, 1, 'DENTRO', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour 45 minutes', NULL, 'Reunión de coordinación de proyectos', FALSE);

-- 3. Laura Sofía Torres dentro de Nutresa (Trabajador con foto)
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (3, 4, 2, 3, 1, 'DENTRO', NOW() - INTERVAL '4 hours', NOW() - INTERVAL '3 hours 55 minutes', NULL, 'Ingreso operativo de auditoría', FALSE);

-- 4. Juan Camilo Rodríguez - Visita APROBADA (Lista para Check-In)
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (4, 8, 1, 2, 1, 'APROBADA', NOW() - INTERVAL '30 minutes', NULL, NULL, 'Visita técnica autorizada', FALSE);

-- 5. Ana María Martínez - Visita PENDIENTE (Solicitada en portería)
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (5, 9, 1, 2, 1, 'PENDIENTE_APROBACION', NOW() - INTERVAL '10 minutes', NULL, NULL, 'Solicitud en portería para entrevista laboral', FALSE);

-- 6. María Alejandra González - Visita CERRADA hoy
INSERT INTO visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id, estado, fecha_hora_registro, fecha_hora_entrada, fecha_hora_salida, observaciones, es_pase_temporal)
VALUES (6, 2, 1, 2, 1, 'CERRADA', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '5 hours 50 minutes', NOW() - INTERVAL '1 hour', 'Entrega de informes completada', FALSE);

-- ========== INCIDENTES REALISTAS ==========
INSERT INTO incidente (id, titulo, descripcion, severidad, estado, persona_id, empresa_id, reportado_por_id, fecha_hora)
VALUES 
(1, 'Vehículo estacionado sin autorización en bahía de carga', 'Se detectó camión bloqueando la salida de emergencia de la bahía 2 sin conductor a la vista.', 'MEDIA', 'ABIERTO', NULL, 1, 1, NOW() - INTERVAL '1 hour 20 minutes'),
(2, 'Intento de ingreso con credencial no vigente', 'Se rechazó intento de acceso de visitante con carnet expirado en portería peatonal norte.', 'BAJA', 'RESUELTO', 10, NULL, 1, NOW() - INTERVAL '4 hours');

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
