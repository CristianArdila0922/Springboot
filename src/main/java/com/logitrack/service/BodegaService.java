package com.logitrack.service;

import com.logitrack.dto.BodegaRequest;
import com.logitrack.exception.BadRequestException;
import com.logitrack.exception.ResourceNotFoundException;
import com.logitrack.model.Bodega;
import com.logitrack.repository.BodegaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BodegaService {

    private final BodegaRepository bodegaRepository;

    @Transactional(readOnly = true)
    public List<Bodega> findAll() {
        return bodegaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Bodega findById(Long id) {
        return bodegaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada con id: " + id));
    }

    public Bodega create(BodegaRequest request) {
        if (bodegaRepository.existsByNombre(request.nombre())) {
            throw new BadRequestException("Ya existe una bodega con el nombre: " + request.nombre());
        }
        Bodega bodega = new Bodega();
        bodega.setNombre(request.nombre());
        bodega.setUbicacion(request.ubicacion());
        bodega.setCapacidad(request.capacidad());
        bodega.setEncargado(request.encargado());
        return bodegaRepository.save(bodega);
    }

    public Bodega update(Long id, BodegaRequest request) {
        Bodega bodega = findById(id);
        if (!bodega.getNombre().equals(request.nombre())
                && bodegaRepository.existsByNombre(request.nombre())) {
            throw new BadRequestException("Ya existe una bodega con el nombre: " + request.nombre());
        }
        bodega.setNombre(request.nombre());
        bodega.setUbicacion(request.ubicacion());
        bodega.setCapacidad(request.capacidad());
        bodega.setEncargado(request.encargado());
        return bodegaRepository.save(bodega);
    }

    public void delete(Long id) {
        Bodega bodega = findById(id);
        bodegaRepository.delete(bodega);
    }
}
