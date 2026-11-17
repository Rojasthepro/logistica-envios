package com.logistica.envios.repository;

import com.logistica.envios.model.Destinatario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinatarioRepository extends JpaRepository<Destinatario, Long> {
}
