# 📦 LogiTrack — Sistema de Gestión y Auditoría de Bodegas

**LogiTrack S.A.** administra varias bodegas distribuidas en distintas ciudades. Este
proyecto es un **backend centralizado en Spring Boot** que permite controlar los
movimientos de inventario entre bodegas, registrar automáticamente los cambios
(auditoría) y proteger la información con **autenticación JWT**.

---

## 🎯 Objetivo

Desarrollar un sistema de gestión y auditoría de bodegas que permita registrar
transacciones de inventario (entradas, salidas y transferencias) y generar reportes
auditables de los cambios realizados por cada usuario.

---

## ✨ Características principales

- **Gestión de bodegas** y **productos** (CRUD completo).
- **Movimientos de inventario**: `ENTRADA`, `SALIDA` y `TRANSFERENCIA`, con ajuste
  automático del stock por bodega y del stock global del producto.
- **Auditoría automática** mediante *listeners* de JPA (`@EntityListeners`): registra
  `INSERT`, `UPDATE` y `DELETE` con valores anteriores/nuevos, fecha y usuario
  responsable.
- **Seguridad con Spring Security + JWT** con roles `ADMIN` y `EMPLEADO`.
- **Consultas avanzadas**: productos con stock bajo, movimientos por rango de fechas,
  auditorías por usuario/tipo de operación y reporte general.
- **Documentación OpenAPI 3** (Swagger UI).
- **Manejo global de errores** con `@ControllerAdvice` y validaciones con Jakarta
  Validation (`@NotNull`, `@Size`, `@Min`, ...).
- **Frontend básico** en HTML/CSS/JS que consume la API.

---

## 🧰 Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | MySQL 8 |
| Seguridad | Spring Security 6 + JWT (jjwt 0.12) |
| Documentación | springdoc-openapi 2.3 (Swagger UI) |
| Build | Maven |
| Frontend | HTML5 + CSS3 + JavaScript (vanilla) |

---

## 📁 Estructura del proyecto

```
Proyecto/
├─ pom.xml
├─ mvnw / mvnw.cmd / .mvn          # Maven Wrapper
├─ schema.sql                      # Script de creación de la BD
├─ data.sql                        # Datos iniciales (bodegas, productos, inventario)
├─ frontend/                       # Frontend entregable (HTML/CSS/JS)
│   ├─ index.html
│   ├─ css/style.css
│   └─ js/app.js
├─ src/main/resources/
│   ├─ application.properties
│   ├─ schema.sql
│   ├─ data.sql
│   └─ static/                     # Copia del frontend servida por la app
└─ src/main/java/com/logitrack/
    ├─ LogiTrackApplication.java
    ├─ config/        # OpenApiConfig, DataInitializer, SpringContextHolder
    ├─ security/      # JWT, Spring Security, UserDetailsService
    ├─ model/         # Entidades JPA + enums
    ├─ repository/    # Spring Data JPA + proyecciones
    ├─ service/       # Lógica de negocio
    ├─ controller/    # Endpoints REST
    ├─ dto/           # Objetos de transferencia (request/response)
    ├─ audit/         # Listener de JPA + evento de auditoría
    └─ exception/     # Excepciones y manejo global
```

---

## ✅ Requisitos previos

- **JDK 17 o superior** (probado con Java 21).
- **MySQL 8** corriendo en `localhost:3306`.
- **Maven** (o usar el Maven Wrapper incluido: `mvnw.cmd`).

---

## 🚀 Instalación y ejecución

### 1. Configurar la base de datos

Edita las credenciales en `src/main/resources/application.properties` si tu usuario/clave
de MySQL no es `root/root`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/logitrack?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```

> La base de datos `logitrack` se crea automáticamente (`createDatabaseIfNotExist=true`).
> Con `ddl-auto=update`, Hibernate crea las tablas al arrancar; `data.sql` inserta datos
> de ejemplo y `DataInitializer` crea los usuarios con la contraseña cifrada (BCrypt).

### 2. Compilar y ejecutar

```bash
# Con Maven Wrapper (Windows)
.\mvnw.cmd spring-boot:run

# O empaquetar y ejecutar el JAR
.\mvnw.cmd clean package
java -jar target/logitrack-1.0.0.jar
```

La aplicación queda disponible en **http://localhost:8080**.

### 3. Usuarios por defecto

| Usuario   | Contraseña  | Rol      |
|-----------|-------------|----------|
| `admin`   | `admin123`  | ADMIN    |
| `empleado`| `empleado123`| EMPLEADO |

---

## 📚 Documentación Swagger

Una vez levantada la app, abre:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

Para probar endpoints protegidos: clic en el botón **Authorize** 🔒 y pega el token JWT
recibido al iniciar sesión (con el prefijo `Bearer `).

---

## 🌐 Frontend

Abre **http://localhost:8080/** en el navegador (la app sirve el frontend desde
`static/`). También puedes abrir el archivo `frontend/index.html` directamente.

Permite: login/registro, listar y crear bodegas y productos, registrar movimientos,
ver stock bajo, consultar auditoría y ver reportes.

---

## 🔌 Endpoints de la API

### Autenticación (públicos)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/auth/login` | Inicia sesión y devuelve el JWT |
| POST | `/auth/register` | Registra un usuario (rol por defecto EMPLEADO) |

### Bodegas (protegidas)
| Método | Ruta | Rol |
|--------|------|-----|
| GET | `/bodegas` | autenticado |
| GET | `/bodegas/{id}` | autenticado |
| POST | `/bodegas` | autenticado |
| PUT | `/bodegas/{id}` | autenticado |
| DELETE | `/bodegas/{id}` | ADMIN |

### Productos (protegidas)
| Método | Ruta | Rol |
|--------|------|-----|
| GET | `/productos` | autenticado |
| GET | `/productos/{id}` | autenticado |
| GET | `/productos/stock-bajo?min=10` | autenticado |
| POST | `/productos` | autenticado |
| PUT | `/productos/{id}` | autenticado |
| DELETE | `/productos/{id}` | ADMIN |

### Movimientos (protegidas)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/movimientos` | Lista todos |
| GET | `/movimientos/{id}` | Obtiene uno |
| GET | `/movimientos/fecha?inicio=...&fin=...` | Por rango de fechas (BETWEEN) |
| GET | `/movimientos/tipo/{tipo}` | Por tipo (ENTRADA/SALIDA/TRANSFERENCIA) |
| POST | `/movimientos` | Registra un movimiento (ajusta stock) |

### Auditoría (protegidas)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/auditorias` | Lista (filtros opcionales `?usuario=` y `?tipo=`) |

### Reportes (protegidas)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/reportes/stock-bodega` | Stock total por bodega |
| GET | `/reportes/productos-movidos` | Productos más movidos |
| GET | `/reportes/general` | Reporte general en JSON |

---

## 🔍 Ejemplos de uso (cURL)

### 1) Iniciar sesión
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
Respuesta:
```json
{"token":"eyJhbGciOi...","type":"Bearer","username":"admin","rol":"ADMIN","userId":1}
```

### 2) Listar bodegas (con token)
```bash
curl http://localhost:8080/bodegas -H "Authorization: Bearer eyJhbGciOi..."
```

### 3) Registrar un movimiento
```bash
curl -X POST http://localhost:8080/movimientos \
  -H "Authorization: Bearer eyJhbGciOi..." \
  -H "Content-Type: application/json" \
  -d '{
    "tipoMovimiento":"ENTRADA",
    "bodegaDestinoId":1,
    "detalles":[{"productoId":2,"cantidad":50}]
  }'
```

### 4) Reporte general
```bash
curl http://localhost:8080/reportes/general -H "Authorization: Bearer eyJhbGciOi..."
```

---

## 🔒 Códigos de error

| Código | Significado |
|--------|-------------|
| 400 | Solicitud inválida / error de validación |
| 401 | No autorizado (token ausente o inválido) |
| 403 | Prohibido (sin permisos de rol) |
| 404 | Recurso no encontrado |
| 500 | Error interno del servidor |

Las respuestas de error siguen el formato:
```json
{
  "status": 404,
  "error": "No encontrado",
  "mensaje": "Bodega no encontrada con id: 99",
  "path": "/bodegas/99",
  "timestamp": "2026-11-12T20:10:00"
}
```

---

## 🗂️ Scripts SQL

- `schema.sql` — crea la estructura completa de la base de datos (DDL).
- `data.sql` — inserta bodegas, productos e inventario de ejemplo.

---

## 📝 Documentación adicional

Ver **`DOCUMENTACION.md`** para el diagrama de clases, la descripción de arquitectura,
el flujo de autenticación JWT y un ejemplo de token decodificado.
