# 14 — Cambios, supuestos y pendientes respecto del cuestionario

El cuestionario del 4/11 es el compromiso base con la cátedra. Todo lo que se aparta de él se registra acá. **Los cambios marcados "avisar" modifican lo comprometido y hay que comunicarlos a la cátedra.**

## A. Cambios decididos por el equipo
| # | Cambio | Impacto | Avisar |
|---|---|---|---|
| C-01 | **Las actividades (hitos) las crea el docente de cada comisión** en lugar del jefe a nivel de curso. Se renombra "hito" → "actividad" y pasan a pertenecer a una comisión lectiva (F06, RN17, endpoints D01/D02, tablas, I05/I06). El jefe solo las consulta | Modelo de `ms-documentacion`; RN18 valida la comisión | **Sí** |
| C-02 | **Nuevo estado de proyecto del equipo** con máquina de estados (F17, RN34, RN35, endpoint C23). `ABIERTO/ASEGURADO` se reemplazan por `DESASEGURADO` + grupo asegurado (`EN_IDEA`…) + finales | Modelo de `ms-catedra`; patrón State | **Sí** (funcionalidad nueva, prioridad media) |
| C-03 | RN10: el docente puede asegurar siempre; el representante solo con cantidad de integrantes entre mínimo y máximo (y GitHub cargado) | `PATCH …/aseguramiento` admite `DOCENTE` | Sí |
| C-04 | RN05/RN09: ingresar, salir, rechazar y dar de baja solo con el equipo `DESASEGURADO`; si se desasegura y queda bajo el mínimo, solo el docente puede volver a asegurar | 409 adicionales | Sí |
| C-05 | RN02: el mail del estudiante se deriva del legajo (`{legajo}@sistemas.frc.utn.edu.ar`); legajo numérico | Alta sin campo mail | No |
| C-06 | RN03: alta con **clave temporal** (`CLAVE_TEMPORAL`) y cambio obligatorio en el primer ingreso, sin correos | — | No |
| C-07 | RN24: nota **entera** de 1 a 10 | Columna `int` | No |
| C-08 | RN27: los commits posteriores al límite del sprint no cuentan | — | No |
| C-09 | Fechas en `timestamptz` (UTC) y offset de Argentina en la API | DER | No |
| C-10 | `postgres/init.sql` pasa a **`postgres/init.sh`** para poder leer las claves del `.env` sin versionarlas | Archivo de arranque | No |
| C-11 | Endpoint interno `GET /internal/cursos-lectivos/{id}` se reemplaza por `GET /internal/comisiones-lectivas/{id}`; `GET /internal/hitos` por `GET /internal/actividades` | Contratos internos | No |
| C-12 | Rutas de listados anidadas bajo la comisión (`/comisiones-lectivas/{id}/actividades`) y query params `comisionLectivaId` | Contratos | No |
| C-13 | Total: **40** endpoints `/api` (antes 39) y 7 `/internal` | — | — |
| C-14 | `ms-catedra` y `ms-seguimiento` usan `spring-boot-starter-oauth2-client` para Client Credentials | Dependencia aprobada | No |

## B. Supuestos propuestos (confirmar; hay un default para no frenar)
| # | Supuesto | Default adoptado |
|---|---|---|
| S-01 | Quién ejecuta cada acción de F17 | Docente: iniciar, aprobar, rechazar, cancelar. Representante: solicitar aprobación, reanudar |
| S-02 | Qué hace cancelar | Cierra las participaciones vigentes (RN35) |
| S-03 | Si el representante necesita GitHub cargado para asegurar | Sí, como en el cuestionario; el docente no tiene esa condición |
| S-04 | Umbral de "baja contribución" | 50 % del promedio de commits por integrante, configurable (`atenas.actividad.umbral-baja-contribucion`) |
| S-05 | Si el jefe puede crear actividades | No; solo consulta |
| S-06 | Permiso del enlace de Drive | Debe permitir al menos comentar. **El sistema no puede verificarlo con API key**: se comunica como instrucción a los alumnos |
| S-07 | `APROBADO` y `CANCELADO` | Estados finales: no admiten acciones ni bajas |
| S-08 | Horario de la tarea nocturna | 02:00, `America/Argentina/Cordoba`, configurable |

## C. Inconsistencias del cuestionario resueltas
- RN04, RN14, RN16 y RN32 no existen; el dashboard degradado es **RN33** (el cuestionario lo cita como RN-32 en dos lugares).
- "Todo endpoint responde 401 sin token" vs documentación OpenAPI pública: se entiende que **la documentación OpenAPI es la única excepción**.
- Rutas con `comisioneslectivas` (corte de línea del PDF) se normalizan a `comisiones-lectivas`.
- Nombre del Gateway: carpeta `api-gateway` (el cuestionario lo llama "API Gateway").

## D. Pendientes abiertos
- Confirmar con la cátedra C-01 y C-02 (alcance comprometido).
- Confirmar S-01 a S-08 con el equipo.
- Spike del día 1: `iss` de Keycloak, healthcheck de Keycloak, Swagger por el Gateway, Drive y GitHub (ver `11`).
