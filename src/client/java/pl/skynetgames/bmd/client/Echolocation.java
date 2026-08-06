package pl.skynetgames.bmd.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/**
 * Zamiast efektu Blindness (ktory da sie rozjasnic gamma i tak czy owak pozwala
 * widziec bloki tuz przy twarzy) slepy dostaje pelna czern plus to: kazdy dzwiek
 * w poblizu zapala na chwile wskaznik kierunku na krawedzi ekranu.
 */
public final class Echolocation {

    private static final long ECHO_LIFETIME_MS = 1400L;
    private static final int MAX_ECHOES = 24;

    public static void onSound(SoundInstance sound) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        // dzwieki interfejsu nie maja pozycji w swiecie - nie ma czego wskazywac
        if (sound.isRelative() || sound.getSource() == SoundSource.MUSIC || sound.getSource() == SoundSource.MASTER) {
            return;
        }

        double range = ClientState.echoRange;
        double dx = sound.getX() - mc.player.getX();
        double dy = sound.getY() - mc.player.getY();
        double dz = sound.getZ() - mc.player.getZ();
        if (dx * dx + dy * dy + dz * dz > range * range) return;

        long now = System.currentTimeMillis();
        synchronized (ClientState.ECHOES) {
            if (ClientState.ECHOES.size() >= MAX_ECHOES) ClientState.ECHOES.removeFirst();
            ClientState.ECHOES.add(new ClientState.Echo(
                    sound.getX(), sound.getY(), sound.getZ(), now, now + ECHO_LIFETIME_MS));
        }
    }

    /** Sprzatanie wygaslych - wolane co tick. */
    public static void tick() {
        long now = System.currentTimeMillis();
        synchronized (ClientState.ECHOES) {
            ClientState.ECHOES.removeIf(e -> e.expiresAt() < now);
        }
    }

    private Echolocation() {
    }
}
