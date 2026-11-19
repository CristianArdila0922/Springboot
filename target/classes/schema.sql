-- =====================================================================
--  LogiTrack - Esquema de base de datos MySQL
--  Script de creacion de la estructura (DDL).
--  Puede ejecutarse manualmente o usarse junto a spring.sql.init.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS logitrack
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE logitrack;

-- ---------------------------------------------------------------------
--  Tabla: usuarios
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)  NOT NULL,
    password        VARCHAR(255) NOT NULL,
    email           VARCHAR(100),
    nombre_completo VARCHAR(100),
    rol             VARCHAR(20)  NOT NULL,
    enabled         TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME,
    updated_at      DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_username (username),
    UNIQUE KEY uk_usuarios_email (email)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  Tabla: bodegas
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bodegas (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100) NOT NULL,
    ubicacion   VARCHAR(200) NOT NULL,
    capacidad   INT          NOT NULL,
    encargado   VARCHAR(100),
    created_at  DATETIME,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bodegas_nombre (nombre)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  Tabla: productos
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS productos (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    nombre     VARCHAR(100)  NOT NULL,
    categoria  VARCHAR(80)   NOT NULL,
    stock      INT           NOT NULL,
    precio     DECIMAL(10,2) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_productos_nombre (nombre)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  Tabla: movimientos
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS movimientos (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    fecha            DATETIME    NOT NULL,
    tipo_movimiento  VARCHAR(20) NOT NULL,
    usuario_id       BIGINT      NOT NULL,
    bodega_origen_id BIGINT,
    bodega_destino_id BIGINT,
    created_at       DATETIME,
    updated_at       DATETIME,
    PRIMARY KEY (id),
    KEY fk_mov_usuario (usuario_id),
    KEY fk_mov_origen (bodega_origen_id),
    KEY fk_mov_destino (bodega_destino_id),
    CONSTRAINT fk_mov_usuario  FOREIGN KEY (usuario_id)        REFERENCES usuarios (id),
    CONSTRAINT fk_mov_origen   FOREIGN KEY (bodega_origen_id)  REFERENCES bodegas (id),
    CONSTRAINT fk_mov_destino  FOREIGN KEY (bodega_destino_id) REFERENCES bodegas (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  Tabla: detalles_movimiento
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalles_movimiento (
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    movimiento_id BIGINT   NOT NULL,
    producto_id   BIGINT   NOT NULL,
    cantidad      INT      NOT NULL,
    created_at    DATETIME,
    updated_at    DATETIME,
    PRIMARY KEY (id),
    KEY fk_det_movimiento (movimiento_id),
    KEY fk_det_producto   (producto_id),
    CONSTRAINT fk_det_movimiento FOREIGN KEY (movimiento_id) REFERENCES movimientos (id) ON DELETE CASCADE,
    CONSTRAINT fk_det_producto   FOREIGN KEY (producto_id)   REFERENCES productos (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  Tabla: inventario (stock por bodega)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventario (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    bodega_id   BIGINT   NOT NULL,
    producto_id BIGINT   NOT NULL,
    cantidad    INT      NOT NULL,
    created_at  DATETIME,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_inv_bodega_producto (bodega_id, producto_id),
    CONSTRAINT fk_inv_bodega   FOREIGN KEY (bodega_id)   REFERENCES bodegas (id),
    CONSTRAINT fk_inv_producto FOREIGN KEY (producto_id) REFERENCES productos (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  Tabla: auditorias
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auditorias (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    tipo_operacion VARCHAR(20) NOT NULL,
    fecha_hora     DATETIME    NOT NULL,
    usuario        VARCHAR(100) NOT NULL,
    entidad        VARCHAR(50) NOT NULL,
    entidad_id     VARCHAR(50),
    valor_anterior TEXT,
    valor_nuevo    TEXT,
    created_at     DATETIME,
    updated_at     DATETIME,
    PRIMARY KEY (id),
    KEY idx_aud_usuario (usuario),
    KEY idx_aud_tipo    (tipo_operacion)
) ENGINE=InnoDB;
