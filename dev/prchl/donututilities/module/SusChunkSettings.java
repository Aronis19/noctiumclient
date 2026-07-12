package dev.prchl.donututilities.module;

public final class SusChunkSettings {
    private int simulationDistance = 4;
    private int sensitivity = 3;
    private int alpha = 40;
    private boolean caveVines;
    private boolean vines;
    private boolean amethyst = true;
    private boolean bamboo;
    private boolean beeNest;
    private boolean rotatedDeepslate;
    private boolean kelp;

    public int simulationDistance() {
        return simulationDistance;
    }

    public void setSimulationDistance(int simulationDistance) {
        this.simulationDistance = clamp(simulationDistance, 2, 12);
    }

    public int sensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = clamp(sensitivity, 1, 10);
    }

    public int alpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = clamp(alpha, 10, 100);
    }

    public boolean caveVines() {
        return caveVines;
    }

    public void toggleCaveVines() {
        caveVines = !caveVines;
    }

    public boolean vines() {
        return vines;
    }

    public void toggleVines() {
        vines = !vines;
    }

    public boolean amethyst() {
        return amethyst;
    }

    public void toggleAmethyst() {
        amethyst = !amethyst;
    }

    public boolean bamboo() {
        return bamboo;
    }

    public void toggleBamboo() {
        bamboo = !bamboo;
    }

    public boolean beeNest() {
        return beeNest;
    }

    public void toggleBeeNest() {
        beeNest = !beeNest;
    }

    public boolean rotatedDeepslate() {
        return rotatedDeepslate;
    }

    public void toggleRotatedDeepslate() {
        rotatedDeepslate = !rotatedDeepslate;
    }

    public boolean kelp() {
        return kelp;
    }

    public void toggleKelp() {
        kelp = !kelp;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
