#!/usr/bin/env python3
"""Sklada ikone projektu (Modrinth, 512x512) z trzech malp Twemoji.

Te same trzy zmysly co w modzie: nie widze, nie mowie, nie slysze. Grafiki sciagamy
z Twemoji w 72 px - to najwiekszy rozmiar rastrowy, jaki tam jest, wiec skalujemy w gore
LANCZOSEM. Licencja CC-BY 4.0, atrybucja siedzi w assets/bmd/textures/font/LICENSE-twemoji.txt.

Uzycie:  python3 tools/make_icon.py
Wynik:   docs/media/icon.png
"""
import io
import urllib.request
from pathlib import Path

from PIL import Image

WYNIK = Path(__file__).resolve().parent.parent / "docs" / "media" / "icon.png"
ZRODLO = "https://cdn.jsdelivr.net/gh/jdecked/twemoji@latest/assets/72x72/{}.png"
MALPY = ["1f648", "1f64a", "1f649"]  # slepy, niemy, gluchy - kolejnosc jak w nazwie moda

BOK = 512
TLO = (22, 24, 28)
TWARZ = 232          # bok pojedynczej malpy
# Trojkat: dwie na gorze, jedna nizej i na srodku. Przy 64 px w liscie Modrintha
# uklad dalej czyta sie jako "trzy malpy", a nie jako plama.
POZYCJE = [(16, 28), (264, 28), (140, 250)]


def pobierz(kod: str) -> Image.Image:
    with urllib.request.urlopen(ZRODLO.format(kod)) as odp:
        return Image.open(io.BytesIO(odp.read())).convert("RGBA")


def main() -> None:
    ikona = Image.new("RGBA", (BOK, BOK), TLO)
    for kod, (x, y) in zip(MALPY, POZYCJE):
        twarz = pobierz(kod).resize((TWARZ, TWARZ), Image.LANCZOS)
        ikona.alpha_composite(twarz, (x, y))
    WYNIK.parent.mkdir(parents=True, exist_ok=True)
    ikona.save(WYNIK)
    print(WYNIK)


if __name__ == "__main__":
    main()
