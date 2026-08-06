#!/usr/bin/env python3
"""Buduje paczke .mrpack dla trybu Blind Mute Deaf.

Baza to Fabulously Optimized (wydajnosc + sensowne domyslne ustawienia), do tego
mody komunikacyjne i sam mod BMD. Mod BMD nie jest na Modrinth, wiec leci
w overrides/ jako plik - reszta jako wpisy z URL-em, zeby launcher pobral je sam.

    python3 build_mrpack.py              # tylko zbuduj
    python3 build_mrpack.py --publish    # zbuduj i wystaw na strone
"""

import hashlib
import json
import shutil
import sys
import tempfile
import urllib.parse
import urllib.request
import zipfile
from pathlib import Path

MC_VERSION = "26.2"
LOADER = "fabric"

BASE_PACK = "fabulously-optimized"

# Mody dobrane pod tryb: glos (SVC), czytelny czat i twarze przy wiadomosciach,
# Collective jest biblioteka wymagana przez Full Brightness Toggle.
EXTRA_MODS = [
    "simple-voice-chat",
    "collective",
    "chatanimation",
    "chat-heads",
    "full-brightness-toggle",
]

BMD_JAR = Path(__file__).parent / "build" / "libs" / "bmd-1.0.0.jar"
# Prism i MultiMC biora ikone instancji z overrides/icon.png - sam modrinth.index.json
# nie ma pola na ikone.
PACK_ICON = Path(__file__).parent / "pack" / "icon.png"
OUT = Path(__file__).parent / "build" / f"BlindMuteDeaf-{MC_VERSION}.mrpack"

# Wersja paczki i katalog strony - uzywane przy --publish
PACK_VERSION = "1.0.0"
WWW_DIR = Path("/var/www/skynetgames.org/html/download")

API = "https://api.modrinth.com/v2"


def fetch_json(url: str):
    req = urllib.request.Request(url, headers={"User-Agent": "skynetgames-bmd-packer"})
    with urllib.request.urlopen(req, timeout=30) as r:
        return json.loads(r.read())


def latest_version(slug: str, loader: str = LOADER) -> dict:
    """Najnowsza wersja moda dla naszego MC i loadera. Filtr po loaderze jest
    istotny - bez niego API potrafi zwrocic build NeoForge jako pierwszy."""
    url = (f"{API}/project/{slug}/version"
           f"?game_versions=%5B%22{MC_VERSION}%22%5D&loaders=%5B%22{loader}%22%5D")
    versions = fetch_json(url)
    if not versions:
        raise SystemExit(f"BRAK wersji {slug} dla {MC_VERSION}/{loader}")
    return versions[0]


def primary_file(version: dict) -> dict:
    for f in version["files"]:
        if f.get("primary"):
            return f
    return version["files"][0]


def main():
    if not BMD_JAR.exists():
        raise SystemExit(f"Brak {BMD_JAR} - najpierw ./gradlew build")

    work = Path(tempfile.mkdtemp(prefix="bmdpack-"))
    try:
        # 1. Baza: pobierz i rozpakuj Fabulously Optimized
        base_ver = latest_version(BASE_PACK)
        base_file = primary_file(base_ver)
        print(f"baza: {base_ver['version_number']} ({base_file['filename']})")

        base_path = work / "base.mrpack"
        urllib.request.urlretrieve(base_file["url"], base_path)
        ext = work / "ext"
        with zipfile.ZipFile(base_path) as z:
            z.extractall(ext)

        index = json.loads((ext / "modrinth.index.json").read_text(encoding="utf-8"))
        have = {f["path"].split("/")[-1].lower() for f in index["files"]}

        # 2. Dodatkowe mody jako wpisy z URL - launcher pobierze je sam
        for slug in EXTRA_MODS:
            v = latest_version(slug)
            f = primary_file(v)
            if f["filename"].lower() in have:
                print(f"  pomijam {slug} - juz jest w bazie")
                continue
            index["files"].append({
                "path": f"mods/{f['filename']}",
                "hashes": {"sha1": f["hashes"]["sha1"], "sha512": f["hashes"]["sha512"]},
                "env": {"client": "required", "server": "required"},
                "downloads": [f["url"]],
                "fileSize": f["size"],
            })
            print(f"  + {slug}: {v['version_number']}")

        # 3. Mod BMD - nie ma go na Modrinth, wiec wchodzi jako plik do overrides
        mods_dir = ext / "overrides" / "mods"
        mods_dir.mkdir(parents=True, exist_ok=True)
        shutil.copy(BMD_JAR, mods_dir / BMD_JAR.name)
        print(f"  + {BMD_JAR.name} (overrides)")

        if PACK_ICON.exists():
            shutil.copy(PACK_ICON, ext / "overrides" / "icon.png")
            print(f"  + icon.png (overrides)")
        else:
            print(f"  ! brak {PACK_ICON} - paczka bez ikony")

        index["name"] = f"Blind Mute Deaf {MC_VERSION}"
        index["versionId"] = f"1.0.0+{base_ver['version_number']}"
        index["summary"] = ("Tryb utraty zmyslow: slepy, niemy, gluchy. "
                            "Na bazie Fabulously Optimized + Simple Voice Chat.")
        (ext / "modrinth.index.json").write_text(
            json.dumps(index, indent=2, ensure_ascii=False), encoding="utf-8")

        # 4. Spakuj z powrotem
        OUT.parent.mkdir(parents=True, exist_ok=True)
        if OUT.exists():
            OUT.unlink()
        with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as z:
            for path in sorted(ext.rglob("*")):
                if path.is_file():
                    z.write(path, path.relative_to(ext))

        sha = hashlib.sha1(OUT.read_bytes()).hexdigest()
        print(f"\ngotowe: {OUT}")
        print(f"  modow w indeksie: {len(index['files'])}  (+1 w overrides)")
        print(f"  rozmiar: {OUT.stat().st_size / 1024:.0f} KB   sha1: {sha[:16]}...")

        # Strona sama znajduje najnowszy plik pasujacy do "Blind Mute Deaf",
        # wiec wystarczy tu skopiowac - index.php nie wymaga zmian przy nowej wersji.
        if "--publish" in sys.argv:
            target = WWW_DIR / f"Blind Mute Deaf [Fabric] {PACK_VERSION}.mrpack"
            if not WWW_DIR.is_dir():
                print(f"  ! brak {WWW_DIR} - pomijam publikacje")
            else:
                shutil.copy(OUT, target)
                target.chmod(0o644)
                print(f"\nopublikowano: {target}")
                print("  https://skynetgames.org/download/"
                      + urllib.parse.quote(target.name))
    finally:
        shutil.rmtree(work, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
