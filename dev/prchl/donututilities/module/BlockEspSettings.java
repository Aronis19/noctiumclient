package dev.prchl.donututilities.module;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2680;
import net.minecraft.class_7923;

public final class BlockEspSettings {
    private boolean normalChests = true;
    private boolean trappedChests = true;
    private boolean barrels = true;
    private boolean enderChests = true;
    private boolean shulkers = true;
    private boolean hoppers;
    private boolean droppers;
    private boolean dispensers;
    private boolean crafters;
    private boolean furnaces;
    private boolean pots;
    private boolean spawners = true;
    private boolean ancientDebris = true;
    private boolean diamondOre = true;
    private boolean emeraldOre;
    private boolean goldOre;
    private boolean utility = true;
    private boolean redstone;
    private boolean obsidian = true;
    private boolean lights;
    private boolean customBlock;
    private String customBlockId = "minecraft:deepslate_diamond_ore,minecraft:spawner";
    private boolean explicitSelection = true;
    private final Set<String> selectedBlockIds = new LinkedHashSet<>();

    public boolean explicitSelection() {
        return explicitSelection;
    }

    public boolean isSelected(class_2248 block) {
        return selectedBlockIds.contains(class_7923.field_41175.method_10221(block).toString());
    }

    public Set<String> selectedBlockIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(selectedBlockIds));
    }

    public int selectedCount() {
        return selectedBlockIds.size();
    }

    public void setSelectedBlockIds(Set<String> ids, boolean explicit) {
        selectedBlockIds.clear();
        if (ids != null) {
            selectedBlockIds.addAll(ids);
        }
        explicitSelection = explicit;
    }

    public void toggleBlock(class_2248 block) {
        materializeLegacySelection();
        String id = class_7923.field_41175.method_10221(block).toString();
        if (!selectedBlockIds.add(id)) {
            selectedBlockIds.remove(id);
        }
    }

    private void materializeLegacySelection() {
        if (explicitSelection) {
            return;
        }
        explicitSelection = true;
        selectedBlockIds.clear();
        for (class_2248 block : class_7923.field_41175) {
            if (matchesLegacy(block.method_9564())) {
                selectedBlockIds.add(class_7923.field_41175.method_10221(block).toString());
            }
        }
    }

    public boolean normalChests() {
        return normalChests;
    }

    public void toggleNormalChests() {
        normalChests = !normalChests;
    }

    public boolean trappedChests() {
        return trappedChests;
    }

    public void toggleTrappedChests() {
        trappedChests = !trappedChests;
    }

    public boolean barrels() {
        return barrels;
    }

    public void toggleBarrels() {
        barrels = !barrels;
    }

    public boolean enderChests() {
        return enderChests;
    }

    public void toggleEnderChests() {
        enderChests = !enderChests;
    }

    public boolean shulkers() {
        return shulkers;
    }

    public void toggleShulkers() {
        shulkers = !shulkers;
    }

    public boolean hoppers() {
        return hoppers;
    }

    public void toggleHoppers() {
        hoppers = !hoppers;
    }

    public boolean droppers() {
        return droppers;
    }

    public void toggleDroppers() {
        droppers = !droppers;
    }

    public boolean dispensers() {
        return dispensers;
    }

    public void toggleDispensers() {
        dispensers = !dispensers;
    }

    public boolean crafters() {
        return crafters;
    }

    public void toggleCrafters() {
        crafters = !crafters;
    }

    public boolean furnaces() {
        return furnaces;
    }

    public void toggleFurnaces() {
        furnaces = !furnaces;
    }

    public boolean pots() {
        return pots;
    }

    public void togglePots() {
        pots = !pots;
    }

    public boolean spawners() {
        return spawners;
    }

    public void toggleSpawners() {
        spawners = !spawners;
    }

    public boolean ancientDebris() {
        return ancientDebris;
    }

    public void toggleAncientDebris() {
        ancientDebris = !ancientDebris;
    }

    public boolean diamondOre() {
        return diamondOre;
    }

    public void toggleDiamondOre() {
        diamondOre = !diamondOre;
    }

    public boolean emeraldOre() {
        return emeraldOre;
    }

    public void toggleEmeraldOre() {
        emeraldOre = !emeraldOre;
    }

    public boolean goldOre() {
        return goldOre;
    }

    public void toggleGoldOre() {
        goldOre = !goldOre;
    }

    public boolean utility() {
        return utility;
    }

    public void toggleUtility() {
        utility = !utility;
    }

    public boolean redstone() {
        return redstone;
    }

    public void toggleRedstone() {
        redstone = !redstone;
    }

    public boolean obsidian() {
        return obsidian;
    }

    public void toggleObsidian() {
        obsidian = !obsidian;
    }

    public boolean lights() {
        return lights;
    }

    public void toggleLights() {
        lights = !lights;
    }

    public boolean customBlock() {
        return customBlock;
    }

    public void toggleCustomBlock() {
        customBlock = !customBlock;
    }

    public String customBlockId() {
        return customBlockId;
    }

    public void setCustomBlockId(String customBlockId) {
        String clean = customBlockId.trim().toLowerCase(java.util.Locale.ROOT);
        if (clean.length() > 160) {
            clean = clean.substring(0, 160);
        }
        this.customBlockId = clean;
    }

    public boolean removeLastCustomChar() {
        if (customBlockId.isEmpty()) {
            return false;
        }
        customBlockId = customBlockId.substring(0, customBlockId.length() - 1);
        return true;
    }

    public void appendCustomChar(String value) {
        if (customBlockId.length() >= 160) {
            return;
        }
        String clean = value.toLowerCase(java.util.Locale.ROOT);
        if (clean.matches("[a-z0-9_:.\\-/,; ]")) {
            customBlockId += clean;
        }
    }

    public boolean matches(class_2680 state) {
        return selectedBlockIds.contains(class_7923.field_41175.method_10221(state.method_26204()).toString());
    }

    private boolean matchesLegacy(class_2680 state) {
        class_2248 block = state.method_26204();
        return (normalChests && isNormalChest(block))
                || (trappedChests && isTrappedChest(block))
                || (barrels && block == class_2246.field_16328)
                || (enderChests && block == class_2246.field_10443)
                || (shulkers && isShulker(block))
                || (hoppers && block == class_2246.field_10312)
                || (droppers && block == class_2246.field_10228)
                || (dispensers && block == class_2246.field_10200)
                || (crafters && block == class_2246.field_46797)
                || (furnaces && isFurnace(block))
                || (pots && block == class_2246.field_42752)
                || (spawners && block == class_2246.field_10260)
                || (ancientDebris && block == class_2246.field_22109)
                || (diamondOre && isDiamondOre(block))
                || (emeraldOre && isEmeraldOre(block))
                || (goldOre && isGoldOre(block))
                || (utility && isUtility(block))
                || (redstone && isRedstone(block))
                || (obsidian && isObsidian(block))
                || (lights && isLight(block))
                || (customBlock && isCustomBlock(block));
    }

    public int color(class_2680 state) {
        class_2248 block = state.method_26204();
        if (isNormalChest(block) || block == class_2246.field_16328) {
            return 0xFF00FF66;
        }
        if (isTrappedChest(block)) {
            return 0xFFFF7A00;
        }
        if (block == class_2246.field_10443) {
            return 0xFF00D9FF;
        }
        if (isShulker(block)) {
            return 0xFFFF3DFF;
        }
        if (block == class_2246.field_10312 || block == class_2246.field_10228 || block == class_2246.field_10200 || block == class_2246.field_46797) {
            return 0xFFFFFFFF;
        }
        if (isFurnace(block)) {
            return 0xFFFF3333;
        }
        if (block == class_2246.field_42752) {
            return 0xFF00FF66;
        }
        if (block == class_2246.field_10260) {
            return 0xFF66E3FF;
        }
        if (block == class_2246.field_22109) {
            return 0xFFFF8A65;
        }
        if (isDiamondOre(block)) {
            return 0xFF64E8FF;
        }
        if (isEmeraldOre(block)) {
            return 0xFF58E88F;
        }
        if (isGoldOre(block)) {
            return 0xFFFFD35A;
        }
        if (isUtility(block)) {
            return 0xFFFFB86C;
        }
        if (isRedstone(block)) {
            return 0xFFFF5A6F;
        }
        if (isObsidian(block)) {
            return 0xFF9D7DFF;
        }
        if (isLight(block)) {
            return 0xFFFFF176;
        }
        if (isCustomBlock(block)) {
            return 0xFF90CAF9;
        }
        return 0xFF90CAF9;
    }

    public String label(class_2680 state) {
        return state.method_26204().method_9518().getString();
    }

    private boolean isNormalChest(class_2248 block) {
        return block == class_2246.field_10034
                || block == class_2246.field_61388
                || block == class_2246.field_61389
                || block == class_2246.field_61390
                || block == class_2246.field_61391
                || block == class_2246.field_61392
                || block == class_2246.field_61393
                || block == class_2246.field_61394
                || block == class_2246.field_61395;
    }

    private boolean isTrappedChest(class_2248 block) {
        return block == class_2246.field_10380;
    }

    private boolean isShulker(class_2248 block) {
        return block == class_2246.field_10603
                || block == class_2246.field_10199
                || block == class_2246.field_10407
                || block == class_2246.field_10063
                || block == class_2246.field_10203
                || block == class_2246.field_10600
                || block == class_2246.field_10275
                || block == class_2246.field_10051
                || block == class_2246.field_10140
                || block == class_2246.field_10320
                || block == class_2246.field_10532
                || block == class_2246.field_10268
                || block == class_2246.field_10605
                || block == class_2246.field_10373
                || block == class_2246.field_10055
                || block == class_2246.field_10068
                || block == class_2246.field_10371;
    }

    private boolean isDiamondOre(class_2248 block) {
        return block == class_2246.field_10442 || block == class_2246.field_29029;
    }

    private boolean isEmeraldOre(class_2248 block) {
        return block == class_2246.field_10013 || block == class_2246.field_29220;
    }

    private boolean isGoldOre(class_2248 block) {
        return block == class_2246.field_10571
                || block == class_2246.field_29026
                || block == class_2246.field_23077;
    }

    private boolean isFurnace(class_2248 block) {
        return block == class_2246.field_10181
                || block == class_2246.field_16333
                || block == class_2246.field_16334;
    }

    private boolean isUtility(class_2248 block) {
        return block == class_2246.field_9980
                || block == class_2246.field_10535
                || block == class_2246.field_10105
                || block == class_2246.field_10414
                || block == class_2246.field_10485
                || block == class_2246.field_10333
                || block == class_2246.field_10593
                || block == class_2246.field_23152
                || block == class_2246.field_10327;
    }

    private boolean isRedstone(class_2248 block) {
        return block == class_2246.field_10091
                || block == class_2246.field_10450
                || block == class_2246.field_10377
                || block == class_2246.field_10560
                || block == class_2246.field_10615
                || block == class_2246.field_10282
                || block == class_2246.field_10200
                || block == class_2246.field_10228
                || block == class_2246.field_10312;
    }

    private boolean isObsidian(class_2248 block) {
        return block == class_2246.field_10540
                || block == class_2246.field_22423
                || block == class_2246.field_10316;
    }

    private boolean isLight(class_2248 block) {
        return block == class_2246.field_10336
                || block == class_2246.field_10099
                || block == class_2246.field_22092
                || block == class_2246.field_22093
                || block == class_2246.field_16541
                || block == class_2246.field_22110
                || block == class_2246.field_10523
                || block == class_2246.field_10301
                || block == class_2246.field_10171
                || block == class_2246.field_10174
                || block == class_2246.field_10009
                || block == class_2246.field_10524
                || block == class_2246.field_17350
                || block == class_2246.field_23860;
    }

    private boolean isCustomBlock(class_2248 block) {
        if (customBlockId.isBlank()) {
            return false;
        }
        String blockId = class_7923.field_41175.method_10221(block).toString();
        String[] selectedBlocks = customBlockId.split("[,;\\s]+");
        for (String selectedBlock : selectedBlocks) {
            if (selectedBlock.isBlank()) {
                continue;
            }
            String selected = selectedBlock.contains(":") ? selectedBlock : "minecraft:" + selectedBlock;
            if (blockId.equals(selected)) {
                return true;
            }
        }
        return false;
    }
}
