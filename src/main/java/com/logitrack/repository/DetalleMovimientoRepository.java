package com.logitrack.repository;

import com.logitrack.model.DetalleMovimiento;
import com.logitrack.repository.projection.ProductoMasMovidoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Long> {

    /** Productos mas movidos segun el total de unidades movidas. */
    @Query("SELECT dm.producto.id AS productoId, dm.producto.nombre AS productoNombre, "
            + "COALESCE(SUM(dm.cantidad), 0) AS total "
            + "FROM DetalleMovimiento dm "
            + "GROUP BY dm.producto.id, dm.producto.nombre "
            + "ORDER BY total DESC")
    List<ProductoMasMovidoProjection> findProductosMasMovidos();
}
