package dev.prchl.donututilities.module;

import java.util.Locale;
import net.minecraft.class_1799;

public final class ItemInfoModule extends Module {
    public static final String NAMETAG_MARKER = "\uE000";
    private static final int DEFAULT_TEXT_COLOR = 0xFFE2E4EA;
    private String textColorHex = "#E2E4EA";
    private int textColor = DEFAULT_TEXT_COLOR;

    public ItemInfoModule() {
        super("item_info", "Item Info", ModuleCategory.MISC, "Shows floating item stack labels in the world.", 0xFFB0BEC5);
    }

    public String textColorHex() {
        return textColorHex;
    }

    public int textColor() {
        return textColor;
    }

    public void setTextColorHex(String value) {
        String clean = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (clean.startsWith("#")) {
            clean = clean.substring(1);
        }
        clean = clean.replaceAll("[^0-9A-F]", "");
        if (clean.length() > 6) {
            clean = clean.substring(0, 6);
        }
        textColorHex = "#" + clean;
        if (clean.length() == 6) {
            try {
                textColor = 0xFF000000 | Integer.parseInt(clean, 16);
            } catch (NumberFormatException ignored) {
                textColor = DEFAULT_TEXT_COLOR;
            }
        }
    }

    public void appendTextColorChar(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        setTextColorHex(textColorHex + value);
    }

    public boolean removeLastTextColorChar() {
        String clean = textColorHex.startsWith("#") ? textColorHex.substring(1) : textColorHex;
        if (clean.isEmpty()) {
            return false;
        }
        setTextColorHex(clean.substring(0, clean.length() - 1));
        return true;
    }

    public static String label(class_1799 stack) {
        String name = stack.method_7964().getString();
        if (name.isBlank()) {
            name = stack.method_7909().toString().toLowerCase(Locale.ROOT);
        }
        return name + " " + stack.method_7947() + "x";
    }
}
