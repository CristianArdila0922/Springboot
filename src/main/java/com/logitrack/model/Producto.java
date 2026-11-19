package com.logitrack.model;

import com.logitrack.audit.AuditableEntity;
import com.logitrack.audit.AuditoriaListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Producto almacenado en las bodegas. El campo {@code stock} representa
 * el stock global (suma de todas las bodegas); el stock por bodega se
 * gestiona en la entidad {@link Inventario}.
 */
@Entity
@Table(name = "productos")
@EntityListeners(AuditoriaListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto extends BaseEntity implements AuditableEntity {

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private BigDecimal precio;

    @Transient
    private String auditSnapshot;

    @Override
    public String getEntityName() {
        return "Producto";
    }
}
