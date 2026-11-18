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
5. Verificar con `docker compose ps` y la colección de `postman/` (ver `docs/11-guia-de-desarrollo.md`).

## Documentación
Empezar por [`docs/README.md`](docs/README.md).
