# Atenas

Backend de microservicios para el seguimiento de equipos de una cátedra universitaria (Seminario Integrador, UTN FRC).
Integra información académica con evidencia de Google Drive (entregas) y GitHub (commits).

**Grupo 73** — Facundo Murua Ortiz, Nicolás Villacorta, Joaquín Morais, Mateo Emanuel Fernández.

## Componentes
| Componente | Rol | Puerto (dev / publicado) |
|---|---|---|
| `api-gateway` | Punto de entrada, JWT y autorización por rol | 8080 |
| `ms-catedra` | Estructura académica, usuarios, equipos | 8082 (interno) |
| `ms-documentacion` | Hitos, entregas, corrección | 8083 (interno) |
| `ms-seguimiento` | Calendario, asistencia, GitHub, dashboard | 8084 (interno) |
| `atenas-commons` | Librería común (JAR Maven) | — |
| Keycloak 26 / PostgreSQL 16 | Identidad / datos | 8081 / 5432 (solo dev) |

## Requisitos
JDK 21, Maven 3.9, Docker Desktop (Compose v2). Credenciales: token de GitHub y API key de Google Drive.

## Arranque
1. `cp .env.example .env` y completar los valores.
2. `cd atenas-commons && mvn clean install`
3. `mvn clean package` en `api-gateway`, `ms-catedra`, `ms-documentacion` y `ms-seguimiento`.
4. `docker compose up --build`
5. Verificar con `docker compose ps` y la colección de `postman/` (ver abajo).

## Colección Postman

1. Importá `postman/atenas.postman_environment.json` y `postman/atenas.postman_collection.json`, y seleccioná el environment `Atenas Local`.
2. Cada request declara su propia autenticación. Las requests de negocio usan **Bearer Token** con una variable `{{token-<usuario>}}`; ninguna hereda el token de la carpeta. Todas envían `X-Correlation-Id`.
3. **(a) Token del jefe.** Abrí `00 Tokens → Token jefe`, pestaña **Authorization**, y usá **Get New Access Token** con el usuario `jefe` y la clave `JEFE_PASSWORD_INICIAL` del `.env`. Copiá el **Access Token** y pegalo en `token-jefe` del environment.
4. **(b) Ejecutar `01 Estructura y usuarios`.** Crea el curso, las comisiones, los docentes y los estudiantes. Todas sus requests usan `token-jefe`.
5. **(c) Obtener los tokens de docentes y alumnos.** En `00 Tokens`:
   - `Token docente A` → `docente-a@atenas.local` → `token-docente-a`
   - `Token docente B` → `docente-b@atenas.local` → `token-docente-b`
   - `Token representante` → `400001@sistemas.frc.utn.edu.ar` → `token-representante`
   - `Token integrante` → `400002@sistemas.frc.utn.edu.ar` → `token-integrante`
   - `Token alumno B` → `400003@sistemas.frc.utn.edu.ar` → `token-alumno-b`
   Cada usuario nuevo inicia sesión con `CLAVE_TEMPORAL` y Keycloak le pide cambiarla. Cerrá la sesión de Keycloak entre un usuario y otro para no reutilizar el token anterior.
6. **(d) Orden de ejecución:** `02 Equipos → 03 Calendario y repositorio → 04 Proceso principal → 05 Consultas del alumno → 07 Negativos → 06 Reglas de dominio → 08 Fallas controladas`.
   - `05` y `07` usan `token-alumno-b`; hay que ejecutarlas antes de `06`, cuando el alumno B todavía no tiene equipo. Si se ejecuta `06` primero, `07` recibe `409` por equipo no asegurado en lugar del `403` esperado.
   - `03` y `04` requieren que los endpoints correspondientes estén implementados en los servicios.
7. Los access tokens vencen a las **4 horas** (`accessTokenLifespan` del realm de desarrollo). Si una request devuelve `401`, regenerá el token desde `00 Tokens` y actualizá su variable.
8. Para que Keycloak tome el cambio de `accessTokenLifespan`, recreá el volumen:
   ```powershell
   docker compose down -v
   docker compose up --build -d
   ```
   Eso borra la base y los usuarios: hay que volver a ejecutar `01` y regenerar todos los tokens.
9. `00 Smoke` es opcional. Sus dos primeras requests no llevan token; la tercera usa `token-jefe`.

### Atajo para E1 y todos los tokens

Después de obtener `token-jefe`, se puede ejecutar `00 Tokens → Seed E1 + todos los tokens`. Esa request:

- Hace la carpeta `01 Estructura y Usuarios` completa.
- Deja cargados los IDs y los JWT en el environment.
- Permite saltear `01` y seguir directamente con `02`.

El endpoint es `POST /api/catedra/dev/seed-e1` y requiere el JWT de jefe. El seed es idempotente: si los datos ya existen, los reutiliza.

### `08 Fallas controladas`

La colección agrega automáticamente la cabecera `X-Simular-Falla` en las requests de esta carpeta. Los servicios simulan la falla correspondiente sin pausar contenedores ni reiniciar `ms-seguimiento`:

- `X-Simular-Falla: github`: fuerza un fallo de GitHub y degrada el hito vigente.
- `X-Simular-Falla: ms-documentacion`: simula la caída de documentación y activa la degradación RN33.
- `X-Simular-Falla: ms-catedra`: simula la caída de catedra y permite verificar los `503`.

También funcionan las pruebas reales con `docker compose pause` y reinicio con token inválido, pero no son necesarias para ejecutar la carpeta desde Postman.

- **Drive:** el environment trae un PDF público de ejemplo en `driveEnlace`. Para probar `modificadoPostEntrega=SI`, reemplazalo por un archivo propio y editá ese archivo antes de ejecutar `D06 Archivo modificado en Drive`.
- La carpeta `04` crea el hito `Sprint vigente` y guarda `hitoVigenteId`; las pruebas de token inválido validan ese hito, porque el Sprint 1 de mayo ya está cerrado y se omite.

La colección cubre todas las rutas documentadas en `docs/06`. Si una ruta todavía no está implementada en el servicio, la request fallará con `404`; eso debe reportarse, no ocultarse.

El environment no contiene claves ni tokens: solo URLs, IDs vacíos, variables de token vacías y datos de Drive/GitHub de ejemplo. Los tokens se pegan en **Current Value** y no se versionan.

## Documentación
Empezar por [`docs/README.md`](docs/README.md).
