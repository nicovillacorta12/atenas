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
2. Cada request declara su propia autenticación. Las requests de negocio usan **Bearer Token** con una variable `{{token-<usuario>}}`; ninguna hereda el token de la carpeta.
3. **(a) Token del jefe.** Abrí `00 Tokens → Token jefe`, pestaña **Authorization**, y usá **Get New Access Token** con el usuario `jefe` y la clave `JEFE_PASSWORD_INICIAL` del `.env`. Copiá el **Access Token** y pegalo en la variable `token-jefe` del environment.
4. **(b) Ejecutar `01 Estructura y usuarios`.** Crea el curso, las comisiones, los docentes y los estudiantes. Todas sus requests usan `token-jefe`.
5. **(c) Obtener los tokens de docentes y alumnos.** Con la carpeta `00 Tokens`:
   - `Token docente A` → `docente-a@atenas.local` → variable `token-docente-a`
   - `Token docente B` → `docente-b@atenas.local` → variable `token-docente-b`
   - `Token representante` → `400001@sistemas.frc.utn.edu.ar` → variable `token-representante`
   - `Token integrante` → `400002@sistemas.frc.utn.edu.ar` → variable `token-integrante`
   - `Token alumno B` → `400003@sistemas.frc.utn.edu.ar` → variable `token-alumno-b`
   Cada usuario nuevo inicia sesión con `CLAVE_TEMPORAL` y Keycloak le pide cambiarla. Cerrá la sesión de Keycloak entre un usuario y otro para no reutilizar el token anterior.
6. **(d) Ejecutar `02 Equipos`, `06 Reglas de dominio` y `07 Negativos`**, en ese orden. La carpeta `05 Consultas del alumno` es opcional y usa `token-alumno-b`.
7. Los access tokens vencen a las **4 horas** (`accessTokenLifespan` del realm de desarrollo). Si una request devuelve `401`, regenerá el token desde `00 Tokens` y actualizá su variable.
8. Para que Keycloak tome el cambio de `accessTokenLifespan`, hay que recrear el volumen:
   ```powershell
   docker compose down -v
   docker compose up --build -d
   ```
   Eso borra la base y los usuarios: hay que volver a ejecutar `01` y regenerar todos los tokens.
9. `00 Smoke` es opcional. Sus dos primeras requests no llevan token; la tercera usa `token-jefe`.

El environment no contiene claves ni tokens: solo URLs, IDs vacíos, variables de token vacías y datos de Drive/GitHub marcados como `completar`. Los tokens se pegan en **Current Value** y no se versionan. Los requests generan `X-Correlation-Id` automáticamente con `{{$guid}}`.

## Documentación
Empezar por [`docs/README.md`](docs/README.md).
