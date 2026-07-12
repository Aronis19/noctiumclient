package dev.prchl.donututilities.scan;

import dev.prchl.donututilities.module.ModuleManager;
import dev.prchl.donututilities.module.SusChunkSettings;
import dev.prchl.donututilities.freecam.FreecamController;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2465;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_638;

public final class ScanManager {
    private static final int MAX_MARKERS = 60000;
    private static final int MAX_SCAN_RADIUS_CHUNKS = 96;
    private static final int KEEP_TICKS = 20 * 240;
    private static final int FAST_ESP_KEEP_TICKS = 20 * 180;
    private static final int ESP_KEEP_TICKS = 20 * 180;
    private static final int LIGHT_BLOCK_SCAN_BUDGET = 4096;
    private static final int HEAVY_BLOCK_SCAN_BUDGET = 1024;
    private static final int FAST_BLOCK_ESP_MAX_HITS_PER_CHUNK = 2048;
    private static final int FOCUSED_BLOCK_ESP_MAX_HITS_PER_CHUNK = 16384;
    private static final int ESP_BURST_TICKS = 20 * 3;
    private static final int ESP_BURST_MAX_CHUNKS = 384;
    private static final int ESP_BACKGROUND_MAX_CHUNKS = 64;
    private static final long ESP_BURST_BUDGET_NANOS = 4_000_000L;
    private static final int AUTO_SUS_CANDIDATES_PER_TICK = 2048;
    private static final long AUTO_SUS_BUDGET_NANOS = 3_000_000L;
    private static final long AUTO_SUS_RESCAN_TICKS = 20L * 60L;

    private final Map<String, BaseMarker> markers = new HashMap<>();
    private final Map<Long, Integer> blockScanCursors = new HashMap<>();
    private final Map<Long, Long> automaticSusScans = new HashMap<>();
    private final List<class_1923> scanOrder = new ArrayList<>();
    private int scanIndex;
    private int automaticSusScanIndex;
    private long ticks;
    private class_2338 lastPlayerPos;
    private class_2338 lastScanCenter;
    private int lastScanRadius = -1;
    private long lastFocusedBlockEspChunk = Long.MIN_VALUE;
    private long lastOverlayBlockEspChunk = Long.MIN_VALUE;
    private boolean lastScanWasFreecam;
    private boolean lastBlockEspEnabled;
    private boolean lastStorageEspEnabled;
    private boolean lastAutomaticSusEnabled;
    private int espBurstTicksRemaining;

    public void tick(class_310 client, ModuleManager modules) {
        ticks++;
        if (client.field_1687 == null || client.field_1724 == null) {
            markers.clear();
            blockScanCursors.clear();
            automaticSusScans.clear();
            scanOrder.clear();
            lastPlayerPos = null;
            lastScanCenter = null;
            lastBlockEspEnabled = false;
            lastStorageEspEnabled = false;
            lastAutomaticSusEnabled = false;
            automaticSusScanIndex = 0;
            espBurstTicksRemaining = 0;
            lastOverlayBlockEspChunk = Long.MIN_VALUE;
            lastScanWasFreecam = false;
            return;
        }

        class_2338 playerPos = client.field_1724.method_24515();
        int clientRenderDistance = client.field_1690.method_42503().method_41753();
        int scanRadius = Math.max(2, Math.min(MAX_SCAN_RADIUS_CHUNKS,
                Math.max(modules.susChunkSettings().simulationDistance(), clientRenderDistance)));
        class_2338 overlayPos = FreecamController.active()
                ? class_2338.method_49638(FreecamController.position())
                : playerPos;
        boolean freecamActive = FreecamController.active();
        class_2338 scanCenter = freecamActive ? overlayPos : playerPos;
        boolean scanOrderChanged = lastScanCenter == null || movedChunk(scanCenter, lastScanCenter)
                || scanRadius != lastScanRadius || freecamActive != lastScanWasFreecam;
        if (scanOrderChanged) {
            rebuildScanOrder(scanCenter, scanRadius);
            if (freecamActive) {
                automaticSusScans.clear();
                automaticSusScanIndex = 0;
            }
        }

        if (modules.enabled("rtp_base_finder") && lastPlayerPos != null && playerPos.method_10262(lastPlayerPos) > 250000.0) {
            markers.clear();
            rebuildScanOrder(playerPos, scanRadius);
        }
        lastPlayerPos = playerPos;
        lastScanCenter = scanCenter;
        lastScanWasFreecam = freecamActive;
        lastOverlayBlockEspChunk = freecamActive ? class_1923.method_8331(overlayPos.method_10263() >> 4, overlayPos.method_10260() >> 4) : Long.MIN_VALUE;
        removeOutOfScopeMarkers(client.field_1687, overlayPos, clientRenderDistance);
        pruneIgnoredChunkFinderMarkers(client.field_1687);

        // Remove visual ESP state immediately when its module is switched off.
        // This runs before the scan-order early return, so old boxes cannot
        // remain visible while the client is between loaded chunks.
        boolean blockEspEnabled = modules.enabled("block_esp");
        boolean storageEspEnabled = modules.enabled("storage_esp");
        if (!blockEspEnabled) {
            clearType(MarkerType.BLOCK_ESP);
            lastFocusedBlockEspChunk = Long.MIN_VALUE;
        }
        if (!storageEspEnabled) {
            clearType(MarkerType.STORAGE);
        }

        if (scanOrder.isEmpty()) {
            return;
        }

        boolean automaticSusEnabled = modules.enabled("sus_chunk_finder")
                || modules.enabled("prime_chunk_finder") || modules.enabled("seed_chunk_finder");
        if (automaticSusEnabled && !lastAutomaticSusEnabled) {
            automaticSusScans.clear();
            automaticSusScanIndex = 0;
        }
        lastAutomaticSusEnabled = automaticSusEnabled;
        if (automaticSusEnabled) {
            scanAutomaticLoadedSusChunk(client.field_1687, modules);
        }

        boolean espEnabled = blockEspEnabled || storageEspEnabled;
        if (espEnabled && (scanOrderChanged || blockEspEnabled != lastBlockEspEnabled || storageEspEnabled != lastStorageEspEnabled)) {
            espBurstTicksRemaining = ESP_BURST_TICKS;
        }
        lastBlockEspEnabled = blockEspEnabled;
        lastStorageEspEnabled = storageEspEnabled;

        if (blockEspEnabled) {
            scanFocusedCurrentBlockEsp(client.field_1687, overlayPos, modules);
        }
        if (FreecamController.active() && storageEspEnabled) {
            int freecamChunkX = overlayPos.method_10263() >> 4;
            int freecamChunkZ = overlayPos.method_10260() >> 4;
            if (client.field_1687.method_8393(freecamChunkX, freecamChunkZ)) {
                scanStorageEsp(client.field_1687, client.field_1687.method_8497(freecamChunkX, freecamChunkZ));
            }
        }
        if (espEnabled) {
            scanEspBurst(client.field_1687, modules, espBurstTicksRemaining > 0 ? ESP_BURST_MAX_CHUNKS : ESP_BACKGROUND_MAX_CHUNKS);
            if (espBurstTicksRemaining > 0) {
                espBurstTicksRemaining--;
            }
        }

        boolean heavyScan = heavyScan(modules);
        if (heavyScan && ticks % 2L != 0L) {
            if (blockEspEnabled) {
                scanFastBlockEspOnly(client.field_1687, modules, 24);
            }
            removeExpiredMarkers();
            trimMarkers();
            return;
        }

        int chunksPerTick = blockEspEnabled ? (heavyScan ? 16 : 32) : (heavyScan ? 3 : 8);
        int loadedScans = 0;
        int attempts = 0;
        while (loadedScans < chunksPerTick && attempts < scanOrder.size()) {
            class_1923 chunkPos = scanOrder.get(scanIndex);
            scanIndex = (scanIndex + 1) % scanOrder.size();
            attempts++;
            if (!client.field_1687.method_8393(chunkPos.field_9181, chunkPos.field_9180)) {
                continue;
            }
            scanChunk(client.field_1687, chunkPos, modules);
            loadedScans++;
        }

        removeExpiredMarkers();
        trimMarkers();
    }

    public List<BaseMarker> markers() {
        return List.copyOf(markers.values());
    }

    public List<BaseMarker> strongestMarkers(int count) {
        return markers.values().stream()
                .sorted(Comparator.comparingInt(BaseMarker::score).reversed())
                .limit(count)
                .toList();
    }

    public void clear() {
        markers.clear();
        blockScanCursors.clear();
        scanOrder.clear();
        automaticSusScans.clear();
        lastPlayerPos = null;
        lastScanRadius = -1;
        scanIndex = 0;
        automaticSusScanIndex = 0;
        lastAutomaticSusEnabled = false;
    }

    public void clearType(MarkerType type) {
        markers.entrySet().removeIf(entry -> entry.getValue().type() == type);
    }

    private boolean movedChunk(class_2338 current, class_2338 previous) {
        return (current.method_10263() >> 4) != (previous.method_10263() >> 4) || (current.method_10260() >> 4) != (previous.method_10260() >> 4);
    }

    private boolean heavyScan(ModuleManager modules) {
        return modules.enabled("sus_chunk_finder")
                && (modules.enabled("suspicious_esp")
                || modules.enabled("block_esp")
                || modules.enabled("light_finder")
                || modules.enabled("hole_esp")
                || modules.enabled("storage_esp")
                || modules.enabled("block_entity_debug"));
    }

    private void rebuildScanOrder(class_2338 center, int chunkRadius) {
        scanOrder.clear();
        if (blockScanCursors.size() > 65536) {
            blockScanCursors.clear();
        }
        lastScanRadius = chunkRadius;
        int chunkX = center.method_10263() >> 4;
        int chunkZ = center.method_10260() >> 4;

        scanOrder.add(new class_1923(chunkX, chunkZ));
        for (int radius = 1; radius <= chunkRadius; radius++) {
            int minX = chunkX - radius;
            int maxX = chunkX + radius;
            int minZ = chunkZ - radius;
            int maxZ = chunkZ + radius;
            for (int x = minX; x <= maxX; x++) {
                scanOrder.add(new class_1923(x, minZ));
                scanOrder.add(new class_1923(x, maxZ));
            }
            for (int z = minZ + 1; z < maxZ; z++) {
                scanOrder.add(new class_1923(minX, z));
                scanOrder.add(new class_1923(maxX, z));
            }
        }
        scanIndex = 0;
        automaticSusScanIndex = 0;
        if (automaticSusScans.size() > MAX_SCAN_RADIUS_CHUNKS * MAX_SCAN_RADIUS_CHUNKS * 4) {
            automaticSusScans.clear();
        }
    }

    private void scanAutomaticLoadedSusChunk(class_638 level, ModuleManager modules) {
        long deadline = System.nanoTime() + AUTO_SUS_BUDGET_NANOS;
        int candidates = 0;
        int scanned = 0;
        while (candidates < Math.min(AUTO_SUS_CANDIDATES_PER_TICK, scanOrder.size())
                && System.nanoTime() < deadline) {
            class_1923 chunkPos = scanOrder.get(automaticSusScanIndex);
            automaticSusScanIndex = (automaticSusScanIndex + 1) % scanOrder.size();
            candidates++;
            if (!level.method_8393(chunkPos.field_9181, chunkPos.field_9180)) {
                continue;
            }

            long chunkKey = chunkPos.method_8324();
            long lastScan = automaticSusScans.getOrDefault(chunkKey, Long.MIN_VALUE / 2L);
            if (ticks - lastScan < AUTO_SUS_RESCAN_TICKS) {
                continue;
            }

            scanSusChunkFully(level, level.method_8497(chunkPos.field_9181, chunkPos.field_9180), modules);
            automaticSusScans.put(chunkKey, ticks);
            scanned++;
            if (scanned >= 4 || System.nanoTime() >= deadline) {
                return;
            }
        }
    }

    private void scanFreecamSusArea(class_638 level, class_2338 center, ModuleManager modules) {
        int centerChunkX = center.method_10263() >> 4;
        int centerChunkZ = center.method_10260() >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;
                if (level.method_8393(chunkX, chunkZ)) {
                    scanSusChunkFully(level, level.method_8497(chunkX, chunkZ), modules);
                }
            }
        }
    }

    private void scanSusChunkFully(class_638 level, class_2818 chunk, ModuleManager modules) {
        class_1923 chunkPos = chunk.method_12004();
        clearChunkFinderMarkers(chunkPos);
        if (containsSpawnerRoom(chunk) || containsAmethystRoom(chunk)
                || (containsTrialChamber(chunk) || containsMineshaftSignature(chunk) || containsLushCaveSignature(chunk))
                && !hasUndergroundStorage(level, chunk)) {
            return;
        }
        ChunkScore score = new ChunkScore(chunkPos);
        for (Map.Entry<class_2338, class_2586> entry : chunk.method_12214().entrySet()) {
            class_2248 block = level.method_8320(entry.getKey()).method_26204();
            score.blockEntity();
            if (isStorageBlock(block)) {
                if (entry.getKey().method_10264() < 0) {
                    score.undergroundStorage();
                } else {
                    score.storage();
                }
            }
        }

        SusChunkSettings settings = modules.susChunkSettings();
        class_2826[] sections = chunk.method_12006();
        int minY = Math.max(level.method_31607(), -64);
        int maxY = Math.min(level.method_31600(), 160);
        int minSectionY = level.method_32891();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            class_2826 section = sections[sectionIndex];
            int sectionBaseY = (minSectionY + sectionIndex) << 4;
            int sectionTopY = sectionBaseY + 16;
            if (sectionTopY <= minY || sectionBaseY >= maxY || section.method_38292()
                    || !section.method_19523(state -> isLightBlock(state.method_26204()) || suspiciousWeight(state, settings) > 0)) {
                continue;
            }

            int localMinY = Math.max(0, minY - sectionBaseY);
            int localMaxY = Math.min(16, maxY - sectionBaseY);
            for (int y = localMinY; y < localMaxY; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        class_2680 state = section.method_12254(x, y, z);
                        if (y + sectionBaseY < 0 && isStorageBlock(state.method_26204())) {
                            score.undergroundStorage();
                        }
                        if (settings.kelp() && isKelp(state.method_26204())) {
                            score.kelp();
                            continue;
                        }
                        if (isLightBlock(state.method_26204())) {
                            score.light();
                        }
                        int weight = suspiciousWeight(state, settings);
                        if (weight > 0 && !isLightBlock(state.method_26204())) {
                            score.suspicious(weight);
                        }
                    }
                }
            }
        }

        int centerX = (chunkPos.field_9181 << 4) + 8;
        int centerZ = (chunkPos.field_9180 << 4) + 8;
        class_2338 center = new class_2338(centerX, Math.max(level.method_31607() + 4, 64), centerZ);
        int threshold = susThreshold(settings);
        if (modules.enabled("sus_chunk_finder") && isProbableBaseChunk(score, threshold)) {
            put(center, MarkerType.SUS_CHUNK, "Sus Chunk " + score.label(), 0xFFFF5C8A, score.score());
        }
        if (modules.enabled("prime_chunk_finder") && isProbableBaseChunk(score, threshold + 18)) {
            put(center.method_10086(2), MarkerType.PRIME_CHUNK, "Prime Chunk " + score.label(), 0xFFFF7A90, score.score() + 10);
        }
        if (modules.enabled("seed_chunk_finder") && isProbableBaseChunk(score, threshold + 6)) {
            put(center.method_10086(4), MarkerType.SEED_PATTERN, "Pattern Chunk " + score.label(), 0xFF63B3FF, score.score());
        }
    }

    private void scanChunk(class_638 level, class_1923 chunkPos, ModuleManager modules) {
        if (!level.method_8393(chunkPos.field_9181, chunkPos.field_9180)) {
            return;
        }

        class_2818 chunk = level.method_8497(chunkPos.field_9181, chunkPos.field_9180);
        clearChunkFinderMarkers(chunkPos);
        boolean ignoredStructure = containsSpawnerRoom(chunk) || containsAmethystRoom(chunk)
                || ((containsTrialChamber(chunk)
                || containsMineshaftSignature(chunk) || containsLushCaveSignature(chunk))
                && !hasUndergroundStorage(level, chunk));
        ChunkScore score = new ChunkScore(chunkPos);

        if (modules.enabled("block_entity_debug") || modules.enabled("storage_esp") || modules.enabled("sus_chunk_finder") || modules.enabled("prime_chunk_finder")) {
            for (Map.Entry<class_2338, class_2586> entry : chunk.method_12214().entrySet()) {
                class_2338 pos = entry.getKey();
                class_2248 block = level.method_8320(pos).method_26204();
                if (!ignoredStructure) {
                    score.blockEntity();
                }

                if (isStorageBlock(block)) {
                    if (!ignoredStructure) {
                        if (pos.method_10264() < 0) {
                            score.undergroundStorage();
                        } else {
                            score.storage();
                        }
                    }
                    if (!ignoredStructure && modules.enabled("storage_esp") && pos.method_10264() < 0) {
                        put(pos, MarkerType.STORAGE, block.method_9518().getString(), storageColor(block), 12);
                    }
                }

                if (modules.enabled("block_entity_debug")) {
                    put(pos, MarkerType.BLOCK_ENTITY, entry.getValue().method_11017().toString(), 0xFF71F5E8, 8);
                }
            }
        }

        boolean scanBlocks = modules.enabled("light_finder") || modules.enabled("hole_esp") || modules.enabled("suspicious_esp")
                || modules.enabled("sus_chunk_finder") || modules.enabled("prime_chunk_finder")
                || modules.enabled("seed_chunk_finder");
        if (scanBlocks) {
            scanBlocks(level, chunkPos, modules, score, !ignoredStructure);
        }
        if (modules.enabled("block_esp")) {
            scanBlockEspFast(level, chunk, modules);
        }

        int centerX = (chunkPos.field_9181 << 4) + 8;
        int centerZ = (chunkPos.field_9180 << 4) + 8;
        int y = Math.max(level.method_31607() + 4, 64);
        class_2338 center = new class_2338(centerX, y, centerZ);

        int susThreshold = susThreshold(modules.susChunkSettings());
        if (modules.enabled("sus_chunk_finder") && isProbableBaseChunk(score, susThreshold)) {
            put(center, MarkerType.SUS_CHUNK, "Sus Chunk " + score.label(), 0xFFFF5C8A, score.score());
        }
        if (modules.enabled("prime_chunk_finder") && isProbableBaseChunk(score, susThreshold + 18)) {
            put(center.method_10086(2), MarkerType.PRIME_CHUNK, "Prime Chunk " + score.label(), 0xFFFF7A90, score.score() + 10);
        }
        if (modules.enabled("seed_chunk_finder") && isProbableBaseChunk(score, susThreshold + 6)) {
            put(center.method_10086(4), MarkerType.SEED_PATTERN, "Pattern Chunk " + score.label(), 0xFF63B3FF, score.score());
        }
    }

    private void scanFastBlockEspOnly(class_638 level, ModuleManager modules, int chunks) {
        if (scanOrder.isEmpty()) {
            return;
        }

        for (int i = 0; i < chunks; i++) {
            class_1923 chunkPos = scanOrder.get(scanIndex);
            scanIndex = (scanIndex + 1) % scanOrder.size();
            if (!level.method_8393(chunkPos.field_9181, chunkPos.field_9180)) {
                continue;
            }

            scanBlockEspFast(level, level.method_8497(chunkPos.field_9181, chunkPos.field_9180), modules);
        }
    }

    private void scanEspBurst(class_638 level, ModuleManager modules, int maxChunks) {
        if (scanOrder.isEmpty()) {
            return;
        }

        long deadline = System.nanoTime() + ESP_BURST_BUDGET_NANOS;
        int loadedChunks = 0;
        int attempts = 0;
        while (loadedChunks < maxChunks && attempts < scanOrder.size() && System.nanoTime() < deadline) {
            class_1923 chunkPos = scanOrder.get(scanIndex);
            scanIndex = (scanIndex + 1) % scanOrder.size();
            attempts++;
            if (!level.method_8393(chunkPos.field_9181, chunkPos.field_9180)) {
                continue;
            }
            loadedChunks++;

            class_2818 chunk = level.method_8497(chunkPos.field_9181, chunkPos.field_9180);
            if (modules.enabled("storage_esp")) {
                scanStorageEsp(level, chunk);
            }
            if (modules.enabled("block_esp")) {
                scanBlockEspFast(level, chunk, modules);
            }
        }
    }

    private void scanStorageEsp(class_638 level, class_2818 chunk) {
        clearMarkersInChunk(MarkerType.STORAGE, chunk.method_12004());
        for (Map.Entry<class_2338, class_2586> entry : chunk.method_12214().entrySet()) {
            class_2338 pos = entry.getKey();
            if (pos.method_10264() >= 0) {
                continue;
            }

            class_2248 block = level.method_8320(pos).method_26204();
            if (isStorageBlock(block)) {
                put(pos, MarkerType.STORAGE, block.method_9518().getString(), storageColor(block), 12);
            }
        }
    }

    private void scanFocusedCurrentBlockEsp(class_638 level, class_2338 playerPos, ModuleManager modules) {
        int chunkX = playerPos.method_10263() >> 4;
        int chunkZ = playerPos.method_10260() >> 4;
        long chunkKey = class_1923.method_8331(chunkX, chunkZ);
        if (chunkKey == lastFocusedBlockEspChunk && ticks % 20L != 0L) {
            return;
        }
        lastFocusedBlockEspChunk = chunkKey;

        if (level.method_8393(chunkX, chunkZ)) {
            scanBlockEsp(level, level.method_8497(chunkX, chunkZ), modules, FOCUSED_BLOCK_ESP_MAX_HITS_PER_CHUNK);
        }
    }

    private void scanBlocks(class_638 level, class_1923 chunkPos, ModuleManager modules, ChunkScore score, boolean scoreChunk) {
        class_2338.class_2339 mutable = new class_2338.class_2339();
        int minY = Math.max(level.method_31607(), -64);
        int maxY = Math.min(level.method_31600(), 160);
        int height = Math.max(1, maxY - minY);
        int total = 16 * 16 * height;
        long chunkKey = chunkPos.method_8324();
        int cursor = blockScanCursors.getOrDefault(chunkKey, 0);
        int budget = blockScanBudget(modules);

        for (int checked = 0; checked < budget && checked < total; checked++) {
            int index = (cursor + checked) % total;
            int yOffset = index % height;
            int zOffset = (index / height) & 15;
            int xOffset = (index / height / 16) & 15;
            mutable.method_10103(chunkPos.method_8326() + xOffset, minY + yOffset, chunkPos.method_8328() + zOffset);
            class_2680 state = level.method_8320(mutable);
            class_2248 block = state.method_26204();

            if (scoreChunk && mutable.method_10264() < 0 && isStorageBlock(block)) {
                score.undergroundStorage();
            }

            if (modules.susChunkSettings().kelp() && isKelp(block)) {
                score.kelp();
                continue;
            }

            if (isLightBlock(block)) {
                if (scoreChunk) {
                    score.light();
                }
                if (modules.enabled("light_finder")) {
                    put(mutable.method_10062(), MarkerType.LIGHT, block.method_9518().getString(), 0xFFFFD25A, 10);
                }
            }

            int suspiciousWeight = suspiciousWeight(state, modules.susChunkSettings());
            if (suspiciousWeight > 0 && !isLightBlock(block)) {
                if (scoreChunk) {
                    score.suspicious(suspiciousWeight);
                }
                if (modules.enabled("suspicious_esp") && !isStorageBlock(block) && !isLightBlock(block)) {
                    put(mutable.method_10062(), MarkerType.SUSPICIOUS, suspiciousLabel(state, modules.susChunkSettings()), 0xFFB48CFF, suspiciousWeight + 5);
                }
            }

            if (modules.enabled("hole_esp") && isHole(level, mutable)) {
                put(mutable.method_10062(), MarkerType.HOLE, "Hole", 0xFF8DE96B, 5);
            }
        }

        blockScanCursors.put(chunkKey, (cursor + budget) % total);
    }

    private int blockScanBudget(ModuleManager modules) {
        if (modules.enabled("hole_esp") || modules.enabled("suspicious_esp")) {
            return HEAVY_BLOCK_SCAN_BUDGET;
        }
        return LIGHT_BLOCK_SCAN_BUDGET;
    }

    private void scanBlockEspFast(class_638 level, class_2818 chunk, ModuleManager modules) {
        scanBlockEsp(level, chunk, modules, FAST_BLOCK_ESP_MAX_HITS_PER_CHUNK);
    }

    private void scanBlockEsp(class_638 level, class_2818 chunk, ModuleManager modules, int maxHits) {
        clearMarkersInChunk(MarkerType.BLOCK_ESP, chunk.method_12004());
        if (modules.blockEspSettings().selectedCount() <= 0) {
            return;
        }
        class_2826[] sections = chunk.method_12006();
        int minY = Math.max(level.method_31607(), -64);
        int maxY = Math.min(level.method_31600(), 0);
        if (minY >= maxY) {
            return;
        }

        int minSectionY = level.method_32891();
        int hits = 0;

        for (int sectionIndex = 0; sectionIndex < sections.length && hits < maxHits; sectionIndex++) {
            class_2826 section = sections[sectionIndex];
            int sectionBaseY = (minSectionY + sectionIndex) << 4;
            int sectionTopY = sectionBaseY + 16;
            if (sectionTopY <= minY || sectionBaseY >= maxY || section.method_38292()
                    || !section.method_19523(state -> modules.blockEspSettings().matches(state))) {
                continue;
            }

            int localMinY = Math.max(0, minY - sectionBaseY);
            int localMaxY = Math.min(16, maxY - sectionBaseY);
            for (int y = localMinY; y < localMaxY && hits < maxHits; y++) {
                for (int x = 0; x < 16 && hits < maxHits; x++) {
                    for (int z = 0; z < 16 && hits < maxHits; z++) {
                        class_2680 state = section.method_12254(x, y, z);
                        if (!modules.blockEspSettings().matches(state)) {
                            continue;
                        }

                        class_2338 pos = new class_2338(chunk.method_12004().method_8326() + x, sectionBaseY + y, chunk.method_12004().method_8328() + z);
                        put(pos, MarkerType.BLOCK_ESP, modules.blockEspSettings().label(state), modules.blockEspSettings().color(state), 14);
                        hits++;
                    }
                }
            }
        }
    }

    private boolean isHole(class_638 level, class_2338 pos) {
        if (!level.method_8320(pos).method_26215() || !level.method_8320(pos.method_10084()).method_26215()) {
            return false;
        }

        return solid(level, pos.method_10074())
                && solid(level, pos.method_10095())
                && solid(level, pos.method_10072())
                && solid(level, pos.method_10078())
                && solid(level, pos.method_10067());
    }

    private boolean solid(class_638 level, class_2338 pos) {
        class_2680 state = level.method_8320(pos);
        return !state.method_26215() && state.method_51366();
    }

    private boolean isStorageBlock(class_2248 block) {
        return block == class_2246.field_10034
                || block == class_2246.field_10380
                || block == class_2246.field_61388
                || block == class_2246.field_61389
                || block == class_2246.field_61390
                || block == class_2246.field_61391
                || block == class_2246.field_61392
                || block == class_2246.field_61393
                || block == class_2246.field_61394
                || block == class_2246.field_61395
                || block == class_2246.field_16328
                || block == class_2246.field_10443
                || block == class_2246.field_10603
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
                || block == class_2246.field_10371
                || block == class_2246.field_10312
                || block == class_2246.field_10181
                || block == class_2246.field_16333
                || block == class_2246.field_16334
                || block == class_2246.field_10200
                || block == class_2246.field_10228;
    }

    private int storageColor(class_2248 block) {
        if (block == class_2246.field_10443) {
            return 0xFFB069FF;
        }
        if (block == class_2246.field_10380) {
            return 0xFFFF8D6B;
        }
        if (isShulkerBlock(block)) {
            return 0xFFB887FF;
        }
        if (block == class_2246.field_16328) {
            return 0xFFD6A45F;
        }
        if (block == class_2246.field_10312) {
            return 0xFF90A4AE;
        }
        if (block == class_2246.field_10200 || block == class_2246.field_10228) {
            return 0xFF81C784;
        }
        if (block == class_2246.field_10181 || block == class_2246.field_16333 || block == class_2246.field_16334) {
            return 0xFFFFB74D;
        }
        return 0xFFE6C55C;
    }

    private boolean isShulkerBlock(class_2248 block) {
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

    private boolean isLightBlock(class_2248 block) {
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

    private int suspiciousWeight(class_2680 state, SusChunkSettings settings) {
        class_2248 block = state.method_26204();
        if (isStorageBlock(block)) {
            return 8;
        }
        if (isLightBlock(block)) {
            return 6;
        }
        if (block == class_2246.field_9980
                || block == class_2246.field_10535
                || block == class_2246.field_10105
                || block == class_2246.field_10414
                || block == class_2246.field_10485
                || block == class_2246.field_10333
                || block == class_2246.field_10593
                || block == class_2246.field_23152) {
            return 7;
        }
        if (block == class_2246.field_10540 || block == class_2246.field_10316) {
            return 5;
        }
        if (block == class_2246.field_10091
                || block == class_2246.field_10450
                || block == class_2246.field_10377
                || block == class_2246.field_10560
                || block == class_2246.field_10615) {
            return 5;
        }
        if (block == class_2246.field_10167
                || block == class_2246.field_10425
                || block == class_2246.field_10025
                || block == class_2246.field_10546
                || block == class_2246.field_9983
                || block == class_2246.field_16492) {
            return 3;
        }
        if (settings.caveVines() && (block == class_2246.field_28675 || block == class_2246.field_28676)) {
            return 4;
        }
        if (settings.vines() && block == class_2246.field_10597) {
            return 3;
        }
        if (settings.kelp() && (block == class_2246.field_9993 || block == class_2246.field_10463)) {
            return 4;
        }
        if (settings.amethyst() && (block == class_2246.field_27159 || block == class_2246.field_27160 || block == class_2246.field_27161)) {
            return 5;
        }
        if (settings.bamboo() && (block == class_2246.field_10211 || block == class_2246.field_10108)) {
            return 4;
        }
        if (settings.beeNest() && (block == class_2246.field_20421 || block == class_2246.field_20422)) {
            return 4;
        }
        if (settings.rotatedDeepslate() && isRotatedDeepslate(state)) {
            return 6;
        }
        return 0;
    }

    private int susThreshold(SusChunkSettings settings) {
        return Math.max(2, 24 - settings.sensitivity() * 2);
    }

    private boolean isProbableBaseChunk(ChunkScore score, int threshold) {
        if (score.undergroundStorageCount() >= 2) {
            return score.score() >= Math.max(18, threshold - 4);
        }
        if (score.undergroundStorageCount() == 1) {
            return score.score() >= threshold + 10
                    || score.blockEntityCount() >= 3
                    || score.lightsCount() >= 2
                    || score.suspiciousCount() >= 3;
        }
        return score.storageCount() >= 4
                && score.blockEntityCount() >= 4
                && score.score() >= threshold + 28;
    }

    private String suspiciousLabel(class_2680 state, SusChunkSettings settings) {
        if (settings.rotatedDeepslate() && isRotatedDeepslate(state)) {
            return "Rotated Deepslate";
        }
        return state.method_26204().method_9518().getString();
    }

    private boolean isRotatedDeepslate(class_2680 state) {
        if (state.method_26204() != class_2246.field_28888) {
            return false;
        }

        if (state.method_28498(class_2465.field_11459)) {
            return state.method_11654(class_2465.field_11459).method_10179();
        }

        return false;
    }

    private boolean containsTrialChamber(class_2818 chunk) {
        boolean trialCore = false;
        boolean trialBuilding = false;
        for (class_2826 section : chunk.method_12006()) {
            if (section.method_38292()) {
                continue;
            }
            trialCore |= section.method_19523(state ->
                    state.method_26204() == class_2246.field_47336 || state.method_26204() == class_2246.field_48851);
            trialBuilding |= section.method_19523(state ->
                    state.method_26204() == class_2246.field_47035
                            || state.method_26204() == class_2246.field_47034
                            || state.method_26204() == class_2246.field_47030
                            || state.method_26204() == class_2246.field_47064
                            || state.method_26204() == class_2246.field_47065);
            if (trialCore) {
                return true;
            }
        }
        return trialBuilding && containsTrialBuildingCluster(chunk);
    }

    private boolean containsTrialBuildingCluster(class_2818 chunk) {
        int matchingSections = 0;
        for (class_2826 section : chunk.method_12006()) {
            if (!section.method_38292() && section.method_19523(state ->
                    state.method_26204() == class_2246.field_47035 || state.method_26204() == class_2246.field_47034
                            || state.method_26204() == class_2246.field_47030 || state.method_26204() == class_2246.field_47064
                            || state.method_26204() == class_2246.field_47065)) {
                matchingSections++;
                if (matchingSections >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsSpawnerRoom(class_2818 chunk) {
        for (class_2826 section : chunk.method_12006()) {
            if (section.method_38292()) {
                continue;
            }
            if (section.method_19523(state -> state.method_26204() == class_2246.field_10260)) {
                return true;
            }
        }
        for (class_2586 blockEntity : chunk.method_12214().values()) {
            if (blockEntity.method_11017().toString().toLowerCase(java.util.Locale.ROOT).contains("spawner")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAmethystRoom(class_2818 chunk) {
        for (class_2826 section : chunk.method_12006()) {
            if (!section.method_38292() && section.method_19523(state ->
                    state.method_26204() == class_2246.field_27160)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasUndergroundStorage(class_638 level, class_2818 chunk) {
        for (Map.Entry<class_2338, class_2586> entry : chunk.method_12214().entrySet()) {
            class_2338 pos = entry.getKey();
            if (pos.method_10264() < 0 && isStorageBlock(level.method_8320(pos).method_26204())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsMineshaftSignature(class_2818 chunk) {
        boolean rails = false;
        boolean cobwebs = false;
        boolean supports = false;
        for (class_2826 section : chunk.method_12006()) {
            if (section.method_38292()) {
                continue;
            }
            rails |= section.method_19523(state -> isRail(state.method_26204()));
            cobwebs |= section.method_19523(state -> state.method_26204() == class_2246.field_10343);
            supports |= section.method_19523(state -> state.method_26204() == class_2246.field_10161
                    || state.method_26204() == class_2246.field_10620);
            if (rails && (cobwebs || supports)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsLushCaveSignature(class_2818 chunk) {
        boolean moss = false;
        boolean lushFeature = false;
        for (class_2826 section : chunk.method_12006()) {
            if (section.method_38292()) {
                continue;
            }
            moss |= section.method_19523(state -> state.method_26204() == class_2246.field_28681
                    || state.method_26204() == class_2246.field_28680);
            lushFeature |= section.method_19523(state -> state.method_26204() == class_2246.field_28677
                    || state.method_26204() == class_2246.field_28678
                    || state.method_26204() == class_2246.field_28679
                    || state.method_26204() == class_2246.field_28675
                    || state.method_26204() == class_2246.field_28676);
            if (moss && lushFeature) {
                return true;
            }
        }
        return false;
    }

    private boolean isKelp(class_2248 block) {
        return block == class_2246.field_9993 || block == class_2246.field_10463;
    }

    private boolean isRail(class_2248 block) {
        return block == class_2246.field_10167
                || block == class_2246.field_10425
                || block == class_2246.field_10025
                || block == class_2246.field_10546;
    }

    private void removeOutOfScopeMarkers(class_638 level, class_2338 playerPos, int renderDistance) {
        int playerChunkX = playerPos.method_10263() >> 4;
        int playerChunkZ = playerPos.method_10260() >> 4;
        int radius = Math.min(MAX_SCAN_RADIUS_CHUNKS, Math.max(2, renderDistance + 2));
        markers.entrySet().removeIf(entry -> {
            class_2338 markerPos = entry.getValue().pos();
            int chunkX = markerPos.method_10263() >> 4;
            int chunkZ = markerPos.method_10260() >> 4;
            return Math.abs(chunkX - playerChunkX) > radius
                    || Math.abs(chunkZ - playerChunkZ) > radius
                    || !level.method_8393(chunkX, chunkZ);
        });
    }

    private void clearMarkersInChunk(MarkerType type, class_1923 chunkPos) {
        markers.entrySet().removeIf(entry -> {
            BaseMarker marker = entry.getValue();
            class_2338 pos = marker.pos();
            return marker.type() == type
                    && (pos.method_10263() >> 4) == chunkPos.field_9181
                    && (pos.method_10260() >> 4) == chunkPos.field_9180;
        });
    }

    private void clearChunkFinderMarkers(class_1923 chunkPos) {
        clearMarkersInChunk(MarkerType.SUS_CHUNK, chunkPos);
        clearMarkersInChunk(MarkerType.PRIME_CHUNK, chunkPos);
        clearMarkersInChunk(MarkerType.SEED_PATTERN, chunkPos);
    }

    private void pruneIgnoredChunkFinderMarkers(class_638 level) {
        Set<Long> ignoredChunks = new HashSet<>();
        markers.entrySet().removeIf(entry -> {
            BaseMarker marker = entry.getValue();
            if (marker.type() != MarkerType.SUS_CHUNK
                    && marker.type() != MarkerType.PRIME_CHUNK
                    && marker.type() != MarkerType.SEED_PATTERN) {
                return false;
            }

            class_2338 pos = marker.pos();
            int chunkX = pos.method_10263() >> 4;
            int chunkZ = pos.method_10260() >> 4;
            long key = class_1923.method_8331(chunkX, chunkZ);
            if (ignoredChunks.contains(key)) {
                return true;
            }
            if (!level.method_8393(chunkX, chunkZ)) {
                return false;
            }

            class_2818 chunk = level.method_8497(chunkX, chunkZ);
            boolean ignored = containsSpawnerRoom(chunk) || containsAmethystRoom(chunk)
                    || ((containsTrialChamber(chunk)
                    || containsMineshaftSignature(chunk)
                    || containsLushCaveSignature(chunk))
                    && !hasUndergroundStorage(level, chunk));
            if (ignored) {
                ignoredChunks.add(key);
            }
            return ignored;
        });
    }

    private void put(class_2338 pos, MarkerType type, String label, int color, int score) {
        String key = type.name() + ":" + pos.method_10063();
        markers.put(key, new BaseMarker(pos, type, label, color, score, ticks));
    }

    private void removeExpiredMarkers() {
        markers.entrySet().removeIf(entry -> {
            MarkerType type = entry.getValue().type();
            if (type == MarkerType.BLOCK_ESP || type == MarkerType.STORAGE) {
                return false;
            }
            return ticks - entry.getValue().seenAtTick() > keepTicks(type);
        });
    }

    private int keepTicks(MarkerType type) {
        return switch (type) {
            case BLOCK_ESP -> FAST_ESP_KEEP_TICKS;
            case STORAGE, BLOCK_ENTITY, LIGHT, SUSPICIOUS -> ESP_KEEP_TICKS;
            case HOLE -> 20 * 60;
            case SUS_CHUNK, PRIME_CHUNK, SEED_PATTERN -> KEEP_TICKS;
        };
    }

    private void trimMarkers() {
        if (markers.size() <= MAX_MARKERS) {
            return;
        }

        List<BaseMarker> keep = strongestMarkers(MAX_MARKERS);
        markers.clear();
        for (BaseMarker marker : keep) {
            put(marker.pos(), marker.type(), marker.label(), marker.color(), marker.score());
        }
    }
}

