# Blind Mute Deaf

Tryb zabawy do Minecrafta: każdy gracz traci jeden zmysł. Ślepy nie widzi, niemy nie mówi,
głuchy nie słyszy. Żeby dogadać się mimo tego — koło gestów nad głową i tabliczki z przedmiotami.

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
/bmd losuj [gracze]           równomiernie rozdaje trzy klasy
/bmd ustaw <gracze> <klasa>   blind | mute | deaf | none
/bmd hard <on|off>            ślepym gaśnie echolokacja
/bmd lista                    kto co ma
/bmd reset                    czyści wszystko
```

Wszystko poza `/bmd` i `/bmd info` wymaga uprawnień gamemastera (dawny poziom 2).

## Klawisze

| Klawisz | Działanie |
|---------|-----------|
| **G** (przytrzymaj) | koło gestów — celuj myszą, puść klawisz |
| **B** | tabliczka z przedmiotem (tylko niemy) |

Gesty: Tak ✔, Nie ✕, Pomocy ❤, Uwaga ⚠, Za mną ➜, Czekaj ⌛, Nie wiem ?, Ha ha ☺.
Każdy ma własny dźwięk — ślepy usłyszy, że ktoś coś pokazuje, ale nie dowie się co.

## Konfiguracja

Plik `bmd_config.json` w folderze świata, tworzony przy pierwszym starcie.
Debuffy włączone domyślnie: `muteCannotAttack`, `muteCannotChat`, `deafCannotUseItems`.

Wyłączone, do włączenia jednym `true`:

| Klucz | Efekt |
|-------|-------|
| `muteCannotOpenContainers` | niemy nie otwiera skrzyń i pieców |
| `muteHalfDamage` | niemy zadaje o połowę mniej obrażeń (zamiast zera) |
| `deafHidesNameTags` | głuchy nie widzi nicków |
| `deafMinesSlower` | głuchy kopie 2× wolniej |
| `deafAggroRangeDoubled` | moby wykrywają głuchego z 2× większej odległości |
| `blindSlowness` | ślepy porusza się wolniej |
| `blindHardMode` | ślepy bez echolokacji (to samo co `/bmd hard on`) |
| `blindEchoRange` | zasięg echolokacji w blokach (domyślnie 24) |

## Build

```bash
./gradlew build          # → build/libs/bmd-1.0.0.jar
./gradlew runServer      # serwer deweloperski
java src/main/java/dev/kajor/bmd/Geometry.java   # self-check matematyki
```

Wymaga JDK 25 (MC 26.2). Ścieżka do niego siedzi w `gradle.properties` jako `org.gradle.java.home`.
