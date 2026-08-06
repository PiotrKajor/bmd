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

        // baza kamery: forward tam, gdzie patrzy gracz; right w prawo; up = forward x right
        double fx = -Math.sin(yaw) * cosPitch, fy = -Math.sin(pitch), fz = Math.cos(yaw) * cosPitch;
        double rx = Math.cos(yaw), ry = 0.0D, rz = Math.sin(yaw);
        double ux = fy * rz - fz * ry, uy = fz * rx - fx * rz, uz = fx * ry - fy * rx;

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
        for (int i = 0; i < 8; i++) {
            double a = sectorAngle(i, 8);
            int got = sector(Math.sin(a) * 50, -Math.cos(a) * 50, 8, 20);
            check(got == i, "ikona sektora " + i + " trafia w swoj sektor (dostalem " + got + ")");
        }

        // Projekcja: cel dokladnie na wprost ladu je w srodku ekranu.
        int[] center = project(0, 0, 0, 0, 0, 70, 800, 600, 0, 0, 10, 64);
        check(center != null, "cel przed kamera jest widoczny");
        check(Math.abs(center[0] - 400) <= 1 && Math.abs(center[1] - 300) <= 1,
                "cel na wprost = srodek ekranu, dostalem " + center[0] + "," + center[1]);

        // yaw=0 patrzy w +Z, wiec cel w +X musi wyladowac po prawej stronie
        int[] right = project(0, 0, 0, 0, 0, 70, 800, 600, 3, 0, 10, 64);
        check(right != null && right[0] > 400, "cel w +X jest po prawej");

        // cel wyzej = wyzej na ekranie (mniejszy Y)
        int[] up = project(0, 0, 0, 0, 0, 70, 800, 600, 0, 3, 10, 64);
        check(up != null && up[1] < 300, "cel wyzej jest wyzej na ekranie");

        check(project(0, 0, 0, 0, 0, 70, 800, 600, 0, 0, -10, 64) == null, "cel za kamera odrzucony");
        check(project(0, 0, 0, 0, 0, 70, 800, 600, 0, 0, 200, 64) == null, "cel za daleko odrzucony");

        // obrot o 180 stopni: ten sam punkt ma trafic za kamere
        check(project(0, 0, 0, 180, 0, 70, 800, 600, 0, 0, 10, 64) == null,
                "po obrocie o 180 stopni cel jest za plecami");

        System.out.println("Geometry: wszystkie sprawdzenia przeszly");
    }

    private static void check(boolean condition, String what) {
        if (!condition) throw new AssertionError("nie zgadza sie: " + what);
    }
}
