// Pantalla "Nueva recolección": búsqueda de remitente, paquetes pendientes, ayudas de reglas y costo estimado.
(function () {
    const estado = window.ESTADO_INICIAL || {};
    const buscador = document.getElementById('buscador');
    const resultados = document.getElementById('resultados');
    const campoRemitenteId = document.getElementById('remitenteId');
    const tarjetaRemitente = document.getElementById('tarjeta-remitente');
    const datosRemitente = document.getElementById('datos-remitente');
    const inicialesRemitente = document.getElementById('remitente-iniciales');
    const tituloNombreRemitente = document.getElementById('remitente-titulo-nombre');
    const btnCambiarRemitente = document.getElementById('cambiar-remitente');
    const avisoRn03 = document.getElementById('aviso-rn03');

    const campoDireccion = document.getElementById('direccionRecoleccion');
    const selectorMensajero = document.getElementById('mensajeroId');
    const estadoZonaRn07 = document.getElementById('estado-zona-rn07');

    const tabla = document.getElementById('tabla-paquetes');
    const cuerpoTabla = tabla.querySelector('tbody');
    const checkTodos = document.getElementById('check-todos');
    const barraPaquetes = document.getElementById('barra-paquetes');
    const resumenDisponibles = document.getElementById('resumen-disponibles');
    const btnMarcarValidos = document.getElementById('btn-marcar-validos');
    const btnMarcarTodos = document.getElementById('btn-marcar-todos');
    const btnLimpiarPaquetes = document.getElementById('btn-limpiar-paquetes');

    const sinRemitente = document.getElementById('sin-remitente');
    const sinPaquetes = document.getElementById('sin-paquetes');
    const costoEstimado = document.getElementById('costo-estimado');
    const cantidadPaquetes = document.getElementById('cantidad-paquetes');
    const pesoFacturableTotal = document.getElementById('peso-facturable-total');

    const moneda = new Intl.NumberFormat('es-CO', {style: 'currency', currency: 'COP', maximumFractionDigits: 0});
    const decimal = new Intl.NumberFormat('es-CO', {maximumFractionDigits: 2});

    let temporizador;
    buscador.addEventListener('input', () => {
        clearTimeout(temporizador);
        temporizador = setTimeout(buscarRemitentes, 200);
    });

    buscador.addEventListener('focus', () => {
        if (buscador.value.trim().length > 0) {
            buscarRemitentes();
        }
    });

    document.addEventListener('click', (e) => {
        if (!resultados.contains(e.target) && e.target !== buscador) {
            resultados.hidden = true;
        }
    });

    if (btnCambiarRemitente) {
        btnCambiarRemitente.addEventListener('click', () => {
            buscador.focus();
            buscador.select();
        });
    }

    // Perfiles rápidos de demostración
    document.querySelectorAll('.chip-demo[data-demo-query]').forEach(chip => {
        chip.addEventListener('click', async () => {
            const query = chip.dataset.demoQuery;
            document.querySelectorAll('.chip-demo').forEach(c => c.classList.remove('chip-demo--activo'));
            chip.classList.add('chip-demo--activo');
            const respuesta = await fetch('/api/remitentes?query=' + encodeURIComponent(query));
            const remitentes = await respuesta.json();
            if (remitentes.length > 0) {
                await seleccionarRemitente(remitentes[0], [], false);
            }
        });
    });

    async function buscarRemitentes() {
        const texto = buscador.value.trim();
        if (texto.length === 0) {
            resultados.hidden = true;
            return;
        }
        const respuesta = await fetch('/api/remitentes?query=' + encodeURIComponent(texto));
        const remitentes = await respuesta.json();
        resultados.replaceChildren();
        if (remitentes.length === 0) {
            const li = document.createElement('li');
            li.className = 'nota';
            li.textContent = 'No hay remitentes que coincidan.';
            resultados.append(li);
        }
        remitentes.forEach(r => {
            const li = document.createElement('li');
            const boton = document.createElement('button');
            boton.type = 'button';

            const infoIzq = document.createElement('span');
            infoIzq.className = 'resultado-principal';
            const nombreStrong = document.createElement('strong');
            nombreStrong.textContent = r.nombre;
            const docSpan = document.createElement('span');
            docSpan.className = 'resultado-meta';
            docSpan.textContent = `Doc: ${r.documento} · ${r.ciudad || 'Sin ciudad'}`;
            infoIzq.append(nombreStrong, docSpan);

            const badgeEstado = document.createElement('span');
            badgeEstado.className = 'etiqueta ' + (r.estado === 'ACTIVO' ? 'etiqueta--ok' : 'etiqueta--mal');
            badgeEstado.textContent = r.estado;

            boton.append(infoIzq, badgeEstado);
            boton.addEventListener('click', () => seleccionarRemitente(r, [], false));
            li.append(boton);
            resultados.append(li);
        });
        resultados.hidden = false;
    }

    function obtenerIniciales(nombre) {
        if (!nombre) return 'RM';
        return nombre.trim().split(/\s+/).slice(0, 2).map(p => p[0].toUpperCase()).join('');
    }

    // conservarDireccion = true cuando se vuelve de un error y hay que respetar lo digitado
    async function seleccionarRemitente(r, paquetesMarcados, conservarDireccion) {
        campoRemitenteId.value = r.idRemitente;
        resultados.hidden = true;
        buscador.value = '';

        document.querySelectorAll('.chip-demo[data-demo-query]').forEach(chip => {
            chip.classList.toggle('chip-demo--activo', chip.dataset.demoQuery === r.documento);
        });

        if (inicialesRemitente) {
            inicialesRemitente.textContent = obtenerIniciales(r.nombre);
            inicialesRemitente.className = 'avatar-iniciales ' + (r.estado === 'ACTIVO' ? '' : 'avatar-iniciales--inactivo');
        }
        if (tituloNombreRemitente) {
            tituloNombreRemitente.textContent = `${r.nombre} (${r.documento})`;
        }

        datosRemitente.querySelectorAll('[data-campo]').forEach(dd => {
            dd.textContent = r[dd.dataset.campo] || '— sin dato —';
        });
        const etiquetaEstado = datosRemitente.querySelector('[data-campo="estado"]');
        etiquetaEstado.className = 'etiqueta ' + (r.estado === 'ACTIVO' ? 'etiqueta--ok' : 'etiqueta--mal');
        datosRemitente.hidden = false;
        if (tarjetaRemitente) {
            tarjetaRemitente.hidden = false;
        }

        evaluarAvisoRemitenteRn03(r);

        if (!conservarDireccion) {
            campoDireccion.value = [r.direccion, r.ciudad].filter(Boolean).join(', ');
            const error = document.getElementById('error-regla');
            if (error) error.remove();
        }
        actualizarIndicadorZonaRn07();

        const respuesta = await fetch('/api/paquetes?remitenteId=' + r.idRemitente);
        pintarPaquetes(await respuesta.json(), paquetesMarcados.map(Number));
    }

    function evaluarAvisoRemitenteRn03(r) {
        if (!avisoRn03) return;
        const faltantes = [];
        if (!r.documento || !r.documento.trim()) faltantes.push('documento');
        if (!r.telefono || !r.telefono.trim()) faltantes.push('teléfono');
        if (!r.direccion || !r.direccion.trim()) faltantes.push('dirección');

        if (r.estado !== 'ACTIVO') {
            avisoRn03.className = 'aviso-inline aviso-inline--peligro';
            avisoRn03.textContent = `Advertencia RN-03: El remitente ${r.nombre} está en estado ${r.estado}. Al confirmar, la transacción será rechazada.`;
            avisoRn03.hidden = false;
        } else if (faltantes.length > 0) {
            avisoRn03.className = 'aviso-inline aviso-inline--peligro';
            avisoRn03.textContent = `Advertencia RN-03: Al remitente ${r.nombre} le falta registrar: ${faltantes.join(', ')}. Al confirmar, la transacción será rechazada.`;
            avisoRn03.hidden = false;
        } else {
            avisoRn03.className = 'aviso-inline aviso-inline--ok';
            avisoRn03.textContent = `RN-03 verificado: Remitente ACTIVO con datos de contacto completos (${r.ciudad}).`;
            avisoRn03.hidden = false;
        }
    }

    function pintarPaquetes(paquetes, marcados) {
        sinRemitente.hidden = true;
        cuerpoTabla.replaceChildren();
        sinPaquetes.hidden = paquetes.length > 0;
        tabla.hidden = paquetes.length === 0;
        if (barraPaquetes) {
            barraPaquetes.hidden = paquetes.length === 0;
        }
        if (resumenDisponibles && paquetes.length > 0) {
            const validos = paquetes.filter(p => p.pesoKg <= 30 && p.sumaDimensionesCm <= 150 && p.zonaCubierta).length;
            resumenDisponibles.textContent = `${paquetes.length} paquete(s) pendiente(s) · ${validos} cumple(n) RN-01 y RN-02`;
        }

        paquetes.forEach(p => {
            const fila = document.createElement('tr');
            const excedePeso = Number(p.pesoKg) > 30;
            const excedeMedidas = Number(p.sumaDimensionesCm) > 150;
            const sinCobertura = !p.zonaCubierta;
            const esVolumetrico = Number(p.pesoFacturable) > Number(p.pesoKg);
            const esValido = !excedePeso && !excedeMedidas && !sinCobertura;

            if (!esValido) {
                fila.classList.add('fila-con-alerta');
            }

            // Checkbox
            const celdaCheck = document.createElement('td');
            celdaCheck.className = 'col-check';
            const check = document.createElement('input');
            check.type = 'checkbox';
            check.name = 'paqueteIds';
            check.value = p.idPaquete;
            check.dataset.costo = p.costoEstimado;
            check.dataset.pesoFacturable = p.pesoFacturable;
            check.dataset.valido = String(esValido);
            check.checked = marcados.includes(p.idPaquete);
            check.setAttribute('aria-label', 'Seleccionar ' + p.codigo);
            check.addEventListener('change', () => {
                fila.classList.toggle('fila-seleccionada', check.checked);
                actualizarCosto();
            });
            celdaCheck.append(check);
            fila.append(celdaCheck);
            fila.classList.toggle('fila-seleccionada', check.checked);

            // Permitir clic en la fila para marcar/desmarcar
            fila.addEventListener('click', (e) => {
                if (e.target.tagName !== 'INPUT') {
                    check.checked = !check.checked;
                    check.dispatchEvent(new Event('change'));
                }
            });

            // 0: Código
            const tdCodigo = document.createElement('td');
            const badgeCodigo = document.createElement('span');
            badgeCodigo.className = 'codigo-paquete';
            badgeCodigo.textContent = p.codigo;
            tdCodigo.append(badgeCodigo);
            fila.append(tdCodigo);

            // 1: Descripción
            const tdDesc = document.createElement('td');
            tdDesc.textContent = p.descripcion;
            fila.append(tdDesc);

            // 2: Peso real
            const tdPeso = document.createElement('td');
            tdPeso.className = 'num' + (excedePeso ? ' texto-alerta' : '');
            tdPeso.append(document.createTextNode(decimal.format(p.pesoKg) + ' kg'));
            if (excedePeso) {
                const alertaPeso = document.createElement('span');
                alertaPeso.className = 'etiqueta etiqueta--mal etiqueta--bloque';
                alertaPeso.textContent = 'Máx 30 kg · RN-01';
                tdPeso.append(alertaPeso);
            }
            fila.append(tdPeso);

            // 3: Dimensiones
            const tdDim = document.createElement('td');
            tdDim.className = 'num' + (excedeMedidas ? ' texto-alerta' : '');
            tdDim.append(document.createTextNode(`${p.altoCm} + ${p.anchoCm} + ${p.largoCm} = ${p.sumaDimensionesCm} cm`));
            if (excedeMedidas) {
                const alertaDim = document.createElement('span');
                alertaDim.className = 'etiqueta etiqueta--mal etiqueta--bloque';
                alertaDim.textContent = 'Máx 150 cm · RN-01';
                tdDim.append(alertaDim);
            }
            fila.append(tdDim);

            // 4: Peso facturable
            const tdFacturable = document.createElement('td');
            tdFacturable.className = 'num';
            const valorFact = document.createElement('strong');
            valorFact.textContent = decimal.format(p.pesoFacturable) + ' kg';
            tdFacturable.append(valorFact);
            if (esVolumetrico) {
                const badgeVol = document.createElement('span');
                badgeVol.className = 'etiqueta etiqueta--info etiqueta--bloque';
                badgeVol.textContent = 'Volumétrico · RN-06';
                tdFacturable.append(badgeVol);
            }
            fila.append(tdFacturable);

            // 5: Destino y cobertura
            const tdDestino = document.createElement('td');
            if (sinCobertura) tdDestino.className = 'texto-alerta';
            const textoDestino = `${p.destinatario} · ${p.ciudadDestino}` + (p.zonaCubierta ? '' : ' (zona sin cobertura)');
            tdDestino.append(document.createTextNode(textoDestino));
            const badgeZona = document.createElement('span');
            badgeZona.className = 'etiqueta etiqueta--bloque ' + (p.zonaCubierta ? 'etiqueta--ok' : 'etiqueta--mal');
            badgeZona.textContent = p.zonaCubierta ? 'Zona cubierta' : 'Sin cobertura · RN-02';
            tdDestino.append(badgeZona);
            fila.append(tdDestino);

            // 6: Costo
            const tdCosto = document.createElement('td');
            tdCosto.className = 'num celda-costo';
            tdCosto.textContent = moneda.format(p.costoEstimado);
            fila.append(tdCosto);

            cuerpoTabla.append(fila);
        });
        actualizarCosto();
    }

    function actualizarCosto() {
        const todosChecks = cuerpoTabla.querySelectorAll('input[name="paqueteIds"]');
        const marcados = cuerpoTabla.querySelectorAll('input[name="paqueteIds"]:checked');
        let total = 0;
        let pesoTotal = 0;
        marcados.forEach(c => {
            total += Number(c.dataset.costo);
            pesoTotal += Number(c.dataset.pesoFacturable || 0);
        });
        costoEstimado.textContent = moneda.format(total);
        cantidadPaquetes.textContent = marcados.length === 1
            ? '1 paquete seleccionado'
            : `${marcados.length} paquetes seleccionados`;
        if (pesoFacturableTotal) {
            pesoFacturableTotal.textContent = `Peso facturable acumulado: ${decimal.format(pesoTotal)} kg`;
        }
        if (checkTodos) {
            checkTodos.checked = todosChecks.length > 0 && marcados.length === todosChecks.length;
            checkTodos.indeterminate = marcados.length > 0 && marcados.length < todosChecks.length;
        }
    }

    if (checkTodos) {
        checkTodos.addEventListener('change', () => {
            cuerpoTabla.querySelectorAll('input[name="paqueteIds"]').forEach(c => {
                c.checked = checkTodos.checked;
                c.closest('tr').classList.toggle('fila-seleccionada', c.checked);
            });
            actualizarCosto();
        });
    }

    if (btnMarcarTodos) {
        btnMarcarTodos.addEventListener('click', () => {
            cuerpoTabla.querySelectorAll('input[name="paqueteIds"]').forEach(c => {
                c.checked = true;
                c.closest('tr').classList.add('fila-seleccionada');
            });
            actualizarCosto();
        });
    }

    if (btnMarcarValidos) {
        btnMarcarValidos.addEventListener('click', () => {
            cuerpoTabla.querySelectorAll('input[name="paqueteIds"]').forEach(c => {
                c.checked = c.dataset.valido === 'true';
                c.closest('tr').classList.toggle('fila-seleccionada', c.checked);
            });
            actualizarCosto();
        });
    }

    if (btnLimpiarPaquetes) {
        btnLimpiarPaquetes.addEventListener('click', () => {
            cuerpoTabla.querySelectorAll('input[name="paqueteIds"]').forEach(c => {
                c.checked = false;
                c.closest('tr').classList.remove('fila-seleccionada');
            });
            actualizarCosto();
        });
    }

    // Validación visual en tiempo real para RN-07 (zona del mensajero vs ciudad de la dirección)
    function normalizarTexto(texto) {
        return (texto || '').trim().normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
    }

    function extraerCiudad(direccion) {
        if (!direccion) return null;
        const coma = direccion.lastIndexOf(',');
        if (coma < 0) return null;
        const ciudad = direccion.substring(coma + 1).trim();
        return ciudad.length > 0 ? ciudad : null;
    }

    function actualizarIndicadorZonaRn07() {
        if (!estadoZonaRn07 || !selectorMensajero || !campoDireccion) return;
        const opcion = selectorMensajero.options[selectorMensajero.selectedIndex];
        const zonaMensajero = opcion ? opcion.getAttribute('data-zona') : null;
        const direccion = campoDireccion.value.trim();
        const ciudadDireccion = extraerCiudad(direccion);

        if (!zonaMensajero && !direccion) {
            estadoZonaRn07.hidden = true;
            return;
        }
        if (direccion && !ciudadDireccion) {
            estadoZonaRn07.className = 'aviso-inline aviso-inline--peligro';
            estadoZonaRn07.textContent = 'Advertencia RN-07: La dirección debe terminar con la ciudad separada por coma (ej. "Calle 45 # 12-30, Bogotá").';
            estadoZonaRn07.hidden = false;
            return;
        }
        if (zonaMensajero && ciudadDireccion) {
            if (normalizarTexto(zonaMensajero) === normalizarTexto(ciudadDireccion)) {
                estadoZonaRn07.className = 'aviso-inline aviso-inline--ok';
                estadoZonaRn07.textContent = `RN-07 verificado: La zona del mensajero (${zonaMensajero}) coincide con la ciudad de recolección (${ciudadDireccion}).`;
            } else {
                estadoZonaRn07.className = 'aviso-inline aviso-inline--peligro';
                estadoZonaRn07.textContent = `Advertencia RN-07: El mensajero seleccionado cubre ${zonaMensajero}, pero la dirección está en ${ciudadDireccion}.`;
            }
            estadoZonaRn07.hidden = false;
            return;
        }
        estadoZonaRn07.hidden = true;
    }

    if (selectorMensajero) {
        selectorMensajero.addEventListener('change', actualizarIndicadorZonaRn07);
    }
    if (campoDireccion) {
        campoDireccion.addEventListener('input', actualizarIndicadorZonaRn07);
    }

    if (estado.remitente) {
        seleccionarRemitente(estado.remitente, estado.paqueteIds || [], Boolean(estado.hayError));
    }
})();
