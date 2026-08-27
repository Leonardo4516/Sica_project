# SICA - Sistema Integrado de Control de Acceso

> Documento de contexto para continuar el proyecto en una nueva sesión.
> Fecha de último estado: 26 de agosto de 2026.

---

## 🎯 Descripción del Proyecto

Sistema de control de acceso para empresas con:
- **Arquitectura hexagonal** (puertos y adaptadores) + vertical slices
- **Java 17+** (probado con Java 21) + **Maven 3.9**
- **JPA + Hibernate 6.4.4.Final** + **PostgreSQL 18**
- **JavaFX 17.0.2** para UI
- **JBCrypt 0.4** para hashing de contraseñas
- **RBAC** (Role-Based Access Control) con permisos en BD
- **4 flujos de acceso**: pre-registrado, no anunciado, carnet olvidado, salida olvidada
- **Auditoría persistente** de todas las operaciones

---

## 📂 Estructura del Proyecto

```
/home/Papi_Leo/VSCODE/JAVA/proyecto_sica/
├── pom.xml                                 # Maven config (JavaFX 17, Hibernate 6.4.4, JBCrypt 0.4, JUnit)
├── schema.sql                              # CREATE TABLE para PostgreSQL
├── data.sql                                # Datos iniciales (roles, permisos, usuarios, empresas)
├── src/main/java/com/sicaproject/
│   ├── Main.java                           # Smoke test BD (ejecuta con: mvn exec:java -Dexec.mainClass="com.sicaproject.Main")
│   ├── SicaApplication.java                # UI JavaFX (entry point visual)
│   └── sica/
│       ├── iam/                            # Identity & Access Management
│       │   ├── domain/                     # Usuario, Rol, Permiso
│       │   ├── application/
│       │   │   ├── port/in/RbacUseCase.java
│       │   │   ├── port/out/UsuarioRepository.java
│       │   │   └── service/                # AuthService, RbacService, PermisoDenegadoException
│       │   └── infrastructure/adapter/out/persistence/  # RolEntity, PermisoEntity, UsuarioEntity, *RepositoryJpaAdapter, Mappers
│       ├── personas/                       # Personas (TRABAJADOR/INVITADO)
│       │   ├── domain/Persona.java
│       │   ├── application/service/PersonaService.java
│       │   └── infrastructure/adapter/out/persistence/  # PersonaEntity, Mapper, RepositoryJpaAdapter
│       ├── empresas/                       # Empresas destino
│       │   ├── domain/Empresa.java
│       │   ├── application/service/EmpresaService.java
│       │   └── infrastructure/adapter/out/persistence/
│       ├── acceso/                         # Visitas (core del negocio)
│       │   ├── domain/                     # Visita, EstadoVisita (con puedeTransicionarA)
│       │   ├── application/
│       │   │   ├── port/out/VisitaRepository.java
│       │   │   └── service/VisitaService.java  # 4 flujos + regularizarSiCorresponde()
│       │   └── infrastructure/adapter/out/persistence/
│       ├── auditoria/                      # Bitácora de auditoría
│       │   ├── domain/BitacoraAuditoria.java
│       │   ├── application/AuditoriaService.java
│       │   └── infrastructure/adapter/out/persistence/
│       ├── shared/infrastructure/          # Configuración global
│       │   ├── config/CompositionRoot.java  # DI manual (Singleton)
│       │   └── persistence/JpaConfig.java    # EntityManagerFactory
│       └── ui/                             # Capa de presentación JavaFX
│           ├── SceneManager.java           # Navegación entre pantallas
│           ├── component/ParticleBackground.java  # Red animada de partículas
│           ├── controller/                 # 5 controllers (Login, Guarda, Funcionario, Admin, Bitacora)
│           └── ui/                         # ⚠️ FXML en src/main/resources/com/sicaproject/sica/ui/
└── src/main/resources/
    ├── META-INF/persistence.xml            # JPA config (jdbc:postgresql://localhost:5432/sica_db, validate, show_sql=true)
    └── com/sicaproject/sica/ui/
        ├── styles.css                      # Tema premium oscuro + glassmorphism
        ├── login.fxml                      # Pantalla de login
        ├── guarda.fxml                     # Dashboard GUARDA (check-in/check-out/visitas activas)
        ├── funcionario.fxml                # Dashboard FUNCIONARIO (solicitudes pendientes + registro)
        ├── admin.fxml                      # Dashboard ADMIN (stats + RBAC)
        └── bitacora.fxml                   # Bitácora con filtros de fecha
```

---

## 🗄️ Base de Datos PostgreSQL

### Conexión (configurada en persistence.xml)
- **URL**: `jdbc:postgresql://localhost:5432/sica_db`
- **Usuario**: `sica_user`
- **Password**: `sica_pass`
- **Override por env vars** (en `JpaConfig.java`):
  - `SICA_DB_URL`, `SICA_DB_USER`, `SICA_DB_PASSWORD`

### Estado actual
✅ PostgreSQL 18 corriendo (PID 633, usuario `postgres`)
✅ Base de datos `sica_db` creada con owner `sica_user`
✅ 7 tablas creadas: `rol`, `permiso`, `rol_permiso`, `usuario`, `persona`, `empresa`, `visita`, `bitacora_auditoria`
✅ 7 índices creados
✅ Datos iniciales cargados (ver `data.sql`)

### Tablas y columnas importantes

```sql
rol (id, nombre)
permiso (id, codigo, descripcion)
rol_permiso (rol_id, permiso_id)  -- many-to-many
usuario (id, username, password_hash, activo, persona_id, rol_id)
persona (id, tipo, nombre, documento, foto_url, empresa_id, bloqueado, motivo_bloqueo)
empresa (id, nombre, nit)
visita (id, persona_id, empresa_destino_id, funcionario_anfitrion_id, guarda_id,
        estado, es_pase_temporal, fecha_hora_registro, fecha_hora_entrada,
        fecha_hora_salida, observaciones)
bitacora_auditoria (id, usuario_id, accion, entidad_afectada, entidad_id,
                    detalle, resultado, fecha_hora)
```

### Reiniciar BD desde cero
```bash
psql -U postgres -h localhost -c "DROP DATABASE sica_db;"
psql -U postgres -h localhost -c "CREATE DATABASE sica_db OWNER sica_user;"
psql -U sica_user -h localhost -d sica_db -f schema.sql
psql -U sica_user -h localhost -d sica_db -f data.sql
```

### Datos de prueba (contraseña para todos: `123456`)
- `guarda1` (rol GUARDA)
- `funcionario1` (rol FUNCIONARIO)
- `admin1` (rol ADMIN)

> ⚠️ Los hashes BCrypt del `data.sql` son placeholder. Si falla el login, regenerar con `AuthService.hashPassword("123456")` y actualizar el SQL.

---

## 🎨 UI JavaFX (Glassmorphism + Partículas)

### Tema (`styles.css`)
- Fondo oscuro premium (`#0a0e1a`)
- Paneles con glassmorphism: `rgba(28, 34, 54, 0.55)` + border sutil + dropshadow
- Acento: `#6b8afd` (azul premium)
- Éxito: `#4ade80`, Advertencia: `#fbbf24`, Error: `#f87171`
- **NO usa neomorfismo** (como pidió el usuario)

### Partículas (`ParticleBackground.java`)
- 60 partículas en Canvas
- Red conectada con líneas cuando distancia < 140
- Timeline a ~30 FPS
- Color: `rgba(107, 138, 253, 0.7)`

### Pantallas
1. **Login** - glass panel central con partículas de fondo
2. **Guarda** - sidebar + 2 tarjetas (check-in / check-out) + tabla visitas activas
3. **Funcionario** - sidebar + tabla pendientes + formulario nueva visita
4. **Admin** - sidebar + 4 stats cards + tabla roles/permisos RBAC
5. **Bitácora** - sidebar + filtros fecha + tabla eventos

### Navegación (`SceneManager`)
- `SceneManager.setPrimaryStage(stage)` al iniciar
- `SceneManager.navigateAfterLogin(usuario)` según rol
- `SceneManager.logout()` para volver al login

---

## 🔧 Estado de Compilación

```bash
mvn clean compile    # ✅ Pasa
mvn test              # ✅ Pasa
mvn exec:java -Dexec.mainClass="com.sicaproject.Main"  # ✅ Smoke test BD (lista 3 empresas)
```

---

## 🐛 Issues Conocidos

### Issue 1: JavaFX plugin apunta a Main en vez de SicaApplication
**Archivo**: `pom.xml`
**Problema**: `<mainClass>com.sicaproject.Main</mainClass>` debería ser `com.sicaproject.SicaApplication`
**Solución**: Cambiar el valor y mergear a main desde una rama feature
**Comando alternativo sin tocar pom**:
```bash
/usr/share/idea/plugins/maven-plugin/lib/maven3/bin/mvn compile exec:java -Dexec.mainClass="com.sicaproject.SicaApplication"
```

### Issue 2: `mvn` no está en PATH del usuario
**Causa**: Maven está en `/usr/share/idea/plugins/maven-plugin/lib/maven3/bin/mvn` (solo IDE)
**Solución temporal**: Usar ruta completa o crear alias:
```bash
alias mvn='/usr/share/idea/plugins/maven-plugin/lib/maven3/bin/mvn'
```

### Issue 3: Necesita entorno gráfico para JavaFX
Si `$DISPLAY` está vacío (SSH sin X forwarding), JavaFX no abrirá ventanas. Usar:
```bash
xvfb-run -a mvn exec:java -Dexec.mainClass="com.sicaproject.SicaApplication"
```

---

## 🌿 Estado de Git

### Ramas actuales
- `main` ← rama principal (todos los merges)
- `feature/acceso-module`, `feature/application-layer`, `feature/domain-model`, `feature/personas-module` (antiguas, ya mergeadas)

### Reglas de Git del usuario
1. Cada feature = rama nueva desde `main`
2. Commit con Conventional Commits (`feat:`, `fix:`, `docs:`)
3. Push de la rama feature (cuando haya remoto)
4. Merge a `main` con `--no-ff`
5. Eliminar rama feature

### Historial reciente (commits importantes)
```
498f2e2 merge configuración inicial PostgreSQL
8b5573f feat: añadir schema.sql y data.sql para inicialización BD PostgreSQL
1d2c77e merge UI glassmorphism con partículas animadas
653c2d7 feat(ui): añadir SicaApplication (entry point JavaFX) y completar integraciones
1da57b7 feat(ui): añadir pantalla y controlador de Bitácora con filtros por fecha
b71983b feat(ui): añadir pantalla y controlador ADMIN con dashboard y RBAC
31d4ea2 feat(ui): añadir pantalla y controlador del FUNCIONARIO con registro y aprobación
8aa1cf1 feat(ui): añadir pantalla y controlador del GUARDA con check-in/check-out
a1f048a feat(ui): añadir SceneManager para navegación global entre pantallas
a60c0bf feat(ui): añadir pantalla y controlador de Login
e6088a8 feat(ui): añadir tema premium oscuro + glassmorphism en styles.css
9e3ce1e feat(ui): añadir componente ParticleBackground con red animada de partículas
45460af merge IA correcciones fase1 con flujo git estructurado
5d0525a feat: aplicar correcciones IA fase1 - infraestructura JPA y entidades
```

---

## 📦 ZIPs Generados (en raíz del proyecto)
- `proyecto_sica_backend.zip` (117 KB) - versión base sin correcciones IA
- `proyecto_sica_backend_fase1.zip` (174 KB) - versión con correcciones IA aplicadas

> Estos ZIPs son **anteriores** a la UI. Regenerar si se necesita distribución.

---

## ✅ Lo que SÍ está hecho
- [x] Backend completo con arquitectura hexagonal
- [x] JPA + Hibernate 6.4.4.Final con repositorios reales
- [x] RBAC con permisos cargados desde BD
- [x] 4 flujos de acceso implementados y validados
- [x] Auditoría persistente
- [x] PostgreSQL funcionando con schema y datos
- [x] UI completa: 5 pantallas JavaFX con glassmorphism
- [x] Animación de partículas funcionando
- [x] Composición Root con DI manual
- [x] Flujo Git estricto (feature → merge → delete)
- [x] Commits con Conventional Commits

---

## ❌ Lo que FALTA

### Crítico
- [ ] **Corregir `pom.xml`**: Cambiar `mainClass` de `Main` a `SicaApplication` (rama feature)
- [ ] **Verificar login real**: Probar que `admin1/123456` entre al panel ADMIN (puede fallar el hash BCrypt del data.sql)
- [ ] **Resolver flujo de aprobación de visitas**: Los botones "Aprobar/Rechazar" están en el FXML del Funcionario pero no en el controller (agregar columnas con botones)

### Mejoras UI
- [ ] **Notificaciones/toasts** cuando una operación es exitosa
- [ ] **Indicador de carga** durante operaciones de BD
- [ ] **Cerrar sesión con confirmación** (modal)
- [ ] **Validación visual** de campos vacíos en formularios
- [ ] **Responsive design** para resoluciones menores a 1024x600
- [ ] **Iconos Material Design** (reemplazar emojis por SVG/PNG)

### Backend
- [ ] **Tests unitarios** para `VisitaService` (los 4 flujos)
- [ ] **Tests de integración** con BD de prueba
- [ ] **Manejo de transacciones** (`@Transactional` en servicios críticos)
- [ ] **Validación de documento único** en Persona
- [ ] **Paginación** en listados (Bitácora puede crecer mucho)
- [ ] **Búsqueda/filtros** en tablas (por nombre, fecha, etc.)

### Documentación
- [ ] **README.md** con instrucciones de instalación
- [ ] **Diagrama de arquitectura** (hexagonal + vertical slices)
- [ ] **Manual de usuario** para los 3 roles
- [ ] **Diccionario de datos** (significado de cada campo)

### DevOps
- [ ] **`.gitignore`** adecuado (target/, *.log, etc.)
- [ ] **CI/CD** con GitHub Actions (build + tests)
- [ ] **Docker** (Dockerfile + docker-compose con PostgreSQL)
- [ ] **Variables de entorno** documentadas

---

## 🚀 Cómo continuar el proyecto

### Para trabajar en cualquier feature nueva
```bash
cd /home/Papi_Leo/VSCODE/JAVA/proyecto_sica
git checkout main
git pull origin main  # si hay remoto
git checkout -b feature/nombre-descriptivo main
# ... hacer cambios ...
git add -A
git commit -m "feat: descripción breve"
git checkout main
git merge --no-ff feature/nombre-descriptivo
git branch -d feature/nombre-descriptivo
```

### Para ejecutar la UI visual
**Opción 1 (requiere fix al pom.xml primero)**:
```bash
mvn javafx:run
```

**Opción 2 (sin tocar pom.xml)**:
```bash
/usr/share/idea/plugins/maven-plugin/lib/maven3/bin/mvn compile exec:java -Dexec.mainClass="com.sicaproject.SicaApplication"
```

**Si no hay display gráfico**:
```bash
xvfb-run -a /usr/share/idea/plugins/maven-plugin/lib/maven3/bin/mvn compile exec:java -Dexec.mainClass="com.sicaproject.SicaApplication"
```

### Para probar conexión BD
```bash
/usr/share/idea/plugins/maven-plugin/lib/maven3/bin/mvn exec:java -Dexec.mainClass="com.sicaproject.Main"
```

---

## 📝 Convenciones del Proyecto

### Nomenclatura
- **Vertical slices**: cada módulo (`iam`, `personas`, `empresas`, `acceso`, `auditoria`) tiene `domain/`, `application/`, `infrastructure/`
- **Puertos**: `application/port/in/` (Use Cases) y `application/port/out/` (Repositories)
- **Adaptadores**: `infrastructure/adapter/out/persistence/` (JPA)
- **Servicios**: `application/service/`
- **UI**: `ui/controller/`, `ui/component/`, recursos en `src/main/resources/com/sicaproject/sica/ui/`

### Estilo de código
- Java 17 features (records, switch expressions, text blocks)
- Sin Lombok (preferir código explícito para enseñar)
- Javadoc en clases y métodos públicos
- Sin comentarios innecesarios (regla del sistema)

---

## 🔗 Referencias Útiles
- Hibernate 6.4 docs: https://docs.jboss.org/hibernate/orm/6.4/
- JavaFX 17 docs: https://openjfx.io/javadoc/17/
- PostgreSQL 18 docs: https://www.postgresql.org/docs/18/
- Hexagonal Architecture: https://alistair.cockburn.us/hexagonal-architecture/

---

**Última actualización**: 26 de agosto de 2026
**Estado**: Backend y UI completos, pendiente corregir pom.xml para lanzamiento de JavaFX
