package com.logitrack.model;

import com.logitrack.audit.AuditableEntity;
import com.logitrack.audit.AuditoriaListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stock de un producto en una bodega determinada. Es la unidad de
 * inventario por bodega y se actualiza automaticamente con cada movimiento.
 */
@Entity
@Table(
        name = "inventario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"bodega_id", "producto_id"})
)
@EntityListeners(AuditoriaListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventario extends BaseEntity implements AuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Transient
    private String auditSnapshot;

    @Override
    public String getEntityName() {
        return "Inventario";
    }
}
