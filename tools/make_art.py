#!/usr/bin/env python3
"""Sklada ikone projektu i baner z tego, co w modzie jest naprawde wlasne: echolokacji.

Konkurencja w tej samej niszy siedzi na trzech malpach i przekreslonych oczach - fala,
ktora wylania postac z ciemnosci, jest jedyna rzecza, ktorej tamte mody nie moga pokazac.
Rysujemy ja, a nie wycinamy ze zrzutu: zrzut ma szum i przypadkowy kadr, a ikona musi byc
czytelna w 48 px na liscie Modrintha.

Postac jest ciemniejsza od tla i ma oswietlony wylacznie zewnetrzny kontur - dokladnie tak
dziala echolokacja w grze: nie widzisz bryly, widzisz to, od czego odbil sie dzwiek.

Uzycie:  python3 tools/make_art.py
         FONT_DIR=/sciezka/do/fontow python3 tools/make_art.py   # dla banera
Wynik:   docs/media/icon.png (512x512), docs/media/banner.png (1280x640)

Baner potrzebuje Antonio i Poppins (oba SIL OFL, Google Fonts). Nie trzymamy ich w repo -
bez nich powstaje sama ikona.
"""
import math
import os
import random
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageFont

WYNIK = Path(__file__).resolve().parent.parent / "docs" / "media"
FONTY = Path(os.environ.get("FONT_DIR", "/usr/share/fonts"))

TLO = (12, 17, 30)           # granat, nie czern - czarny kafelek na liscie czyta sie jak dziura
POSTAC = (2, 3, 5)
KONTUR = (198, 221, 255)
ZRODLO = (255, 214, 150)     # jedyny cieply punkt na calej grafice - oko samo do niego idzie
ZIARNO = 11                  # wybrane z kilkunastu; reszta ukladala sie w rowniejsze, nudniejsze luki

FRONTY = ((95, 255), (140, 220), (185, 175), (230, 125))   # (promien, jasnosc)
# Blokowa postac: glowa, tulow, dwie rece. Wspolrzedne wzgledem srodka.
CZESCI = ((-40, -118, 40, -40), (-30, -40, 30, 66), (-58, -36, -30, 40), (30, -36, 58, 40))


def fala(rys, cx, cy, skala=1.0, ziarno=ZIARNO):
    """Kolejne fronty fali. Dziury sa celowe - rowny luk wyglada jak tarcza, nie jak dzwiek."""
    random.seed(ziarno)
    piksel = 10 * skala
    for promien, jasnosc in FRONTY:
        promien *= skala
        for i in range(int(promien * 1.2)):
            if random.random() > 0.62:
                continue
            kat = -0.1 + math.pi * 1.2 * i / (promien * 1.2)
            x = cx + promien * math.cos(kat) + random.randint(-4, 4) * skala
            y = cy - promien * math.sin(kat) * 0.9 + random.randint(-4, 4) * skala
            rys.rectangle([x, y, x + piksel, y + piksel],
                          fill=(int(jasnosc * .72), int(jasnosc * .85), jasnosc))


def postac(obraz, cx, cy, s=1.0):
    """Sylwetka jako jedna bryla. Kontur liczymy z maski (maska minus jej erozja), bo obrys
    rysowany osobno wokol kazdego prostokata czytal sie jak plot, a nie jak gracz."""
    maska = Image.new("L", obraz.size, 0)
    rys = ImageDraw.Draw(maska)
    for x0, y0, x1, y1 in CZESCI:
        rys.rectangle([cx + x0 * s, cy + y0 * s, cx + x1 * s, cy + y1 * s], fill=255)

    okno = max(3, int(9 * s) | 1)          # MinFilter chce nieparzystego okna
    kontur = ImageChops.subtract(maska, maska.filter(ImageFilter.MinFilter(okno)))

    obraz.paste(Image.new("RGB", obraz.size, POSTAC), (0, 0), maska)
    obraz.paste(Image.new("RGB", obraz.size, KONTUR), (0, 0), kontur)


def ikona(bok=512):
    im = Image.new("RGB", (bok, bok), TLO)
    cx, cy = bok // 2, bok // 2 + 30
    fala(ImageDraw.Draw(im), cx, cy)
    postac(im, cx, cy)
    ImageDraw.Draw(im).rectangle([cx - 9, cy - 100, cx + 9, cy - 82], fill=ZRODLO)
    return im


def baner(szer=1280, wys=640):
    im = Image.new("RGB", (szer, wys), TLO)
    cx, cy = 330, 400
    fala(ImageDraw.Draw(im), cx, cy, skala=1.3, ziarno=3)
    postac(im, cx, cy, s=1.3)
    d = ImageDraw.Draw(im)
    d.rectangle([cx - 12, cy - 130, cx + 12, cy - 106], fill=ZRODLO)
    d.text((690, 236), "BLIND MUTE DEAF",
           font=ImageFont.truetype(str(FONTY / "Antonio-VariableFont_wght.ttf"), 92),
           fill=(238, 242, 248))
    d.text((694, 344), "One sense missing. Talk your way out.",
           font=ImageFont.truetype(str(FONTY / "Poppins-Medium.ttf"), 29),
           fill=(150, 178, 214))
    d.text((694, 400), "Fabric · Minecraft 26.2",
           font=ImageFont.truetype(str(FONTY / "Poppins-Regular.ttf"), 23),
           fill=(96, 106, 124))
    return im


def main() -> None:
    WYNIK.mkdir(parents=True, exist_ok=True)
    ikona().save(WYNIK / "icon.png")
    print(WYNIK / "icon.png")
    try:
        baner().save(WYNIK / "banner.png")
        print(WYNIK / "banner.png")
    except OSError as e:
        print(f"baner pominiety (brak fontow w {FONTY}): {e}")


if __name__ == "__main__":
    main()
