package dev.prchl.donututilities.module;

import net.minecraft.class_310;

public final class FullbrightModule extends Module {
    private Double oldGamma;

    public FullbrightModule() {
        super("fullbright", "Fullbright", ModuleCategory.RENDER, "Raises client gamma while enabled.", 0xFFF5C542);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == enabled()) {
            return;
        }

        class_310 client = class_310.method_1551();
        if (enabled) {
            oldGamma = client.field_1690.method_42473().method_41753();
            client.field_1690.method_42473().method_41748(12.0);
        } else if (oldGamma != null) {
            client.field_1690.method_42473().method_41748(oldGamma);
            oldGamma = null;
        }

        super.setEnabled(enabled);
    }
}
