-- =====================================================================
--  LogiTrack - Datos iniciales (seed)
--  Se ejecuta de forma idempotente (INSERT IGNORE) para no duplicar.
--  NOTA: los usuarios (que requieren contrasena cifrada con BCrypt) se
--  crean automaticamente por DataInitializer al arrancar la app:
--     admin / admin123        (rol ADMIN)
--     empleado / empleado123  (rol EMPLEADO)
-- =====================================================================

USE logitrack;

-- Bodegas
INSERT IGNORE INTO bodegas (id, nombre, ubicacion, capacidad, encargado, created_at, updated_at) VALUES
    (1, 'Bodega Central',   'Quito - Av. Amazonas N34-451', 5000, 'Carlos Andrade',  NOW(), NOW()),
    (2, 'Bodega Guayaquil',  'Guayaquil - Av. Francisco de Orellana', 8000, 'Maria Lopez', NOW(), NOW()),
    (3, 'Bodega Cuenca',     'Cuenca - Calle Larga 12-30',   3000, 'Pedro Castillo', NOW(), NOW());

-- Productos (3 y 5 tienen stock bajo para probar el reporte)
INSERT IGNORE INTO productos (id, nombre, categoria, stock, precio, created_at, updated_at) VALUES
    (1, 'Laptop Dell Latitude 5440', 'Computacion',  50,   850.00, NOW(), NOW()),
    (2, 'Mouse Logitech M190',       'Accesorios',  200,    15.50, NOW(), NOW()),
    (3, 'Teclado Mecanico Redragon', 'Accesorios',    8,    45.00, NOW(), NOW()),
    (4, 'Monitor Samsung 24" FHD',  'Computacion',  30,   180.00, NOW(), NOW()),
    (5, 'Silla Ergonomica Exec',     'Mobiliario',    5,   120.00, NOW(), NOW());

-- Inventario (stock por bodega; suma igual al stock global del producto)
INSERT IGNORE INTO inventario (id, bodega_id, producto_id, cantidad, created_at, updated_at) VALUES
    (1, 1, 1, 20,  NOW(), NOW()),
    (2, 1, 2, 100, NOW(), NOW()),
    (3, 1, 3, 8,   NOW(), NOW()),
    (4, 2, 1, 30,  NOW(), NOW()),
    (5, 2, 4, 30,  NOW(), NOW()),
    (6, 3, 5, 5,   NOW(), NOW()),
    (7, 3, 2, 100, NOW(), NOW());
