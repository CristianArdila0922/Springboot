package com.logitrack.repository;

import com.logitrack.model.Inventario;
import com.logitrack.repository.projection.ProductoMasMovidoProjection;
import com.logitrack.repository.projection.StockPorBodegaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByBodegaIdAndProductoId(Long bodegaId, Long productoId);

    List<Inventario> findByBodegaId(Long bodegaId);

    /** Stock total (suma de cantidades) agrupado por bodega. */
    @Query("SELECT i.bodega.id AS bodegaId, i.bodega.nombre AS bodegaNombre, "
            + "COALESCE(SUM(i.cantidad), 0) AS total "
            + "FROM Inventario i GROUP BY i.bodega.id, i.bodega.nombre "
            + "ORDER BY total DESC")
    List<StockPorBodegaProjection> findStockTotalPorBodega();
}
