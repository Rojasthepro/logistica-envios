"""Genera la entrega completa del proyecto.

1. Renderiza los diagramas a img/*.png:
   - diagramas/mermaid/*.mmd (secuencia, actividad) con mermaid-cli, líneas en codo.
   - diagramas/uml/*.html (clases, casos de uso; el de clases sale de generar_diagrama_clases.py)
     y diagramas/bocetos/*.html con Chromium.
2. Convierte DOCUMENTO_ENTREGA_PROYECTO_GRUPAL.md a entrega/ENTREGA_PROYECTO_GRUPAL.pdf.
3. Empaqueta el proyecto en entrega/ENTREGA_PROYECTO_GRUPAL.zip.

Uso:  python3 scripts/generar_entrega.py
Requiere: chromium, npx (Node), ImageMagick (magick) y python-markdown-it-py.
"""
import json
import os
import re
import shutil
import subprocess
import tempfile
import zipfile
from pathlib import Path

from markdown_it import MarkdownIt

PROYECTO = Path(__file__).resolve().parent.parent
DOC = PROYECTO / "DOCUMENTO_ENTREGA_PROYECTO_GRUPAL.md"
IMG = PROYECTO / "img"
MERMAID = PROYECTO / "diagramas" / "mermaid"
ENTREGA = PROYECTO / "entrega"
PDF = ENTREGA / "ENTREGA_PROYECTO_GRUPAL.pdf"
ZIP = ENTREGA / "ENTREGA_PROYECTO_GRUPAL.zip"
CHROMIUM = shutil.which("chromium") or "chromium"
NOMBRES = ["diagrama_secuencia", "diagrama_actividad"]
PAGINAS_HTML = sorted((PROYECTO / "diagramas").glob("uml/*.html")) + sorted((PROYECTO / "diagramas").glob("bocetos/*.html"))

# Qué entra al ZIP (rutas relativas al proyecto)
CONTENIDO_ZIP = ["src", "pom.xml", "mvnw", "mvnw.cmd", ".mvn", ".gitignore", "README.md",
                 "DOCUMENTO_ENTREGA_PROYECTO_GRUPAL.md", "img", "diagramas", "docs/evidencia-pruebas.md",
                 "docs/guion-video-entrega.md", "entrega/ENTREGA_PROYECTO_GRUPAL.pdf"]


def renderizar_diagramas(tmp: Path) -> None:
    subprocess.run(["python3", str(PROYECTO / "scripts" / "generar_diagrama_clases.py")], check=True)
    puppeteer = tmp / "puppeteer.json"
    puppeteer.write_text(json.dumps({"executablePath": CHROMIUM, "args": ["--no-sandbox"]}))
    for nombre in NOMBRES:
        crudo = tmp / f"{nombre}.png"
        subprocess.run(["npx", "-y", "-p", "@mermaid-js/mermaid-cli@11", "mmdc",
                        "-p", str(puppeteer), "-c", str(MERMAID / "config.json"),
                        "-i", str(MERMAID / f"{nombre}.mmd"), "-o", str(crudo),
                        "-s", "2", "-b", "white"],
                       check=True, capture_output=True, env={**os.environ, "PUPPETEER_SKIP_DOWNLOAD": "1"})
        subprocess.run(["magick", str(crudo), "-trim", "+repage", "-bordercolor", "white",
                        "-border", "30", str(IMG / f"{nombre}.png")], check=True)
        print("Diagrama renderizado:", nombre)
    for pagina in PAGINAS_HTML:
        crudo = tmp / f"{pagina.stem}.png"
        subprocess.run([CHROMIUM, "--headless", "--disable-gpu", "--no-sandbox", "--hide-scrollbars",
                        "--force-device-scale-factor=2", "--window-size=1400,1600", f"--screenshot={crudo}",
                        pagina.as_uri()], check=True, capture_output=True)
        subprocess.run(["magick", str(crudo), "-trim", "+repage", "-bordercolor", "white",
                        "-border", "30", str(IMG / f"{pagina.stem}.png")], check=True)
        print("Diagrama renderizado:", pagina.stem)


def latex_a_texto(expr: str) -> str:
    reglas = [
        (r"\\left\(", "("), (r"\\right\)", ")"),
        (r"\\text\{([^}]*)\}", r"\1"),
        (r"\\frac\{([^{}]*)\}\{([^{}]*)\}", r"(\1) / (\2)"),
        (r"\\le", "≤"), (r"\\ge", "≥"), (r"\\times", "×"), (r"\\to", "→"),
        (r"\\rightarrow", "→"), (r"\\sum", "Σ"), (r"\\max", "max"),
    ]
    for p, r in reglas:
        expr = re.sub(p, r, expr)
    return expr.replace("{", "").replace("}", "")


CSS = """
@page { size: Letter; margin: 25mm 22mm; }
body { font-family: "Fira Sans", "DejaVu Sans", sans-serif; font-size: 11pt; line-height: 1.45; color: #000; }
.portada { text-align: center; padding-top: 55mm; page-break-after: always; font-size: 12.5pt; }
.portada strong { font-size: 15pt; }
h2 { font-size: 14pt; margin-top: 22px; page-break-after: avoid; }
h3 { font-size: 12pt; margin-top: 16px; page-break-after: avoid; }
code { font-family: "DejaVu Sans Mono", monospace; font-size: 9.5pt; }
pre { border: 1px solid #999; padding: 8px; font-size: 9.5pt; }
table { border-collapse: collapse; width: 100%; margin: 8px 0 12px; font-size: 9.5pt; }
th, td { border: 1px solid #555; padding: 4px 6px; vertical-align: top; text-align: left; }
th { background: #e6e6e6; }
tr { page-break-inside: avoid; }
img { max-width: 100%; max-height: 215mm; display: block; margin: 10px auto; page-break-inside: avoid; }
p:has(> img) { page-break-inside: avoid; page-break-after: avoid; margin-bottom: 2px; }
a { color: #000; text-decoration: none; }
"""


def generar_pdf() -> None:
    texto = DOC.read_text(encoding="utf-8")
    texto = re.sub(r"\$([^$\n]+)\$", lambda m: latex_a_texto(m.group(1)), texto)
    texto = texto.replace("\\*", "*").replace("\\$", "$")
    cuerpo = MarkdownIt("commonmark", {"html": True}).enable("table").enable("strikethrough").render(texto)
    html = (f'<!DOCTYPE html><html lang="es"><head><meta charset="utf-8">'
            f"<title>Proyecto Grupal — Sistema de Logística de Envíos</title><style>{CSS}</style></head>"
            f"<body>{cuerpo}</body></html>")
    pagina = PROYECTO / "_entrega_tmp.html"  # junto al .md para que resuelvan las rutas img/
    pagina.write_text(html, encoding="utf-8")
    try:
        subprocess.run([CHROMIUM, "--headless", "--disable-gpu", "--no-sandbox", "--hide-scrollbars",
                        "--virtual-time-budget=5000", "--no-pdf-header-footer",
                        f"--print-to-pdf={PDF}", pagina.as_uri()], check=True, capture_output=True)
    finally:
        pagina.unlink(missing_ok=True)
    print("PDF generado:", PDF)


def generar_zip() -> None:
    with zipfile.ZipFile(ZIP, "w", zipfile.ZIP_DEFLATED) as z:
        for entrada in CONTENIDO_ZIP:
            ruta = PROYECTO / entrada
            archivos = [ruta] if ruta.is_file() else sorted(p for p in ruta.rglob("*") if p.is_file())
            for f in archivos:
                z.write(f, Path("logistica-envios") / f.relative_to(PROYECTO))
    print("ZIP generado:", ZIP)


if __name__ == "__main__":
    ENTREGA.mkdir(exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        renderizar_diagramas(Path(tmp))
    generar_pdf()
    generar_zip()
