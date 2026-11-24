#!/usr/bin/env python3
"""
Genera l'icona adattiva Android da giudice-robot.svg (radice del progetto).

- ic_launcher_background: gradiente + alone rosso, a tutto campo (senza angoli arrotondati).
- ic_launcher_foreground: solo il robot, ridimensionato nella zona sicura (66/108 dp).
Uso: python3 art/icon/build-icons.py   (richiede ImageMagick `magick`)
"""
import pathlib, re, subprocess

ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = (ROOT / "giudice-robot.svg").read_text()
OUT_SVG = ROOT / "art" / "icon"
RES = ROOT / "app" / "src" / "main" / "res"

# Robot nell'SVG 512x512: circa x 92-420, y 96-436 -> centro (256, 266), altezza ~340.
# Lo portiamo a ~70 dp su 108 dp: lato del viewBox = 340 * 108 / 70 ~ 524.
SIDE = 524
CX, CY = 256, 266
VIEWBOX = f'{CX - SIDE // 2} {CY - SIDE // 2} {SIDE} {SIDE}'

svg_open = re.search(r'<svg[^>]*>', SRC).group(0)
new_open = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{VIEWBOX}" width="{SIDE}" height="{SIDE}">'
bg_rect = re.search(r'\s*<rect width="512" height="512"[^>]*/>', SRC).group(0)
halo = re.search(r'\s*<circle cx="256" cy="250" r="230"[^>]*/>', SRC).group(0)
defs = re.search(r'<defs>.*?</defs>', SRC, re.S).group(0)

foreground = SRC.replace(svg_open, new_open).replace(bg_rect, "").replace(halo, "")
x0, y0 = CX - SIDE // 2, CY - SIDE // 2
background = (
    f'{new_open}\n  {defs}\n'
    f'  <rect x="{x0}" y="{y0}" width="{SIDE}" height="{SIDE}" fill="url(#bg)"/>\n'
    f'  {halo.strip()}\n</svg>\n'
)
(OUT_SVG / "ic_launcher_foreground.svg").write_text(foreground)
(OUT_SVG / "ic_launcher_background.svg").write_text(background)

# 108 dp per densità.
DENSITIES = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}
for name, px in DENSITIES.items():
    d = RES / f"mipmap-{name}"
    d.mkdir(parents=True, exist_ok=True)
    for layer in ("foreground", "background"):
        subprocess.run(
            ["magick", "-background", "none", "-density", "300",
             str(OUT_SVG / f"ic_launcher_{layer}.svg"), "-resize", f"{px}x{px}",
             "-strip", f"PNG32:{d / f'ic_launcher_{layer}.png'}"],
            check=True,
        )
print("ok")
