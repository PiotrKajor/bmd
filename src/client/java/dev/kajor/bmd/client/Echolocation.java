package dev.kajor.bmd.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import dev.kajor.bmd.Geometry;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Slepy nie widzi swiata, tylko jego echo. Kazdy dzwiek wystrzeliwuje ze swojego
 * zrodla snop promieni; tam, gdzie trafiaja w bloki, zostaja punkty. Fala rozchodzi
 * sie w czasie, wiec pokoj nie pojawia sie plaska plamka, tylko rozlewa od zrodla
 * i gasnie - ksztalt scian, podlogi i sufitu widac w trzech wymiarach.
 *
 * Promienie leca ze zrodla, nie z oczu: to, co zaslonieta scianka od strony dzwieku,
 * nie zapali sie wcale, a to, co slychac zza pleców, zapali sie tak samo jak reszta.
 */
public final class Echolocation {

    /** Ile blokow na sekunde pokonuje fala. Nie predkosc dzwieku - predkosc czytelna. */
    public static final double WAVE_SPEED = 30.0D;
    /** Jak dlugo swieci punkt od chwili, gdy dosiegla go fala. */
    public static final long POINT_FADE_MS = 1600L;
    /** Znacznik samego zrodla zyje krocej - ma mowic "teraz", nie "gdzies tam". */
    public static final long CORE_FADE_MS = 1400L;

    private static final int MAX_ECHOES = 10;
    private static final int RAYS = 96;
    /** Skanow na tick - kazdy to 96 raycastow, wiec nie wszystkie naraz. */
    private static final int SCANS_PER_TICK = 2;
    /** Kolejka nie moze puchnac, gdy gracz stoi w tlumie mobow. */
    private static final int MAX_PENDING = 24;
    /** Dwa dzwieki z tego samego miejsca w tej samej chwili daja ten sam skan. */
    private static final double DEDUPE_DIST = 1.5D;
    private static final long DEDUPE_MS = 200L;

    private static final float[] DIRS = Geometry.sphereDirections(RAYS);

    /** Dzwiek moze przyjsc z watku silnika audio - skan robimy dopiero w ticku. */
    private static final Queue<double[]> PENDING = new ConcurrentLinkedQueue<>();

    public static void onSound(SoundInstance sound) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        // dzwieki interfejsu nie maja pozycji w swiecie - nie ma czego oswietlac
        if (sound.isRelative() || sound.getSource() == SoundSource.MUSIC || sound.getSource() == SoundSource.MASTER) {
            return;
        }

        double range = ClientState.echoRange;
        double dx = sound.getX() - mc.player.getX();
        double dy = sound.getY() - mc.player.getY();
        double dz = sound.getZ() - mc.player.getZ();
        if (dx * dx + dy * dy + dz * dz > range * range) return;

        if (PENDING.size() >= MAX_PENDING) PENDING.poll();
        PENDING.add(new double[]{sound.getX(), sound.getY(), sound.getZ()});
    }

    /** Sprzatanie wygaslych i skan nowych - wolane co tick, na watku klienta. */
    public static void tick() {
        long now = System.currentTimeMillis();
        synchronized (ClientState.ECHOES) {
            ClientState.ECHOES.removeIf(e -> e.expiresAt() < now);
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            PENDING.clear();
            return;
        }

        double range = ClientState.echoRange;
        long life = (long) (range / WAVE_SPEED * 1000.0D) + POINT_FADE_MS;
        for (int scans = 0; scans < SCANS_PER_TICK; scans++) {
            double[] src = PENDING.poll();
            if (src == null) break;
            if (isDuplicate(src, now)) {
                scans--;
                continue;
            }
            float[] cloud = scan(mc.level, mc.player, src[0], src[1], src[2], range);
            synchronized (ClientState.ECHOES) {
                if (ClientState.ECHOES.size() >= MAX_ECHOES) ClientState.ECHOES.removeFirst();
                ClientState.ECHOES.add(new ClientState.Echo(src[0], src[1], src[2], now, now + life, cloud));
            }
        }
    }

    private static boolean isDuplicate(double[] src, long now) {
        synchronized (ClientState.ECHOES) {
            for (ClientState.Echo e : ClientState.ECHOES) {
                if (now - e.bornAt() > DEDUPE_MS) continue;
                double dx = e.x() - src[0], dy = e.y() - src[1], dz = e.z() - src[2];
                if (dx * dx + dy * dy + dz * dz < DEDUPE_DIST * DEDUPE_DIST) return true;
            }
        }
        return false;
    }

    /**
     * Snop promieni ze zrodla dzwieku. Kazdy trafiony blok zostawia punkt zapisany
     * wzgledem zrodla (float wystarcza na kilkanascie blokow, a na wspolrzednych
     * swiata przy granicy mapy juz nie) razem z dystansem i rodzajem powierzchni.
     *
     * @return tablica po 5 liczb na punkt: dx, dy, dz, dystans, rodzaj (0 podloga, 1 sciana, 2 sufit)
     */
    private static float[] scan(Level level, Entity self, double sx, double sy, double sz, double range) {
        float[] out = new float[RAYS * 5];
        Vec3 from = new Vec3(sx, sy, sz);
        int n = 0;
        for (int i = 0; i < RAYS; i++) {
            Vec3 to = new Vec3(sx + DIRS[i * 3] * range, sy + DIRS[i * 3 + 1] * range, sz + DIRS[i * 3 + 2] * range);
            BlockHitResult hit = level.clip(new ClipContext(
                    from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, self));
            if (hit.getType() != HitResult.Type.BLOCK) continue;

            Vec3 at = hit.getLocation();
            float dx = (float) (at.x - sx), dy = (float) (at.y - sy), dz = (float) (at.z - sz);
            Direction face = hit.getDirection();
            out[n++] = dx;
            out[n++] = dy;
            out[n++] = dz;
            out[n++] = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            out[n++] = face == Direction.UP ? 0 : (face == Direction.DOWN ? 2 : 1);
        }
        return Arrays.copyOf(out, n);
    }

    private Echolocation() {
    }
}
