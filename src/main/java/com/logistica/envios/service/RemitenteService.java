package com.logistica.envios.service;

import com.logistica.envios.dto.RemitenteResumen;
import com.logistica.envios.exception.NegocioException;
import com.logistica.envios.model.Remitente;
import com.logistica.envios.repository.PaqueteRepository;
import com.logistica.envios.repository.RecoleccionRepository;
import com.logistica.envios.repository.RemitenteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RemitenteService {

    public static final int TAMANO_PAGINA = 10;

    private final RemitenteRepository remitenteRepository;
    private final RecoleccionRepository recoleccionRepository;
    private final PaqueteRepository paqueteRepository;

    public RemitenteService(RemitenteRepository remitenteRepository, RecoleccionRepository recoleccionRepository,
                            PaqueteRepository paqueteRepository) {
        this.remitenteRepository = remitenteRepository;
        this.recoleccionRepository = recoleccionRepository;
        this.paqueteRepository = paqueteRepository;
    }

    @Transactional(readOnly = true)
    public Page<Remitente> listar(String texto, int pagina) {
        String filtro = texto == null ? "" : texto.trim();
        PageRequest pageRequest = PageRequest.of(Math.max(pagina, 0), TAMANO_PAGINA, Sort.by("nombre"));
        return remitenteRepository.findByNombreContainingIgnoreCaseOrDocumentoContaining(filtro, filtro, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<RemitenteResumen> buscar(String texto) {
        String filtro = texto == null ? "" : texto.trim();
        return remitenteRepository
                .findTop10ByNombreContainingIgnoreCaseOrDocumentoContainingOrderByNombreAsc(filtro, filtro)
                .stream().map(RemitenteResumen::de).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Remitente> buscarPorId(Long id) {
        return remitenteRepository.findById(id);
    }

    @Transactional
    public Remitente crear(Remitente remitente) {
        if (remitenteRepository.existsByDocumento(remitente.getDocumento())) {
            throw new NegocioException("DOCUMENTO", "ya existe un remitente con el documento " + remitente.getDocumento());
        }
        remitente.setIdRemitente(null);
        return remitenteRepository.save(remitente);
    }

    @Transactional
    public Remitente actualizar(Long id, Remitente datos) {
        Remitente remitente = remitenteRepository.findById(id)
                .orElseThrow(() -> new NegocioException("NO_EXISTE", "el remitente no existe"));
        if (remitenteRepository.existsByDocumentoAndIdRemitenteNot(datos.getDocumento(), id)) {
            throw new NegocioException("DOCUMENTO", "ya existe otro remitente con el documento " + datos.getDocumento());
        }
        remitente.setNombre(datos.getNombre());
        remitente.setDocumento(datos.getDocumento());
        remitente.setTelefono(datos.getTelefono());
        remitente.setDireccion(datos.getDireccion());
        remitente.setCiudad(datos.getCiudad());
        remitente.setEstado(datos.getEstado());
        return remitente;
    }

    /** No se elimina un remitente con recolecciones (ni con paquetes, que quedarían sin dueño). */
    @Transactional
    public void eliminar(Long id) {
        Remitente remitente = remitenteRepository.findById(id)
                .orElseThrow(() -> new NegocioException("NO_EXISTE", "el remitente no existe"));
        long recolecciones = recoleccionRepository.countByRemitenteIdRemitente(id);
        if (recolecciones > 0) {
            throw new NegocioException("ELIMINAR", "no se puede eliminar a " + remitente.getNombre()
                    + " porque tiene " + recolecciones + (recolecciones == 1 ? " recolección asociada" : " recolecciones asociadas"));
        }
        if (paqueteRepository.existsByRemitenteIdRemitente(id)) {
            throw new NegocioException("ELIMINAR", "no se puede eliminar a " + remitente.getNombre()
                    + " porque tiene paquetes registrados a su nombre");
        }
        remitenteRepository.delete(remitente);
    }
}
