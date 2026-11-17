"""Genera diagramas/uml/diagrama_clases.html: diagrama de clases UML en SVG con líneas en codo."""
from pathlib import Path

SALIDA = Path(__file__).resolve().parent.parent / "diagramas" / "uml" / "diagrama_clases.html"
LH, HEAD, PAD = 20, 32, 8

CLASES = {
    # nombre: (x, y, ancho, atributos, métodos, estereotipo)
    "Remitente": (30, 30, 280, ["-idRemitente: Long", "-nombre: String", "-documento: String", "-telefono: String",
                                "-direccion: String", "-ciudad: String", "-estado: EstadoRemitente"],
                  ["+isHabilitado(): boolean"], None),
    "Recoleccion": (430, 30, 320, ["-idRecoleccion: Long", "-numeroGuia: String", "-fechaHora: LocalDateTime",
                                   "-direccionRecoleccion: String", "-costoTotal: BigDecimal",
                                   "-estado: EstadoRecoleccion", "-usuarioOperador: String",
                                   "-selloTiempo: LocalDateTime"], [], None),
    "Mensajero": (870, 30, 290, ["-idMensajero: Long", "-nombre: String", "-documento: String", "-zona: String",
                                 "-estado: EstadoMensajero"], [], None),
    "Paquete": (430, 480, 320, ["-idPaquete: Long", "-descripcion: String", "-pesoKg: BigDecimal",
                                "-altoCm: Integer", "-anchoCm: Integer", "-largoCm: Integer",
                                "-valorDeclarado: BigDecimal", "-estado: EstadoPaquete"],
                ["+getCodigo(): String", "+getSumaDimensionesCm(): int"], None),
    "Destinatario": (870, 480, 290, ["-idDestinatario: Long", "-nombre: String", "-telefono: String",
                                     "-direccion: String", "-ciudad: String"], [], None),
    "ZonaCobertura": (870, 730, 290, ["-idZona: Long", "-nombre: String", "-cubierta: boolean",
                                      "-tarifaBase: BigDecimal", "-valorPorKg: BigDecimal"], [], None),
    "EstadoRemitente": (30, 320, 190, ["ACTIVO", "INACTIVO"], None, "enumeration"),
    "EstadoRecoleccion": (560, 310, 190, ["PROGRAMADA", "REALIZADA", "FALLIDA"], None, "enumeration"),
    "EstadoMensajero": (870, 250, 190, ["DISPONIBLE", "EN_RUTA"], None, "enumeration"),
    "EstadoPaquete": (480, 820, 200, ["REGISTRADO", "RECOGIDO", "EN_RUTA", "ENTREGADO"], None, "enumeration"),
}


def caja(nombre, x, y, w, attrs, metodos, estereotipo):
    out = []
    head = HEAD + (16 if estereotipo else 0)
    h_attrs = len(attrs) * LH + 2 * PAD
    h_met = (len(metodos) * LH + 2 * PAD) if metodos else (0 if metodos is None else 14)
    h = head + h_attrs + h_met
    out.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" class="caja"/>')
    ty = y + 21
    if estereotipo:
        out.append(f'<text x="{x + w / 2}" y="{ty}" text-anchor="middle" class="est">«{estereotipo}»</text>')
        ty += 17
    out.append(f'<text x="{x + w / 2}" y="{ty}" text-anchor="middle" class="nom">{nombre}</text>')
    out.append(f'<line x1="{x}" y1="{y + head}" x2="{x + w}" y2="{y + head}" class="sep"/>')
    for i, a in enumerate(attrs):
        out.append(f'<text x="{x + 10}" y="{y + head + PAD + 15 + i * LH}">{a}</text>')
    if metodos is not None:
        ys = y + head + h_attrs
        out.append(f'<line x1="{x}" y1="{ys}" x2="{x + w}" y2="{ys}" class="sep"/>')
        for i, m in enumerate(metodos):
            out.append(f'<text x="{x + 10}" y="{ys + PAD + 15 + i * LH}">{m}</text>')
    return out, h


def ruta(puntos, clase="asoc", inicio=None, fin=None):
    d = " ".join(f"{'M' if i == 0 else 'L'}{px},{py}" for i, (px, py) in enumerate(puntos))
    m = (f' marker-start="url(#{inicio})"' if inicio else "") + (f' marker-end="url(#{fin})"' if fin else "")
    return f'<path d="{d}" class="{clase}"{m}/>'


def texto(x, y, t, ancla="start", clase="mult"):
    return f'<text x="{x}" y="{y}" text-anchor="{ancla}" class="{clase}">{t}</text>'


partes, alto = [], {}
for nombre, (x, y, w, at, me, est) in CLASES.items():
    elems, h = caja(nombre, x, y, w, at, me, est)
    partes += elems
    alto[nombre] = h


def fondo(n):
    x, y, *_ = CLASES[n]
    return y + alto[n]


# Asociaciones (flecha abierta = navegable hacia esa clase)
rem_x, rec_x, men_x = 310, 430, 870
partes += [
    # Remitente 1 -> 0..* Recoleccion : solicita
    ruta([(rem_x, 110), (rec_x, 110)], fin="flecha"),
    texto(rem_x + 6, 104, "1"), texto(rec_x - 14, 102, "0..*", "end"), texto(370, 128, "solicita", "middle", "rol"),
    # Mensajero 1 -> 0..* Recoleccion : ejecuta
    ruta([(men_x, 110), (750, 110)], fin="flecha"),
    texto(men_x - 6, 104, "1", "end"), texto(764, 102, "0..*"), texto(810, 128, "ejecuta", "middle", "rol"),
    # Recoleccion 0..1 <>-- 1..* Paquete : agrupa
    ruta([(480, fondo("Recoleccion")), (480, 480)], inicio="rombo"),
    texto(488, fondo("Recoleccion") + 34, "0..1"), texto(488, 472, "1..*"),
    texto(472, (fondo("Recoleccion") + 480) / 2 + 5, "agrupa", "end", "rol"),
    # Remitente 1 -> 0..* Paquete : registra
    ruta([(250, fondo("Remitente")), (250, 570), (430, 570)], fin="flecha"),
    texto(258, fondo("Remitente") + 18, "1"), texto(416, 562, "0..*", "end"), texto(340, 588, "registra", "middle", "rol"),
    # Paquete 0..* -> 1 Destinatario : consignado a
    ruta([(750, 570), (870, 570)], fin="flecha"),
    texto(756, 564, "0..*"), texto(856, 562, "1", "end"), texto(810, 588, "consignado a", "middle", "rol"),
    # Destinatario 0..* -> 1 ZonaCobertura : ubicado en
    ruta([(1015, fondo("Destinatario")), (1015, 730)], fin="flecha"),
    texto(1023, fondo("Destinatario") + 18, "0..*"), texto(1023, 724, "1"),
    texto(1007, (fondo("Destinatario") + 730) / 2 + 5, "ubicado en", "end", "rol"),
    # Dependencias hacia los enums
    ruta([(125, fondo("Remitente")), (125, 320)], "dep", fin="flecha"),
    ruta([(655, fondo("Recoleccion")), (655, 310)], "dep", fin="flecha"),
    ruta([(965, fondo("Mensajero")), (965, 250)], "dep", fin="flecha"),
    ruta([(580, fondo("Paquete")), (580, 820)], "dep", fin="flecha"),
]

ancho = 1190
alto_total = max(CLASES[n][1] + alto[n] for n in CLASES) + 30
svg = f"""<svg xmlns="http://www.w3.org/2000/svg" width="{ancho}" height="{alto_total}" viewBox="0 0 {ancho} {alto_total}">
<defs>
  <marker id="flecha" viewBox="0 0 12 12" refX="11" refY="6" markerWidth="12" markerHeight="12" orient="auto-start-reverse" markerUnits="userSpaceOnUse">
    <path d="M1,1 L11,6 L1,11" fill="none" stroke="#111" stroke-width="1.6"/></marker>
  <marker id="rombo" viewBox="0 0 20 12" refX="1" refY="6" markerWidth="20" markerHeight="12" orient="auto" markerUnits="userSpaceOnUse">
    <path d="M1,6 L10,1 L19,6 L10,11 Z" fill="#fff" stroke="#111" stroke-width="1.4"/></marker>
</defs>
<style>
  text {{ font-family: "DejaVu Sans", sans-serif; font-size: 13px; fill: #111; }}
  .nom {{ font-weight: bold; font-size: 14px; }} .est {{ font-size: 12px; }}
  .mult {{ font-size: 12px; }} .rol {{ font-size: 12.5px; font-style: italic; }}
  .caja {{ fill: #fff; stroke: #111; stroke-width: 1.5; }} .sep {{ stroke: #111; stroke-width: 1.2; }}
  .asoc {{ fill: none; stroke: #111; stroke-width: 1.4; }}
  .dep {{ fill: none; stroke: #111; stroke-width: 1.2; stroke-dasharray: 6 4; }}
</style>
{chr(10).join(partes)}
</svg>"""
SALIDA.write_text(f'<html><head><meta charset="utf-8"><style>body{{margin:0;background:#fff}}</style></head><body>{svg}</body></html>',
                  encoding="utf-8")
print("Generado:", SALIDA)
