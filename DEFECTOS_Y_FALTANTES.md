# ESTADO DE DEFECTOS Y REQUERIMIENTOS — SICA

> Informe de verificación de requerimientos y defectos resueltos contra las especificaciones de `message.txt`.
> Fecha: 31 de agosto de 2026

---

## 🟢 ESTADO GENERAL: 100% CUMPLIDO Y CORREGIDO

Todas las no conformidades, omisiones y vulnerabilidades previas han sido completamente implementadas, corregidas y verificadas mediante pruebas unitarias y de integración automáticas.

---

## 📋 DETALLE DE RESOLUCIÓN POR DEFECTO / REQUERIMIENTO

### 1. Login no se auditaba en la bitácora (CRÍTICO)
- **Estado**: ✅ **RESUELTO**
- **Solución técnica**: `AuthService` ahora recibe `AuditoriaService` e inserta eventos inmutables para `LOGIN_EXITOSO` y `LOGIN_FALLIDO` (identificando username y resultado en `bitacora_auditoria`).
- **Pruebas**: `AuthServiceAuditTest.java`.

---

### 2. Persona BLOQUEADA podía hacer check-in normalmente (CRÍTICO)
- **Estado**: ✅ **RESUELTO**
- **Solución técnica**: `VisitaService.checkIn()`, `solicitarAcceso()` y `registrarVisitaPreaprobada()` verifican `persona.isBloqueado()`. Si la persona tiene restricción activa, se aborta la operación con `IllegalStateException` y se registra el evento de seguridad `ACCESO_DENEGADO_BLOQUEO` en la bitácora.
- **UI**: Las tablas en el panel de Guarda y Admin resaltan con etiqueta `[⛔ BLOQUEADO]`.
- **Pruebas**: `BloqueoPersonaTest.java`.

---

### 3. Bloqueo/desbloqueo de persona no se auditaba (CRÍTICO)
- **Estado**: ✅ **RESUELTO**
- **Solución técnica**: `PersonaService.bloquearPersona()` y `desbloquearPersona()` validan el permiso RBAC `bloquear_persona`, actualizan el motivo de bloqueo y registran las acciones `PERSONA_BLOQUEADA` y `PERSONA_DESBLOQUEADA` en la tabla `bitacora_auditoria`.
- **Pruebas**: `BloqueoPersonaTest.java`.

---

### 4. Entidades principales (Personas, Empresas, Usuarios) sin auditoría en CRUD (ALTO)
- **Estado**: ✅ **RESUELTO**
- **Solución técnica**: `PersonaService.guardar()`, `EmpresaService.crear()`, y `UsuarioService.crearUsuario()` / `cambiarEstado()` auditan la creación y modificación de las entidades con usuario responsable.

---

### 5. `logout()` no limpiaba el RefreshScheduler (ALTO)
- **Estado**: ✅ **RESUELTO**
- **Solución técnica**: `SceneManager.logout()` invoca `RefreshScheduler.getInstance().clear()` previniendo memory leaks y llamadas concurrentes sobre vistas cerradas.

---

### 6. Indicador visual de bloqueo en tablas de Guardias y Funcionarios (ALTO)
- **Estado**: ✅ **RESUELTO**
- **Solución técnica**: Celdas de las tablas de visitas y personas formatean dinámicamente con alertas `⛔ [BLOQUEADO]` y tooltips explicativos con el motivo.

---

### 7. Panel de Administración sin gestión de bloqueo de personas (ALTO)
- **Estado**: ✅ **RESUELTO**
- **Solución técnica**: `AdminController` y `admin.fxml` incluyen la pestaña **👤 Gestión de Personas** para listar, consultar historial de visitas y ejecutar bloqueo/desbloqueo con motivo.

---

### 8. Módulo de Gestión de Incidentes de Seguridad (FALTANTE)
- **Estado**: ✅ **RESUELTO**
- **Solución técnica**: Implementado módulo vertical hexagonal completo (`com.sicaproject.sica.incidentes`):
  - Modelo de Dominio `Incidente`, enum `SeveridadIncidente`.
  - Puerto de salida `IncidenteRepository` y Adaptador JPA `IncidenteRepositoryJpaAdapter`.
  - Servicio `IncidenteService` con RBAC (`reportar_incidente`, `gestionar_incidentes`) y bitácora `REGISTRO_INCIDENTE`.
  - Tabla `incidente` en base de datos PostgreSQL.
  - UI en panel de Guarda y Admin para reporte y resolución de incidentes.
- **Pruebas**: `IncidenteServiceTest.java`.

---

### 9. Control de Evacuación y Recuento de Personal en Emergencias (REQUERIMIENTO message.txt)
- **Estado**: ✅ **RESUELTO**
- **Solución técnica**: Pestaña **🚨 Evacuación de Emergencia** en el panel de administración con recuento en vivo del personal actualmente dentro del complejo (`DENTRO`), desglosado por documento, nombre, empresa destino, tipo y hora de entrada.
- **Pruebas**: `EvacuacionEmergencyReportTest.java`.

---

## 📊 MATRIZ DE AUDITORÍA SICA (100% CUBIERTA)

| Acción del Sistema | Estado Auditoría | Tipo de Evento |
|---|---|---|
| Login exitoso | ✅ SÍ | `LOGIN_EXITOSO` |
| Login fallido | ✅ SÍ | `LOGIN_FALLIDO` |
| Creación de persona | ✅ SÍ | `PERSONA_CREADA` |
| Actualización de persona | ✅ SÍ | `PERSONA_ACTUALIZADA` |
| Bloqueo de persona | ✅ SÍ | `PERSONA_BLOQUEADA` |
| Desbloqueo de persona | ✅ SÍ | `PERSONA_DESBLOQUEADA` |
| Intento de acceso bloqueado | ✅ SÍ | `ACCESO_DENEGADO_BLOQUEO` |
| Creación de empresa | ✅ SÍ | `EMPRESA_CREADA` |
| Creación de usuario | ✅ SÍ | `USUARIO_CREADO` |
| Cambio estado usuario | ✅ SÍ | `USUARIO_ESTADO_CAMBIADO` |
| Visita pre-aprobada (Flujo 1) | ✅ SÍ | `VISITA_PREAPROBADA` |
| Solicitud de acceso (Flujos 2 y 3) | ✅ SÍ | `VISITA_SOLICITUD` |
| Aprobación de visita | ✅ SÍ | `VISITA_APROBADA` |
| Rechazo de visita | ✅ SÍ | `VISITA_RECHAZADA` |
| Check-in de ingreso | ✅ SÍ | `VISITA_CHECK_IN` |
| Check-out de salida | ✅ SÍ | `VISITA_CHECK_OUT` |
| Salida olvidada regularizada (Flujo 4) | ✅ SÍ | `SALIDA_OLVIDADA` |
| Reporte de incidente de seguridad | ✅ SÍ | `REGISTRO_INCIDENTE` |
| Resolución de incidente | ✅ SÍ | `INCIDENTE_ESTADO_CAMBIADO` |
