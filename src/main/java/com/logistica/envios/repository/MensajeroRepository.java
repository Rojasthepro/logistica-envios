package com.logistica.envios.repository;

import com.logistica.envios.model.EstadoMensajero;
import com.logistica.envios.model.Mensajero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeroRepository extends JpaRepository<Mensajero, Long> {

    List<Mensajero> findByEstadoOrderByNombreAsc(EstadoMensajero estado);
}
