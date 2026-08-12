<div align="center">

<sub><b>Polski</b> · <a href="README.en.md">English</a></sub>

<sub><a href="CHANGELOG.md">Historia zmian</a></sub>

# 👁🔇🔈 Blind Mute Deaf

**Tryb zabawy do Minecrafta: każdy gracz traci jeden zmysł — i ma sobie z tym poradzić.**

Ślepy nie widzi, niemy nie mówi, głuchy nie słyszy. Żeby dogadać się mimo tego —
koło gestów nad głową i tabliczki z przedmiotami.

[![Pobierz jar](https://img.shields.io/badge/Pobierz-bmd.jar-4fb4ff?style=for-the-badge)](../../releases/latest)
&nbsp;
![Minecraft](https://img.shields.io/badge/Minecraft%2026.2%20%7C%20Fabric-2a3245?style=for-the-badge)
&nbsp;
![Licencja](https://img.shields.io/badge/licencja-MIT-3ddc84?style=for-the-badge)

</div>

---

**Fabric 26.2** (Java 25). Wymaga [Fabric API](https://modrinth.com/mod/fabric-api).
Integracja z [Simple Voice Chat](https://modrinth.com/mod/simple-voice-chat) jest opcjonalna,
ale bez niej Mute i Deaf tracą połowę sensu.

Mod musi być **i na serwerze, i u graczy** — ślepota, wyciszenie dźwięku i koło gestów są klienckie.

## Klasy

| Klasa | Co traci | Co dostaje |
|-------|----------|------------|
| 👁 **Ślepy** | Ekran całkowicie czarny | Echolokacja 3D: dźwięk wystrzeliwuje falę, która oświetla kształt pokoju |
| 🔇 **Niemy** | Mikrofon odcięty na serwerze, brak czatu, brak obrażeń | Koło gestów + tabliczka z dowolnym przedmiotem |
| 🔈 **Głuchy** | Zero dźwięku (gra i głos), brak PPM | Mówi normalnie, widzi wszystkie gesty |

Ślepota to **nie** efekt Blindness — to czarny prostokąt na samym wierzchu HUD-u.
Blindness da się rozjaśnić gammą i pozwala widzieć bloki tuż przy twarzy; tego nie da się obejść.

Manipulacja głosem dzieje się **po stronie serwera** przez API Simple Voice Chat:
niememu anulowany jest pakiet mikrofonu, głuchemu nie wysyłane są pakiety audio.
Konfiguracja SVC u gracza nic tu nie zmieni.

## Komendy

```
/bmd                          opis własnej klasy (każdy)
/bmd random [gracze]          równomiernie rozdaje trzy klasy
/bmd set <gracze> <klasa>     blind | mute | deaf | none
/bmd mode <easy|normal|hard>  jak mocno ślepy jest ślepy (zapisuje się do configu)
/bmd list                     kto co ma
/bmd reset                    czyści wszystko

/bmd goal                     jakie wyzwanie leci i od ilu (każdy)
/bmd goal random <trudność>   losuje cel: easy | normal | hard
/bmd goal set <id>            konkretny cel
/bmd goal list <trudność>     20 celów danej trudności
/bmd goal clear               kasuje wyzwanie
```

Wszystko poza `/bmd`, `/bmd info`, `/bmd goal` i `/bmd goal list` wymaga uprawnień
gamemastera (dawny poziom 2). Ukończenie celu gasi wszystkie ograniczenia naraz.

## Klawisze

| Klawisz | Działanie |
|---------|-----------|
| **Lewy Ctrl** (przytrzymaj) | koło gestów — celuj myszą, puść klawisz |
| **Lewy Alt** | tabliczka z przedmiotem (tylko niemy) |

Gesty: Tak ✔, Nie ✕, Pomocy ❤, Uwaga ⚠, Za mną ➜, Czekaj ⌛, Nie wiem ?, Ha ha ☺.
Każdy ma własny dźwięk — ślepy usłyszy, że ktoś coś pokazuje, ale nie dowie się co.

## Konfiguracja

Plik `bmd_config.json` w folderze świata, tworzony przy pierwszym starcie.
Debuffy włączone domyślnie: `muteCannotAttack`, `muteCannotChat`, `deafCannotUseItems`.

Działające klucze:

| Klucz | Domyślnie | Efekt |
|-------|-----------|-------|
| `muteCannotAttack` | `true` | niemy nie zadaje obrażeń niczym (też strzałą) |
| `muteCannotChat` | `true` | niemy nie pisze na czacie ani przez `/msg`, `/me`, `/say` |
| `muteItemSign` | `true` | niemy wystawia nad głowę dowolny przedmiot |
| `deafCannotUseItems` | `true` | głuchy nie używa PPM (jedzenie zostaje) |
| `onlyBlindCanCraft` | `true` | craftować może wyłącznie ślepy |
| `requireClientMod` | `true` | wyrzuca graczy bez moda po stronie klienta |
| `blindMode` | `"NORMAL"` | `EASY` \| `NORMAL` \| `HARD` — to samo co `/bmd mode` |
| `blindEchoRange` | `24.0` | zasięg echolokacji w blokach |
| `blindShowHud` | `true` | czerń idzie pod HUD (hotbar i paski widoczne) |
| `blindEasyDarkness` | `0.6` | dodatkowe przyciemnienie ekranu w trybie `EASY` (0.0–1.0) |

Klucze zapisane w configu, ale **jeszcze nic nie robią** — ustawienie `true` doda tylko
linijkę w opisie klasy: `muteCannotOpenContainers`, `muteHalfDamage`, `deafHidesNameTags`,
`deafMinesSlower`, `deafAggroRangeDoubled`, `blindSlowness`.

## Build

```bash
./gradlew build          # → build/libs/bmd-<wersja>.jar
./gradlew runServer      # serwer deweloperski
java src/main/java/dev/kajor/bmd/Geometry.java   # self-check matematyki
```

Wymaga JDK 25 (MC 26.2). Ścieżka do niego siedzi w `gradle.properties` jako `org.gradle.java.home`.

## Licencja

MIT — patrz [LICENSE](LICENSE).
