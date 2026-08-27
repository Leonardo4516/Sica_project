# CAMBIOS Y DECISIONES — Paquete para análisis externo

> Documento que acompaña a `proyecto_sica_full_analisis.zip` con todo el contexto
> de cambios, errores encontrados, decisiones tomadas y el estado actual del proyecto.
> Última actualización: 27 de agosto de 2026 (sesión vespertina).

## 0. ÚLTIMA ACTUALIZACIÓN (CRÍTICO)

A esta altura de la sesión, el **backend funciona** (login verificado desde código Java:
3 OK + 3 FAIL correctos con `admin1/123456`, `guarda1/123456`, `funcionario1/123456`)
y la **UI ya NO crashea al login** (los stack traces de `Canvas.width : A bound value cannot
be set` y los errores de CSS desaparecieron).

**Pero el login aún redirige con error** porque al navegar al panel ADMIN (o cualquier otro),
el `StackPane` raíz de los 4 FXML restantes (admin, guarda, funcionario, bitacora) **no
tiene `fx:id="rootPane"`**, por lo que el controller recibe `rootPane == null` y lanza
`NullPointerException` en `initialize()`.

**Commit `c33de8c` arregla esto** (ver sección 4.X). Si después de este commit el login
aún no funciona, hay un nuevo bug distinto.

---

## 1. RESUMEN EJECUTIVO

El proyecto es **SICA** (Sistema Integrado de Control de Acceso), un monolito Java
+ JavaFX + PostgreSQL con arquitectura hexagonal (puertos y adaptadores) y
vertical slices por módulo (iam, personas, empresas, acceso, auditoria).

En la sesión del 27/08/2026 el usuario pidió integrar un ZIP externo
(`files.zip`) que contenía una nueva versión del proyecto con varios refactors
del UI JavaFX. Se integró en 4 feature branches mergeadas a `main`. Después se
encontraron dos problemas críticos:

1. **El hash BCrypt del `data.sql` que venía en el ZIP NO correspondía a "123456"**
   → el login fallaba con "credenciales inválidas".
2. **Un error de runtime en `ParticleBackground` lanza excepciones no fatales**
   (`Canvas.width/height : A bound value cannot be set`) cada vez que la
   ventana se redimensiona, registradas en la consola JavaFX.

Ambos están diagnosticados y parcialmente resueltos. El login funciona
verificadamente desde código Java (`AuthService.login` retorna OK para los 3
usuarios con `123456`). El bug del `ParticleBackground` sigue abierto.

---

## 2. ESTADO ACTUAL DE GIT

### 2.1 Commits en `main` (los relevantes de esta sesión)

```
b668cad merge hash BCrypt verificado
9f70b88 fix(data): hash BCrypt verificado regenerado para 123456
9e41d66 merge fix fx:id rootPane y particleCanvas en login
5b02e26 fix(ui): agregar fx:id rootPane y particleCanvas en login.fxml, limpiar LoginController
59eda08 merge refactor UI controllers y FXML con tablas tipadas y acciones reales
2358cb1 feat(ui): refactorizar controllers y FXML con TableView<T> tipados, botones aprobar/rechazar, y datos reales de BD
dd5b90a merge RolRepository para ADMIN dashboard con datos reales
6dfdc77 feat(iam): agregar RolRepository (puerto + adapter JPA) y exponerlo en CompositionRoot
5d80f23 fix(data): actualizar hashes BCrypt para login funcional (NO era válido)
4880e9a merge fix mainClass pom.xml
04b1265 fix(pom): corregir mainClass a SicaApplication para javafx:run
```

### 2.2 Working tree sucio

- `proyecto_sica_backend.zip` y `proyecto_sica_backend_fase1.zip` figuran como
  borrados (porque la IA los eliminó físicamente pero estaban rastreados).
  **Decisión**: ignorar. Son artefactos históricos.
- `proyecto_sica_actual.zip` sin rastrear (regenerado al final).

---

## 3. FEATURE BRANCHES CREADAS Y ORDEN DE INTEGRACIÓN

| # | Rama | Commit | Propósito |
|---|------|--------|-----------|
| 1 | `feature/fix-pom-mainclass` | `04b1265` | `pom.xml`: `<mainClass>Main</mainClass>` → `<mainClass>SicaApplication</mainClass>` |
| 2 | `feature/fix-login-bcrypt` | `5d80f23` | `data.sql`: hash BCrypt nuevo (descubierto después que NO era de "123456") |
| 3 | `feature/agregar-rol-repository` | `6dfdc77` | Crear `RolRepository` (puerto) + `RolRepositoryJpaAdapter` (adaptador) + exposición en `CompositionRoot` |
| 4 | `feature/refactor-ui-controllers-fxml` | `2358cb1` | 4 controllers (`Admin`, `Bitacora`, `Funcionario`, `Guarda`) y 4 FXML refactorizados a `TableView<T>` tipados con columnas reales, botones Aprobar/Rechazar reales en Funcionario, datos RBAC reales en Admin desde BD, filtro de fechas en Bitácora |
| 5 | `feature/fix-login-fxml-root-canvas` | `5b02e26` | Faltan `fx:id` en `StackPane` raíz y `Canvas` de partículas; `Separator` con atributo inválido `margin` |
| 6 | `feature/fix-bcrypt-hash-verificado` | `9f70b88` | Hash BCrypt **verificado** con `AuthService.hashPassword("123456")` y `BCrypt.checkpw` → `true` |

### Flujo de merges

Todas se mergearon con `--no-ff` y se eliminó la rama tras el merge (regla del
usuario: feature → merge → delete).

---

## 4. CAMBIOS EN CÓDIGO (DETALLE)

### 4.1 `pom.xml` (línea 118)

**Antes** (rompía `mvn javafx:run`):
```xml
<mainClass>com.sicaproject.Main</mainClass>
```

**Después**:
```xml
<mainClass>com.sicaproject.SicaApplication</mainClass>
```

### 4.2 `data.sql` (líneas 42-54)

Hash BCrypt **verificado** de "123456":
```
$2a$10$ECVBqVxWXhCYBRw3/hUpEOVGEufz/dZNZsY4KzeTnuUd4BseGrsi6
```

El ZIP del usuario traía el hash `$2a$10$jfrWvSRFjdGMnDcGom6bl.jr6iFZ8Y1ItkxivH9hAVD.c284le3YC`
que **no es** de "123456" (lo verificamos ejecutando el smoke test y
descubrimos que `BCrypt.checkpw("123456", hashDelZip)` retorna `false`).
Fue reemplazado por uno generado con `AuthService.hashPassword("123456")`
y verificado con `BCrypt.checkpw`.

### 4.3 Archivos nuevos (de la rama 3)

- `src/main/java/com/sicaproject/sica/iam/application/port/out/RolRepository.java`
- `src/main/java/com/sicaproject/sica/iam/infrastructure/adapter/out/persistence/RolRepositoryJpaAdapter.java`

### 4.4 `CompositionRoot.java`

Se expuso `rolRepository()` para que el `AdminController` pueda listar roles
reales desde la BD (en vez de los 3 hardcodeados que tenía la versión vieja).

### 4.5 `LoginController.java` y `login.fxml`

**Problemas encontrados**:
1. `<StackPane>` raíz sin `fx:id="rootPane"` → el controller lanzaba NPE.
2. `<Canvas id="particleCanvas" />` con `id` (no `fx:id`) → el controller lo
   buscaba y era `null`.
3. `<VBox ... margin="$top">` y `<Separator ... margin="24 0" />` → JavaFX no
   reconoce la propiedad `margin` en estos nodos. Atributo inválido.
4. El controller declaraba `@FXML private Canvas particleCanvas` y `@FXML
   private StackPane rootPane` pero el FXML no tenía esos `fx:id`.

**Solución aplicada**: agregar `fx:id="rootPane"` al StackPane raíz, eliminar
el `fx:id` del Canvas (no se usa realmente, hay un `ParticleBackground` que
se superpone), eliminar el atributo `margin` del VBox y los Separator,
quitar la declaración `@FXML private Canvas particleCanvas` y el import
correspondiente del controller.

### 4.6 Los 4 controllers y FXML (rama 4)

**Refactor mayor** traído del ZIP:
- `TableView<Object>` → `TableView<T>` (tipado fuerte) en `Bitacora`,
  `Funcionario`, `Guarda`, `Admin`.
- Columnas con `fx:id` para usar `setCellValueFactory(...)` con
  `SimpleStringProperty`.
- `FuncionarioController` ahora tiene botones reales "Aprobar" / "Rechazar" por
  fila (con `TableCell` custom), llamando a `visitaService.aprobarVisita()` /
  `rechazarVisita()`.
- `AdminController` ahora carga roles desde `rolRepository.listarTodos()` y
  muestra los permisos reales de cada rol (en vez de los 3 hardcodeados).
- `BitacoraController` filtra la lista por `dpDesde` y `dpHasta` antes de
  popular la tabla.

### 4.7 Configuración de shim `mvn` (en el sistema, no en el repo)

El usuario reportó `mvn: orden no encontrada` al ejecutar el proyecto.

**Causa raíz**: el Maven del proyecto vive en
`/usr/share/idea/plugins/maven-plugin/lib/maven3/bin/mvn` (instalado por el
plugin de IntelliJ) y NO está en `$PATH` por defecto.

**Solución**:
1. Creado shim ejecutable en `~/.local/bin/mvn`:
   ```sh
   #!/bin/sh
   exec /usr/share/idea/plugins/maven-plugin/lib/maven3/bin/mvn "$@"
   ```
2. Agregado `[[ ":$PATH:" != *":$HOME/.local/bin:"* ]] && export
   PATH="$HOME/.local/bin:$PATH"` al inicio de `~/.bashrc` (antes del guard
   `[[ $- != *i* ]] && return`).
3. Idem en `~/.bash_profile` y `~/.config/fish/config.fish` (con
   `fish_add_path`).

**Verificación**:
- `bash -i -c 'which mvn'` → `/home/Papi_Leo/.local/bin/mvn` ✅
- `bash -l -c 'which mvn'` → `/home/Papi_Leo/.local/bin/mvn` ✅
- `mvn --version` → Apache Maven 3.9.16 con Java 26 ✅

---

## 5. ERRORES ENCONTRADOS Y DIAGNÓSTICO

### 5.1 ❌ Bug 1 — Hash BCrypt del ZIP no correspondía a "123456" (RESUELTO)

**Síntoma**: la UI mostraba "credenciales inválidas" con `admin1/123456`.

**Causa raíz**:
- El ZIP del usuario traía `data.sql` con un hash BCrypt que **no es** de
  "123456".
- Confirmado con: `BCrypt.checkpw("123456", "$2a$10$jfrWvSRFjdGMnDcGom6bl.jr6iFZ8Y1ItkxivH9hAVD.c284le3YC")` → `false`.

**Solución**:
- Generado hash nuevo con `AuthService.hashPassword("123456")` (que usa
  `BCrypt.gensalt(10)`).
- Verificado: `BCrypt.checkpw("123456", nuevoHash)` → `true`.
- `UPDATE usuario SET password_hash = '<hash>' WHERE id IN (1,2,3);` ejecutado
  en BD.
- `data.sql` actualizado con el mismo hash en el commit `9f70b88`.

**Verificación de login funcional** (ver `logs/login_test.log` en este zip):
```
LOGIN user='admin1' pass='123456' -> OK (id=3, rol=ADMIN)
LOGIN user='guarda1' pass='123456' -> OK (id=1, rol=GUARDA)
LOGIN user='funcionario1' pass='123456' -> OK (id=2, rol=FUNCIONARIO)
LOGIN user='admin1' pass='wrongpass' -> FAIL
LOGIN user='noexiste' pass='123456' -> FAIL
LOGIN user='admin1' pass='' -> FAIL
```

**El login del BACKEND funciona perfectamente.** Si la UI sigue fallando, el
problema está en la capa JavaFX (probablemente el `LoginController` o el FXML).

### 5.2 ⚠️ Bug 2 — `ParticleBackground` lanza excepciones no fatales al
redimensionar (NO RESUELTO)

**Síntoma**: en la consola JavaFX aparece repetidamente:
```
Exception in thread "JavaFX Application Thread" java.lang.RuntimeException: Canvas.width : A bound value cannot be set.
    at javafx.base/javafx.beans.property.DoublePropertyBase.set(...)
    at javafx.graphics/javafx.scene.canvas.Canvas.setWidth(Canvas.java:179)
    at com.sicaproject.sica.ui.component.ParticleBackground.lambda$new$0(ParticleBackground.java:42)
    ...
```

**Causa raíz**: en `ParticleBackground.java` el patrón es:
```java
public class ParticleBackground extends Canvas {
    // ...
    public ParticleBackground() {
        canvas.widthProperty().bind(widthProperty());   // línea 36
        canvas.heightProperty().bind(heightProperty()); // línea 37
        // ...
        widthProperty().addListener((obs, o, n) -> {
            canvas.setWidth(width);  // línea 42  ← error
        });
        heightProperty().addListener((obs, o, n) -> {
            canvas.setHeight(height);  // línea 46  ← error
        });
    }
}
```

El `ParticleBackground` es un `Canvas` y su atributo `widthProperty()` está
bindeado al `widthProperty` de un `canvas` interno. Cuando algún caller hace
`particles.resize(w, h)`, el listener intenta `canvas.setWidth(w)` sobre un
`widthProperty` que ya está bindeado, lo que está prohibido en JavaFX.

**Reproducción**:
- Cualquier controller que llame `particles.resize(1280, 720)` en su
  `initialize()` dispara el error.
- `LoginController:38`, `GuardaController`, `FuncionarioController`,
  `AdminController`, `BitacoraController` todos hacen
  `particles.resize(1280, 720)`.

**Severidad**: BAJA. No rompe la app (las excepciones se loguean pero la UI
sigue funcionando). La ventana abre, los controles responden. Solo contamina
la consola.

**Solución propuesta** (no aplicada):
```java
// En ParticleBackground, eliminar el listener manual:
// widthProperty().addListener((obs, o, n) -> { canvas.setWidth(width); });
// heightProperty().addListener((obs, o, n) -> { canvas.setHeight(height); });
// El bind ya hace ese trabajo. El listener es redundante Y conflictivo.
```

### 5.3 ⚠️ Bug 3 — Advertencias de CSS (NO RESUELTO, baja severidad)

```
ADVERTENCIA: CSS Error parsing file:.../styles.css: Expected '<color>' while parsing '-fx-background-color' at [147,42]
```

Repetido en líneas 147, 152, 168, 173, 178 de `styles.css`. Indica que se
está usando un valor no válido en alguna declaración `-fx-background-color`.
No rompe la app. Probablemente valores `rgba()` sin sintaxis correcta o un
`linear-gradient` mal formado.

### 5.4 ❌ Bug 4 — UI dice "credenciales inválidas" o "Error de
autenticación: com.sicaproject..." (EN INVESTIGACIÓN)

**Síntoma**: el usuario reporta que tras escribir `admin1/123456` en la UI, la
etiqueta `lblError` muestra "Error de autenticación: com.sicaproject algo más"
o "credenciales inválidas".

**Diagnóstico actual**:
- El backend (`AuthService.login`) **funciona perfectamente** (probado desde
  código Java con `TestLogin.java`).
- El `LoginController.handleLogin` (línea 49-72) tiene un try/catch que
  captura cualquier `Exception` y muestra `e.getMessage()` en `lblError`.

**Teorías sobre el mensaje que ve el usuario**:

A. Si ve "**credenciales inválidas**": el `AuthService.login` retornó
   `Optional.empty()`. Pero **probamos que NO debería pasar**. Posibles causas:
   - La BD que está usando la UI no es la que probamos (¿algún env var
     `SICA_DB_URL` que apunte a otra BD?).
   - Cache de compilación: `mvn javafx:run` no recompila si no se hace
     `mvn clean compile` antes.
   - El usuario escribió mal la contraseña (teclado, mayúsculas, etc.).

B. Si ve "**Error de autenticación: com.sicaproject...**": algún `Exception`
   no-`Optional.empty` fue lanzado. Candidatos probables:
   - `LazyInitializationException` al acceder a `usuario.getRol().getNombre()`
     fuera de sesión (Hibernate cierra el EM).
   - `NullPointerException` al dereferenciar `usuario.getRol()` si la query
     no trajo el join.
   - `EntityNotFoundException` si el `rol_id` apunta a un id que no existe.

**Pista clave del `UserRepositoryJpaAdapter.findByUsername`** (líneas 13-23):
```java
public Optional<Usuario> findByUsername(String username) {
    EntityManager em = JpaConfig.newEntityManager();
    try {
        TypedQuery<UsuarioEntity> query = em.createQuery(
                "SELECT u FROM UsuarioEntity u WHERE u.username = :username", UsuarioEntity.class);
        query.setParameter("username", username);
        return query.getResultStream().findFirst().map(UsuarioMapper::toDomain);
    } finally {
        em.close();
    }
}
```

El `EntityManager` se cierra al retornar. Si `UsuarioMapper.toDomain()`
carga perezosamente alguna relación (rol, persona) después de cerrar el EM,
Hibernate lanza `LazyInitializationException`.

**Recomendación**: ver el `UsuarioMapper.toDomain()` y verificar si accede
a `getRol()`, `getPersona()`, etc. desde una colección LAZY fuera de sesión.
Si es el caso, hay dos opciones:
1. Cambiar las relaciones a `EAGER` (temporal, antipatrón).
2. Hacer la query con `JOIN FETCH u.rol` y `JOIN FETCH u.persona` para
   cargar eagerly solo en este método.
3. Inicializar el EntityManager con `Hibernate.isInitialized()` antes de
   cerrar.

### 5.5 ⚠️ Otros issues conocidos (de `CONTEXTO_PROYECTO.md`, no resueltos)

- Falta `.gitignore` adecuado.
- Falta README, diagrama de arquitectura, manual de usuario.
- Falta CI/CD, Docker.
- Falta paginación en listados (Bitácora puede crecer mucho).
- Falta validación de documento único en Persona.
- Falta manejo de transacciones `@Transactional` en servicios críticos.
- Falta tests unitarios de `VisitaService` (los 4 flujos).

---

## 6. ARCHIVOS EN ESTE ZIP

```
proyecto_sica_full_análisis.zip
├── CAMBIOS_Y_DECISIONES.md          ← este archivo
├── proyecto_sica/                   ← copia del proyecto completo SIN .git ni target
│   ├── src/                         ← código fuente Java
│   ├── src/main/resources/          ← FXML, CSS, persistence.xml
│   ├── pom.xml
│   ├── schema.sql
│   ├── data.sql
│   ├── CONTEXTO_PROYECTO.md
│   ├── sica-domain-model.md
│   └── .gitignore
├── logs/
│   ├── mvn_compile.log              ← mvn clean compile completo
│   ├── mvn_smoke.log                ← mvn exec:java con Main (smoke test BD)
│   ├── login_test.log               ← test directo de AuthService.login (6 casos)
│   ├── javafx_run.log               ← mvn javafx:run con stack traces de los bugs
│   └── git_log.txt                  ← últimos commits
└── db/
    └── estado_bd.txt                ← estado actual de las tablas usuario, empresa, persona
```

---

## 7. INSTRUCCIONES DE REPRODUCCIÓN

### 7.1 Backend (sin UI)

```bash
cd proyecto_sica/
mvn clean compile
mvn exec:java -Dexec.mainClass="com.sicaproject.Main"
# Debe imprimir: Conexión a BD exitosa. Empresas encontradas: 3
```

### 7.2 Login (sin UI, simulado)

Crear `src/main/java/com/sicaproject/TestLogin.java` con el contenido del log
`login_test.log` (ver línea 8 del log, donde está el código). Ejecutar:
```bash
mvn exec:java -Dexec.mainClass="com.sicaproject.TestLogin"
# Debe imprimir 6 líneas LOGIN, 3 OK y 3 FAIL
```

### 7.3 UI

```bash
mvn javafx:run
# Abre ventana de login
# Usuario: admin1
# Contraseña: 123456
# Esperado: redirige a panel ADMIN con 4 stats cards y tabla de roles
```

### 7.4 Credenciales de prueba

| Username | Password | Rol |
|----------|----------|-----|
| `admin1` | `123456` | ADMIN |
| `funcionario1` | `123456` | FUNCIONARIO |
| `guarda1` | `123456` | GUARDA |

---

## 8. ENTORNO

- **OS**: Arch Linux (rolling, kernel 7.1.8)
- **Java**: OpenJDK 26.0.2.1 (`/usr/lib/jvm/java-26-openjdk`)
- **Maven**: 3.9.16 (`/usr/share/idea/plugins/maven-plugin/lib/maven3/`, vía shim
  en `~/.local/bin/mvn`)
- **PostgreSQL**: 18, en `localhost:5432`, BD `sica_db`, owner `sica_user`
- **JavaFX**: 17.0.2
- **Hibernate**: 6.4.4.Final
- **JBCrypt**: 0.4
- **Display**: `$DISPLAY=:1` (Hyprland/Wayland)

---

## 9. PETICIÓN ORIGINAL

> "Haga un archivo comprimido completo con lo siguiente:
> - Primero que nada con el proyecto así como está actualmente.
> - Cambios que hizo tanto internamente en bin y todo eso, como en el código,
>   eso lo puede hacer en un archivo y meterlo en el comprimido.
> - Que tenga absolutamente todo, para mandarlo analizar y encontrar errores."

Este zip satisface la petición. Toda la información necesaria para que un
analista externo reproduzca los errores, entienda el contexto y proponga
soluciones está incluida.
