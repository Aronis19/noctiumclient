package dev.prchl.donututilities.module;

import dev.prchl.donututilities.DonutUtilitiesClient;
import dev.prchl.donututilities.freecam.FreecamController;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import org.lwjgl.glfw.GLFW;

public final class FreecamModule extends Module {
    public static final int MIN_SPEED = 1;
    public static final int MAX_SPEED = 50;

    private class_304 keyMapping;
    private boolean keyWasDown;
    private boolean capturingKey;
    private int keyCode = GLFW.GLFW_KEY_F4;
    private int speed = 4;

    public FreecamModule() {
        super("freecam", "Freecam", ModuleCategory.RENDER, "Moves the client camera separately from the player.", 0xFF82CFFF);
    }

    public void registerKeybind() {
        if (keyMapping != null) {
            return;
        }

        keyMapping = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.donututilities.toggle_freecam",
                class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_F4,
                class_304.class_11900.field_62556));
    }

    public void tickKeybind(class_310 client) {
        if (client == null || client.method_22683() == null || client.field_1755 != null || capturingKey) {
            keyWasDown = false;
            return;
        }

        boolean rawKeyDown = GLFW.glfwGetKey(client.method_22683().method_4490(), keyCode) == GLFW.GLFW_PRESS;
        if (rawKeyDown && !keyWasDown) {
            toggle();
            DonutUtilitiesClient.saveConfig();
        }
        keyWasDown = rawKeyDown;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == enabled()) {
            return;
        }

        super.setEnabled(enabled);
        if (enabled) {
            FreecamController.enable(class_310.method_1551());
        } else {
            FreecamController.disable();
        }
    }

    @Override
    public void tick(class_310 client) {
        if (client.field_1687 == null || client.field_1724 == null) {
            setEnabled(false);
            return;
        }

        FreecamController.tick(client, speed);
    }

    public int speed() {
        return speed;
    }

    public int keyCode() {
        return keyCode;
    }

    public void setStoredKeyCode(int keyCode) {
        if (keyCode != GLFW.GLFW_KEY_UNKNOWN && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            this.keyCode = keyCode;
        }
    }

    public void setSpeed(int speed) {
        this.speed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed));
    }

    public String keyName() {
        if (capturingKey) {
            return "PRESS KEY";
        }

        return keyName(keyCode);
    }

    public boolean capturingKey() {
        return capturingKey;
    }

    public void startCapturingKey() {
        capturingKey = true;
    }

    public void stopCapturingKey() {
        capturingKey = false;
    }

    public void setKeyCode(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            capturingKey = false;
            return;
        }

        this.keyCode = keyCode;
        this.keyWasDown = true;
        this.capturingKey = false;
    }

    private String keyName(int keyCode) {
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null && !name.isBlank()) {
            return name.toUpperCase(java.util.Locale.ROOT);
        }

        return switch (keyCode) {
            case GLFW.GLFW_KEY_F1 -> "F1";
            case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3";
            case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5";
            case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7";
            case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_PAGE_UP -> "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PGDN";
            default -> "KEY " + keyCode;
        };
    }
}
