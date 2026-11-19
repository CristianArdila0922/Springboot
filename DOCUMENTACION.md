# 📘 LogiTrack — Documentación Técnica

Documento explicativo del sistema **LogiTrack**: arquitectura, diagrama de clases,
flujo de autenticación JWT y funcionamiento de la auditoría automática.

---

## 1. 🏗️ Descripción de la arquitectura

LogiTrack sigue una **arquitectura en capas** clásica de Spring Boot, con un canal de
seguridad basado en JWT y un mecanismo de auditoría transversal basado en *listeners*
de JPA.

```
                     ┌──────────────────────────────┐
                     │          Frontend            │
                     │   (HTML / CSS / JS vanilla)  │
                     └───────────────┬──────────────┘
                             HTTP + JWT (Authorization)
                             ▼
 ┌───────────────────────────────────────────────────────────────────┐
 │                     Capa de Seguridad (security/)                 │
 │  JwtAuthenticationFilter → SecurityContext → @PreAuthorize (roles)│
 └───────────────┬───────────────────────────────────┬───────────────┘
                 ▼                                   ▼
 ┌──────────────────────────┐         ┌──────────────────────────────┐
 │   Capa Controller (REST)  │         │   Capa Audit (audit/)        │
 │  /auth /bodegas /productos│         │  AuditoriaListener (JPA)     │
 │  /movimientos /auditorias │         │  → AuditEvent → AuditListener │
 │  /reportes                │         │  → AuditoriaRepository        │
 └───────────────┬──────────┘         └───────────────┬──────────────┘
                 ▼                                     ▼
 ┌──────────────────────────────────────────────────────────────────┐
 │                      Capa Service (service/)                     │
 │   AuthService, BodegaService, ProductoService, MovimientoService,│
 │   AuditoriaService, ReporteService                               │
 └───────────────┬──────────────────────────────────────────────────┘
                 ▼
 ┌──────────────────────────────────────────────────────────────────┐
 │                  Capa Repository (repository/)                   │
 │       Spring Data JPA (JpaRepository + consultas JPQL)           │
 └───────────────┬──────────────────────────────────────────────────┘
                 ▼
 ┌──────────────────────────────────────────────────────────────────┐
 │            Capa Model (model/) + Base de datos MySQL             │
 │  Usuario, Bodega, Producto, Movimiento, DetalleMovimiento,      │
 │  Inventario, Auditoria                                          │
 └──────────────────────────────────────────────────────────────────┘
```

**Responsabilidades por capa**

- **Controller**: expone los endpoints REST, valida la entrada (`@Valid`) y devuelve
  las respuestas. No contiene lógica de negocio.
- **Service**: orquesta la lógica de negocio y las transacciones (`@Transactional`).
- **Repository**: acceso a datos con Spring Data JPA y consultas JPQL personalizadas.
- **Model**: entidades JPA que representan el modelo de dominio.
- **Security**: filtro JWT, codificador de contraseñas (BCrypt) y configuración de
  reglas de autorización por rol.
- **Audit**: listener de JPA que captura cambios en entidades auditadas y los persiste
  como registros de auditoría de forma desacoplada.

---

## 2. 🧩 Diagrama de clases

```mermaid
classDiagram
    class BaseEntity {
        +Long id
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }
    class Usuario {
        +String username
        -String password
        +String email
        +String nombreCompleto
        +Rol rol
        +boolean enabled
    }
    class Bodega {
        +String nombre
        +String ubicacion
        +Integer capacidad
        +String encargado
    }
    class Producto {
        +String nombre
        +String categoria
        +Integer stock
        +BigDecimal precio
    }
    class Movimiento {
        +LocalDateTime fecha
        +TipoMovimiento tipoMovimiento
        +addDetalle(DetalleMovimiento)
    }
    class DetalleMovimiento {
        +Integer cantidad
    }
    class Inventario {
        +Integer cantidad
    }
    class Auditoria {
        +TipoOperacion tipoOperacion
        +LocalDateTime fechaHora
        +String usuario
        +String entidad
        +String entidadId
        +String valorAnterior
        +String valorNuevo
    }

    class Rol {
        <<enum>>
        ADMIN
        EMPLEADO
    }
    class TipoMovimiento {
        <<enum>>
        ENTRADA
        SALIDA
        TRANSFERENCIA
    }
    class TipoOperacion {
        <<enum>>
        INSERT
        UPDATE
        DELETE
    }

    BaseEntity <|-- Usuario
    BaseEntity <|-- Bodega
    BaseEntity <|-- Producto
    BaseEntity <|-- Movimiento
    BaseEntity <|-- DetalleMovimiento
    BaseEntity <|-- Inventario
    BaseEntity <|-- Auditoria

    Usuario --> Rol
    Movimiento --> TipoMovimiento
    Auditoria --> TipoOperacion

    Movimiento "1" --> "1" Usuario : responsable
    Movimiento "1" --> "0..1" Bodega : origen
    Movimiento "1" --> "0..1" Bodega : destino
    Movimiento "1" *-- "1..*" DetalleMovimiento : contiene
    DetalleMovimiento "* --> "1" Producto
    Inventario "*" --> "1" Bodega
    Inventario "*" --> "1" Producto
```

> Las entidades `Bodega`, `Producto`, `Movimiento` e `Inventario` implementan la
> interfaz `AuditableEntity` y declaran `@EntityListeners(AuditoriaListener.class)`
> para ser auditadas automáticamente.

---

## 3. 🗃️ Modelo entidad-relación

```mermaid
erDiagram
    usuarios ||--o{ movimientos : "registra"
    bodegas ||--o{ movimientos : "origen"
    bodegas ||--o{ movimientos : "destino"
    bodegas ||--o{ inventario : "almacena"
    productos ||--o{ inventario : "almacenado en"
    productos ||--o{ detalles_movimiento : "movido en"
    movimientos ||--o{ detalles_movimiento : "tiene"

    usuarios {
        BIGINT id PK
        VARCHAR username
        VARCHAR password
        VARCHAR rol
    }
    bodegas {
        BIGINT id PK
        VARCHAR nombre
        VARCHAR ubicacion
        INT capacidad
    }
    productos {
        BIGINT id PK
        VARCHAR nombre
        VARCHAR categoria
        INT stock
        DECIMAL precio
    }
    movimientos {
        BIGINT id PK
        DATETIME fecha
        VARCHAR tipo_movimiento
        BIGINT usuario_id FK
        BIGINT bodega_origen_id FK
        BIGINT bodega_destino_id FK
    }
    detalles_movimiento {
        BIGINT id PK
        BIGINT movimiento_id FK
        BIGINT producto_id FK
        INT cantidad
    }
    inventario {
        BIGINT id PK
        BIGINT bodega_id FK
        BIGINT producto_id FK
        INT cantidad
    }
    auditorias {
        BIGINT id PK
        VARCHAR tipo_operacion
        DATETIME fecha_hora
        VARCHAR usuario
        VARCHAR entidad
        VARCHAR entidad_id
        TEXT valor_anterior
        TEXT valor_nuevo
    }
```

---

## 4. 🔐 Autenticación con JWT

### Flujo de autenticación

```mermaid
sequenceDiagram
    participant C as Cliente (Frontend)
    participant F as JwtAuthenticationFilter
    participant S as AuthService / AuthController
    participant U as UsuarioRepository
    participant M as JwtUtil

    C->>S: POST /auth/login {username, password}
    S->>U: findByUsername(username)
    U-->>S: Usuario (BCrypt)
    S->>S: passwordEncoder.matches(...)
    S->>M: generateToken(username, rol)
    M-->>S: JWT firmado (HS256)
    S-->>C: { token, type:"Bearer", username, rol }

    Note over C: Guarda el token en localStorage

    C->>F: GET /bodegas  (Header: Authorization: Bearer <token>)
    F->>M: validateToken(token)
    F->>U: loadUserByUsername(subject)
    F->>F: SecurityContext.setAuthentication(...)
    F->>S: petición autenticada
    S-->>C: 200 OK + datos
```

### Estructura del token JWT

Un JWT tiene tres partes separadas por `.`: **header**.**payload**.**signature**.

**Header** (decodificado):
```json
{ "alg": "HS256", "typ": "JWT" }
```

**Payload** (decodificado):
```json
{
  "sub": "admin",
  "rol": "ADMIN",
  "iat": 1731400000,
  "exp": 1731486400
}
```
- `sub`: nombre de usuario (subject).
- `rol`: rol del usuario (claim personalizada).
- `iat` / `exp`: emisión y vencimiento (24 h por defecto).

**Signature**:
```
HMAC-SHA256( base64(header) + "." + base64(payload), <jwt.secret> )
```

### Ejemplo de uso del token

```http
GET /bodegas HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbCI6IkFETUlOIiwiaWF0IjoxNzMxNDAwMDAwLCJleHAiOjE3MzE0ODY0MDB9.s5aP9pK...rQ
```

> En Swagger UI, usa el botón **Authorize** y pega `Bearer <token>`.

---

## 5. 🧾 Auditoría automática

La auditoría registra automáticamente cada `INSERT`, `UPDATE` y `DELETE` sobre las
entidades auditadas (`Bodega`, `Producto`, `Movimiento`, `Inventario`).

### ¿Cómo funciona?

1. **`AuditableEntity`** (interfaz): la implementan las entidades auditadas; exponen un
   nombre lógico y un *snapshot* JSON de su estado.
2. **`AuditoriaListener`** (listener JPA con `@EntityListeners`):
   - `@PostLoad` → guarda un snapshot del estado cargado (valores anteriores).
   - `@PostPersist` → publica un evento `INSERT` (valor nuevo = estado actual).
   - `@PostUpdate` → publica un evento `UPDATE` (anterior = snapshot, nuevo = actual).
   - `@PostRemove` → publica un evento `DELETE` (anterior = estado eliminado).
3. **`AuditEvent`** + **`AuditEventListener`**: el listener publica un evento de Spring
   que se procesa en `@TransactionalEventListener(AFTER_COMMIT)` dentro de una transacción
   nueva (`REQUIRES_NEW`). Así se evita un *flush* anidado y se garantiza que la
   auditoría se escriba **después** de confirmar el cambio original.
4. El usuario responsable se obtiene del contexto de seguridad (`SecurityContextHolder`)
   en el momento de publicar el evento.

### Registro resultante (tabla `auditorias`)

| campo | ejemplo |
|-------|---------|
| tipo_operacion | `UPDATE` |
| fecha_hora | `2026-11-12 20:15:30` |
| usuario | `empleado` |
| entidad | `Producto` |
| entidad_id | `2` |
| valor_anerior | `{"id":2,"nombre":"Mouse","stock":100,...}` |
| valor_nuevo | `{"id":2,"nombre":"Mouse","stock":50,...}` |

### Ventajas del enfoque

- **Desacoplado**: el código de negocio no contiene llamadas explícitas de auditoría.
- **Transaccional**: la auditoría no interfiere con el *flush* de JPA.
- **Trazable**: cada cambio queda con usuario, fecha y valores antes/después.

---

## 6. 🔄 Reglas de stock por tipo de movimiento

| Tipo | Bodega origen | Bodega destino | Stock por bodega | Stock global producto |
|------|---------------|----------------|-------------------|-----------------------|
| `ENTRADA` | opcional | obligatoria | destino **+ cantidad** | **+ cantidad** |
| `SALIDA` | obligatoria | opcional | origen **− cantidad** | **− cantidad** |
| `TRANSFERENCIA` | obligatoria | obligatoria (distinta) | origen **−** / destino **+** | sin cambio |

Si un ajuste deja stock negativo se lanza `BadRequestException` (400) y la transacción
se revierte completa.
