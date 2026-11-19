package com.logitrack.repository;

import com.logitrack.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByNombre(String nombre);

    /** Productos con stock menor al minimo indicado (stock bajo). */
    List<Producto> findByStockLessThanOrderByStockAsc(Integer min);

    @Query("SELECT p FROM Producto p WHERE p.stock < :min ORDER BY p.stock ASC")
    List<Producto> findStockBajo(@Param("min") int min);
}
