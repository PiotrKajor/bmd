#!/usr/bin/env python3
"""Buduje wlasna czcionke bitmapowa moda z prawdziwymi emoji.

Vanilla font Minecrafta rysuje tylko BMP, wiec 👋 czy 👏 wychodza pustym
kwadratem. Mod dostaje wiec wlasny font: atlas PNG + definicja w JSON.
Glify siedza w Private Use Area (U+E000+), zeby nie kolidowac z niczym.

Grafiki: Twemoji (https://github.com/jdecked/twemoji), licencja CC-BY 4.0 -
atrybucja jest w LICENSE-twemoji obok atlasu.

    python3 tools/build_emoji_font.py
"""

import json
import urllib.request
from pathlib import Path

from PIL import Image

# Kolejnosc MUSI zgadzac sie z kolejnoscia stalych w Emote.java -
# indeks gestu wyznacza znak PUA (U+E000 + indeks).
EMOJI = [
    ("YES", "1f44d"), ("NO", "1f44e"), ("DUNNO", "1f937"), ("LAUGH", "1f602"),
    ("HELP", "1f198"), ("DANGER", "26a0"), ("ENEMY", "2694"), ("DYING", "1f480"),
    ("FOLLOW", "1f449"), ("WAIT", "23f3"), ("HERE", "1f4cd"), ("HOME", "1f3e0"),
    ("HELLO", "1f44b"), ("CLAP", "1f44f"), ("RUDE", "1f595"),
]

CDN = "https://cdn.jsdelivr.net/gh/jdecked/twemoji@latest/assets/72x72/"
CELL = 32          # rozmiar glifu w atlasie
COLS = 4           # 4x4 = 16 miejsc, mamy 15 glifow
PUA_START = 0xE000
# MC oznacza puste miejsca w siatce znakiem U+0000
EMPTY_GLYPH = "\u0000"

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src" / "client" / "resources" / "assets" / "bmd"
ATLAS = ASSETS / "textures" / "font" / "emoji.png"
FONT = ASSETS / "font" / "emoji.json"
LICENSE = ASSETS / "textures" / "font" / "LICENSE-twemoji.txt"


def fetch(code: str) -> Image.Image:
    req = urllib.request.Request(CDN + code + ".png", headers={"User-Agent": "bmd-font-builder"})
    with urllib.request.urlopen(req, timeout=30) as r:
        from io import BytesIO
        return Image.open(BytesIO(r.read())).convert("RGBA")


def main() -> None:
    rows = (len(EMOJI) + COLS - 1) // COLS
    atlas = Image.new("RGBA", (COLS * CELL, rows * CELL), (0, 0, 0, 0))

    for i, (name, code) in enumerate(EMOJI):
        # LANCZOS, nie NEAREST: Twemoji to grafika wektorowa w duzej rozdzielczosci,
        # wiec zmniejszamy ja gladko. NEAREST poszarpalby krawedzie.
        glyph = fetch(code).resize((CELL, CELL), Image.LANCZOS)
        atlas.paste(glyph, ((i % COLS) * CELL, (i // COLS) * CELL), glyph)
        print(f"  {chr(PUA_START + i)!r} U+{PUA_START + i:04X}  {name:8} ({code})")

    ATLAS.parent.mkdir(parents=True, exist_ok=True)
    FONT.parent.mkdir(parents=True, exist_ok=True)
    atlas.save(ATLAS)

    # Puste miejsca w siatce dopelniamy U+0000 - MC wymaga rownych wierszy.
    chars = []
    for r in range(rows):
        row = "".join(
            chr(PUA_START + r * COLS + c) if r * COLS + c < len(EMOJI) else EMPTY_GLYPH
            for c in range(COLS)
        )
        chars.append(row)

    FONT.write_text(json.dumps({
        "providers": [{
            "type": "bitmap",
            "file": "bmd:font/emoji.png",
            # height wiekszy niz 8 (wysokosc zwyklej litery) - emoji ma byc
            # troche wieksze od tekstu, ascent trzyma je na linii pisma.
            "height": 10,
            "ascent": 8,
            "chars": chars,
        }]
    }, indent=2, ensure_ascii=False), encoding="utf-8")

    LICENSE.write_text(
        "Grafiki emoji: Twemoji\n"
        "https://github.com/jdecked/twemoji\n"
        "Licencja: CC-BY 4.0 (https://creativecommons.org/licenses/by/4.0/)\n"
        "Copyright 2020 Twitter, Inc and other contributors.\n",
        encoding="utf-8")

    print(f"\natlas: {ATLAS}  ({atlas.size[0]}x{atlas.size[1]})")
    print(f"font : {FONT}")


if __name__ == "__main__":
    main()
