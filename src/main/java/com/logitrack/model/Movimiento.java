package com.logitrack.model;

import com.logitrack.audit.AuditableEntity;
import com.logitrack.audit.AuditoriaListener;
import com.logitrack.model.enums.TipoMovimiento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Movimiento de inventario (ENTRADA, SALIDA o TRANSFERENCIA).
 *
 * Guarda el usuario responsable, la bodega origen/destino y el listado
 * de productos con sus cantidades (ver {@link DetalleMovimiento}).
 */
@Entity
@Table(name = "movimientos")
@EntityListeners(AuditoriaListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movimiento extends BaseEntity implements AuditableEntity {

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    private TipoMovimiento tipoMovimiento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "bodega_origen_id")
    private Bodega bodegaOrigen;

    @ManyToOne
    @JoinColumn(name = "bodega_destino_id")
    private Bodega bodegaDestino;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleMovimiento> detalles = new ArrayList<>();

    @Transient
    private String auditSnapshot;

    /** Agrega una linea de detalle manteniendo la relacion bidireccional. */
    public void addDetalle(DetalleMovimiento detalle) {
        detalle.setMovimiento(this);
        detalles.add(detalle);
    }

    @Override
    public String getEntityName() {
        return "Movimiento";
    }
}
