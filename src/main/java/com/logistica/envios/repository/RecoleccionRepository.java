package com.logistica.envios.repository;

import com.logistica.envios.model.Recoleccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecoleccionRepository extends JpaRepository<Recoleccion, Long> {

    long countByRemitenteIdRemitente(Long idRemitente);

    // Las guías tienen ancho fijo (GUIA-000001), así que el máximo alfabético es el último consecutivo
    @Query("select max(r.numeroGuia) from Recoleccion r")
    String findUltimoNumeroGuia();
}
