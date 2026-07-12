package dev.prchl.donututilities.module;

import java.util.Locale;

public final class BrandSpooferSettings {
    private String brand = "vanilla";

    public String brand() {
        return brand;
    }

    public void setBrand(String brand) {
        String clean = brand == null ? "" : brand.trim().toLowerCase(Locale.ROOT);
        if (clean.isBlank()) {
            clean = "vanilla";
        }
        if (clean.length() > 32) {
            clean = clean.substring(0, 32);
        }
        this.brand = clean;
    }
}
