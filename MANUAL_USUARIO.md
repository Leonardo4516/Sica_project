# SICA — Manual de Usuario

> **Sistema Integrado de Control de Acceso**
> Aplicación de escritorio para el control de acceso físico de personas (visitantes, funcionarios, contratistas) a una organización.

---

## 1. Descripción general

SICA permite registrar la entrada y salida de cualquier persona que ingresa a las instalaciones. Gestiona tres tipos de usuarios con responsabilidades distintas y mantiene un historial auditable de todos los movimientos.

**Funciones principales:**
- Registro de visitas con foto del documento y motivo.
- Aprobación o rechazo de solicitudes por parte de funcionarios.
- Control de estado (dentro/fuera) en tiempo real.
- Bitácora de auditoría con exportación.
- Administración de usuarios, roles y empresas.

---

## 2. Roles del sistema

### GUARDA (operador en portería)

Opera el sistema desde la entrada. Es el primer punto de contacto con cualquier visitante.

**Puede:**
- Registrar el ingreso de una persona (Check-In).
- Registrar la salida (Check-Out).
- Solicitar aprobación de un visitante no pre-registrado.
- Ver el historial de visitas del día.

### FUNCIONARIO (empleado de la organización)

Autoriza o rechaza las solicitudes de acceso a las áreas donde trabaja.

**Puede:**
- Aprobar o rechazar solicitudes pendientes.
- Pre-registrar visitantes conocidos (con anticipación).
- Ver sus propias solicitudes gestionadas.

### ADMIN (administrador del sistema)

Gestiona la configuración global y supervisa la operación.

**Puede:**
- Ver estadísticas globales (visitas del día, dentro, pendientes, cerradas).
- Gestionar usuarios y asignar roles.
- Gestionar empresas visitantes (NIT, razón social).
- Consultar y exportar la bitácora de auditoría.

---

## 3. Inicio de sesión

Cada usuario accede con su nombre de usuario y contraseña. Las credenciales de prueba son:

| Usuario    | Contraseña | Rol         |
|------------|------------|-------------|
| `guarda1`    | `123456`     | GUARDA      |
| `funcionario1` | `123456`  | FUNCIONARIO |
| `admin1`     | `123456`     | ADMIN       |

La pantalla de login muestra el logo SICA y dos campos centrados con el botón "Ingresar".

---

## 4. Flujos de trabajo

### 4.1 Visita pre-registrada (la más común)

```
FUNCIONARIO pre-registra al visitante con nombre, documento, empresa y motivo
        ↓
Llega el visitante a portería
        ↓
GUARDA busca por documento y encuentra el pre-registro
        ↓
GUARDA hace Check-In → el visitante entra
        ↓
Al salir, GUARDA hace Check-Out
```

### 4.2 Visita no anunciada (sin pre-registro)

```
GUARDA no encuentra al visitante en el sistema
        ↓
Registra manualmente los datos (nombre, documento, empresa, motivo)
        ↓
Solicita aprobación al funcionario del área destino
        ↓
FUNCIONARIO aprueba o rechaza en su panel
        ↓
Si aprueba → GUARDA hace Check-In
        ↓
Al salir, GUARDA hace Check-Out
```

### 4.3 Salida olvidada (pase temporal)

Si al cierre del día una persona sigue "dentro" en el sistema, se le considera en estado irregular. El ADMIN puede cerrarla manualmente desde la bitácora.

---

## 5. Pantalla GUARDA paso a paso

1. **Ingresar como `guarda1`**
2. Verás tres tarjetas de estadísticas arriba: Total visitas hoy, Dentro ahora, Pendientes de aprobación.
3. **Formulario de Check-In** (panel izquierdo):
   - Cédula / Documento
   - Nombre (se autocompleta si existe)
   - Empresa (combo con búsqueda)
   - Motivo de visita
   - Persona a visitar (funcionario destino)
   - Botón "Registrar Ingreso"
4. **Solicitar acceso** si el visitante no está pre-registrado (botón amarillo).
5. **Tabla de visitas activas**: muestra todas las personas que están dentro ahora, con su hora de entrada.
6. **Tabla de solicitudes pendientes**: las que esperan aprobación del funcionario.
7. **Tabla de cerradas hoy**: las que ya salieron.

---

## 6. Pantalla FUNCIONARIO paso a paso

1. **Ingresar como `funcionario1`**
2. Verás un panel de "Solicitudes pendientes" y otro de "Mis pre-registros".
3. Para cada solicitud puedes:
   - **Aprobar** (botón verde) → el guarda podrá hacer Check-In.
   - **Rechazar** (botón rojo) → se cierra la solicitud.
4. Botón "Pre-registrar visitante" para agendar visitas futuras.

---

## 7. Pantalla ADMIN paso a paso

1. **Ingresar como `admin1`**
2. Verás 4 tarjetas de estadísticas con números en grande:
   - **TOTAL VISITAS** (azul)
   - **DENTRO** (verde) — personas actualmente en las instalaciones
   - **PENDIENTES** (amarillo) — solicitudes por aprobar
   - **CERRADAS HOY** (azul claro) — visitas que ya terminaron
3. **Pestaña Usuarios**: lista de todos los usuarios del sistema, con su rol y persona asignada.
4. **Pestaña Roles y Permisos**: muestra los roles definidos y sus permisos.
5. **Pestaña Empresas**: lista de empresas visitantes (NIT y razón social).
6. **Botón Refrescar** recarga todos los datos.

---

## 8. Bitácora de auditoría

Accesible desde el menú lateral en cualquier pantalla. Registra cada acción del sistema con:
- Fecha y hora
- Usuario que ejecutó la acción
- Tipo de evento (login, check-in, aprobación, etc.)
- Detalle del cambio

Se puede exportar a CSV con el botón "Exportar CSV".

---

## 9. Arquitectura técnica (para exposición)

### Stack
- **Java 17**
- **JavaFX 17** — interfaz gráfica
- **JPA / Hibernate 6.4** — persistencia
- **PostgreSQL 18** — base de datos
- **Maven** — build

### Patrón arquitectónico: **Hexagonal (Ports & Adapters)**

El código se organiza en tres capas concéntricas:

```
┌─────────────────────────────────────────┐
│  INFRAESTRUCTURA (adapters in/out)      │
│  - JPA repositories, JavaFX controllers │
├─────────────────────────────────────────┤
│  APLICACIÓN (use cases)                 │
│  - LoginUseCase, RegistrarIngresoUseCase│
├─────────────────────────────────────────┤
│  DOMINIO (modelo + ports out)           │
│  - Visita, Persona, Empresa, Usuario    │
│  - VisitaRepository (interfaz)          │
└─────────────────────────────────────────┘
```

**Reglas:**
- El dominio no conoce JPA, ni JavaFX, ni PostgreSQL.
- Las dependencias apuntan hacia adentro (Dependency Inversion).
- Cada "slice" vertical (iam, personas, empresas, acceso, auditoria) tiene su propia carpeta con la misma estructura.

### Módulos verticales
- **iam** — autenticación y usuarios
- **personas** — registro de personas (funcionarios, visitantes, guardias)
- **empresas** — empresas visitantes
- **acceso** — visitas, check-in/out, aprobaciones
- **auditoria** — bitácora de eventos

---

## 10. Demo sugerida (5 minutos)

1. **Login como `admin1`** — mostrar el dashboard con 4 estadísticas (azul, verde, amarillo, azul claro).
2. Pestaña **Usuarios** → explicar que ve los 3 usuarios pre-cargados.
3. Pestaña **Empresas** → mostrar las 3 empresas visitantes.
4. **Cerrar sesión**, volver a login.
5. **Login como `funcionario1`** → mostrar panel de solicitudes (vacío).
6. **Cerrar sesión**.
7. **Login como `guarda1`** → registrar un Check-In con documento "9999999", nombre "Visitante Demo", empresa "Constructora Andina".
8. Mostrar que la visita aparece en "Visitas activas".
9. Hacer Check-Out → pasa a "Cerradas hoy".
10. **Cerrar sesión**, **login como `admin1`** → las estadísticas se actualizaron.
11. Ir a **Bitácora** → mostrar todos los eventos de la sesión.

---

## 11. Reglas de negocio clave

1. Una persona **no puede tener dos visitas abiertas** simultáneamente.
2. Un visitante **no puede entrar sin que la visita esté APROBADA** o pre-registrada.
3. El Check-Out **requiere que la visita esté DENTRO** (no aprobada pendiente).
4. La contraseña se almacena **hasheada con BCrypt**.
5. Toda acción relevante **genera un evento en la bitácora**.

---

## 12. Atajos y tips

- **Tab** navega entre campos del formulario.
- **Enter** en el campo de documento dispara la búsqueda.
- **Esc** cierra los popups de selección.
- Los **badges de colores** indican el estado:
  - Verde = activo / aprobado
  - Amarillo = pendiente
  - Rojo = rechazado / cerrado
  - Azul = información

---

## 13. Soporte y contacto

Para reportar problemas o sugerir mejoras, contactar al equipo de desarrollo.
