package com.logitrack.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Linea de detalle de un movimiento: un producto y la cantidad movida.
 *
 * No se audita de forma independiente; forma parte del snapshot del
 * {@link Movimiento} al que pertenece.
 */
@Entity
@Table(name = "detalles_movimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleMovimiento extends BaseEntity {

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "movimiento_id", nullable = false)
    private Movimiento movimiento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;
}
