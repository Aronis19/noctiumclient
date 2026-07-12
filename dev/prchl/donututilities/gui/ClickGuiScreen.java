package dev.prchl.donututilities.gui;

import dev.prchl.donututilities.DonutUtilitiesClient;
import dev.prchl.donututilities.module.BlockEspSettings;
import dev.prchl.donututilities.module.EspSettings;
import dev.prchl.donututilities.module.FreecamModule;
import dev.prchl.donututilities.module.HudSettings;
import dev.prchl.donututilities.module.Module;
import dev.prchl.donututilities.module.ModuleCategory;
import dev.prchl.donututilities.module.MenuScaleSettings;
import dev.prchl.donututilities.module.PopupSettings;
import dev.prchl.donututilities.module.RadioModule;
import dev.prchl.donututilities.module.SusChunkSettings;
import dev.prchl.donututilities.render.GuiTheme;
import dev.prchl.donututilities.render.shape.RoundedRectRenderer;
import dev.prchl.donututilities.scan.BaseMarker;
import dev.prchl.donututilities.scan.MarkerType;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;

public final class ClickGuiScreen extends class_437 {
    private static final int MAX_PANEL_WIDTH = 220;
    private static final int HEADER_HEIGHT = GuiTheme.HEADER_HEIGHT;
    private static final int ROW_HEIGHT = GuiTheme.ROW_HEIGHT;
    private static final int BASE_GAP = 22;
    private static final int BASE_LEFT = 70;
    private static final int TOP = 55;
    private static final int SEARCH_BOX_HEIGHT = 26;
    private static final int SEARCH_TOP_GAP = 12;
    private static final int SEARCH_RESULTS_GAP = 5;
    private static final int BACKDROP = 0x88000000;
    private static final int ACCENT = GuiTheme.ACCENT;
    private static final int BLUE = GuiTheme.ACCENT;
    private static final int TEXT = GuiTheme.TEXT;
    private static final int MUTED = GuiTheme.MUTED;
    private static final int SETTINGS_ROWS = 10;
    private static final int ESP_SETTINGS_ROWS = 8;
    private static final int BLOCK_ESP_ROWS = 1;
    private static final int FREECAM_SETTINGS_ROWS = 2;
    private static final int HUD_SETTINGS_ROWS = 8;
    private static final int RADIO_SETTINGS_ROWS = 4;
    private static final int MENU_SCALE_SETTINGS_ROWS = 1;
    private static final int POPUP_SETTINGS_ROWS = 1;
    private static final int ITEM_INFO_SETTINGS_ROWS = 1;
    private static final int SEARCH_RESULTS = 8;
    private static final int CORNER = GuiTheme.RADIUS;
    private final Map<ModuleCategory, int[]> panelPositions = new EnumMap<>(ModuleCategory.class);
    private final Map<String, Float> hoverAmounts = new java.util.HashMap<>();
    private boolean susSettingsOpen;
    private boolean espSettingsOpen;
    private boolean blockEspSettingsOpen = true;
    private boolean freecamSettingsOpen;
    private boolean hudSettingsOpen;
    private boolean radioSettingsOpen;
    private boolean menuScaleSettingsOpen;
    private boolean popupSettingsOpen;
    private boolean itemInfoSettingsOpen;
    private String espSettingsAnchorId = "player_esp";
    private ModuleCategory draggingCategory;
    private boolean draggingSearch;
    private int dragOffsetX;
    private int dragOffsetY;
    private int searchX;
    private int searchY;
    private boolean searchFocused;
    private boolean blockInputFocused;
    private boolean itemInfoHexFocused;
    private String searchText = "";

    public ClickGuiScreen() {
        super(class_2561.method_43470("DonutSMP Utilities"));
    }

    @Override
    public boolean method_25421() {
        return false;
    }

    @Override
    public void method_25419() {
        DonutUtilitiesClient.saveConfig();
        super.method_25419();
    }

    @Override
    public void method_25394(class_332 graphics, int mouseX, int mouseY, float partialTick) {
        method_52752(graphics);
        graphics.method_25294(0, 0, field_22789, field_22790, BACKDROP);

        float scale = menuScale();
        int virtualMouseX = (int) Math.floor(mouseX / scale);
        int virtualMouseY = (int) Math.floor(mouseY / scale);
        graphics.method_51448().pushMatrix();
        graphics.method_51448().scale(scale, scale);
        int panelWidth = panelWidth();
        ensurePanelPositions(panelWidth);
        for (ModuleCategory category : ModuleCategory.values()) {
            int[] pos = panelPositions.get(category);
            drawPanel(graphics, category, pos[0], pos[1], virtualMouseX, virtualMouseY, panelWidth);
        }
        drawSearchPanel(graphics, searchX, searchY, virtualMouseX, virtualMouseY, panelWidth);
        graphics.method_51448().popMatrix();

        super.method_25394(graphics, mouseX, mouseY, partialTick);
    }

    private int panelWidth() {
        int count = ModuleCategory.values().length + 1;
        int available = virtualWidth() - layoutLeft() * 2 - (layoutGap() * (count - 1));
        return Math.min(MAX_PANEL_WIDTH, Math.max(90, available / count));
    }

    private void ensurePanelPositions(int panelWidth) {
        int virtualWidth = virtualWidth();
        int virtualHeight = virtualHeight();
        int x = layoutLeft();
        for (ModuleCategory category : ModuleCategory.values()) {
            if (!panelPositions.containsKey(category)) {
                panelPositions.put(category, new int[] {x, TOP});
            }
            int[] position = panelPositions.get(category);
            position[0] = clamp(position[0], 0, Math.max(0, virtualWidth - panelWidth));
            position[1] = clamp(position[1], 0, Math.max(0, virtualHeight - HEADER_HEIGHT));
            x += panelWidth + layoutGap();
        }
        if (searchX == 0 && searchY == 0) {
            searchX = Math.min(x, Math.max(layoutLeft(), virtualWidth - layoutLeft() - panelWidth));
            searchY = TOP;
            if (x + panelWidth > virtualWidth - layoutLeft()) {
                searchX = layoutLeft();
                searchY = TOP + defaultSearchRowOffset();
            }
        }
        searchX = clamp(searchX, 0, Math.max(0, virtualWidth - panelWidth));
        searchY = clamp(searchY, 0, Math.max(0, virtualHeight - HEADER_HEIGHT));
    }

    private void drawPanel(class_332 graphics, ModuleCategory category, int x, int y, int mouseX, int mouseY, int panelWidth) {
        List<Module> modules = DonutUtilitiesClient.MODULES.modules(category);
        int height = panelHeight(modules);

        drawHudFrame(graphics, x, y, panelWidth, height);
        drawHudHeader(graphics, x, y, panelWidth, category.title(), category);

        int rowY = y + HEADER_HEIGHT;
        for (Module module : modules) {
            boolean hovered = mouseX >= x && mouseX <= x + panelWidth && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT;
            drawModuleRow(graphics, module, x, rowY, panelWidth, hovered);
            rowY += ROW_HEIGHT;

            if (menuScaleSettingsOpen && module.id().equals("menu_scale")) {
                drawMenuScaleSettings(graphics, x, rowY, mouseX, mouseY, panelWidth);
                rowY += menuScaleSettingsHeight();
            }

            if (popupSettingsOpen && module.id().equals("popup")) {
                drawPopupSettings(graphics, x, rowY, mouseX, mouseY, panelWidth);
                rowY += popupSettingsHeight();
            }

            if (susSettingsOpen && module.id().equals("sus_chunk_finder")) {
                drawSusSettings(graphics, x, rowY, mouseX, mouseY, panelWidth);
                rowY += susSettingsHeight();
            }

            if (espSettingsOpen && module.id().equals(espSettingsAnchorId)) {
                drawEspSettings(graphics, x, rowY, mouseX, mouseY, panelWidth);
                rowY += espSettingsHeight();
            }

            if (blockEspSettingsOpen && module.id().equals("block_esp")) {
                drawBlockEspSettings(graphics, x, rowY, mouseX, mouseY, panelWidth);
                rowY += blockEspSettingsHeight();
            }

            if (freecamSettingsOpen && module.id().equals("freecam")) {
                drawFreecamSettings(graphics, x, rowY, mouseX, mouseY, panelWidth);
                rowY += freecamSettingsHeight();
            }

            if (hudSettingsOpen && module.id().equals("hud")) {
                drawHudSettings(graphics, x, rowY, mouseX, mouseY, panelWidth);
                rowY += hudSettingsHeight();
            }

            if (radioSettingsOpen && module.id().equals("radio")) {
                drawRadioSettings(graphics, x, rowY, mouseX, mouseY, panelWidth);
                rowY += radioSettingsHeight();
            }

            if (itemInfoSettingsOpen && module.id().equals("item_info")) {
                drawItemInfoSettings(graphics, x, rowY, mouseX, mouseY, panelWidth);
                rowY += itemInfoSettingsHeight();
            }
        }
    }

    private void drawModuleRow(class_332 graphics, Module module, int x, int y, int panelWidth, boolean hovered) {
        drawModuleRowBackground(graphics, module.id(), x, y, panelWidth, hovered);
        if (module.id().equals("menu_scale")) {
            drawHdText(graphics, fitText(module.name(), panelWidth - 70), x + 12, y + 8, TEXT);
            String value = DonutUtilitiesClient.MODULES.menuScaleSettings().percent() + "%";
            drawHdText(graphics, value, x + panelWidth - 12 - GuiTheme.scaledWidth(value), y + 8, GuiTheme.ACCENT);
            return;
        }
        drawHdText(graphics, fitText(module.name(), panelWidth - 58), x + 12, y + 8,
                module.enabled() ? GuiTheme.HEADER_TEXT : TEXT);
        drawSwitch(graphics, x + panelWidth - 38, y + 6, module.enabled());
    }

    private int panelHeight(List<Module> modules) {
        int height = HEADER_HEIGHT + Math.max(1, modules.size()) * ROW_HEIGHT;
        if (susSettingsOpen && modules.stream().anyMatch(module -> module.id().equals("sus_chunk_finder"))) {
            height += susSettingsHeight();
        }
        if (espSettingsOpen && modules.stream().anyMatch(module -> module.id().equals(espSettingsAnchorId))) {
            height += espSettingsHeight();
        }
        if (blockEspSettingsOpen && modules.stream().anyMatch(module -> module.id().equals("block_esp"))) {
            height += blockEspSettingsHeight();
        }
        if (freecamSettingsOpen && modules.stream().anyMatch(module -> module.id().equals("freecam"))) {
            height += freecamSettingsHeight();
        }
        if (hudSettingsOpen && modules.stream().anyMatch(module -> module.id().equals("hud"))) {
            height += hudSettingsHeight();
        }
        if (radioSettingsOpen && modules.stream().anyMatch(module -> module.id().equals("radio"))) {
            height += radioSettingsHeight();
        }
        if (itemInfoSettingsOpen && modules.stream().anyMatch(module -> module.id().equals("item_info"))) {
            height += itemInfoSettingsHeight();
        }
        if (menuScaleSettingsOpen && modules.stream().anyMatch(module -> module.id().equals("menu_scale"))) {
            height += menuScaleSettingsHeight();
        }
        if (popupSettingsOpen && modules.stream().anyMatch(module -> module.id().equals("popup"))) {
            height += popupSettingsHeight();
        }
        return height;
    }

    private int susSettingsHeight() {
        return SETTINGS_ROWS * ROW_HEIGHT;
    }

    private int espSettingsHeight() {
        return ESP_SETTINGS_ROWS * ROW_HEIGHT;
    }

    private int blockEspSettingsHeight() {
        return BLOCK_ESP_ROWS * ROW_HEIGHT;
    }

    private int freecamSettingsHeight() {
        return FREECAM_SETTINGS_ROWS * ROW_HEIGHT;
    }

    private int hudSettingsHeight() {
        return HUD_SETTINGS_ROWS * ROW_HEIGHT;
    }

    private int radioSettingsHeight() {
        return RADIO_SETTINGS_ROWS * ROW_HEIGHT;
    }

    private int menuScaleSettingsHeight() {
        return MENU_SCALE_SETTINGS_ROWS * ROW_HEIGHT;
    }

    private int popupSettingsHeight() {
        return POPUP_SETTINGS_ROWS * ROW_HEIGHT;
    }

    private int itemInfoSettingsHeight() {
        return ITEM_INFO_SETTINGS_ROWS * ROW_HEIGHT;
    }

    private void drawSwitch(class_332 graphics, int x, int y, boolean enabled) {
        GuiTheme.toggle(graphics, x, y, enabled);
    }

    private void drawSusSettings(class_332 graphics, int x, int y, int mouseX, int mouseY, int panelWidth) {
        SusChunkSettings settings = DonutUtilitiesClient.MODULES.susChunkSettings();
        GuiTheme.inset(graphics, x + 8, y + 6, panelWidth - 16, SETTINGS_ROWS * ROW_HEIGHT - 12);

        int row = y;
        drawSlider(graphics, x, row, panelWidth, "SIMULATION DISTANCE", settings.simulationDistance(), 2, 12, mouseX, mouseY);
        row += ROW_HEIGHT;
        drawSlider(graphics, x, row, panelWidth, "SENSITIVITY", settings.sensitivity(), 1, 10, mouseX, mouseY);
        row += ROW_HEIGHT;
        drawSlider(graphics, x, row, panelWidth, "ALPHA", settings.alpha(), 10, 100, mouseX, mouseY);
        row += ROW_HEIGHT;
        drawCheckbox(graphics, x, row, panelWidth, "KELP", settings.kelp(), mouseX, mouseY);
        row += ROW_HEIGHT;
        drawCheckbox(graphics, x, row, panelWidth, "CAVE VINES", settings.caveVines(), mouseX, mouseY);
        row += ROW_HEIGHT;
        drawCheckbox(graphics, x, row, panelWidth, "VINES", settings.vines(), mouseX, mouseY);
        row += ROW_HEIGHT;
        drawCheckbox(graphics, x, row, panelWidth, "AMETHYST", settings.amethyst(), mouseX, mouseY);
        row += ROW_HEIGHT;
        drawCheckbox(graphics, x, row, panelWidth, "BAMBOO", settings.bamboo(), mouseX, mouseY);
        row += ROW_HEIGHT;
        drawCheckbox(graphics, x, row, panelWidth, "BEE NEST", settings.beeNest(), mouseX, mouseY);
        row += ROW_HEIGHT;
        drawCheckbox(graphics, x, row, panelWidth, "ROTATED DEEPSLATE", settings.rotatedDeepslate(), mouseX, mouseY);
    }

    private void drawEspSettings(class_332 graphics, int x, int y, int mouseX, int mouseY, int panelWidth) {
        EspSettings settings = DonutUtilitiesClient.MODULES.espSettings();
        GuiTheme.inset(graphics, x + 8, y + 6, panelWidth - 16, ESP_SETTINGS_ROWS * ROW_HEIGHT - 12);

        int row = y;
        drawCheckbox(graphics, x, row, panelWidth, "TRACES", settings.traces(), mouseX, mouseY);
        row += ROW_HEIGHT;
        drawCheckbox(graphics, x, row, panelWidth, "ENTITY TRACES", settings.entityTraces(), mouseX, mouseY);
        row += ROW_HEIGHT;
        drawCheckbox(graphics, x, row, panelWidth, "BLOCK TRACES", settings.blockTraces(), mouseX, mouseY);
        row += ROW_HEIGHT;
        drawSlider(graphics, x, row, panelWidth, "TRACE DISTANCE", settings.traceDistance(), 32, 1024, mouseX, mouseY);
        row += ROW_HEIGHT;
        drawSlider(graphics, x, row, panelWidth, "TRACE ALPHA", settings.traceAlpha(), 15, 100, mouseX, mouseY);
        row += ROW_HEIGHT;
        drawSlider(graphics, x, row, panelWidth, "TRACE RED", settings.traceRed(espSettingsAnchorId), 0, 255, mouseX, mouseY);
        row += ROW_HEIGHT;
        drawSlider(graphics, x, row, panelWidth, "TRACE GREEN", settings.traceGreen(espSettingsAnchorId), 0, 255, mouseX, mouseY);
        row += ROW_HEIGHT;
        drawSlider(graphics, x, row, panelWidth, "TRACE BLUE", settings.traceBlue(espSettingsAnchorId), 0, 255, mouseX, mouseY);
    }

    private void drawBlockEspSettings(class_332 graphics, int x, int y, int mouseX, int mouseY, int panelWidth) {
        BlockEspSettings settings = DonutUtilitiesClient.MODULES.blockEspSettings();
        boolean hovered = mouseX >= x && mouseX <= x + panelWidth && mouseY >= y && mouseY <= y + ROW_HEIGHT;
        drawRowBackground(graphics, x, y, panelWidth, hovered);
        drawHdText(graphics, "CHOOSE...", x + 12, y + 8, GuiTheme.ACCENT);
        String count = settings.selectedCount() + " BLOCKS";
        drawHdText(graphics, fitText(count, panelWidth - 92), x + panelWidth - 82, y + 8, GuiTheme.MUTED);
    }

    private void drawFreecamSettings(class_332 graphics, int x, int y, int mouseX, int mouseY, int panelWidth) {
        GuiTheme.inset(graphics, x + 8, y + 6, panelWidth - 16, FREECAM_SETTINGS_ROWS * ROW_HEIGHT - 12);
        drawSlider(graphics, x, y, panelWidth, "SPEED", DonutUtilitiesClient.MODULES.freecamModule().speed(), FreecamModule.MIN_SPEED, FreecamModule.MAX_SPEED, mouseX, mouseY);
        drawInfoRow(graphics, x, y + ROW_HEIGHT, panelWidth, "KEYBIND", DonutUtilitiesClient.MODULES.freecamModule().keyName(), mouseX, mouseY);
    }

    private void drawHudSettings(class_332 graphics, int x, int y, int mouseX, int mouseY, int panelWidth) {
        HudSettings settings = DonutUtilitiesClient.MODULES.hudSettings();
        GuiTheme.inset(graphics, x + 8, y + 6, panelWidth - 16, HUD_SETTINGS_ROWS * ROW_HEIGHT - 12);
        drawCheckbox(graphics, x, y, panelWidth, "BASE SCAN", settings.baseScan(), mouseX, mouseY);
        drawCheckbox(graphics, x, y + ROW_HEIGHT, panelWidth, "ESP STATS", settings.espStats(), mouseX, mouseY);
        drawCheckbox(graphics, x, y + ROW_HEIGHT * 2, panelWidth, "SPOTIFY HUD", settings.spotifyHud(), mouseX, mouseY);
        drawCheckbox(graphics, x, y + ROW_HEIGHT * 3, panelWidth, "COORDINATES", settings.coordinates(), mouseX, mouseY);
        drawCheckbox(graphics, x, y + ROW_HEIGHT * 4, panelWidth, "REAL TIME", settings.realTime(), mouseX, mouseY);
        drawCheckbox(graphics, x, y + ROW_HEIGHT * 5, panelWidth, "PING", settings.ping(), mouseX, mouseY);
        drawCheckbox(graphics, x, y + ROW_HEIGHT * 6, panelWidth, "TICKS", settings.ticks(), mouseX, mouseY);
        drawCheckbox(graphics, x, y + ROW_HEIGHT * 7, panelWidth, "BPS", settings.bps(), mouseX, mouseY);
    }

    private void drawRadioSettings(class_332 graphics, int x, int y, int mouseX, int mouseY, int panelWidth) {
        GuiTheme.inset(graphics, x + 8, y + 6, panelWidth - 16, RADIO_SETTINGS_ROWS * ROW_HEIGHT - 12);
        RadioModule radio = DonutUtilitiesClient.MODULES.radioModule();
        aqys.melodify.client.HUDSettings spotify = aqys.melodify.client.HUDSettings.getInstance();
        drawInfoRow(graphics, x, y, panelWidth, "CONNECT SPOTIFY", "OPEN LOGIN", mouseX, mouseY);
        drawInfoRow(graphics, x, y + ROW_HEIGHT, panelWidth, "KEYBIND", radio.keyName(), mouseX, mouseY);
        drawInfoRow(graphics, x, y + ROW_HEIGHT * 2, panelWidth, "POSITION", spotify.getPosition().getDisplayName(), mouseX, mouseY);
        drawSlider(graphics, x, y + ROW_HEIGHT * 3, panelWidth, "SCALE %", Math.round(spotify.getHudScale() * 100.0F), 50, 150, mouseX, mouseY);
    }

    private void drawItemInfoSettings(class_332 graphics, int x, int y, int mouseX, int mouseY, int panelWidth) {
        GuiTheme.inset(graphics, x + 8, y + 6, panelWidth - 16, ITEM_INFO_SETTINGS_ROWS * ROW_HEIGHT - 12);
        boolean hovered = mouseX >= x && mouseX <= x + panelWidth && mouseY >= y && mouseY <= y + ROW_HEIGHT;
        drawRowBackground(graphics, x, y, panelWidth, hovered);
        drawHdText(graphics, "TEXT HEX", x + 12, y + 8, TEXT);
        String value = DonutUtilitiesClient.MODULES.itemInfoModule().textColorHex();
        String shown = value + (itemInfoHexFocused && (System.currentTimeMillis() / 450L) % 2L == 0L ? "_" : "");
        drawHdText(graphics, fitText(shown, 74), x + panelWidth - 86, y + 8, DonutUtilitiesClient.MODULES.itemInfoModule().textColor());
    }

    private void drawMenuScaleSettings(class_332 graphics, int x, int y, int mouseX, int mouseY, int panelWidth) {
        MenuScaleSettings settings = DonutUtilitiesClient.MODULES.menuScaleSettings();
        GuiTheme.inset(graphics, x + 8, y + 6, panelWidth - 16, MENU_SCALE_SETTINGS_ROWS * ROW_HEIGHT - 12);
        drawSlider(graphics, x, y, panelWidth, "GUI SCALE %", settings.percent(),
                MenuScaleSettings.MIN_PERCENT, MenuScaleSettings.MAX_PERCENT, mouseX, mouseY);
    }

    private void drawPopupSettings(class_332 graphics, int x, int y, int mouseX, int mouseY, int panelWidth) {
        PopupSettings settings = DonutUtilitiesClient.MODULES.popupSettings();
        GuiTheme.inset(graphics, x + 8, y + 6, panelWidth - 16, POPUP_SETTINGS_ROWS * ROW_HEIGHT - 12);
        drawInfoRow(graphics, x, y, panelWidth, "LOCATION", settings.position().displayName(), mouseX, mouseY);
    }

    private void drawInfoRow(class_332 graphics, int x, int y, int panelWidth, String label, String value, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + panelWidth && mouseY >= y && mouseY <= y + ROW_HEIGHT;
        drawRowBackground(graphics, x, y, panelWidth, hovered);
        drawHdText(graphics, fitText(label, panelWidth - 60), x + 12, y + 8, TEXT);
        drawHdText(graphics, fitText(value, 48), x + panelWidth - 58, y + 8, BLUE);
    }

    private void drawBlockInput(class_332 graphics, int x, int y, int panelWidth, String value, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + panelWidth && mouseY >= y && mouseY <= y + ROW_HEIGHT;
        drawRowBackground(graphics, x, y, panelWidth, hovered);
        String text = value.isBlank() && !blockInputFocused ? "minecraft:block_id" : value + (blockInputFocused && (System.currentTimeMillis() / 450L) % 2L == 0L ? "_" : "");
        int color = value.isBlank() && !blockInputFocused ? GuiTheme.DISABLED : TEXT;
        drawHdText(graphics, "ID", x + 12, y + 8, BLUE);
        drawHdText(graphics, fitText(text, panelWidth - 48), x + 34, y + 8, color);
    }

    private void drawSlider(class_332 graphics, int x, int y, int panelWidth, String label, int value, int min, int max, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + panelWidth && mouseY >= y && mouseY <= y + ROW_HEIGHT;
        drawRowBackground(graphics, x, y, panelWidth, hovered);
        int trackStart = x + 12;
        int trackEnd = x + panelWidth - 12;
        int knobX = trackStart + Math.round((trackEnd - trackStart) * ((float) (value - min) / (float) (max - min)));
        drawRoundedRect(graphics, trackStart, y + ROW_HEIGHT - 6, trackEnd - trackStart, 3, 0xFF2B313A);
        drawRoundedRect(graphics, trackStart, y + ROW_HEIGHT - 6, Math.max(3, knobX - trackStart), 3, BLUE);
        drawRoundedRect(graphics, knobX - 3, y + ROW_HEIGHT - 9, 7, 7, BLUE);
        drawHdText(graphics, fitText(label, panelWidth - 48), x + 12, y + 6, TEXT);
        drawHdText(graphics, Integer.toString(value), x + panelWidth - 29, y + 6, BLUE);
    }

    private void drawCheckbox(class_332 graphics, int x, int y, int panelWidth, String label, boolean checked, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + panelWidth && mouseY >= y && mouseY <= y + ROW_HEIGHT;
        drawRowBackground(graphics, x, y, panelWidth, hovered);
        drawRoundedRect(graphics, x + 12, y + 8, 10, 10, checked ? GuiTheme.ACCENT : 0xFF252A31);
        drawRoundedRect(graphics, x + 15, y + 11, 4, 4, checked ? 0xFFE5FBFF : 0xFF11151B);
        drawHdText(graphics, fitText(label, panelWidth - 42), x + 30, y + 8, checked ? GuiTheme.ACCENT : TEXT);
    }

    private void drawSearchPanel(class_332 graphics, int x, int y, int mouseX, int mouseY, int panelWidth) {
        List<Module> results = searchResults();
        int searchBoxHeight = SEARCH_BOX_HEIGHT;
        int height = HEADER_HEIGHT + SEARCH_TOP_GAP + searchBoxHeight + SEARCH_RESULTS_GAP + results.size() * ROW_HEIGHT + 8;
        drawHudFrame(graphics, x, y, panelWidth, height);
        drawHudHeader(graphics, x, y, panelWidth, "SEARCH", null);

        int inputY = y + HEADER_HEIGHT + SEARCH_TOP_GAP;
        String text = searchText.isEmpty() && !searchFocused ? "SEARCH MODULES" : searchText + (searchFocused && (System.currentTimeMillis() / 450L) % 2L == 0L ? "_" : "");
        GuiTheme.searchBox(graphics, x + 12, inputY, panelWidth - 24, searchBoxHeight, fitText(text, panelWidth - 42));

        int rowY = inputY + searchBoxHeight + SEARCH_RESULTS_GAP;
        for (Module module : results) {
            boolean hovered = mouseX >= x && mouseX <= x + panelWidth && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT;
            drawModuleRow(graphics, module, x, rowY, panelWidth, hovered);
            rowY += ROW_HEIGHT;
        }
    }

    private List<Module> searchResults() {
        String query = searchText.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return List.of();
        }
        return DonutUtilitiesClient.MODULES.modules().stream()
                .filter(module -> module.name().toLowerCase(Locale.ROOT).contains(query) || module.id().contains(query))
                .limit(SEARCH_RESULTS)
                .toList();
    }

    private void drawHudFrame(class_332 graphics, int x, int y, int width, int height) {
        GuiTheme.panel(graphics, x, y, width, height);
    }

    private void drawHudHeader(class_332 graphics, int x, int y, int panelWidth, String title, ModuleCategory category) {
        GuiTheme.header(graphics, x, y, panelWidth, HEADER_HEIGHT, title, category == ModuleCategory.BASE);
    }

    private void drawRowBackground(class_332 graphics, int x, int y, int panelWidth, boolean hovered) {
        GuiTheme.row(graphics, x + 7, y + 4, panelWidth - 14, ROW_HEIGHT - 8, hovered);
        graphics.method_25294(x + 12, y + ROW_HEIGHT - 1, x + panelWidth - 12, y + ROW_HEIGHT, 0x38323642);
    }

    private void drawModuleRowBackground(class_332 graphics, String id, int x, int y, int panelWidth, boolean hovered) {
        float current = hoverAmounts.getOrDefault(id, 0.0F);
        float target = hovered ? 1.0F : 0.0F;
        current += (target - current) * 0.24F;
        if (Math.abs(target - current) < 0.01F) {
            current = target;
        }
        if (current <= 0.0F) {
            hoverAmounts.remove(id);
        } else {
            hoverAmounts.put(id, current);
        }

        GuiTheme.row(graphics, x + 7, y + 4, panelWidth - 14, ROW_HEIGHT - 8, current);
        graphics.method_25294(x + 12, y + ROW_HEIGHT - 1, x + panelWidth - 12, y + ROW_HEIGHT, 0x38323642);
    }

    private void drawScanFooter(class_332 graphics) {
        if (!DonutUtilitiesClient.MODULES.enabled("hud")) {
            return;
        }

        class_310 client = class_310.method_1551();
        int footerX = layoutLeft();
        int footerY = virtualHeight() - 76;
        GuiTheme.panel(graphics, footerX, footerY, 420, 54);
        String pos = "No world loaded";
        if (client.field_1724 != null) {
            pos = "XYZ " + client.field_1724.method_24515().method_10263() + " " + client.field_1724.method_24515().method_10264() + " " + client.field_1724.method_24515().method_10260();
        }
        drawHdText(graphics, "SCAN: " + DonutUtilitiesClient.SCANNER.markers().size() + " markers   " + pos, footerX + 10, footerY + 9, ACCENT);
        List<BaseMarker> strongest = DonutUtilitiesClient.SCANNER.strongestMarkers(3);
        int y = footerY + 25;
        for (BaseMarker marker : strongest) {
            drawHdText(graphics, marker.label() + " @ " + marker.pos().method_23854(), footerX + 10, y, MUTED);
            y += 10;
        }
    }

    @Override
    public boolean method_25402(class_11909 event, boolean doubleClick) {
        double mouseX = virtualX(event.comp_4798());
        double mouseY = virtualY(event.comp_4799());
        if (event.method_74245() != 0 && event.method_74245() != 1) {
            return super.method_25402(event, doubleClick);
        }

        int panelWidth = panelWidth();
        ensurePanelPositions(panelWidth);
        if (handleSearchClick(event, panelWidth)) {
            return true;
        }

        for (ModuleCategory category : ModuleCategory.values()) {
            int[] pos = panelPositions.get(category);
            if (event.method_74245() == 0 && inside(mouseX, mouseY, pos[0], pos[1], panelWidth, HEADER_HEIGHT)) {
                draggingCategory = category;
                dragOffsetX = (int) mouseX - pos[0];
                dragOffsetY = (int) mouseY - pos[1];
                searchFocused = false;
                blockInputFocused = false;
                itemInfoHexFocused = false;
                return true;
            }

            if (handlePanelClick(event, category, pos[0], pos[1], panelWidth)) {
                searchFocused = false;
                return true;
            }
        }

        searchFocused = false;
        blockInputFocused = false;
        itemInfoHexFocused = false;
        return super.method_25402(event, doubleClick);
    }

    private boolean handlePanelClick(class_11909 event, ModuleCategory category, int x, int y, int panelWidth) {
        double mouseX = virtualX(event.comp_4798());
        double mouseY = virtualY(event.comp_4799());
        int rowY = y + HEADER_HEIGHT;
        for (Module module : DonutUtilitiesClient.MODULES.modules(category)) {
            if (inside(mouseX, mouseY, x, rowY, panelWidth, ROW_HEIGHT)) {
                handleModuleClick(event, module, x, panelWidth);
                return true;
            }
            rowY += ROW_HEIGHT;

            if (menuScaleSettingsOpen && module.id().equals("menu_scale")) {
                if (inside(mouseX, mouseY, x, rowY, panelWidth, MENU_SCALE_SETTINGS_ROWS * ROW_HEIGHT)) {
                    handleMenuScaleSettingsClick(mouseX, x, panelWidth);
                    return true;
                }
                rowY += menuScaleSettingsHeight();
            }

            if (popupSettingsOpen && module.id().equals("popup")) {
                if (inside(mouseX, mouseY, x, rowY, panelWidth, POPUP_SETTINGS_ROWS * ROW_HEIGHT)) {
                    DonutUtilitiesClient.MODULES.popupSettings().cyclePosition();
                    DonutUtilitiesClient.saveConfig();
                    return true;
                }
                rowY += popupSettingsHeight();
            }

            if (susSettingsOpen && module.id().equals("sus_chunk_finder")) {
                if (inside(mouseX, mouseY, x, rowY, panelWidth, SETTINGS_ROWS * ROW_HEIGHT)) {
                    handleSusSettingsClick(mouseX, mouseY, rowY, x, panelWidth);
                    return true;
                }
                rowY += susSettingsHeight();
            }
            if (espSettingsOpen && module.id().equals(espSettingsAnchorId)) {
                if (inside(mouseX, mouseY, x, rowY, panelWidth, ESP_SETTINGS_ROWS * ROW_HEIGHT)) {
                    handleEspSettingsClick(mouseX, mouseY, rowY, x, panelWidth);
                    return true;
                }
                rowY += espSettingsHeight();
            }
            if (blockEspSettingsOpen && module.id().equals("block_esp")) {
                if (inside(mouseX, mouseY, x, rowY, panelWidth, BLOCK_ESP_ROWS * ROW_HEIGHT)) {
                    handleBlockEspSettingsClick(mouseX, mouseY, rowY, x, panelWidth);
                    return true;
                }
                rowY += blockEspSettingsHeight();
            }
            if (freecamSettingsOpen && module.id().equals("freecam")) {
                if (inside(mouseX, mouseY, x, rowY, panelWidth, FREECAM_SETTINGS_ROWS * ROW_HEIGHT)) {
                    handleFreecamSettingsClick(mouseX, mouseY, rowY, x, panelWidth);
                    return true;
                }
                rowY += freecamSettingsHeight();
            }
            if (hudSettingsOpen && module.id().equals("hud")) {
                if (inside(mouseX, mouseY, x, rowY, panelWidth, HUD_SETTINGS_ROWS * ROW_HEIGHT)) {
                    handleHudSettingsClick(mouseX, mouseY, rowY, x, panelWidth);
                    return true;
                }
                rowY += hudSettingsHeight();
            }
            if (radioSettingsOpen && module.id().equals("radio")) {
                if (inside(mouseX, mouseY, x, rowY, panelWidth, RADIO_SETTINGS_ROWS * ROW_HEIGHT)) {
                    handleRadioSettingsClick(mouseX, mouseY, rowY, x, panelWidth);
                    return true;
                }
                rowY += radioSettingsHeight();
            }
            if (itemInfoSettingsOpen && module.id().equals("item_info")) {
                if (inside(mouseX, mouseY, x, rowY, panelWidth, ITEM_INFO_SETTINGS_ROWS * ROW_HEIGHT)) {
                    itemInfoHexFocused = true;
                    searchFocused = false;
                    blockInputFocused = false;
                    return true;
                }
                rowY += itemInfoSettingsHeight();
            }
        }
        return false;
    }

    private boolean handleSearchClick(class_11909 event, int panelWidth) {
        double mouseX = virtualX(event.comp_4798());
        double mouseY = virtualY(event.comp_4799());
        if (event.method_74245() == 0 && inside(mouseX, mouseY, searchX, searchY, panelWidth, HEADER_HEIGHT)) {
            draggingSearch = true;
            dragOffsetX = (int) mouseX - searchX;
            dragOffsetY = (int) mouseY - searchY;
            blockInputFocused = false;
            return true;
        }

        int inputY = searchY + HEADER_HEIGHT + SEARCH_TOP_GAP;
        int searchBoxHeight = SEARCH_BOX_HEIGHT;
        if (inside(mouseX, mouseY, searchX + 12, inputY, panelWidth - 24, searchBoxHeight)) {
            searchFocused = true;
            blockInputFocused = false;
            itemInfoHexFocused = false;
            return true;
        }

        int rowY = inputY + searchBoxHeight + SEARCH_RESULTS_GAP;
        for (Module module : searchResults()) {
            if (inside(mouseX, mouseY, searchX, rowY, panelWidth, ROW_HEIGHT)) {
                handleModuleClick(event, module, searchX, panelWidth);
                searchFocused = true;
                return true;
            }
            rowY += ROW_HEIGHT;
        }
        return false;
    }

    private void handleModuleClick(class_11909 event, Module module, int panelX, int panelWidth) {
        if (module.id().equals("menu_scale")) {
            menuScaleSettingsOpen = !menuScaleSettingsOpen;
            return;
        }
        if (module.id().equals("popup") && event.method_74245() == 1) {
            popupSettingsOpen = !popupSettingsOpen;
            return;
        }
        boolean clickedSwitch = virtualX(event.comp_4798()) >= panelX + panelWidth - 52;
        if (module.id().equals("block_esp") && event.method_74245() == 0 && !clickedSwitch) {
            field_22787.method_1507(new BlockPickerScreen(this));
            return;
        }
        if (event.method_74245() == 1 && module.id().equals("sus_chunk_finder")) {
            susSettingsOpen = !susSettingsOpen;
        } else if (event.method_74245() == 1 && module.id().equals("freecam")) {
            freecamSettingsOpen = !freecamSettingsOpen;
        } else if (event.method_74245() == 1 && module.id().equals("hud")) {
            hudSettingsOpen = !hudSettingsOpen;
        } else if (event.method_74245() == 1 && module.id().equals("radio")) {
            radioSettingsOpen = !radioSettingsOpen;
        } else if (event.method_74245() == 1 && module.id().equals("item_info")) {
            itemInfoSettingsOpen = !itemInfoSettingsOpen;
        } else if (event.method_74245() == 1 && isEspSettingsModule(module)) {
            espSettingsOpen = !espSettingsOpen || !module.id().equals(espSettingsAnchorId);
            espSettingsAnchorId = module.id();
        } else if (event.method_74245() == 0) {
            module.toggle();
            if (module.id().equals("block_esp")) {
                DonutUtilitiesClient.SCANNER.clearType(MarkerType.BLOCK_ESP);
            }
        }
        DonutUtilitiesClient.saveConfig();
    }

    @Override
    public boolean method_25403(class_11909 event, double dragX, double dragY) {
        int mouseX = (int) virtualX(event.comp_4798());
        int mouseY = (int) virtualY(event.comp_4799());
        if (draggingCategory != null) {
            panelPositions.put(draggingCategory, new int[] {clamp(mouseX - dragOffsetX, 0, Math.max(0, virtualWidth() - panelWidth())), clamp(mouseY - dragOffsetY, 0, Math.max(0, virtualHeight() - HEADER_HEIGHT))});
            return true;
        }
        if (draggingSearch) {
            searchX = clamp(mouseX - dragOffsetX, 0, Math.max(0, virtualWidth() - panelWidth()));
            searchY = clamp(mouseY - dragOffsetY, 0, Math.max(0, virtualHeight() - HEADER_HEIGHT));
            return true;
        }
        return super.method_25403(event, dragX, dragY);
    }

    @Override
    public boolean method_25406(class_11909 event) {
        draggingCategory = null;
        draggingSearch = false;
        return super.method_25406(event);
    }

    @Override
    public boolean method_25400(class_11905 event) {
        if (!searchFocused || !event.method_74227()) {
            if (blockInputFocused && event.method_74227()) {
                DonutUtilitiesClient.MODULES.blockEspSettings().appendCustomChar(event.method_74226());
                DonutUtilitiesClient.SCANNER.clearType(MarkerType.BLOCK_ESP);
                return true;
            }
            if (itemInfoHexFocused && event.method_74227()) {
                DonutUtilitiesClient.MODULES.itemInfoModule().appendTextColorChar(event.method_74226());
                DonutUtilitiesClient.saveConfig();
                return true;
            }
            return super.method_25400(event);
        }
        if (searchText.length() < 32) {
            searchText += event.method_74226();
        }
        return true;
    }

    @Override
    public boolean method_25404(class_11908 event) {
        if (searchFocused) {
            if (event.comp_4795() == GLFW.GLFW_KEY_BACKSPACE && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
                return true;
            }
            if (event.comp_4795() == GLFW.GLFW_KEY_ESCAPE || event.comp_4795() == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }
        if (blockInputFocused) {
            if (event.comp_4795() == GLFW.GLFW_KEY_BACKSPACE) {
                boolean changed = DonutUtilitiesClient.MODULES.blockEspSettings().removeLastCustomChar();
                if (changed) {
                    DonutUtilitiesClient.SCANNER.clearType(MarkerType.BLOCK_ESP);
                }
                return changed;
            }
            if (event.comp_4795() == GLFW.GLFW_KEY_ESCAPE || event.comp_4795() == GLFW.GLFW_KEY_ENTER) {
                blockInputFocused = false;
                return true;
            }
        }
        if (itemInfoHexFocused) {
            if (event.comp_4795() == GLFW.GLFW_KEY_BACKSPACE) {
                boolean changed = DonutUtilitiesClient.MODULES.itemInfoModule().removeLastTextColorChar();
                if (changed) {
                    DonutUtilitiesClient.saveConfig();
                }
                return changed;
            }
            if (event.comp_4795() == GLFW.GLFW_KEY_ESCAPE || event.comp_4795() == GLFW.GLFW_KEY_ENTER) {
                itemInfoHexFocused = false;
                DonutUtilitiesClient.saveConfig();
                return true;
            }
        }
        if (DonutUtilitiesClient.MODULES.freecamModule().capturingKey()) {
            DonutUtilitiesClient.MODULES.freecamModule().setKeyCode(event.comp_4795());
            DonutUtilitiesClient.saveConfig();
            return true;
        }
        if (DonutUtilitiesClient.MODULES.radioModule().capturingKey()) {
            DonutUtilitiesClient.MODULES.radioModule().setKeyCode(event.comp_4795());
            DonutUtilitiesClient.saveConfig();
            return true;
        }
        return super.method_25404(event);
    }

    private void handleEspSettingsClick(double mouseX, double mouseY, int settingsY, int x, int panelWidth) {
        EspSettings settings = DonutUtilitiesClient.MODULES.espSettings();
        int row = Math.max(0, Math.min(ESP_SETTINGS_ROWS - 1, ((int) mouseY - settingsY) / ROW_HEIGHT));
        int sliderValue = sliderValue(mouseX, x, panelWidth);
        switch (row) {
            case 0 -> settings.toggleTraces();
            case 1 -> settings.toggleEntityTraces();
            case 2 -> settings.toggleBlockTraces();
            case 3 -> settings.setTraceDistance(32 + Math.round(sliderValue / 100.0f * 992));
            case 4 -> settings.setTraceAlpha(15 + Math.round(sliderValue / 100.0f * 85));
            case 5 -> settings.setTraceRed(espSettingsAnchorId, Math.round(sliderValue / 100.0f * 255));
            case 6 -> settings.setTraceGreen(espSettingsAnchorId, Math.round(sliderValue / 100.0f * 255));
            case 7 -> settings.setTraceBlue(espSettingsAnchorId, Math.round(sliderValue / 100.0f * 255));
            default -> {
            }
        }
        DonutUtilitiesClient.saveConfig();
    }

    private boolean isEspSettingsModule(Module module) {
        return module.id().equals("block_entity_debug")
                || module.id().equals("hole_esp")
                || module.id().equals("light_finder")
                || module.id().equals("player_esp")
                || module.id().equals("mob_esp")
                || module.id().equals("storage_esp")
                || module.id().equals("block_esp")
                || module.id().equals("suspicious_esp");
    }

    private void handleBlockEspSettingsClick(double mouseX, double mouseY, int settingsY, int x, int panelWidth) {
        searchFocused = false;
        blockInputFocused = false;
        field_22787.method_1507(new BlockPickerScreen(this));
    }

    private void handleSusSettingsClick(double mouseX, double mouseY, int settingsY, int x, int panelWidth) {
        SusChunkSettings settings = DonutUtilitiesClient.MODULES.susChunkSettings();
        int row = Math.max(0, Math.min(SETTINGS_ROWS - 1, ((int) mouseY - settingsY) / ROW_HEIGHT));
        int sliderValue = sliderValue(mouseX, x, panelWidth);
        switch (row) {
            case 0 -> settings.setSimulationDistance(2 + Math.round(sliderValue / 100.0f * 10));
            case 1 -> settings.setSensitivity(1 + Math.round(sliderValue / 100.0f * 9));
            case 2 -> settings.setAlpha(10 + Math.round(sliderValue / 100.0f * 90));
            case 3 -> settings.toggleKelp();
            case 4 -> settings.toggleCaveVines();
            case 5 -> settings.toggleVines();
            case 6 -> settings.toggleAmethyst();
            case 7 -> settings.toggleBamboo();
            case 8 -> settings.toggleBeeNest();
            case 9 -> settings.toggleRotatedDeepslate();
            default -> {
            }
        }
        DonutUtilitiesClient.saveConfig();
    }

    private void handleFreecamSettingsClick(double mouseX, double mouseY, int settingsY, int x, int panelWidth) {
        int row = Math.max(0, Math.min(FREECAM_SETTINGS_ROWS - 1, ((int) mouseY - settingsY) / ROW_HEIGHT));
        if (row == 0) {
            int sliderValue = sliderValue(mouseX, x, panelWidth);
            DonutUtilitiesClient.MODULES.freecamModule().setSpeed(FreecamModule.MIN_SPEED + Math.round(sliderValue / 100.0f * (FreecamModule.MAX_SPEED - FreecamModule.MIN_SPEED)));
        } else if (row == 1) {
            DonutUtilitiesClient.MODULES.freecamModule().startCapturingKey();
        }
        DonutUtilitiesClient.saveConfig();
    }

    private void handleHudSettingsClick(double mouseX, double mouseY, int settingsY, int x, int panelWidth) {
        HudSettings settings = DonutUtilitiesClient.MODULES.hudSettings();
        int row = Math.max(0, Math.min(HUD_SETTINGS_ROWS - 1, ((int) mouseY - settingsY) / ROW_HEIGHT));
        switch (row) {
            case 0 -> settings.toggleBaseScan();
            case 1 -> settings.toggleEspStats();
            case 2 -> settings.toggleSpotifyHud();
            case 3 -> settings.toggleCoordinates();
            case 4 -> settings.toggleRealTime();
            case 5 -> settings.togglePing();
            case 6 -> settings.toggleTicks();
            case 7 -> settings.toggleBps();
            default -> {
            }
        }
        DonutUtilitiesClient.saveConfig();
    }

    private void handleRadioSettingsClick(double mouseX, double mouseY, int settingsY, int x, int panelWidth) {
        RadioModule radio = DonutUtilitiesClient.MODULES.radioModule();
        aqys.melodify.client.HUDSettings spotify = aqys.melodify.client.HUDSettings.getInstance();
        int row = Math.max(0, Math.min(RADIO_SETTINGS_ROWS - 1, ((int) mouseY - settingsY) / ROW_HEIGHT));
        switch (row) {
            case 0 -> radio.connect();
            case 1 -> radio.startCapturingKey();
            case 2 -> spotify.cyclePosition();
            case 3 -> spotify.setHudScale(0.5F + sliderValue(mouseX, x, panelWidth) / 100.0F);
            default -> {
            }
        }
        DonutUtilitiesClient.saveConfig();
    }

    private void handleMenuScaleSettingsClick(double mouseX, int x, int panelWidth) {
        int sliderValue = sliderValue(mouseX, x, panelWidth);
        int range = MenuScaleSettings.MAX_PERCENT - MenuScaleSettings.MIN_PERCENT;
        DonutUtilitiesClient.MODULES.menuScaleSettings().setPercent(
                MenuScaleSettings.MIN_PERCENT + Math.round(sliderValue / 100.0F * range));
        DonutUtilitiesClient.saveConfig();
    }

    private int sliderValue(double mouseX, int x, int panelWidth) {
        int trackStart = x + 12;
        int trackEnd = x + panelWidth - 12;
        double clamped = Math.max(trackStart, Math.min(trackEnd, mouseX));
        return (int) Math.round(((clamped - trackStart) / (trackEnd - trackStart)) * 100.0);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private int layoutLeft() {
        int width = virtualWidth();
        if (width < 1000) {
            return 12;
        }
        if (width < 1400) {
            return 36;
        }
        return BASE_LEFT;
    }

    private int layoutGap() {
        int width = virtualWidth();
        if (width < 1000) {
            return 6;
        }
        if (width < 1400) {
            return 10;
        }
        return BASE_GAP;
    }

    private int defaultSearchRowOffset() {
        return HEADER_HEIGHT + ROW_HEIGHT * 5 + 22;
    }

    private float menuScale() {
        return DonutUtilitiesClient.MODULES.menuScaleSettings().scale();
    }

    private int virtualWidth() {
        return Math.max(1, (int) Math.floor(field_22789 / menuScale()));
    }

    private int virtualHeight() {
        return Math.max(1, (int) Math.floor(field_22790 / menuScale()));
    }

    private double virtualX(double x) {
        return x / menuScale();
    }

    private double virtualY(double y) {
        return y / menuScale();
    }

    private String fitText(String text, int maxWidth) {
        String value = text;
        while (!value.isEmpty() && GuiTheme.scaledWidth(value) > maxWidth) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private void drawHdText(class_332 graphics, String text, int x, int y, int color) {
        GuiTheme.text(graphics, text, x, y, color);
    }

    private void drawRoundedRect(class_332 graphics, int x, int y, int width, int height, int color) {
        RoundedRectRenderer.roundedRect(graphics, x, y, width, height, CORNER, color);
    }

}
