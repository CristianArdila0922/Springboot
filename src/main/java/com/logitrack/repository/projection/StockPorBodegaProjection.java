package com.logitrack.repository.projection;

/**
 * Proyeccion para el reporte de stock total por bodega.
 */
public interface StockPorBodegaProjection {

    Long getBodegaId();

    String getBodegaNombre();

    Long getTotal();
}
