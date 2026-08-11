package dev.kajor.bmd;

/**
 * Czysta matematyka babelkow i kola gestow - bez ani jednej klasy Minecrafta,
 * dzieki czemu da sie ja odpalic i sprawdzic zwykla java:
 *   java src/main/java/dev/kajor/bmd/Geometry.java
 */
public final class Geometry {

    /**
     * Punkt swiata -> piksel ekranu. Zwraca null, gdy punkt jest za kamera
     * albo poza ekranem (z marginesem, zeby babelek nie wisial polowa poza kadrem).
     *
     * @param yawDeg   obrot kamery w poziomie (stopnie, konwencja Minecrafta)
     * @param pitchDeg obrot kamery w pionie
     * @param fovDeg   pionowy kat widzenia
     */
    public static int[] project(double camX, double camY, double camZ,
                                double yawDeg, double pitchDeg, double fovDeg,
                                int guiW, int guiH,
                                double tx, double ty, double tz,
                                double maxDist) {
        double vx = tx - camX;
        double vy = ty - camY;
        double vz = tz - camZ;
        if (vx * vx + vy * vy + vz * vz > maxDist * maxDist) return null;

        double yaw = Math.toRadians(yawDeg);
        double pitch = Math.toRadians(pitchDeg);
        double cosPitch = Math.cos(pitch);

        // baza kamery: forward tam, gdzie patrzy gracz; right w prawo; up = right x forward.
        // Uwaga na konwencje Minecrafta: przy yaw=0 patrzymy na poludnie (+Z), wiec prawa
        // reka wskazuje zachod (-X) - stad minusy w r. Z (+cos, +sin) wychodzi lewo i caly
        // HUD jest odbity w lustrze.
        double fx = -Math.sin(yaw) * cosPitch, fy = -Math.sin(pitch), fz = Math.cos(yaw) * cosPitch;
        double rx = -Math.cos(yaw), ry = 0.0D, rz = -Math.sin(yaw);
        double ux = ry * fz - rz * fy, uy = rz * fx - rx * fz, uz = rx * fy - ry * fx;

        double z = vx * fx + vy * fy + vz * fz;
        if (z < 0.15D) return null;

        double tanHalfFov = Math.tan(Math.toRadians(fovDeg) / 2.0D);
        if (tanHalfFov <= 0.0D) return null;

        double aspect = (double) guiW / guiH;
        double sx = guiW / 2.0D + ((vx * rx + vy * ry + vz * rz) / (z * tanHalfFov * aspect)) * (guiW / 2.0D);
        double sy = guiH / 2.0D - ((vx * ux + vy * uy + vz * uz) / (z * tanHalfFov)) * (guiH / 2.0D);

        if (sx < -64 || sx > guiW + 64 || sy < -64 || sy > guiH + 64) return null;
        return new int[]{(int) sx, (int) sy};
    }

    /**
     * Rownomiernie rozrzucone kierunki na sferze (spirala Fibonacciego) - promienie,
     * ktore echolokacja wystrzeliwuje ze zrodla dzwieku, zeby obmacac ksztalt pokoju.
     * Rownomiernie, bo siatka po dlugosci/szerokosci zageszcza promienie na biegunach:
     * sufit i podloga bylyby gestsze od scian.
     *
     * @return tablica n*3 skladowych x,y,z (wektory jednostkowe)
     */
    public static float[] sphereDirections(int n) {
        float[] dirs = new float[n * 3];
        double golden = Math.PI * (3.0D - Math.sqrt(5.0D));
        for (int i = 0; i < n; i++) {
            double y = n == 1 ? 0.0D : 1.0D - 2.0D * i / (n - 1);
            double r = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            double theta = golden * i;
            dirs[i * 3] = (float) (Math.cos(theta) * r);
            dirs[i * 3 + 1] = (float) y;
            dirs[i * 3 + 2] = (float) (Math.sin(theta) * r);
        }
        return dirs;
    }

    /**
     * Jasnosc punktu trafionego przez rozchodzaca sie fale dzwieku.
     *
     * Fala leci ze zrodla z predkoscia speed - punkt oddalony o dist zapala sie
     * dopiero, gdy fala do niego dotrze, i od tej chwili gasnie przez fadeMs.
     * Dzieki temu pokoj nie pojawia sie caly naraz, tylko rozlewa sie od zrodla.
     *
     * @return 1.0 tuz po odbiciu, 0.0 gdy fala jeszcze nie dotarla albo juz zgaslo
     */
    public static float waveFade(long ageMs, double dist, double speed, long fadeMs) {
        double sinceHit = ageMs - dist / speed * 1000.0D;
        if (sinceHit < 0.0D || sinceHit >= fadeMs) return 0.0F;
        return (float) (1.0D - sinceHit / fadeMs);
    }

    /**
     * Ktory sektor kola wskazuje mysz. Srodek to strefa martwa, inaczej kazde
     * drgniecie myszy cos wybiera.
     *
     * @return indeks sektora albo -1
     */
    public static int sector(double dx, double dy, int count, double deadZone) {
        if (dx * dx + dy * dy < deadZone * deadZone) return -1;
        double angle = Math.toDegrees(Math.atan2(dx, -dy));
        if (angle < 0) angle += 360.0D;
        double step = 360.0D / count;
        return (int) Math.floor(((angle + step / 2) % 360.0D) / step);
    }

    /** Kat srodka sektora w radianach - do rozstawienia ikon na okregu. */
    public static double sectorAngle(int index, int count) {
        return Math.toRadians(index * (360.0D / count));
    }

    private Geometry() {
    }

    // --- self-check ---------------------------------------------------------

    public static void main(String[] args) {
        // Sektory: gora to 0, potem zgodnie z ruchem wskazowek zegara.
        check(sector(0, -50, 8, 20) == 0, "gora = sektor 0");
        check(sector(50, 0, 8, 20) == 2, "prawo = sektor 2");
        check(sector(0, 50, 8, 20) == 4, "dol = sektor 4");
        check(sector(-50, 0, 8, 20) == 6, "lewo = sektor 6");
        check(sector(5, 5, 8, 20) == -1, "strefa martwa");

        // Kolo miewa rozna liczbe gestow - kazda ikona musi trafiac we wlasny sektor,
        // inaczej klikniecie w ikone wysyla sasiedni gest.
        for (int count : new int[]{4, 8, 12, 15, 16}) {
            for (int i = 0; i < count; i++) {
                double a = sectorAngle(i, count);
                int got = sector(Math.sin(a) * 50, -Math.cos(a) * 50, count, 20);
                check(got == i, "przy " + count + " sektorach ikona " + i
                        + " trafia w swoj sektor (dostalem " + got + ")");
            }
        }

        // Projekcja: cel dokladnie na wprost ladu je w srodku ekranu.
        int[] center = project(0, 0, 0, 0, 0, 70, 800, 600, 0, 0, 10, 64);
        check(center != null, "cel przed kamera jest widoczny");
        check(Math.abs(center[0] - 400) <= 1 && Math.abs(center[1] - 300) <= 1,
                "cel na wprost = srodek ekranu, dostalem " + center[0] + "," + center[1]);

        // yaw=0 patrzy na poludnie (+Z), wiec +X (wschod) jest po LEWEJ - to test na odbicie lustrzane
        int[] east = project(0, 0, 0, 0, 0, 70, 800, 600, 3, 0, 10, 64);
        check(east != null && east[0] < 400, "cel w +X jest po lewej");
        // ...a przy yaw=90 (patrzymy na zachod, -X) po prawej jest polnoc (-Z)
        int[] eastFromWest = project(0, 0, 0, 90, 0, 70, 800, 600, -10, 0, -3, 64);
        check(eastFromWest != null && eastFromWest[0] > 400, "przy yaw=90 cel w -Z jest po prawej");

        // cel wyzej = wyzej na ekranie (mniejszy Y)
        int[] up = project(0, 0, 0, 0, 0, 70, 800, 600, 0, 3, 10, 64);
        check(up != null && up[1] < 300, "cel wyzej jest wyzej na ekranie");

        check(project(0, 0, 0, 0, 0, 70, 800, 600, 0, 0, -10, 64) == null, "cel za kamera odrzucony");
        check(project(0, 0, 0, 0, 0, 70, 800, 600, 0, 0, 200, 64) == null, "cel za daleko odrzucony");

        // obrot o 180 stopni: ten sam punkt ma trafic za kamere
        check(project(0, 0, 0, 180, 0, 70, 800, 600, 0, 0, 10, 64) == null,
                "po obrocie o 180 stopni cel jest za plecami");

        // Promienie echolokacji: same wektory jednostkowe i zadnego zbitka w jednym miejscu.
        float[] dirs = sphereDirections(96);
        check(dirs.length == 96 * 3, "96 kierunkow to 288 skladowych");
        double minY = 1.0D, maxY = -1.0D;
        for (int i = 0; i < 96; i++) {
            double len = Math.sqrt(dirs[i * 3] * dirs[i * 3] + dirs[i * 3 + 1] * dirs[i * 3 + 1]
                    + dirs[i * 3 + 2] * dirs[i * 3 + 2]);
            check(Math.abs(len - 1.0D) < 1e-5, "kierunek " + i + " jest jednostkowy (dlugosc " + len + ")");
            minY = Math.min(minY, dirs[i * 3 + 1]);
            maxY = Math.max(maxY, dirs[i * 3 + 1]);
        }
        check(minY < -0.98D && maxY > 0.98D, "promienie siegaja i podlogi, i sufitu");
        check(sphereDirections(1).length == 3, "jeden promien nie dzieli przez zero");

        // Fala: 28 blokow na sekunde, punkt 28 blokow dalej zapala sie po sekundzie.
        check(waveFade(500, 28, 28.0D, 1000) == 0.0F, "fala jeszcze nie doleciala");
        check(Math.abs(waveFade(1000, 28, 28.0D, 1000) - 1.0F) < 1e-4F, "swieze odbicie swieci pelnia");
        check(Math.abs(waveFade(1500, 28, 28.0D, 1000) - 0.5F) < 1e-4F, "w polowie zaniku polowa jasnosci");
        check(waveFade(2000, 28, 28.0D, 1000) == 0.0F, "po zaniku punkt gasnie");
        check(waveFade(0, 0, 28.0D, 1000) == 1.0F, "punkt w samym zrodle zapala sie od razu");

        System.out.println("Geometry: wszystkie sprawdzenia przeszly");
    }

    private static void check(boolean condition, String what) {
        if (!condition) throw new AssertionError("nie zgadza sie: " + what);
    }
}
