package dev.kajor.bmd.voice;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.LocationalSoundPacketEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.api.events.StaticSoundPacketEvent;
import dev.kajor.bmd.BmdMod;
import dev.kajor.bmd.BmdState;
import dev.kajor.bmd.Sense;

import java.util.UUID;

/**
 * Cala manipulacja glosem dzieje sie po stronie serwera, nie klienta.
 * Niemy ma anulowany pakiet mikrofonu, gluchy nie dostaje zadnych pakietow audio.
 * Klient nie ma tu nic do gadania - nie da sie tego obejsc konfiguracja SVC.
 *
 * Wpinane przez entrypoint "voicechat" w fabric.mod.json. Bez moda SVC ta klasa
 * nigdy sie nie zaladuje, wiec brak SVC niczego nie wywraca.
 */
public class BmdVoicechatPlugin implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return BmdMod.MOD_ID;
    }

    @Override
    public void registerEvents(EventRegistration reg) {
        reg.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
        // Trzy konkretne typy zamiast wspolnego SoundPacketEvent - rejestracja
        // w SVC idzie po dokladnej klasie eventu.
        reg.registerEvent(EntitySoundPacketEvent.class, this::onSound);
        reg.registerEvent(LocationalSoundPacketEvent.class, this::onSound);
        reg.registerEvent(StaticSoundPacketEvent.class, this::onSound);
        BmdMod.LOG.info("Podpieto sie pod Simple Voice Chat");
    }

    /** Niemy: mikrofon nie opuszcza serwera. */
    private void onMicrophone(MicrophonePacketEvent event) {
        if (senseOf(event.getSenderConnection()) == Sense.MUTE) {
            event.cancel();
        }
    }

    /** Gluchy: zaden pakiet audio do niego nie dolatuje. */
    private void onSound(SoundPacketEvent<?> event) {
        if (senseOf(event.getReceiverConnection()) == Sense.DEAF) {
            event.cancel();
        }
    }

    private static Sense senseOf(VoicechatConnection connection) {
        // Po ukonczeniu wyzwania glos wraca wszystkim.
        if (!BmdState.effectsActive()) return Sense.NONE;
        if (connection == null || connection.getPlayer() == null) return Sense.NONE;
        UUID uuid = connection.getPlayer().getUuid();
        return uuid == null ? Sense.NONE : BmdState.get(uuid);
    }
}
