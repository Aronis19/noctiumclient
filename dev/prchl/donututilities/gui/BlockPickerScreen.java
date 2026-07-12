package dev.prchl.donututilities.gui;

import dev.prchl.donututilities.DonutUtilitiesClient;
import dev.prchl.donututilities.module.BlockEspSettings;
import dev.prchl.donututilities.render.GuiTheme;
import dev.prchl.donututilities.scan.MarkerType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2248;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_7923;
import org.lwjgl.glfw.GLFW;

public final class BlockPickerScreen extends class_437 {
    private static final int PANEL_WIDTH = 620;
    private static final int PANEL_TOP = 34;
    private static final int HEADER_HEIGHT = GuiTheme.HEADER_HEIGHT;
    private static final int SEARCH_HEIGHT = 26;
    private static final int TILE_WIDTH = 70;
    private static final int TILE_HEIGHT = 60;
    private static final int TILE_GAP = 5;
    private static final int GRID_TOP_GAP = 10;

    private final class_437 parent;
    private final List<class_2248> blocks = new ArrayList<>();
    private String searchText = "";
    private boolean searchFocused;
    private int scroll;

    public BlockPickerScreen(class_437 parent) {
        super(class_2561.method_43470("Block ESP"));
        this.parent = parent;
        for (class_2248 block : class_7923.field_41175) {
            blocks.add(block);
        }
        blocks.sort(Comparator.comparing(block -> class_7923.field_41175.method_10221(block).toString()));
    }

    @Override
    public boolean method_25421() {
        return false;
    }

    @Override
    public void method_25394(class_332 graphics, int mouseX, int mouseY, float partialTick) {
        method_52752(graphics);
        graphics.method_25294(0, 0, field_22789, field_22790, 0x99000000);

        int panelWidth = Math.min(PANEL_WIDTH, field_22789 - 24);
        int panelX = Math.max(12, (field_22789 - panelWidth) / 2);
        int panelHeight = Math.min(field_22790 - 24, 720);
        int panelY = Math.max(12, Math.min(PANEL_TOP, field_22790 - panelHeight - 12));
        GuiTheme.panel(graphics, panelX, panelY, panelWidth, panelHeight);
        GuiTheme.header(graphics, panelX, panelY, panelWidth, HEADER_HEIGHT, "BLOCK ESP", true);

        int inputX = panelX + 12;
        int inputY = panelY + HEADER_HEIGHT + 10;
        String input = searchText.isEmpty() && !searchFocused ? "SEARCH BLOCKS" : searchText
                + (searchFocused && (System.currentTimeMillis() / 450L) % 2L == 0L ? "_" : "");
        GuiTheme.searchBox(graphics, inputX, inputY, panelWidth - 24, SEARCH_HEIGHT, input);

        List<class_2248> visible = visibleBlocks();
        int columns = Math.max(1, (panelWidth - 24 + TILE_GAP) / (TILE_WIDTH + TILE_GAP));
        int gridX = panelX + 12;
        int gridWidth = panelWidth - 24;
        int gridTop = inputY + SEARCH_HEIGHT + GRID_TOP_GAP;
        int gridBottom = panelY + panelHeight - 28;
        int viewportHeight = Math.max(1, gridBottom - gridTop);
        int maxScroll = Math.max(0, rows(visible.size(), columns) * (TILE_HEIGHT + TILE_GAP) - viewportHeight);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        int gridY = gridTop - scroll;

        BlockEspSettings settings = DonutUtilitiesClient.MODULES.blockEspSettings();
        graphics.method_44379(gridX, gridTop, gridX + gridWidth, gridBottom);
        for (int i = 0; i < visible.size(); i++) {
            int column = i % columns;
            int row = i / columns;
            int tileX = gridX + column * (TILE_WIDTH + TILE_GAP);
            int tileY = gridY + row * (TILE_HEIGHT + TILE_GAP);
            if (tileY + TILE_HEIGHT < panelY + HEADER_HEIGHT + 8 || tileY > panelY + panelHeight - 8) {
                continue;
            }

            class_2248 block = visible.get(i);
            boolean hovered = inside(mouseX, mouseY, tileX, tileY, TILE_WIDTH, TILE_HEIGHT);
            boolean selected = settings.isSelected(block);
            if (selected) {
                GuiTheme.selection(graphics, tileX, tileY, TILE_WIDTH, TILE_HEIGHT, hovered);
            } else {
                GuiTheme.row(graphics, tileX, tileY, TILE_WIDTH, TILE_HEIGHT, hovered);
            }

            if (block.method_8389() != class_1802.field_8162) {
                graphics.method_51445(new class_1799(block.method_8389()), tileX + 26, tileY + 5);
            }
            String name = block.method_9518().getString();
            String idName = class_7923.field_41175.method_10221(block).method_12832().replace('_', ' ');
            String label = GuiTheme.fit(name, TILE_WIDTH - 8);
            if (GuiTheme.scaledWidth(label) > TILE_WIDTH - 8) {
                label = GuiTheme.fit(idName, TILE_WIDTH - 8);
            }
            GuiTheme.text(graphics, label, tileX + Math.max(4, (TILE_WIDTH - GuiTheme.scaledWidth(label)) / 2), tileY + 39,
                    selected ? GuiTheme.ENABLED : GuiTheme.TEXT);
        }
        graphics.method_44380();

        String count = settings.selectedBlockIds().size() + " SELECTED";
        GuiTheme.mutedText(graphics, count, panelX + 14, panelY + panelHeight - 16);
        GuiTheme.mutedText(graphics, "ESC CLOSE", panelX + panelWidth - 78, panelY + panelHeight - 16);
        super.method_25394(graphics, mouseX, mouseY, partialTick);
    }

    private List<class_2248> visibleBlocks() {
        String query = searchText.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return blocks;
        }
        return blocks.stream()
                .filter(block -> {
                    String id = class_7923.field_41175.method_10221(block).toString().toLowerCase(Locale.ROOT);
                    return id.contains(query) || block.method_9518().getString().toLowerCase(Locale.ROOT).contains(query);
                })
                .toList();
    }

    private int rows(int size, int columns) {
        return Math.max(1, (size + columns - 1) / columns);
    }

    @Override
    public boolean method_25402(class_11909 event, boolean doubleClick) {
        if (event.method_74245() != 0) {
            return super.method_25402(event, doubleClick);
        }

        int panelWidth = Math.min(PANEL_WIDTH, field_22789 - 24);
        int panelX = Math.max(12, (field_22789 - panelWidth) / 2);
        int panelHeight = Math.min(field_22790 - 24, 720);
        int panelY = Math.max(12, Math.min(PANEL_TOP, field_22790 - panelHeight - 12));
        int inputX = panelX + 12;
        int inputY = panelY + HEADER_HEIGHT + 10;
        if (inside(event.comp_4798(), event.comp_4799(), inputX, inputY, panelWidth - 24, SEARCH_HEIGHT)) {
            searchFocused = true;
            return true;
        }

        List<class_2248> visible = visibleBlocks();
        int columns = Math.max(1, (panelWidth - 24 + TILE_GAP) / (TILE_WIDTH + TILE_GAP));
        int gridX = panelX + 12;
        int gridTop = inputY + SEARCH_HEIGHT + GRID_TOP_GAP;
        int gridBottom = panelY + panelHeight - 28;
        if (!inside(event.comp_4798(), event.comp_4799(), gridX, gridTop, panelWidth - 24, Math.max(1, gridBottom - gridTop))) {
            return super.method_25402(event, doubleClick);
        }
        int gridY = gridTop - scroll;
        for (int i = 0; i < visible.size(); i++) {
            int tileX = gridX + (i % columns) * (TILE_WIDTH + TILE_GAP);
            int tileY = gridY + (i / columns) * (TILE_HEIGHT + TILE_GAP);
            if (inside(event.comp_4798(), event.comp_4799(), tileX, tileY, TILE_WIDTH, TILE_HEIGHT)) {
                DonutUtilitiesClient.MODULES.blockEspSettings().toggleBlock(visible.get(i));
                DonutUtilitiesClient.SCANNER.clearType(MarkerType.BLOCK_ESP);
                DonutUtilitiesClient.saveConfig();
                return true;
            }
        }
        return super.method_25402(event, doubleClick);
    }

    @Override
    public boolean method_25401(double mouseX, double mouseY, double scrollX, double scrollY) {
        int panelWidth = Math.min(PANEL_WIDTH, field_22789 - 24);
        int panelX = Math.max(12, (field_22789 - panelWidth) / 2);
        int panelHeight = Math.min(field_22790 - 24, 720);
        int panelY = Math.max(12, Math.min(PANEL_TOP, field_22790 - panelHeight - 12));
        int gridTop = panelY + HEADER_HEIGHT + 10 + SEARCH_HEIGHT + GRID_TOP_GAP;
        int gridBottom = panelY + panelHeight - 28;
        if (!inside(mouseX, mouseY, panelX + 12, gridTop, panelWidth - 24, Math.max(1, gridBottom - gridTop))) {
            return super.method_25401(mouseX, mouseY, scrollX, scrollY);
        }
        scroll -= (int) Math.round(scrollY * (TILE_HEIGHT + TILE_GAP) * 2.0);
        return true;
    }

    @Override
    public boolean method_25400(class_11905 event) {
        if (!searchFocused || !event.method_74227()) {
            return super.method_25400(event);
        }
        if (searchText.length() < 48) {
            searchText += event.method_74226();
            scroll = 0;
        }
        return true;
    }

    @Override
    public boolean method_25404(class_11908 event) {
        if (searchFocused) {
            if (event.comp_4795() == GLFW.GLFW_KEY_BACKSPACE && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
                scroll = 0;
                return true;
            }
            if (event.comp_4795() == GLFW.GLFW_KEY_ESCAPE || event.comp_4795() == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
        }
        if (event.comp_4795() == GLFW.GLFW_KEY_ESCAPE) {
            method_25419();
            return true;
        }
        return super.method_25404(event);
    }

    @Override
    public void method_25419() {
        DonutUtilitiesClient.saveConfig();
        field_22787.method_1507(parent);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
