# DEFECTOS Y FALTANTES — SICA

> Análisis generado mediante roleplay de los 4 flujos de acceso contra el código
> fuente. Cada item referencia el archivo/línea donde se encontró el problema
> y el requerimiento de message.txt que lo origina.
>
> Fecha: 31 de agosto de 2026
> Rama: feature/real-time-refresh

---

## 🔴 CRÍTICOS (bloquean el cumplimiento del requerimiento)

### 1. Login NO se audita en la bitácora

**Archivos**: AuthService.java (ausencia de llamada)
**Requerimiento**: message.txt línea 53-54: "Intentos de login (exitosos y fallidos)"
**Descripción**: AuthService.login() nunca llama a auditoriaService.registrar(). No hay forma
de auditar quién intentó iniciar sesión ni si falló o tuvo éxito.
**Impacto**: Incumplimiento del requerimiento #2 (Módulo de Auditoría).
**Fix sugerido**: Agregar auditoría en AuthService.login() para EXITOSO y FALLIDO.

---

### 2. Persona BLOQUEADA puede hacer check-in normalmente

**Archivos**: VisitaService.checkIn() línea 46-63, GuardaController.handleCheckIn()
**Requerimiento**: message.txt línea 10: "No hay forma de marcar a una persona con una
restricción de acceso de manera inmediata y efectiva en todos los puntos de entrada"
**Descripción**: El método checkIn() no verifica persona.isBloqueado(). Una persona bloqueada
puede entrar al complejo sin restricciones. El guarda solo ve una tabla con nombres, sin
indicador visual de bloqueo.
**Impacto**: Vulnerabilidad de seguridad grave. El sistema no cumple su promesa de bloquear acceso.
**Fix sugerido**:
1. En checkIn(), lanzar excepción si visita.getPersona().isBloqueado().
2. Mostrar ícono/alerta visual en las tablas del Guarda cuando la persona esté bloqueada.
3. Auditar el intento de acceso bloqueado.

---

### 3. Bloqueo/desbloqueo de persona NO se audita

**Archivos**: PersonaService.bloquearPersona() línea 44, PersonaService.desbloquearPersona() línea 50
**Requerimiento**: message.txt línea 53-54: "Cambios de estado de acceso de una persona"
**Descripción**: bloquearPersona() y desbloquearPersona() modifican la BD pero no registran
en la bitácora. No hay forma de saber quién bloqueó/desbloqueó, cuándo ni por qué.
**Impacto**: Incumplimiento del requerimiento de auditoría.
**Fix sugerido**: Agregar auditoriaService.registrar() en ambos métodos, incluyendo el motivo.

---

## 🟠 ALTOS (rompen flujos funcionales)

### 4. Persona existente NO se audita al crearla/actualizarla desde la UI

**Archivos**: FuncionarioController.handleRegistrar() línea 221-294,
GuardaController.handleSolicitarAcceso() línea 246-310
**Requerimiento**: message.txt línea 55: "Creación, actualización o eliminación de cualquier
entidad principal (Personas)"
**Descripción**: Cuando se registra una persona existente, se actualiza su nombre/tipo y se
llama a personaService.guardar(), pero no se audita. Solo se audita la visita.
**Impacto**: Incumplimiento parcial de auditoría.

---

### 5. logout() no limpia el RefreshScheduler

**Archivos**: SceneManager.logout() línea 86, GuardaController.initialize() línea 120,
FuncionarioController.initialize()
**Descripción**: Al cerrar sesión, RefreshScheduler sigue con los controllers anteriores
registrados. El scheduler sigue ejecutando refreshData() en listeners que ya no son visibles.
**Impacto**: Memory leak + posibles excepciones al refrescar después del logout.
**Fix sugerido**: Llamar RefreshScheduler.getInstance().clear() en SceneManager.logout().

---

### 6. Tablas no indican visualmente si la persona está BLOQUEADA

**Archivos**: GuardaController (tblActivas, tblAprobadas, tblPendientes),
FuncionarioController (tblPendientes)
**Requerimiento**: message.txt línea 10: "marcar a una persona con una restricción de acceso
de manera inmediata y efectiva"
**Descripción**: Las tablas solo muestran nombre, empresa, hora y estado. No hay columna de
color rojo, icono de candado, o indicador de bloqueo. El guarda no puede saber a simple
vista si debe Denegar el ingreso.
**Fix sugerido**: Agregar TableCell con icono rojo cuando persona.isBloqueado().

---

### 7. ADMIN no puede bloquear/desbloquear personas desde su panel

**Archivos**: AdminController.java, admin.fxml
**Requerimiento**: ADMIN tiene el permiso bloquear_persona (data.sql línea 17) pero no hay UI.
**Impacto**: ADMIN no puede ejercer su rol de administrador del sistema.
**Fix sugerido**: Agregar tabla de personas con botón Bloquear/Desbloquear en AdminController.

---

## 🟡 MEDIOS

### 8. Incremento de visitas NO se audita

**Archivos**: PersonaService.incrementarVisitas() línea 31
**Fix sugerido**: Auditar en incrementarVisitas() o en los servicios que lo llaman.

---

### 9. AdminController no tiene botones de acción (CRUD de usuarios/empresas)

**Archivos**: AdminController.java, admin.fxml
**Descripción**: ADMIN no puede crear/editar/eliminar usuarios, roles ni empresas desde la UI.
**Impacto**: ADMIN es de solo lectura.

---

### 10. No existe módulo de "Gestión de Incidentes"

**Archivos**: Ninguno (no existe)
**Requerimiento**: message.txt líneas 10 y 57: "Gestión de Incidentes" y "Registro de un incidente"
**Descripción**: Es un entregable explícito mencionado dos veces en el documento. No existe nada.
**Impacto**: Requisito completamente faltante.

---

### 11. RBAC no se verifica en toggleBloqueo del FuncionarioController

**Archivos**: FuncionarioController.toggleBloqueo() línea 195
**Requerimiento**: message.txt línea 48: "Antes de ejecutar cualquier operación, el sistema
debe verificar si el rol del usuario tiene el permiso específico"
**Descripción**: toggleBloqueo() llama directamente a personaService sin verificar el permiso
bloquear_persona mediante rbacService.verificarPermiso().
**Fix sugerido**: Agregar rbacService.verificarPermiso(SceneManager.getCurrentUser(),
"bloquear_persona") al inicio de toggleBloqueo().

---

### 12. RBAC no se verifica en handleRegistrar del FuncionarioController

**Archivos**: FuncionarioController.handleRegistrar() línea 221
**Descripción**: El controller no verifica aprobar_visita antes de llamar al servicio.
(El servicio lo hace internamente, pero debería hacerse en el controller).

---

## 🔵 BAJOS

### 13. No hay paginación en tablas grandes
### 14. No hay búsqueda/filtro por nombre en tablas del Guarda
### 15. El Guarda necesita saber qué funcionario anfitrión hizo la solicitud
### 16. No hay validación de documento único por tipo de documento
### 17. No hay toasts/notificaciones de éxito/error persistentes

---

## 📊 RESUMEN DE AUDITORÍA FALTANTE

| Acción | Audita? |
|--------|---------|
| Login exitoso | ❌ NO |
| Login fallido | ❌ NO |
| Persona creada/actualizada | ❌ NO |
| Persona bloqueada/desbloqueada | ❌ NO |
| totalVisitas incrementado | ❌ NO |
| Visita pre-aprobada | ✅ SÍ |
| Solicitud de acceso | ✅ SÍ |
| Check-in | ✅ SÍ |
| Check-out | ✅ SÍ |
| Visita aprobada | ✅ SÍ |
| Visita rechazada | ✅ SÍ |
| Salida olvidada | ✅ SÍ |

Total de acciones auditadas: 6 de 12 (50%)

---

## ✅ FLUJOS QUE SÍ FUNCIONAN BIEN

1. Flujo 1 (Pre-registrado): ✅
2. Flujo 2 (No anunciado): ✅
3. Flujo 3 (Carnet olvidado): ✅ (usa flujo 2 con paseTemporal=true)
4. Flujo 4 (Salida olvidada): ✅ regularizarSiCorresponde() cierra y audita

---

## 🔧 ORDEN RECOMENDADA DE CORRECCIÓN

1. Inmediato: #2 (bloqueado puede entrar) + #3 (bloqueo sin auditar) + #1 (login sin auditar)
2. Corto plazo: #6 (UI no muestra bloqueo) + #7 (ADMIN sin panel de bloqueo) + #11 (RBAC en toggleBloqueo)
3. Mediano plazo: #4 (auditar persona CRUD) + #5 (logout limpia scheduler) + #10 (módulo de incidentes)
4. Largo plazo: #9 (CRUD admin) + #13 (paginación) + #15 (mostrar anfitrión en tablas del guarda)
