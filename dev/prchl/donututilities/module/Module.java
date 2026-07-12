package dev.prchl.donututilities.module;

import dev.prchl.donututilities.DonutUtilitiesClient;
import net.minecraft.class_310;

public class Module {
    private final String id;
    private final String name;
    private final ModuleCategory category;
    private final String description;
    private final int color;
    private boolean enabled;

    public Module(String id, String name, ModuleCategory category, String description, int color) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public ModuleCategory category() {
        return category;
    }

    public String description() {
        return description;
    }

    public int color() {
        return color;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        DonutUtilitiesClient.markConfigDirty();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void tick(class_310 client) {
    }
}
