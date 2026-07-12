package dev.prchl.donututilities.module;

import dev.prchl.donututilities.DonutUtilitiesClient;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;

public final class RadioModule extends Module {
    private class_304 keyMapping;
    private boolean keyWasDown;
    private boolean capturingKey;
    private int keyCode = GLFW.GLFW_KEY_F7;

    public RadioModule() {
        super("radio", "Radio", ModuleCategory.MISC, "Connects to Spotify and shows the Melodify HUD.", 0xFF9FE870);
    }

    public void registerKeybind() {
        if (keyMapping != null) {
            return;
        }
        keyMapping = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.donututilities.connect_spotify",
                class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_F7,
                class_304.class_11900.field_62556));
    }

    public void tickKeybind(class_310 client) {
        if (client == null || client.method_22683() == null || client.field_1755 != null || capturingKey) {
            keyWasDown = false;
            return;
        }
        boolean rawKeyDown = GLFW.glfwGetKey(client.method_22683().method_4490(), keyCode) == GLFW.GLFW_PRESS;
        if (rawKeyDown && !keyWasDown) {
            connect();
        }
        keyWasDown = rawKeyDown;
    }

    public void connect() {
        try {
            aqys.melodify.client.ConfigManager config = aqys.melodify.client.ConfigManager.getInstance();
            if (!config.hasCustomSpotifyCredentials()) {
                class_310 client = class_310.method_1551();
                Class<?> screenClass = Class.forName("aqys.melodify.client.SpotifyCredentialsScreen");
                Object screen = screenClass.getConstructors()[0].newInstance(client.field_1755);
                client.method_1507((class_437) screen);
                return;
            }
            aqys.melodify.client.SpotifyApiClient.getInstance().startAuthorization();
        } catch (Throwable exception) {
            DonutUtilitiesClient.LOGGER.warn("Could not start Spotify authorization", exception);
        }
    }

    public int keyCode() {
        return keyCode;
    }

    public String keyName() {
        return capturingKey ? "PRESS KEY" : keyName(keyCode);
    }

    public boolean capturingKey() {
        return capturingKey;
    }

    public void startCapturingKey() {
        capturingKey = true;
    }

    public void setStoredKeyCode(int keyCode) {
        if (keyCode != GLFW.GLFW_KEY_UNKNOWN && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            this.keyCode = keyCode;
        }
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
            default -> "KEY " + keyCode;
        };
    }
}
