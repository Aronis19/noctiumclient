package dev.prchl.donututilities.module;

public final class HudSettings {
    private boolean baseScan = true;
    private boolean espStats = true;
    private boolean spotifyHud = true;
    private boolean coordinates = true;
    private boolean realTime = true;
    private boolean ping = true;
    private boolean ticks = true;
    private boolean bps = true;

    public boolean baseScan() {
        return baseScan;
    }

    public void toggleBaseScan() {
        baseScan = !baseScan;
    }

    public boolean espStats() {
        return espStats;
    }

    public void toggleEspStats() {
        espStats = !espStats;
    }

    public boolean spotifyHud() {
        return spotifyHud;
    }

    public void toggleSpotifyHud() {
        spotifyHud = !spotifyHud;
    }

    public boolean coordinates() {
        return coordinates;
    }

    public void toggleCoordinates() {
        coordinates = !coordinates;
    }

    public boolean realTime() {
        return realTime;
    }

    public void toggleRealTime() {
        realTime = !realTime;
    }

    public boolean ping() {
        return ping;
    }

    public void togglePing() {
        ping = !ping;
    }

    public boolean ticks() {
        return ticks;
    }

    public void toggleTicks() {
        ticks = !ticks;
    }

    public boolean bps() {
        return bps;
    }

    public void toggleBps() {
        bps = !bps;
    }
}
