package com.logitrack.repository.projection;

/**
 * Proyeccion para el reporte de productos mas movidos.
 */
public interface ProductoMasMovidoProjection {

    Long getProductoId();

    String getProductoNombre();

    Long getTotal();
}
