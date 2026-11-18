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

1. En Postman, importá `postman/atenas.postman_environment.json` y `postman/atenas.postman_collection.json`.
2. Seleccioná el environment `Atenas Local` antes de ejecutar la colección.
3. Abrí la carpeta `00 Smoke`, elegí el request `GET equipo interno con token`, y en `Auth` hacé `Get New Access Token`:
   - `Auth URL`: `{{keycloakUrl}}/realms/{{realm}}/protocol/openid-connect/auth`
   - `Access Token URL`: `{{keycloakUrl}}/realms/{{realm}}/protocol/openid-connect/token`
   - `Client ID`: `{{clientId}}`
   - `Redirect URI`: `https://oauth.pstmn.io/v1/callback`
   - `Scope`: `openid profile email`
4. Postman redirige a Keycloak; ingresá con el usuario que necesités (`jefe` para el smoke). Postman guarda el token en su propio almacenamiento, no en el environment.
5. Los usuarios nuevos creados por el sistema usan la clave temporal y Keycloak les exige cambiarla.
6. Ejecutá siempre `00 Smoke` primero:
   - `GET /api/documentacion/v3/api-docs` sin token → `200`
   - `POST /api/documentacion/hitos/1/entregas` sin token → `401`
   - `GET /internal/equipos/1` con token → `404`
7. Las carpetas `01`–`08` están vacías; se completan junto con las historias. Seguí ese orden cuando se agreguen requests.

El environment no contiene claves ni tokens: solo URLs, IDs vacíos y datos de Drive/GitHub marcados como `completar`. Los requests generan `X-Correlation-Id` automáticamente con `{{$guid}}`.

## Documentación
Empezar por [`docs/README.md`](docs/README.md).
