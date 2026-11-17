# AGENTS.md — Atenas

Backend de microservicios (Java 21, Spring Boot 3.5) para el seguimiento de equipos de una cátedra universitaria.
TPI de Backend de Aplicaciones, UTN FRC. Lo leen OpenCode y Claude Code (`CLAUDE.md` importa este archivo).

## Fuente de verdad
Las decisiones viven en `docs/`. Este archivo solo resume reglas de trabajo.
**Si algo de este archivo o de un pedido contradice `docs/`, gana `docs/` y hay que avisar antes de seguir.**

| Necesitás saber... | Leé |
|---|---|
| Qué se construye y qué no | `docs/01-vision-y-alcance.md`, `docs/02-requisitos.md` |
| Reglas de negocio y estados (RN, F) | `docs/03-reglas-de-negocio.md` |
| Arquitectura, seguridad, resiliencia | `docs/04-arquitectura.md` |
| Tablas y relaciones | `docs/05-modelo-de-datos.md` |
| Endpoints, errores, contratos internos | `docs/06-contratos-api.md` |
| Cómo se estructura y escribe un microservicio | `docs/07-estandar-microservicio.md` |
| Cómo se testea | `docs/08-testing.md` |
| Patrones (State, Adapter) | `docs/10-patrones-de-diseno.md` |
| Arranque, puertos, `.env`, Git | `docs/11-guia-de-desarrollo.md` |
| Por qué se decidió algo | `docs/adr.md` |

Antes de tocar un servicio, leé **su** `AGENTS.md` (`ms-catedra/`, `ms-documentacion/`, `ms-seguimiento/`, `api-gateway/`, `atenas-commons/`).

## Reglas de trabajo (IMPORTANTE)
1. Trabajá SOLO en la historia de usuario que te pasen. No adelantes otras ni refactorices lo que no se pide.
2. Antes de escribir código, resumí en 3-5 líneas qué vas a hacer. Si algo contradice `docs/`, avisá en vez de improvisar.
3. No agregues dependencias sin decirlo y justificarlo. Las aprobadas están en `docs/07-estandar-microservicio.md`.
4. **Cada historia incluye sus tests** (ver `docs/08-testing.md`). Sin tests no está terminada.
5. Terminar significa: `mvn clean test` pasando en el servicio tocado y el servicio levantando (`/actuator/health` en `UP`). Reportá la salida real de los comandos; no la supongas.
6. Código, nombres de dominio, comentarios, mensajes, commits y documentación en español (reglas finas en `docs/07`, sección Idioma).
7. No toques `docs/` ni los `AGENTS.md` salvo que la historia lo pida.
8. Una historia toca un solo microservicio (salvo la inicialización). Si necesitás algo de otro servicio, usá su contrato de `docs/06`; no edites su código.
9. Al terminar: listá archivos creados/modificados y cómo probarlo a mano (curl o Postman).

## Reglas de arquitectura (no negociables)
- Un servicio = un esquema de PostgreSQL. Sin claves foráneas entre esquemas. Nunca compartir entidades, repositorios ni DTO entre servicios: se habla por HTTP.
- Capas: `controller` → `service` → `repository`. Las entidades JPA nunca salen por la API: se devuelven `record` DTO con mapeo manual.
- Errores: siempre `ProblemDetail` mediante `atenas-commons`. Prohibido `catch (Exception)` genérico y devolver `500` por reglas de negocio.
- Seguridad: roles solo desde las constantes de `atenas-commons`. Las rutas `/internal/**` jamás tienen ruta en el Gateway.
- Llamadas a otros servicios, Drive y GitHub: siempre por una interfaz en `client` (patrón Adapter) y **antes** de abrir la transacción.
- Fechas: `Instant` / `timestamptz` (UTC) en base; la API responde con offset de Argentina (`-03:00`).
- Prohibido: circuit breaker, reintentos automáticos, Testcontainers, MapStruct, Eureka, mensajería, `@Data` en entidades, `@ManyToMany`.
- Secretos, tokens y contraseñas: nunca en el código ni en los logs. Todo por variables de entorno (`.env`, no versionado).

## Comandos
| Acción | Comando |
|---|---|
| Instalar la librería común | `cd atenas-commons && mvn clean install` |
| Tests de un servicio | `cd ms-<x> && mvn clean test` |
| Empaquetar un servicio | `cd ms-<x> && mvn clean package` |
| Levantar todo | `docker compose up --build` (después de empaquetar) |
| Bajar y limpiar datos | `docker compose down -v` |

## Estructura del repositorio
```
atenas-commons/  api-gateway/  ms-catedra/  ms-documentacion/  ms-seguimiento/
docs/  postman/  postgres/  keycloak/  docker-compose.yml  docker-compose.dev.yml  .env.example
```
