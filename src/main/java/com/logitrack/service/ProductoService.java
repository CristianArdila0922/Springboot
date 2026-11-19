package com.logitrack.service;

import com.logitrack.dto.ProductoRequest;
import com.logitrack.exception.BadRequestException;
import com.logitrack.exception.ResourceNotFoundException;
import com.logitrack.model.Producto;
import com.logitrack.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Producto findById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    /** Productos con stock menor al minimo indicado. */
    @Transactional(readOnly = true)
    public List<Producto> findStockBajo(int min) {
        return productoRepository.findByStockLessThanOrderByStockAsc(min);
    }

    public Producto create(ProductoRequest request) {
        if (productoRepository.existsByNombre(request.nombre())) {
            throw new BadRequestException("Ya existe un producto con el nombre: " + request.nombre());
        }
        Producto producto = new Producto();
        producto.setNombre(request.nombre());
        producto.setCategoria(request.categoria());
        producto.setStock(request.stock());
        producto.setPrecio(request.precio());
        return productoRepository.save(producto);
    }

    public Producto update(Long id, ProductoRequest request) {
        Producto producto = findById(id);
        if (!producto.getNombre().equals(request.nombre())
                && productoRepository.existsByNombre(request.nombre())) {
            throw new BadRequestException("Ya existe un producto con el nombre: " + request.nombre());
        }
        producto.setNombre(request.nombre());
        producto.setCategoria(request.categoria());
        producto.setStock(request.stock());
        producto.setPrecio(request.precio());
        return productoRepository.save(producto);
    }

    public void delete(Long id) {
        Producto producto = findById(id);
        productoRepository.delete(producto);
    }
}
