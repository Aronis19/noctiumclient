package dev.prchl.donututilities.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_310;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<ModuleCategory, List<Module>> byCategory = new EnumMap<>(ModuleCategory.class);
    private final SusChunkSettings susChunkSettings = new SusChunkSettings();
    private final EspSettings espSettings = new EspSettings();
    private final BlockEspSettings blockEspSettings = new BlockEspSettings();
    private final HudSettings hudSettings = new HudSettings();
    private final FreecamModule freecamModule = new FreecamModule();
    private final RadioModule radioModule = new RadioModule();
    private final MenuScaleSettings menuScaleSettings = new MenuScaleSettings();
    private final PopupSettings popupSettings = new PopupSettings();
    private final BrandSpooferSettings brandSpooferSettings = new BrandSpooferSettings();
    private final ItemInfoModule itemInfoModule = new ItemInfoModule();

    public void registerDefaults() {
        if (!modules.isEmpty()) {
            return;
        }

        add(new Module("block_entity_debug", "Block Entity Debug", ModuleCategory.BASE, "Marks loaded block entities and shows their chunk.", 0xFF71F5E8));
        add(new Module("hole_esp", "Hole ESP", ModuleCategory.BASE, "Marks enclosed air pockets and possible hidden entrances.", 0xFF8DE96B));
        add(new Module("light_finder", "Light Finder", ModuleCategory.BASE, "Marks player-like light sources in loaded chunks.", 0xFFFFD25A));
        add(new Module("prime_chunk_finder", "Prime Chunk Finder", ModuleCategory.DONUT, "Highlights high-score chunks with multiple base signals.", 0xFFFF7A90));
        add(new Module("rtp_base_finder", "RTP Base Finder", ModuleCategory.DONUT, "Resets and focuses scanning after large teleports.", 0xFFFF9D43));
        add(new Module("sus_chunk_finder", "Sus Chunk Finder", ModuleCategory.DONUT, "Scores chunks from storage, light, and unnatural block signals.", 0xFFFF5C8A));
        add(new Module("suspicious_esp", "Suspicious ESP", ModuleCategory.BASE, "Marks crafted, redstone, portal, rail, and utility blocks.", 0xFFB48CFF));
        add(new Module("seed_chunk_finder", "Seed Chunk Finder", ModuleCategory.DONUT, "Pattern helper for unusual loaded chunk features, not server-seed extraction.", 0xFF63B3FF));

        add(new Module("storage_esp", "Storage ESP", ModuleCategory.RENDER, "Marks chests, barrels, hoppers, furnaces, and shulkers.", 0xFFE6C55C));
        add(new Module("block_esp", "Block ESP", ModuleCategory.RENDER, "Marks selected suspicious blocks from the scanner.", 0xFF90CAF9));
        add(new Module("player_esp", "Player ESP", ModuleCategory.RENDER, "Adds visible particles around loaded players.", 0xFF64FFDA));
        add(new Module("mob_esp", "Mob ESP", ModuleCategory.RENDER, "Adds visible particles around nearby mobs.", 0xFFFFAB91));
        add(new FullbrightModule());
        add(freecamModule);

        add(new Module("menu_scale", "Menu Scale", ModuleCategory.MISC, "Changes the size of the Right Shift ClickGUI.", 0xFF90CAF9));
        add(itemInfoModule);
        add(radioModule);

        Module moduleList = new Module("module_list", "Module List", ModuleCategory.CLIENT, "Shows enabled modules in the top-right HUD.", 0xFF9CF7F2);
        moduleList.setEnabled(true);
        add(moduleList);
        add(new Module("click_gui", "Click GUI", ModuleCategory.CLIENT, "Right Shift menu.", 0xFF9CF7F2));
        add(new Module("hud", "HUD", ModuleCategory.CLIENT, "Shows scan state and the Spotify radio HUD.", 0xFFFFFFFF));
        Module popup = new Module("popup", "Popup", ModuleCategory.CLIENT, "Shows animated module status notifications.", 0xFF8BE9FD);
        popup.setEnabled(true);
        add(popup);
        add(new Module("brand_spoofer", "Brand Spoofer", ModuleCategory.CLIENT, "Spoofs the client brand sent to servers.", 0xFF8BE9FD));
    }

    private void add(Module module) {
        modules.add(module);
        byCategory.computeIfAbsent(module.category(), ignored -> new ArrayList<>()).add(module);
    }

    public List<Module> modules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> modules(ModuleCategory category) {
        return byCategory.getOrDefault(category, List.of());
    }

    public Optional<Module> find(String id) {
        return modules.stream().filter(module -> module.id().equals(id)).findFirst();
    }

    public boolean enabled(String id) {
        return find(id).map(Module::enabled).orElse(false);
    }

    public SusChunkSettings susChunkSettings() {
        return susChunkSettings;
    }

    public EspSettings espSettings() {
        return espSettings;
    }

    public BlockEspSettings blockEspSettings() {
        return blockEspSettings;
    }

    public HudSettings hudSettings() {
        return hudSettings;
    }

    public FreecamModule freecamModule() {
        return freecamModule;
    }

    public RadioModule radioModule() {
        return radioModule;
    }

    public MenuScaleSettings menuScaleSettings() {
        return menuScaleSettings;
    }

    public PopupSettings popupSettings() {
        return popupSettings;
    }

    public BrandSpooferSettings brandSpooferSettings() {
        return brandSpooferSettings;
    }

    public ItemInfoModule itemInfoModule() {
        return itemInfoModule;
    }

    public void registerKeybinds() {
        freecamModule.registerKeybind();
        radioModule.registerKeybind();
    }

    public void tickKeybinds(class_310 client) {
        freecamModule.tickKeybind(client);
        radioModule.tickKeybind(client);
    }

    public void tick(class_310 client) {
        for (Module module : modules) {
            if (module.enabled()) {
                module.tick(client);
            }
        }
    }
}
