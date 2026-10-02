<div align="center">

<sub><b>Polski</b> · <a href="CHANGELOG.en.md">English</a></sub>

</div>

# Historia zmian

Format wg [Keep a Changelog](https://keepachangelog.com/pl/1.1.0/),
wersjonowanie wg [SemVer](https://semver.org/lang/pl/).

## [Unreleased]

### Zmienione

- **Komunikat wyrzucenia w języku gracza.** Gracz bez moda nie ma naszych tłumaczeń,
  więc serwer tłumaczy sam — na język ustawiony w grze gracza; gdy go nie ma, po angielsku.
- Komunikat wskazuje Modrinth zamiast nieistniejącego już pliku `bmd.jar`.

## [1.12.1] — 2026-10-02

Zgłoszenie błędu: **SlayerIbn** — dzięki!

### Naprawione

- **Gracz bez moda widział gołe klucze na ekranie wyrzucenia** (`bmd.kick.no_mod`…)
  zamiast instrukcji — jego klient nie ma plików językowych BMD. Teraz komunikat
  przychodzi z angielskim tekstem zapasowym, który zna serwer.
- **Tabliczka z przedmiotem podawała identyfikator** (`diamond_sword`) zamiast nazwy
  przedmiotu w języku gracza.

## [1.12.0] — 2026-09-29

Pomysły i zgłoszenie błędu: **SlayerIbn** — dzięki!

### Dodane

- **`gestureWheel`** — koło gestów da się wyłączyć wszystkim. Tabliczkę z przedmiotem
  wyłączało już `muteItemSign`; teraz oba klawisze mówią wprost, że funkcja jest
  wyłączona na serwerze, zamiast otwierać ekran na próżno.
- **`everyoneCannotChat`, `everyoneCannotWriteSigns`, `everyoneCannotWriteBooks`** —
  zamykają obejścia gestów wszystkim graczom z klasą (gracz bez klasy nie podlega).
  Na tabliczce da się dalej postawić pustą tabliczkę, edytor się nie otwiera.
  Każda włączona reguła pojawia się w opisie klasy (`/bmd`).

### Naprawione

- **F1 zdejmował czerń ślepego.** Czerń jest częścią HUD-u, więc schowanie HUD-u
  odsłaniało świat — w każdym trybie ślepoty. Ślepy nie może już schować HUD-u,
  także gdy schował go, zanim dostał klasę.
- **Gracz wchodzący na serwer nie dostawał swojej klasy.** Synchronizacja przy wejściu
  szła do listy graczy, na której wchodzącego jeszcze nie ma — klasa i ustawienia
  docierały do klienta dopiero przy najbliższym `/bmd set`/`random`/`mode`. Ślepy po
  ponownym wejściu widział więc świat, a licznik wyzwania stał.

### Uwaga

- Zmienił się pakiet synchronizacji — **klient i serwer muszą mieć 1.12.0.**

## [1.11.4] — 2026-08-15

### Zmienione

- **Nazwa pliku niesie silnik i wersję gry:** `bmd-fabric-26.2-1.11.4.jar` zamiast
  `bmd-1.11.3.jar`. Mod jest przywiązany do konkretnego wydania Minecrafta, a sama
  wersja moda tego nie mówi — łatwo było wgrać jar nie na tę wersję.

## [1.11.3] — 2026-08-12

Pierwsze wydanie publiczne. Wcześniejsze wersje chodziły tylko na prywatnym serwerze.

### Naprawione

- **Tytuły ekranów koła gestów i tabliczki z przedmiotem** były wpisane na sztywno po polsku.
  Widać je tylko przez narratora, ale to znaczy, że gracz korzystający z czytnika ekranu
  dostawał polski tekst niezależnie od ustawionego języka. Teraz idą przez `en_us`/`pl_pl`.

### Zmienione

- Metadane moda po angielsku, autor i odnośniki do repozytorium w `fabric.mod.json`.
- Plik `LICENSE` (MIT), angielski `README.en.md` i ta historia zmian.

## [1.11.2] — 2026-08-11

### Zmienione

- Ukończone wyzwanie widać w opisie klasy (`/bmd`) i przy `/bmd mode` — wcześniej
  po zdjęciu ograniczeń nic o tym nie mówiło.

## [1.11.1] — 2026-08-11

### Dodane

- Stan wyzwania pokazywany przy wejściu na serwer.

### Zmienione

- `/bmd mode` zapisuje się do configu, więc przeżywa restart serwera.

## [1.11.0] — 2026-08-10

### Dodane

- **Echolokacja 3D.** Dźwięk wystrzeliwuje falę, która oświetla kształt otoczenia zamiast
  rysować płaski obrys. Gęstość 480 promieni zamiast dotychczasowych 96.

### Naprawione

- Odbicie lustrzane HUD-u ślepego: wektor `right` kamery miał znak lewej strony.

## [1.10.0] — 2026-08-06

### Zmienione

- Ukończenie celu gasi wszystkie ograniczenia naraz — wcześniej trzeba było je zdejmować ręcznie.

## [1.9.0] — 2026-08-06

### Dodane

- Tłumaczenia PL/EN wszystkich komunikatów.
- 60 celów wyzwania w trzech trudnościach i licznik czasu.

## [1.8.0] — 2026-08-06

### Dodane

- Echolokacja w przestrzeni (wcześniej tylko w płaszczyźnie) i emoji klas.
- 1.8.1: ikony także dla bloków, nie tylko przedmiotów.

## [1.7.0] — 2026-08-06

### Zmienione

- Gesty rysowane na nametagu gracza zamiast we własnych dymkach — widać je z dowolnej odległości
  i nie zasłaniają świata.

## [1.6.0] — 2026-08-06

### Zmienione

- Głuchy może jeść mimo blokady prawego przycisku.
- Craftować może wyłącznie ślepy.

## [1.0.0] – [1.5.0] — 2026-08-06

Wersje wewnętrzne, jeden dzień szybkich iteracji: trzy klasy i uszczelnienie ślepoty
i głuchoty, tryby ślepoty (`EASY`/`NORMAL`/`HARD`), HUD dla ślepego, 12 gestów,
klawisze LCtrl/LAlt, blokada wejścia bez moda po stronie klienta oraz własny font moda,
żeby emoji gestów wyglądały jak emoji, a nie jak kwadraty.
