package dev.prchl.donututilities.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.prchl.donututilities.DonutUtilitiesClient;
import dev.prchl.donututilities.module.BlockEspSettings;
import dev.prchl.donututilities.module.BrandSpooferSettings;
import dev.prchl.donututilities.module.EspSettings;
import dev.prchl.donututilities.module.Module;
import dev.prchl.donututilities.module.ModuleManager;
import dev.prchl.donututilities.module.MenuScaleSettings;
import dev.prchl.donututilities.module.PopupSettings;
import dev.prchl.donututilities.module.HudSettings;
import dev.prchl.donututilities.module.SusChunkSettings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.class_310;

public final class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ClientConfig() {
    }

    public static void load(ModuleManager modules) {
        Path path = path();
        JsonObject root = readConfigWithBackup(path);
        if (root == null) {
            return;
        }

        try {
            JsonObject moduleObject = object(root, "modules");
            for (Module module : modules.modules()) {
                if (moduleObject.has(module.id())) {
                    module.setEnabled(moduleObject.get(module.id()).getAsBoolean());
                }
            }

            loadSus(modules.susChunkSettings(), object(root, "susChunkFinder"));
            loadEsp(modules.espSettings(), object(root, "esp"));
            loadBlockEsp(modules.blockEspSettings(), object(root, "blockEsp"));
            loadHud(modules.hudSettings(), object(root, "hud"));
            loadMenuScale(modules.menuScaleSettings(), object(root, "menuScale"));
            loadPopup(modules.popupSettings(), object(root, "popup"));
            loadBrandSpoofer(modules.brandSpooferSettings(), object(root, "brandSpoofer"));
            loadItemInfo(modules.itemInfoModule(), object(root, "itemInfo"));
            JsonObject freecam = object(root, "freecam");
            if (freecam.has("speed")) {
                modules.freecamModule().setSpeed(freecam.get("speed").getAsInt());
            }
            if (freecam.has("keyCode")) {
                modules.freecamModule().setStoredKeyCode(freecam.get("keyCode").getAsInt());
            }
            JsonObject radio = object(root, "radio");
            if (radio.has("keyCode")) {
                modules.radioModule().setStoredKeyCode(radio.get("keyCode").getAsInt());
            }
            loadSpotifyHud(object(root, "spotifyHud"));
        } catch (Exception exception) {
            DonutUtilitiesClient.LOGGER.warn("Could not load client config", exception);
        }
    }

    public static void save() {
        try {
            Path path = path();
            Files.createDirectories(path.getParent());

            JsonObject root = new JsonObject();
            JsonObject moduleObject = new JsonObject();
            for (Module module : DonutUtilitiesClient.MODULES.modules()) {
                moduleObject.addProperty(module.id(), module.enabled());
            }
            root.add("modules", moduleObject);
            root.add("susChunkFinder", saveSus(DonutUtilitiesClient.MODULES.susChunkSettings()));
            root.add("esp", saveEsp(DonutUtilitiesClient.MODULES.espSettings()));
            root.add("blockEsp", saveBlockEsp(DonutUtilitiesClient.MODULES.blockEspSettings()));
            root.add("hud", saveHud(DonutUtilitiesClient.MODULES.hudSettings()));
            root.add("menuScale", saveMenuScale(DonutUtilitiesClient.MODULES.menuScaleSettings()));
            root.add("popup", savePopup(DonutUtilitiesClient.MODULES.popupSettings()));
            root.add("brandSpoofer", saveBrandSpoofer(DonutUtilitiesClient.MODULES.brandSpooferSettings()));
            root.add("itemInfo", saveItemInfo(DonutUtilitiesClient.MODULES.itemInfoModule()));

            JsonObject freecam = new JsonObject();
            freecam.addProperty("speed", DonutUtilitiesClient.MODULES.freecamModule().speed());
            freecam.addProperty("keyCode", DonutUtilitiesClient.MODULES.freecamModule().keyCode());
            root.add("freecam", freecam);

            JsonObject radio = new JsonObject();
            radio.addProperty("keyCode", DonutUtilitiesClient.MODULES.radioModule().keyCode());
            root.add("radio", radio);
            root.add("spotifyHud", saveSpotifyHud());
            preservePreviousBlockSelection(path, root);
            writeWithBackup(path, GSON.toJson(root));
        } catch (Exception exception) {
            DonutUtilitiesClient.LOGGER.warn("Could not save client config", exception);
        }
    }

    public static void reapplySpotifyHud() {
        Path path = path();
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            JsonElement parsed = JsonParser.parseString(Files.readString(path));
            if (parsed.isJsonObject()) {
                loadSpotifyHud(object(parsed.getAsJsonObject(), "spotifyHud"));
            }
        } catch (Exception exception) {
            DonutUtilitiesClient.LOGGER.warn("Could not reapply Spotify HUD config", exception);
        }
    }

    private static JsonObject saveSus(SusChunkSettings settings) {
        JsonObject json = new JsonObject();
        json.addProperty("simulationDistance", settings.simulationDistance());
        json.addProperty("sensitivity", settings.sensitivity());
        json.addProperty("alpha", settings.alpha());
        json.addProperty("caveVines", settings.caveVines());
        json.addProperty("vines", settings.vines());
        json.addProperty("amethyst", settings.amethyst());
        json.addProperty("bamboo", settings.bamboo());
        json.addProperty("beeNest", settings.beeNest());
        json.addProperty("rotatedDeepslate", settings.rotatedDeepslate());
        json.addProperty("kelp", settings.kelp());
        return json;
    }

    private static void loadSus(SusChunkSettings settings, JsonObject json) {
        if (json.has("simulationDistance")) settings.setSimulationDistance(json.get("simulationDistance").getAsInt());
        if (json.has("sensitivity")) settings.setSensitivity(json.get("sensitivity").getAsInt());
        if (json.has("alpha")) settings.setAlpha(json.get("alpha").getAsInt());
        setBoolean(json, "caveVines", settings.caveVines(), settings::toggleCaveVines);
        setBoolean(json, "vines", settings.vines(), settings::toggleVines);
        setBoolean(json, "amethyst", settings.amethyst(), settings::toggleAmethyst);
        setBoolean(json, "bamboo", settings.bamboo(), settings::toggleBamboo);
        setBoolean(json, "beeNest", settings.beeNest(), settings::toggleBeeNest);
        setBoolean(json, "rotatedDeepslate", settings.rotatedDeepslate(), settings::toggleRotatedDeepslate);
        setBoolean(json, "kelp", settings.kelp(), settings::toggleKelp);
    }

    private static JsonObject saveEsp(EspSettings settings) {
        JsonObject json = new JsonObject();
        json.addProperty("traces", settings.traces());
        json.addProperty("entityTraces", settings.entityTraces());
        json.addProperty("blockTraces", settings.blockTraces());
        json.addProperty("traceDistance", settings.traceDistance());
        json.addProperty("traceAlpha", settings.traceAlpha());
        json.addProperty("traceRed", settings.traceRed());
        json.addProperty("traceGreen", settings.traceGreen());
        json.addProperty("traceBlue", settings.traceBlue());
        JsonObject colors = new JsonObject();
        settings.traceColors().forEach((moduleId, color) -> {
            JsonObject value = new JsonObject();
            value.addProperty("red", color[0]);
            value.addProperty("green", color[1]);
            value.addProperty("blue", color[2]);
            colors.add(moduleId, value);
        });
        json.add("traceColors", colors);
        return json;
    }

    private static void loadEsp(EspSettings settings, JsonObject json) {
        setBoolean(json, "traces", settings.traces(), settings::toggleTraces);
        setBoolean(json, "entityTraces", settings.entityTraces(), settings::toggleEntityTraces);
        setBoolean(json, "blockTraces", settings.blockTraces(), settings::toggleBlockTraces);
        if (json.has("traceDistance")) settings.setTraceDistance(json.get("traceDistance").getAsInt());
        if (json.has("traceAlpha")) settings.setTraceAlpha(json.get("traceAlpha").getAsInt());
        if (json.has("traceRed")) settings.setTraceRed(json.get("traceRed").getAsInt());
        if (json.has("traceGreen")) settings.setTraceGreen(json.get("traceGreen").getAsInt());
        if (json.has("traceBlue")) settings.setTraceBlue(json.get("traceBlue").getAsInt());
        if (json.has("traceColors") && json.get("traceColors").isJsonObject()) {
            for (var entry : json.getAsJsonObject("traceColors").entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }
                JsonObject color = entry.getValue().getAsJsonObject();
                settings.setTraceColor(entry.getKey(),
                        intValue(color, "red", settings.traceRed()),
                        intValue(color, "green", settings.traceGreen()),
                        intValue(color, "blue", settings.traceBlue()));
            }
        }
    }

    private static int intValue(JsonObject json, String key, int fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsInt() : fallback;
    }

    private static JsonObject saveBlockEsp(BlockEspSettings settings) {
        JsonObject json = new JsonObject();
        json.addProperty("explicitSelection", settings.explicitSelection());
        JsonArray selected = new JsonArray();
        settings.selectedBlockIds().forEach(selected::add);
        json.add("selectedBlocks", selected);
        json.addProperty("customBlockId", settings.customBlockId());
        json.addProperty("customBlock", settings.customBlock());
        return json;
    }

    private static void loadBlockEsp(BlockEspSettings settings, JsonObject json) {
        Set<String> selected = new LinkedHashSet<>();
        if (json.has("selectedBlocks") && json.get("selectedBlocks").isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray("selectedBlocks")) {
                if (element.isJsonPrimitive()) {
                    selected.add(element.getAsString());
                }
            }
        }
        settings.setSelectedBlockIds(selected, true);
        if (json.has("customBlockId")) settings.setCustomBlockId(json.get("customBlockId").getAsString());
        setBoolean(json, "customBlock", settings.customBlock(), settings::toggleCustomBlock);
    }

    private static JsonObject saveHud(HudSettings settings) {
        JsonObject json = new JsonObject();
        json.addProperty("baseScan", settings.baseScan());
        json.addProperty("espStats", settings.espStats());
        json.addProperty("spotifyHud", settings.spotifyHud());
        json.addProperty("coordinates", settings.coordinates());
        json.addProperty("realTime", settings.realTime());
        json.addProperty("ping", settings.ping());
        json.addProperty("ticks", settings.ticks());
        json.addProperty("bps", settings.bps());
        return json;
    }

    private static void loadHud(HudSettings settings, JsonObject json) {
        setBoolean(json, "baseScan", settings.baseScan(), settings::toggleBaseScan);
        setBoolean(json, "espStats", settings.espStats(), settings::toggleEspStats);
        setBoolean(json, "spotifyHud", settings.spotifyHud(), settings::toggleSpotifyHud);
        setBoolean(json, "coordinates", settings.coordinates(), settings::toggleCoordinates);
        setBoolean(json, "realTime", settings.realTime(), settings::toggleRealTime);
        setBoolean(json, "ping", settings.ping(), settings::togglePing);
        setBoolean(json, "ticks", settings.ticks(), settings::toggleTicks);
        setBoolean(json, "bps", settings.bps(), settings::toggleBps);
    }

    private static JsonObject saveMenuScale(MenuScaleSettings settings) {
        JsonObject json = new JsonObject();
        json.addProperty("percent", settings.percent());
        return json;
    }

    private static void loadMenuScale(MenuScaleSettings settings, JsonObject json) {
        if (json.has("percent")) {
            settings.setPercent(json.get("percent").getAsInt());
        }
    }

    private static JsonObject savePopup(PopupSettings settings) {
        JsonObject json = new JsonObject();
        json.addProperty("position", settings.position().name());
        return json;
    }

    private static void loadPopup(PopupSettings settings, JsonObject json) {
        if (json.has("position")) {
            try {
                settings.setPosition(PopupSettings.Position.valueOf(json.get("position").getAsString()));
            } catch (IllegalArgumentException ignored) {
                // Keep the default popup position when the saved value is unknown.
            }
        }
    }

    private static JsonObject saveBrandSpoofer(BrandSpooferSettings settings) {
        JsonObject json = new JsonObject();
        json.addProperty("brand", settings.brand());
        return json;
    }

    private static void loadBrandSpoofer(BrandSpooferSettings settings, JsonObject json) {
        if (json.has("brand")) {
            settings.setBrand(json.get("brand").getAsString());
        }
    }

    private static JsonObject saveItemInfo(dev.prchl.donututilities.module.ItemInfoModule settings) {
        JsonObject json = new JsonObject();
        json.addProperty("textColorHex", settings.textColorHex());
        return json;
    }

    private static void loadItemInfo(dev.prchl.donututilities.module.ItemInfoModule settings, JsonObject json) {
        if (json.has("textColorHex")) {
            settings.setTextColorHex(json.get("textColorHex").getAsString());
        }
    }

    private static JsonObject saveSpotifyHud() {
        JsonObject json = new JsonObject();
        try {
            aqys.melodify.client.HUDSettings settings = aqys.melodify.client.HUDSettings.getInstance();
            json.addProperty("position", settings.getPosition().name());
            json.addProperty("scale", settings.getHudScale());
        } catch (Throwable exception) {
            DonutUtilitiesClient.LOGGER.debug("Could not save Spotify HUD settings", exception);
        }
        return json;
    }

    private static void loadSpotifyHud(JsonObject json) {
        aqys.melodify.client.HUDSettings settings = aqys.melodify.client.HUDSettings.getInstance();
        if (json.has("position")) {
            try {
                settings.setPosition(aqys.melodify.client.HUDSettings.HUDPosition.valueOf(json.get("position").getAsString()));
            } catch (IllegalArgumentException ignored) {
                // Keep Melodify's default position when the saved value is unknown.
            }
        }
        if (json.has("scale")) {
            settings.setHudScale(json.get("scale").getAsFloat());
        }
    }

    private static void setBoolean(JsonObject json, String key, boolean current, Runnable toggle) {
        if (json.has(key) && json.get(key).getAsBoolean() != current) {
            toggle.run();
        }
    }

    private static boolean bool(JsonObject json, String key, boolean fallback) {
        return json.has(key) ? json.get(key).getAsBoolean() : fallback;
    }

    private static JsonObject object(JsonObject parent, String key) {
        return parent.has(key) && parent.get(key).isJsonObject() ? parent.getAsJsonObject(key) : new JsonObject();
    }

    private static JsonObject readConfigWithBackup(Path path) {
        JsonObject primary = readJsonObject(path);
        if (primary != null) {
            return primary;
        }

        Path backup = backupPath(path);
        JsonObject restored = readJsonObject(backup);
        if (restored != null) {
            try {
                Files.createDirectories(path.getParent());
                Files.copy(backup, path, StandardCopyOption.REPLACE_EXISTING);
                DonutUtilitiesClient.LOGGER.warn("Restored Donut Utilities config from backup");
            } catch (IOException exception) {
                DonutUtilitiesClient.LOGGER.warn("Could not restore Donut Utilities config backup", exception);
            }
            return restored;
        }
        return null;
    }

    private static JsonObject readJsonObject(Path path) {
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try {
            String content = Files.readString(path);
            if (content.isBlank()) {
                return null;
            }
            JsonElement parsed = JsonParser.parseString(content);
            return parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
        } catch (Exception exception) {
            DonutUtilitiesClient.LOGGER.warn("Could not read config {}", path.getFileName(), exception);
            return null;
        }
    }

    private static void preservePreviousBlockSelection(Path path, JsonObject nextRoot) {
        JsonObject previousRoot = readConfigWithBackup(path);
        if (previousRoot == null) {
            return;
        }
        JsonObject previousBlockEsp = object(previousRoot, "blockEsp");
        JsonObject nextBlockEsp = object(nextRoot, "blockEsp");
        if (!previousBlockEsp.has("selectedBlocks") || !previousBlockEsp.get("selectedBlocks").isJsonArray()) {
            return;
        }
        if (!nextBlockEsp.has("selectedBlocks") || !nextBlockEsp.get("selectedBlocks").isJsonArray()) {
            return;
        }
        JsonArray previousSelected = previousBlockEsp.getAsJsonArray("selectedBlocks");
        JsonArray nextSelected = nextBlockEsp.getAsJsonArray("selectedBlocks");
        if (!previousSelected.isEmpty() && nextSelected.isEmpty()) {
            JsonArray restored = new JsonArray();
            previousSelected.forEach(restored::add);
            nextBlockEsp.add("selectedBlocks", restored);
            nextRoot.add("blockEsp", nextBlockEsp);
        }
    }

    private static void writeWithBackup(Path path, String content) throws IOException {
        JsonElement parsed = JsonParser.parseString(content);
        if (!parsed.isJsonObject()) {
            throw new IOException("Refusing to write invalid config JSON");
        }
        if (Files.isRegularFile(path) && readJsonObject(path) != null) {
            Files.copy(path, backupPath(path), StandardCopyOption.REPLACE_EXISTING);
        }
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temp, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path backupPath(Path path) {
        return path.resolveSibling(path.getFileName() + ".bak");
    }

    private static Path path() {
        return class_310.method_1551().field_1697.toPath().resolve("config").resolve("donututilities.json");
    }
}
