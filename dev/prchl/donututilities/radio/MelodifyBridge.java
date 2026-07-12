package dev.prchl.donututilities.radio;

import java.lang.reflect.Method;

public final class MelodifyBridge {
    private static final long POLL_INTERVAL_MS = 500L;
    private static long nextPollAt;
    private static Snapshot cached = Snapshot.unavailable();
    private static boolean resolved;
    private static Method getInstance;
    private static Method getCurrentTrack;
    private static Method getTitle;
    private static Method getArtist;
    private static Method getProgressMs;
    private static Method getDurationMs;
    private static Method isPlaying;

    private MelodifyBridge() {
    }

    public static synchronized Snapshot currentTrack() {
        long now = System.currentTimeMillis();
        if (now < nextPollAt) {
            return cached;
        }
        nextPollAt = now + POLL_INTERVAL_MS;

        try {
            resolve();
            Object client = getInstance.invoke(null);
            Object track = getCurrentTrack.invoke(client);
            if (track == null) {
                cached = Snapshot.empty();
            } else {
                cached = new Snapshot(
                        string(getTitle.invoke(track), "Unknown title"),
                        string(getArtist.invoke(track), "Unknown artist"),
                        number(getProgressMs.invoke(track)),
                        number(getDurationMs.invoke(track)),
                        Boolean.TRUE.equals(isPlaying.invoke(track)),
                        true);
            }
        } catch (Throwable ignored) {
            cached = Snapshot.unavailable();
            resolved = false;
        }
        return cached;
    }

    private static void resolve() throws ReflectiveOperationException {
        if (resolved) {
            return;
        }
        Class<?> api = Class.forName("aqys.melodify.client.SpotifyApiClient");
        Class<?> track = Class.forName("aqys.melodify.client.TrackInfo");
        getInstance = api.getMethod("getInstance");
        getCurrentTrack = api.getMethod("getCurrentTrack");
        getTitle = track.getMethod("getTitle");
        getArtist = track.getMethod("getArtist");
        getProgressMs = track.getMethod("getProgressMs");
        getDurationMs = track.getMethod("getDurationMs");
        isPlaying = track.getMethod("isPlaying");
        resolved = true;
    }

    private static String string(Object value, String fallback) {
        return value instanceof String text && !text.isBlank() ? text : fallback;
    }

    private static long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    public record Snapshot(String title, String artist, long progressMs, long durationMs, boolean playing, boolean available) {
        public static Snapshot unavailable() {
            return new Snapshot("MELODIFY NOT FOUND", "Install Melodify for Spotify data", 0L, 0L, false, false);
        }

        public static Snapshot empty() {
            return new Snapshot("NOT PLAYING", "Spotify has no active track", 0L, 0L, false, true);
        }
    }
}
