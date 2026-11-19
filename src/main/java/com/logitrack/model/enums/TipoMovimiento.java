package com.logitrack.model.enums;

/**
 * Tipos de movimiento de inventario.
 *
 * - ENTRADA:       ingresa mercaderia a una bodega (aumenta stock).
 * - SALIDA:        sale mercaderia de una bodega (disminuye stock).
 * - TRANSFERENCIA: traslado de mercaderia entre dos bodegas.
 */
public enum TipoMovimiento {
    ENTRADA,
    SALIDA,
    TRANSFERENCIA
}
