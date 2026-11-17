package com.logistica.envios.repository;

import com.logistica.envios.model.Remitente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RemitenteRepository extends JpaRepository<Remitente, Long> {

    Page<Remitente> findByNombreContainingIgnoreCaseOrDocumentoContaining(String nombre, String documento, Pageable pageable);

    List<Remitente> findTop10ByNombreContainingIgnoreCaseOrDocumentoContainingOrderByNombreAsc(String nombre, String documento);

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdRemitenteNot(String documento, Long idRemitente);
}
