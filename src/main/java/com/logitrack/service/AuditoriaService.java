package com.logitrack.service;

import com.logitrack.model.Auditoria;
import com.logitrack.model.enums.TipoOperacion;
import com.logitrack.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public List<Auditoria> findAll() {
        return auditoriaRepository.findAllByOrderByFechaHoraDesc();
    }

    public List<Auditoria> findByUsuario(String usuario) {
        return auditoriaRepository.findByUsuarioOrderByFechaHoraDesc(usuario);
    }

    public List<Auditoria> findByTipoOperacion(TipoOperacion tipo) {
        return auditoriaRepository.findByTipoOperacionOrderByFechaHoraDesc(tipo);
    }

    public List<Auditoria> findByUsuarioAndTipo(String usuario, TipoOperacion tipo) {
        return auditoriaRepository.findByUsuarioAndTipoOperacionOrderByFechaHoraDesc(usuario, tipo);
    }
}
