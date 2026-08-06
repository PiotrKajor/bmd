package pl.skynetgames.bmd.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Ikona przedmiotu wstawiona wprost w tekst (nametag) jako sprite z atlasu.
 *
 * Przedmioty maja plaska teksture w atlasie przedmiotow, ale bloki jej nie maja -
 * one sa rysowane z modelu 3D, a ich tekstury siedza w atlasie blokow pod innymi
 * nazwami (kamien to block/stone, ale dab to block/oak_planks, nie block/oak_log).
 * Dlatego zamiast zgadywac, sprawdzamy po kolei kandydatow i bierzemy pierwszego,
 * ktory naprawde jest w atlasie - reszta odpada po tym, ze atlas zwraca
 * teksture zastepcza.
 */
public final class ItemIcon {

    /** Wynik jest staly w ramach sesji, a nametagi rysuja sie co klatke. */
    private static final Map<Identifier, Component> CACHE = new HashMap<>();

    public static Component of(Identifier itemId) {
        return CACHE.computeIfAbsent(itemId, ItemIcon::resolve);
    }

    private static Component resolve(Identifier id) {
        String ns = id.getNamespace();
        String path = id.getPath();

        // Kolejnosc ma znaczenie: przedmiot przed blokiem, bo np. drzwi maja
        // i plaska ikone w ekwipunku, i teksture bloku - chcemy tej pierwszej.
        Component icon = trySprite(TextureAtlas.LOCATION_ITEMS, ns, "item/" + path);
        if (icon == null) icon = trySprite(TextureAtlas.LOCATION_BLOCKS, ns, "block/" + path);
        // Czesc blokow trzyma teksture pod nazwa z sufiksem strony
        if (icon == null) icon = trySprite(TextureAtlas.LOCATION_BLOCKS, ns, "block/" + path + "_side");
        if (icon == null) icon = trySprite(TextureAtlas.LOCATION_BLOCKS, ns, "block/" + path + "_top");

        return icon == null ? Component.empty() : icon;
    }

    /** null, gdy takiego sprite'a nie ma - atlas oddaje wtedy teksture zastepcza. */
    private static Component trySprite(Identifier atlasId, String ns, String path) {
        try {
            Identifier spriteId = Identifier.fromNamespaceAndPath(ns, path);
            TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(atlasId);
            TextureAtlasSprite sprite = atlas.getSprite(spriteId);
            if (sprite == null || sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
                return null;
            }
            return Component.literal(" ").withStyle(Style.EMPTY.withFont(
                    new FontDescription.AtlasSprite(atlasId, spriteId)));
        } catch (RuntimeException e) {
            // brak atlasu (np. zbyt wczesnie po starcie) nie moze wywalic nametaga
            return null;
        }
    }

    private ItemIcon() {
    }
}
