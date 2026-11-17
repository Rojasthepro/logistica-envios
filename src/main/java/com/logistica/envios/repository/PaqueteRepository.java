package com.logistica.envios.repository;

import com.logistica.envios.model.EstadoPaquete;
import com.logistica.envios.model.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

    List<Paquete> findByRemitenteIdRemitenteAndEstadoAndRecoleccionIsNullOrderByIdPaqueteAsc(Long idRemitente, EstadoPaquete estado);

    boolean existsByRemitenteIdRemitente(Long idRemitente);
}
