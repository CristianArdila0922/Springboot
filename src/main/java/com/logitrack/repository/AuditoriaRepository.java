package com.logitrack.repository;

import com.logitrack.model.Auditoria;
import com.logitrack.model.enums.TipoOperacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findAllByOrderByFechaHoraDesc();

    List<Auditoria> findByUsuarioOrderByFechaHoraDesc(String usuario);

    List<Auditoria> findByTipoOperacionOrderByFechaHoraDesc(TipoOperacion tipo);

    List<Auditoria> findByUsuarioAndTipoOperacionOrderByFechaHoraDesc(String usuario, TipoOperacion tipo);
}
