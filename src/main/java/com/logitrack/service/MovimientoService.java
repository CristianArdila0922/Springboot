package com.logitrack.service;

import com.logitrack.dto.DetalleMovimientoRequest;
import com.logitrack.dto.MovimientoRequest;
import com.logitrack.exception.BadRequestException;
import com.logitrack.exception.ResourceNotFoundException;
import com.logitrack.model.Bodega;
import com.logitrack.model.DetalleMovimiento;
import com.logitrack.model.Inventario;
import com.logitrack.model.Movimiento;
import com.logitrack.model.Producto;
import com.logitrack.model.Usuario;
import com.logitrack.model.enums.TipoMovimiento;
import com.logitrack.repository.BodegaRepository;
import com.logitrack.repository.InventarioRepository;
import com.logitrack.repository.MovimientoRepository;
import com.logitrack.repository.ProductoRepository;
import com.logitrack.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Logica de movimientos de inventario. Cada movimiento ajusta el stock por
 * bodega (tabla inventario) y el stock global del producto, validando que no
 * quede stock negativo.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioRepository inventarioRepository;

    @Transactional(readOnly = true)
    public List<Movimiento> findAll() {
        return movimientoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Movimiento findById(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Movimiento> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
    }

    @Transactional(readOnly = true)
    public List<Movimiento> findByTipo(TipoMovimiento tipo) {
        return movimientoRepository.findByTipoMovimientoOrderByFechaDesc(tipo);
    }

    @Transactional(readOnly = true)
    public List<Movimiento> findByUsuario(Long usuarioId) {
        return movimientoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    /**
     * Registra un movimiento y aplica los cambios de stock correspondientes.
     *
     * @param request  datos del movimiento
     * @param username usuario responsable (empleado autenticado)
     */
    public Movimiento registrar(MovimientoRequest request, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        TipoMovimiento tipo = request.tipoMovimiento();
        Bodega origen = request.bodegaOrigenId() != null ? buscarBodega(request.bodegaOrigenId()) : null;
        Bodega destino = request.bodegaDestinoId() != null ? buscarBodega(request.bodegaDestinoId()) : null;
        validarBodegas(tipo, origen, destino);

        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setTipoMovimiento(tipo);
        movimiento.setUsuario(usuario);
        movimiento.setBodegaOrigen(origen);
        movimiento.setBodegaDestino(destino);

        for (DetalleMovimientoRequest detalleReq : request.detalles()) {
            Producto producto = productoRepository.findById(detalleReq.productoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con id: " + detalleReq.productoId()));

            DetalleMovimiento detalle = new DetalleMovimiento();
            detalle.setProducto(producto);
            detalle.setCantidad(detalleReq.cantidad());
            movimiento.addDetalle(detalle);

            aplicarMovimiento(tipo, origen, destino, producto, detalleReq.cantidad());
        }

        return movimientoRepository.save(movimiento);
    }

    private void validarBodegas(TipoMovimiento tipo, Bodega origen, Bodega destino) {
        switch (tipo) {
            case ENTRADA -> {
                if (destino == null) {
                    throw new BadRequestException("La ENTRADA requiere una bodega destino");
                }
            }
            case SALIDA -> {
                if (origen == null) {
                    throw new BadRequestException("La SALIDA requiere una bodega origen");
                }
            }
            case TRANSFERENCIA -> {
                if (origen == null || destino == null) {
                    throw new BadRequestException("La TRANSFERENCIA requiere bodega origen y destino");
                }
                if (origen.getId().equals(destino.getId())) {
                    throw new BadRequestException("La bodega origen y destino deben ser diferentes");
                }
            }
        }
    }

    private void aplicarMovimiento(TipoMovimiento tipo, Bodega origen, Bodega destino, Producto producto, int cantidad) {
        switch (tipo) {
            case ENTRADA -> {
                ajustarInventario(destino, producto, cantidad);
                producto.setStock(producto.getStock() + cantidad);
            }
            case SALIDA -> {
                ajustarInventario(origen, producto, -cantidad);
                producto.setStock(producto.getStock() - cantidad);
            }
            case TRANSFERENCIA -> {
                ajustarInventario(origen, producto, -cantidad);
                ajustarInventario(destino, producto, cantidad);
                // El stock global del producto no cambia en una transferencia
            }
        }
    }

    private void ajustarInventario(Bodega bodega, Producto producto, int delta) {
        Inventario inventario = inventarioRepository
                .findByBodegaIdAndProductoId(bodega.getId(), producto.getId())
                .orElseGet(() -> {
                    Inventario nuevo = new Inventario();
                    nuevo.setBodega(bodega);
                    nuevo.setProducto(producto);
                    nuevo.setCantidad(0);
                    return nuevo;
                });
        int nuevaCantidad = inventario.getCantidad() + delta;
        if (nuevaCantidad < 0) {
            throw new BadRequestException("Stock insuficiente de '" + producto.getNombre()
                    + "' en la bodega '" + bodega.getNombre() + "'");
        }
        inventario.setCantidad(nuevaCantidad);
        inventarioRepository.save(inventario);
    }

    private Bodega buscarBodega(Long id) {
        return bodegaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada con id: " + id));
    }
}
