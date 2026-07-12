package dev.prchl.donututilities.module;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EspSettings {
    private static final int DEFAULT_RED = 121;
    private static final int DEFAULT_GREEN = 200;
    private static final int DEFAULT_BLUE = 255;
    private boolean traces = true;
    private boolean entityTraces = true;
    private boolean blockTraces = true;
    private int traceDistance = 512;
    private int traceAlpha = 65;
    private int traceRed = DEFAULT_RED;
    private int traceGreen = DEFAULT_GREEN;
    private int traceBlue = DEFAULT_BLUE;
    private final Map<String, int[]> traceColors = new LinkedHashMap<>();

    public boolean traces() {
        return traces;
    }

    public void toggleTraces() {
        traces = !traces;
    }

    public boolean entityTraces() {
        return entityTraces;
    }

    public void toggleEntityTraces() {
        entityTraces = !entityTraces;
    }

    public boolean blockTraces() {
        return blockTraces;
    }

    public void toggleBlockTraces() {
        blockTraces = !blockTraces;
    }

    public int traceDistance() {
        return traceDistance;
    }

    public void setTraceDistance(int traceDistance) {
        this.traceDistance = clamp(traceDistance, 32, 1024);
    }

    public int traceAlpha() {
        return traceAlpha;
    }

    public void setTraceAlpha(int traceAlpha) {
        this.traceAlpha = clamp(traceAlpha, 15, 100);
    }

    public int traceRed() {
        return traceRed;
    }

    public void setTraceRed(int traceRed) {
        this.traceRed = clamp(traceRed, 0, 255);
    }

    public int traceGreen() {
        return traceGreen;
    }

    public void setTraceGreen(int traceGreen) {
        this.traceGreen = clamp(traceGreen, 0, 255);
    }

    public int traceBlue() {
        return traceBlue;
    }

    public void setTraceBlue(int traceBlue) {
        this.traceBlue = clamp(traceBlue, 0, 255);
    }

    public int traceRed(String moduleId) {
        return color(moduleId)[0];
    }

    public int traceGreen(String moduleId) {
        return color(moduleId)[1];
    }

    public int traceBlue(String moduleId) {
        return color(moduleId)[2];
    }

    public void setTraceRed(String moduleId, int value) {
        color(moduleId)[0] = clamp(value, 0, 255);
    }

    public void setTraceGreen(String moduleId, int value) {
        color(moduleId)[1] = clamp(value, 0, 255);
    }

    public void setTraceBlue(String moduleId, int value) {
        color(moduleId)[2] = clamp(value, 0, 255);
    }

    public Map<String, int[]> traceColors() {
        return traceColors;
    }

    public void setTraceColor(String moduleId, int red, int green, int blue) {
        traceColors.put(moduleId, new int[] {clamp(red, 0, 255), clamp(green, 0, 255), clamp(blue, 0, 255)});
    }

    private int[] color(String moduleId) {
        return traceColors.computeIfAbsent(moduleId, ignored -> new int[] {traceRed, traceGreen, traceBlue});
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
