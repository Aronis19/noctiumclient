package dev.prchl.donututilities.module;

public final class MenuScaleSettings {
    public static final int MIN_PERCENT = 60;
    public static final int MAX_PERCENT = 125;

    private int percent = 90;

    public int percent() {
        return percent;
    }

    public float scale() {
        return percent / 100.0F;
    }

    public void setPercent(int percent) {
        this.percent = Math.max(MIN_PERCENT, Math.min(MAX_PERCENT, percent));
    }
}
