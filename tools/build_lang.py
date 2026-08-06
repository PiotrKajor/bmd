#!/usr/bin/env python3
"""Generuje pliki tlumaczen moda (pl_pl, en_us).

Opisy celow trzymamy tutaj obok siebie, zeby polska i angielska wersja nie
rozjechaly sie przy dodawaniu nowego celu - brak pary od razu wywala skrypt.

    python3 tools/build_lang.py
"""

import json
from pathlib import Path

LANG_DIR = Path(__file__).resolve().parent.parent / "src" / "client" / "resources" / "assets" / "bmd" / "lang"

# klucz: (polski, angielski)
COMMON = {
    "key.bmd.wheel": ("Koło gestów (przytrzymaj)", "Gesture wheel (hold)"),
    "key.bmd.item_sign": ("Tabliczka z przedmiotem (niemy)", "Item sign (mute only)"),

    "bmd.sense.blind": ("Ślepy", "Blind"),
    "bmd.sense.mute": ("Niemy", "Mute"),
    "bmd.sense.deaf": ("Głuchy", "Deaf"),
    "bmd.sense.none": ("Widzący", "Sighted"),

    "bmd.mode.easy": ("łatwy", "easy"),
    "bmd.mode.normal": ("normalny", "normal"),
    "bmd.mode.hard": ("trudny", "hard"),

    "bmd.emote.yes": ("Tak", "Yes"),
    "bmd.emote.no": ("Nie", "No"),
    "bmd.emote.dunno": ("Nie wiem", "No idea"),
    "bmd.emote.laugh": ("Ha ha!", "Ha ha!"),
    "bmd.emote.help": ("Pomocy!", "Help!"),
    "bmd.emote.danger": ("Uwaga!", "Danger!"),
    "bmd.emote.enemy": ("Wróg!", "Enemy!"),
    "bmd.emote.dying": ("Umieram", "Dying"),
    "bmd.emote.follow": ("Za mną", "Follow me"),
    "bmd.emote.wait": ("Czekaj", "Wait"),
    "bmd.emote.here": ("Tutaj", "Over here"),
    "bmd.emote.home": ("Do bazy", "To base"),
    "bmd.emote.hello": ("Cześć!", "Hello!"),
    "bmd.emote.clap": ("Brawo!", "Nice!"),
    "bmd.emote.rude": ("Spadaj!", "Get lost!"),

    "bmd.wheel.pick": ("wybierz kierunek", "pick a direction"),
    "bmd.wheel.showing": ("Pokazujesz: %s", "Showing: %s"),
    "bmd.sign.showing": ("Pokazujesz przedmiot: %s", "Showing item: %s"),
    "bmd.sign.mute_only": ("Tabliczka z przedmiotem to przywilej niemego.",
                           "The item sign is the mute player's privilege."),
    "bmd.sign.search": ("Co chcesz pokazać?", "What do you want to show?"),
    "bmd.sign.nothing": ("nic nie znaleziono", "nothing found"),

    "bmd.blind.hint": ("[T] czat  •  /bmd info", "[T] chat  •  /bmd info"),

    "bmd.warn.mute_no_attack": ("Niemy nie zadaje obrażeń.", "The mute deals no damage."),
    "bmd.warn.mute_no_chat": ("Niemy nie pisze. Użyj koła gestów [LCtrl].",
                              "The mute cannot type. Use the gesture wheel [LCtrl]."),
    "bmd.warn.deaf_no_place": ("Głuchy nie stawia bloków ani nie używa PPM.",
                               "The deaf cannot place blocks or right-click."),
    "bmd.warn.deaf_no_use": ("Głuchy nie używa przedmiotów prawym przyciskiem (jeść możesz).",
                             "The deaf cannot right-click items (eating is allowed)."),

    "bmd.kick.no_mod": ("Brakuje moda Blind Mute Deaf", "Blind Mute Deaf mod is missing"),
    "bmd.kick.no_mod_why": ("Bez niego ślepy widzi, a głuchy słyszy grę.",
                            "Without it the blind can see and the deaf can hear."),
    "bmd.kick.no_mod_how": ("Wrzuć bmd.jar do folderu mods/.", "Put bmd.jar into your mods/ folder."),

    "bmd.cmd.assigned": ("Rozdano klasy: %s graczy.", "Roles assigned to %s player(s)."),
    "bmd.cmd.set": ("Ustawiono %s dla %s gracz(y).", "Set %s for %s player(s)."),
    "bmd.cmd.reset": ("Zmysły wróciły. Jesteś zwykłym graczem.",
                      "Your senses are back. You are an ordinary player."),
    "bmd.cmd.cleared": ("Wyczyszczono wszystkie klasy.", "All roles cleared."),
    "bmd.cmd.nobody": ("Nikt nie ma przydzielonej klasy.", "Nobody has a role assigned."),
    "bmd.cmd.player_only": ("Ta komenda jest dla gracza w grze.", "This command is for an in-game player."),
    "bmd.cmd.blind_mode": ("Ślepota: %s", "Blindness: %s"),

    "bmd.goal.none": ("Żadne wyzwanie nie trwa.", "No challenge is running."),
    "bmd.goal.current": ("AKTUALNE WYZWANIE", "CURRENT CHALLENGE"),
    "bmd.goal.started": ("NOWE WYZWANIE", "NEW CHALLENGE"),
    "bmd.goal.done": ("WYZWANIE UKOŃCZONE!", "CHALLENGE COMPLETE!"),
    "bmd.goal.done_by": ("Wykonał: %s w czasie %s", "Completed by %s in %s"),
    "bmd.goal.already_done": ("To wyzwanie jest już ukończone.", "This challenge is already complete."),
    "bmd.goal.cleared": ("Wyzwanie anulowane.", "Challenge cancelled."),
    "bmd.goal.unknown": ("Nie ma takiego celu.", "No such goal."),
    "bmd.goal.progress": ("Postęp: %s / %s", "Progress: %s / %s"),
    "bmd.goal.time": ("Czas: %s", "Time: %s"),
    "bmd.goal.difficulty.easy": ("Poziom: łatwy", "Difficulty: easy"),
    "bmd.goal.difficulty.normal": ("Poziom: normalny", "Difficulty: normal"),
    "bmd.goal.difficulty.hard": ("Poziom: trudny", "Difficulty: hard"),

    # opis klasy (/bmd info)
    "bmd.brief.blind.mode": ("Tryb ślepoty: %s", "Blindness mode: %s"),
    "bmd.brief.blind.easy1": ("Widzisz zarys tuż przed sobą (Blindness + Darkness)",
                              "You see outlines right in front of you (Blindness + Darkness)"),
    "bmd.brief.blind.easy2": ("Nie zobaczysz nic dalej niż kilka kroków",
                              "Nothing beyond a few steps is visible"),
    "bmd.brief.blind.echo": ("Echolokacja: dźwięki w promieniu %s bloków zapalają znacznik",
                             "Echolocation: sounds within %s blocks light up a marker"),
    "bmd.brief.blind.echo2": ("▲ dźwięk nad tobą   ● na twoim poziomie   ▼ pod tobą",
                              "▲ sound above you   ● at your level   ▼ below you"),
    "bmd.brief.blind.echo3": ("Bliżej źródło, bliżej środka ekranu i jaśniej",
                              "The closer the source, the closer to the centre and brighter"),
    "bmd.brief.blind.hard": ("Zero podpowiedzi - zostaje sam słuch",
                             "No hints at all - hearing is all you get"),
    "bmd.brief.blind.hears": ("Słyszysz wszystko - grę i głos na Simple Voice Chat",
                              "You hear everything - the game and Simple Voice Chat"),
    "bmd.brief.blind.speaks": ("Mówisz normalnie - jesteś uszami i ustami drużyny",
                               "You speak normally - you are the team's ears and voice"),
    "bmd.brief.blind.hud": ("Widzisz swój HUD: hotbar, życie i głód",
                            "You see your HUD: hotbar, health and hunger"),
    "bmd.brief.blind.nohud": ("Nie widzisz nawet własnego HUD-u", "You cannot even see your own HUD"),
    "bmd.brief.blind.noworld": ("Nie widzisz świata", "You cannot see the world"),
    "bmd.brief.blind.craft": ("Tylko ty craftujesz - reszta nosi ci surowce",
                              "Only you can craft - the others bring you materials"),
    "bmd.brief.blind.slow": ("Poruszasz się wolniej (Spowolnienie I)", "You move slower (Slowness I)"),
    "bmd.brief.blind.hint": ("Czat otworzysz [T] - komendy działają, wiadomości i tak nie przeczytasz",
                             "Open chat with [T] - commands work, you cannot read messages anyway"),

    "bmd.brief.mute.flavor": ("Nie wydajesz dźwięku. Mikrofon jest odcięty na serwerze.",
                              "You make no sound. Your microphone is cut off on the server."),
    "bmd.brief.mute.novoice": ("Nie mówisz na Simple Voice Chat - nikt cię nie usłyszy",
                               "You cannot speak on Simple Voice Chat - nobody will hear you"),
    "bmd.brief.mute.nochat": ("Nie piszesz na czacie ani /msg", "You cannot use chat or /msg"),
    "bmd.brief.mute.noattack": ("Nie zadajesz obrażeń - żadnych, nikomu",
                                "You deal no damage - none, to anyone"),
    "bmd.brief.mute.halfdmg": ("Zadajesz o połowę mniejsze obrażenia", "You deal half damage"),
    "bmd.brief.mute.nocontainers": ("Nie otwierasz skrzyń ani pieców", "You cannot open chests or furnaces"),
    "bmd.brief.mute.nocraft": ("Nie craftujesz - crafting umie tylko ślepy",
                               "You cannot craft - only the blind can"),
    "bmd.brief.mute.sees": ("Widzisz i słyszysz wszystko - jesteś oczami drużyny",
                            "You see and hear everything - you are the team's eyes"),
    "bmd.brief.mute.wheel": ("Koło gestów [LCtrl] - 15 znaków z dźwiękiem, widoczne nad głową",
                             "Gesture wheel [LCtrl] - 15 signs with sound, shown above your head"),
    "bmd.brief.mute.sign": ("Tabliczka z przedmiotem [LAlt] - pokaż dowolny przedmiot z gry",
                            "Item sign [LAlt] - show any item from the game"),
    "bmd.brief.mute.hint": ("Gest ma dźwięk - ślepy usłyszy, że coś pokazujesz, ale nie co",
                            "Gestures make sound - the blind hears you signalling, but not what"),

    "bmd.brief.deaf.flavor": ("Cisza absolutna. Zero dźwięku z gry i zero głosu.",
                              "Total silence. No game sound, no voices."),
    "bmd.brief.deaf.novoice": ("Nie słyszysz nikogo na Simple Voice Chat",
                               "You hear nobody on Simple Voice Chat"),
    "bmd.brief.deaf.nogame": ("Nie słyszysz gry: kroków, creepera, dzwonka, muzyki",
                              "You hear no game sounds: steps, creepers, bells, music"),
    "bmd.brief.deaf.noplace": ("Nie stawiasz bloków i nie używasz PPM (łuk, perły)",
                               "You cannot place blocks or right-click (bow, pearls)"),
    "bmd.brief.deaf.caneat": ("Jeść i pić możesz normalnie", "You can eat and drink normally"),
    "bmd.brief.deaf.nocraft": ("Nie craftujesz - crafting umie tylko ślepy",
                               "You cannot craft - only the blind can"),
    "bmd.brief.deaf.slowmine": ("Kopiesz dwa razy wolniej", "You mine twice as slow"),
    "bmd.brief.deaf.nonames": ("Nie widzisz nicków nad głowami", "You cannot see names above heads"),
    "bmd.brief.deaf.aggro": ("Moby wykrywają cię z dwa razy większej odległości",
                             "Mobs detect you from twice the distance"),
    "bmd.brief.deaf.speaks": ("Mówisz normalnie - inni cię słyszą, ty ich nie",
                              "You speak normally - others hear you, you do not hear them"),
    "bmd.brief.deaf.wheel": ("Koło gestów [LCtrl] działa tak samo u ciebie",
                             "The gesture wheel [LCtrl] works for you too"),
    "bmd.brief.deaf.hint": ("Patrz na znaki nad głowami. To twój jedyny kanał odbiorczy.",
                            "Watch the signs above heads. That is your only incoming channel."),

    "bmd.brief.none.flavor": ("Masz wszystkie zmysły. Nie masz żadnych ograniczeń.",
                              "You have all your senses. No restrictions."),
    "bmd.brief.none.hint": ("/bmd random przydzieli klasy graczom.", "/bmd random assigns roles to players."),
}

# Opisy celow - id: (polski, angielski)
GOALS = {
    # latwe
    "wood": ("Zdobądź 16 dębowych kłód", "Get 16 oak logs"),
    "planks": ("Zdobądź 32 deski", "Get 32 planks"),
    "crafting_table": ("Zrób stół rzemieślniczy", "Craft a crafting table"),
    "stone_pickaxe": ("Zrób kamienny kilof", "Craft a stone pickaxe"),
    "furnace": ("Zbuduj piec", "Build a furnace"),
    "torches": ("Zrób 16 pochodni", "Make 16 torches"),
    "bread": ("Upiecz 3 chleby", "Bake 3 loaves of bread"),
    "coal": ("Wykop 8 węgla", "Mine 8 coal"),
    "iron_ingot": ("Wytop 5 sztabek żelaza", "Smelt 5 iron ingots"),
    "bed": ("Zrób łóżko", "Craft a bed"),
    "chest": ("Zrób 2 skrzynie", "Craft 2 chests"),
    "bucket": ("Zrób wiadro", "Craft a bucket"),
    "wool": ("Zdobądź 8 wełny", "Get 8 wool"),
    "cooked_beef": ("Usmaż 5 steków", "Cook 5 steaks"),
    "boat": ("Zbuduj łódkę", "Build a boat"),
    "fishing_rod": ("Zrób wędkę", "Craft a fishing rod"),
    "shield": ("Zrób tarczę", "Craft a shield"),
    "kill_zombies": ("Zabij 5 zombie", "Kill 5 zombies"),
    "kill_skeletons": ("Zabij 3 szkielety", "Kill 3 skeletons"),
    "kill_spiders": ("Zabij 3 pająki", "Kill 3 spiders"),
    # normalne
    "diamond": ("Wykop 3 diamenty", "Mine 3 diamonds"),
    "iron_armor": ("Zrób żelazny napierśnik", "Craft an iron chestplate"),
    "gold_ingot": ("Wytop 10 sztabek złota", "Smelt 10 gold ingots"),
    "redstone": ("Zdobądź 16 redstone", "Get 16 redstone"),
    "obsidian": ("Zdobądź 10 obsydianu", "Get 10 obsidian"),
    "enchanting_table": ("Zbuduj stół zaklęć", "Build an enchanting table"),
    "bookshelf": ("Zrób 5 regałów", "Craft 5 bookshelves"),
    "golden_apple": ("Zdobądź złote jabłko", "Get a golden apple"),
    "ender_pearl": ("Zdobądź 4 perły Endu", "Get 4 ender pearls"),
    "lapis": ("Wykop 12 lazurytu", "Mine 12 lapis lazuli"),
    "emerald": ("Zdobądź 5 szmaragdów", "Get 5 emeralds"),
    "diamond_pickaxe": ("Zrób diamentowy kilof", "Craft a diamond pickaxe"),
    "anvil": ("Zbuduj kowadło", "Build an anvil"),
    "cake": ("Upiecz tort", "Bake a cake"),
    "map": ("Zrób mapę", "Craft a map"),
    "brewing_stand": ("Zbuduj stojak alchemiczny", "Build a brewing stand"),
    "reach_nether": ("Wejdź do Netheru", "Enter the Nether"),
    "kill_creepers": ("Zabij 5 creeperów", "Kill 5 creepers"),
    "kill_endermen": ("Zabij 2 endermeny", "Kill 2 endermen"),
    "kill_witch": ("Zabij wiedźmę", "Kill a witch"),
    # trudne
    "netherite_ingot": ("Zdobądź sztabkę netherytu", "Get a netherite ingot"),
    "ancient_debris": ("Wykop 3 pradawne szczątki", "Mine 3 ancient debris"),
    "diamond_armor_full": ("Zrób diamentowy napierśnik", "Craft a diamond chestplate"),
    "blaze_rod": ("Zdobądź 6 płomiennych prętów", "Get 6 blaze rods"),
    "ender_eye": ("Zrób 8 oczu Endu", "Craft 8 eyes of ender"),
    "nether_star": ("Zdobądź gwiazdę Netheru", "Get a nether star"),
    "elytra": ("Znajdź elytrę", "Find an elytra"),
    "shulker_shell": ("Zdobądź 2 muszle shulkera", "Get 2 shulker shells"),
    "totem": ("Zdobądź totem nieśmiertelności", "Get a totem of undying"),
    "trident": ("Zdobądź trójząb", "Get a trident"),
    "beacon": ("Zbuduj sygnalizator", "Build a beacon"),
    "dragon_egg": ("Zdobądź jajo smoka", "Get the dragon egg"),
    "golden_apple_ench": ("Znajdź zaklęte złote jabłko", "Find an enchanted golden apple"),
    "wither_skull": ("Zdobądź 3 czaszki withera", "Get 3 wither skeleton skulls"),
    "reach_end": ("Wejdź do Endu", "Enter the End"),
    "kill_blazes": ("Zabij 10 płomyków", "Kill 10 blazes"),
    "kill_wither_skeletons": ("Zabij 5 witherowych szkieletów", "Kill 5 wither skeletons"),
    "kill_piglin_brute": ("Zabij 3 piglinie brutale", "Kill 3 piglin brutes"),
    "kill_ravager": ("Zabij ravagera", "Kill a ravager"),
    "kill_dragon": ("Pokonaj Smoka Kresu", "Defeat the Ender Dragon"),
}


def main() -> None:
    pl, en = {}, {}
    for key, (p, e) in COMMON.items():
        pl[key], en[key] = p, e
    for gid, (p, e) in GOALS.items():
        pl[f"bmd.goal.{gid}"], en[f"bmd.goal.{gid}"] = p, e

    LANG_DIR.mkdir(parents=True, exist_ok=True)
    for name, data in (("pl_pl", pl), ("en_us", en)):
        path = LANG_DIR / f"{name}.json"
        path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        print(f"  {path.name}: {len(data)} kluczy")

    # Zgodnosc z lista celow w kodzie - inaczej cel bez opisu wyszedlby
    # w grze jako goly klucz tlumaczenia.
    goal_java = (Path(__file__).resolve().parent.parent
                 / "src/main/java/dev/kajor/bmd/goal/Goal.java").read_text()
    import re
    ids = set(re.findall(r'(?:have|kill|reach)\("([a-z_0-9]+)"', goal_java))
    missing = ids - set(GOALS)
    extra = set(GOALS) - ids
    if missing:
        raise SystemExit(f"BRAK opisow dla celow: {sorted(missing)}")
    if extra:
        raise SystemExit(f"Opisy bez celu w kodzie: {sorted(extra)}")
    print(f"\nwszystkie {len(ids)} celow ma opis w obu jezykach")


if __name__ == "__main__":
    main()
