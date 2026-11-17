# 03 — Reglas de negocio y estados

Los IDs `RN` y `F` conservan la numeración del cuestionario para mantener la trazabilidad con lo comprometido.
**Huecos:** RN04, RN14, RN16 y RN32 no existen en el cuestionario (no hay que implementarlos). Donde el cuestionario cita "RN-32" para el dashboard degradado, es **RN33**.
Marcas: **[CAMBIÓ]** modifica el cuestionario, **[NUEVA]** no estaba, **[PROPUESTA]** supuesto a confirmar (detalle en `14`).

## Catálogo de reglas
| ID | Regla | Rechazos | F |
|---|---|---|---|
| RN01 | Solo el jefe administra estructura y usuarios. Cada docente opera y consulta solo sus comisiones | 403 | F02–F04, F06, F08–F10, F12–F17 |
| RN02 **[CAMBIÓ]** | Alta de estudiante: legajo numérico y único. El mail institucional **se deriva**: `{legajo}@sistemas.frc.utn.edu.ar` (no se recibe) | 400, 409 | F03 |
| RN03 **[CAMBIÓ]** | Alta de usuario: guarda en la base y luego crea la cuenta en Keycloak (Admin API) con **clave temporal** y la acción obligatoria de cambiarla en el primer inicio. Si Keycloak falla, revierte | 503 | F03 |
| RN05 **[CAMBIÓ]** | Baja lógica: conserva el historial y deshabilita la cuenta; si Keycloak falla, revierte. Un estudiante con equipo **solo puede darse de baja si el equipo está `DESASEGURADO`**; entonces cierra su participación y, si era representante, aplica RN09 | 404, 409, 503 | F03 |
| RN06 | Crear espacio de equipo: `cupoMinimo ≥ 1` y `cupoMinimo ≤ cupoMaximo`. El equipo nace `DESASEGURADO` | 400 | F04 |
| RN07 | Un alumno integra un solo equipo vigente (garantizado por índice único parcial) | 409 | F04 |
| RN08 **[CAMBIÓ]** | Ingresar a un equipo exige misma comisión, estado `DESASEGURADO` y cupo disponible (`SELECT … FOR UPDATE` sobre el equipo) | 403, 409 | F04 |
| RN09 **[CAMBIÓ]** | El primer ingresante es representante y, con el equipo `DESASEGURADO`, puede rechazar ingresos. Si sale, lo reemplaza el integrante vigente más antiguo (desempate por id). **Salir o rechazar solo es posible en `DESASEGURADO`** | 403 si otro rechaza; 409 si no está `DESASEGURADO` | F04 |
| RN10 **[CAMBIÓ]** | Asegurar: un **docente de la comisión puede siempre** (el equipo debe estar `DESASEGURADO`). Un **representante solo si** `cupoMinimo ≤ integrantes vigentes ≤ cupoMaximo` **y** todos cargaron su usuario de GitHub **[PROPUESTA: se mantiene la condición de GitHub del cuestionario]** | 403, 409 | F04, F05 |
| RN11 **[CAMBIÓ]** | Desasegurar (reapertura): solo un docente de la comisión, desde cualquier estado del grupo `ASEGURADO`. El equipo vuelve a `DESASEGURADO`. Si después sale un integrante y queda bajo el mínimo, los alumnos ya no pueden asegurar (RN10): solo el docente | 403, 409 | F04 |
| RN12 | Crear evento: solo docente asignado; tipo `CLASE` o `PRESENTACION` | 400, 403, 503 | F14 |
| RN13 | Asistencia: una por alumno y evento, de la misma comisión, sin fechas futuras; repetir actualiza | 403, 404, 409, 503 | F15 |
| RN15 | Un alumno solo ve su asistencia y las entregas de su equipo | 403, 503 | F08, F16 |
| RN17 **[CAMBIÓ]** | Crear actividad: **solo un docente asignado a esa comisión lectiva**; la comisión debe existir; `fechaInicio < fechaLimite` y ambas dentro del período del curso lectivo | 400, 403, 404, 503 | F06 |
| RN18 **[CAMBIÓ]** | Registrar entrega: solo el representante de un equipo **asegurado** (`EN_IDEA`, `EN_PROCESO`, `PENDIENTE_APROBACION` o `RECHAZADO`), para una actividad **de su comisión** | 403, 409, 503 | F07 |
| RN19 | Validar enlace: archivo existente, accesible y de tipo PDF o Google Doc. El enlace debe permitir al menos comentar (convención de uso: **no es verificable** con API key; ver `14`) | 400, 422 | F07 |
| RN20 | Estado de entrega: `PENDIENTE` se calcula si no hay fila; pasa a `ENTREGADA` al registrarla y a `CORREGIDA` al calificarla. No se corrige una pendiente | 404 si pendiente; 409 si ya corregida | F07, F08, F10 |
| RN21 | Entrega fuera de término: se acepta y se marca `tardia = true` | — | F07 |
| RN22 | Drive no responde al entregar: no registra; el equipo reintenta | 503 | F07 |
| RN23 | La corrección se hace en Drive; el sistema no gestiona comentarios | — | F09, F10 |
| RN24 **[CAMBIÓ]** | Corregir: solo docente de la comisión, sobre entrega `ENTREGADA`, nota **entera** de 1 a 10 | 403, 409, 422, 503 | F10 |
| RN25 | Vincular repositorio: solo el representante, repositorio público y rama existente | 403, 422, 503 | F11 |
| RN26 | Los commits se atribuyen a un integrante por su usuario de GitHub (en minúsculas); si no coincide, quedan sin atribuir | — | F05, F12 |
| RN27 **[CAMBIÓ]** | Actividad del sprint: commits de la rama elegida con fecha dentro de `[fechaInicio, fechaLimite]` de la actividad de tipo `SPRINT` de la comisión; **los posteriores al límite no cuentan**; sin merges (commits con más de un padre); atribuidos por usuario. Marca "sin actividad" (0 commits del equipo) y "baja contribución" (integrante con menos del 50 % del promedio por integrante **[PROPUESTA: umbral configurable]**) solo de forma informativa | — | F12, F13 |
| RN28 | Si GitHub o un servicio interno no responde, se muestra el último snapshot como `DESACTUALIZADO` o la evidencia como `NO_DISPONIBLE` si no hay. Una respuesta inesperada de GitHub deja la evidencia `NO_DISPONIBLE`. Nunca bloquea | — | F12, F13 |
| RN29 | Ya existe entrega para esa actividad y equipo: no se puede cambiar | 409 | F07 |
| RN30 | Al abrir una entrega se compara `modifiedTime` de Drive con la fecha de entrega y el límite; es informativo y **se congela al corregir**. Si Drive falla: `NO_VERIFICADO` | — | F09, F10 |
| RN31 | Actualizar actividad: de noche (cron, zona `America/Argentina/Cordoba`) o a pedido de docente/jefe; guarda un snapshot | 403 | F12 |
| RN33 | Si `ms-documentacion` no responde al armar el dashboard, se responde sin la sección de entregas marcada como no disponible | — | F13 |
| RN34 **[NUEVA]** | Estados del equipo: solo las transiciones de la tabla de abajo. Una transición inválida es 409; un actor no autorizado, 403 | 403, 409 | F04, F17 |
| RN35 **[NUEVA][PROPUESTA]** | Cancelar un equipo cierra las participaciones vigentes (`fechaBaja`) para que sus alumnos puedan integrar otro | 403, 409 | F17 |
| RN36 **[NUEVA]** | Visibilidad de actividades: el jefe ve todas; el docente, las de sus comisiones; el alumno, las de su comisión | 403 | F08 |

## Estados del equipo (patrón State, ver `10`)
Un único campo persistido `estado`. **"Asegurado" es un grupo**: `EN_IDEA`, `EN_PROCESO`, `PENDIENTE_APROBACION`, `RECHAZADO`.

```mermaid
stateDiagram-v2
    [*] --> DESASEGURADO
    DESASEGURADO --> EN_IDEA : asegurar
    EN_IDEA --> EN_PROCESO : iniciar
    EN_PROCESO --> PENDIENTE_APROBACION : solicitar aprobación
    PENDIENTE_APROBACION --> RECHAZADO : rechazar
    RECHAZADO --> EN_PROCESO : reanudar
    PENDIENTE_APROBACION --> APROBADO : aprobar
    EN_IDEA --> DESASEGURADO : desasegurar
    EN_PROCESO --> DESASEGURADO : desasegurar
    PENDIENTE_APROBACION --> DESASEGURADO : desasegurar
    RECHAZADO --> DESASEGURADO : desasegurar
    DESASEGURADO --> CANCELADO : cancelar
    EN_IDEA --> CANCELADO : cancelar
    EN_PROCESO --> CANCELADO : cancelar
    PENDIENTE_APROBACION --> CANCELADO : cancelar
    RECHAZADO --> CANCELADO : cancelar
    APROBADO --> [*]
    CANCELADO --> [*]
```

| Acción | Desde | Hasta | Quién | Condición |
|---|---|---|---|---|
| `ASEGURAR` | `DESASEGURADO` | `EN_IDEA` | Docente de la comisión; alumno representante | RN10 |
| `DESASEGURAR` (endpoint `reapertura`) | grupo asegurado | `DESASEGURADO` | Docente | — |
| `INICIAR` | `EN_IDEA` | `EN_PROCESO` | Docente **[PROPUESTA]** | — |
| `SOLICITAR_APROBACION` | `EN_PROCESO` | `PENDIENTE_APROBACION` | Representante **[PROPUESTA]** | Equivale a "inscripto a un final" |
| `APROBAR` | `PENDIENTE_APROBACION` | `APROBADO` | Docente **[PROPUESTA]** | Estado final |
| `RECHAZAR` | `PENDIENTE_APROBACION` | `RECHAZADO` | Docente **[PROPUESTA]** | — |
| `REANUDAR` | `RECHAZADO` | `EN_PROCESO` | Representante **[PROPUESTA]** | — |
| `CANCELAR` | `DESASEGURADO` o grupo asegurado | `CANCELADO` | Docente **[PROPUESTA]** | RN35. Estado final |

Qué permite cada estado: ingresar, salir, rechazar ingresos y darse de baja **solo en `DESASEGURADO`**; registrar entregas y vincular repositorio **solo en el grupo asegurado**; `APROBADO` y `CANCELADO` no admiten ninguna acción.

## Estados de la entrega
| Estado | Persistido | Cómo se llega | Quién |
|---|---|---|---|
| `PENDIENTE` | No (se calcula si no hay fila) | Existe la actividad y no hay entrega del equipo | — |
| `ENTREGADA` | Sí | Registro validado | Representante |
| `CORREGIDA` | Sí | Carga de nota | Docente |

## Estados del snapshot de actividad
| Estado | Cuándo |
|---|---|
| `ACTUALIZADO` | Consulta exitosa a GitHub |
| `DESACTUALIZADO` | GitHub o un servicio interno no responde; conserva los datos del último snapshot |
| `NO_DISPONIBLE` | No hay datos previos y la consulta falla, o GitHub devuelve algo inesperado; contadores en `NULL` |
