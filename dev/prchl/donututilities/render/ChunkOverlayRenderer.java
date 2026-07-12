package dev.prchl.donututilities.render;

import dev.prchl.donututilities.DonutUtilitiesClient;
import dev.prchl.donututilities.freecam.FreecamController;
import dev.prchl.donututilities.module.EspSettings;
import dev.prchl.donututilities.scan.BaseMarker;
import dev.prchl.donututilities.scan.MarkerType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2902;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import org.joml.Vector3fc;

public final class ChunkOverlayRenderer {
    private static final int MAX_CHUNK_OVERLAYS = 2048;
    private static final int MAX_MARKER_BOXES = 20000;
    private static final int MAX_ENTITY_BOXES = 64;
    private static final int SURFACE_CELL_SIZE = 4;
    private static final float SURFACE_OVERLAY_THICKNESS = 0.24F;
    private static final float FLAT_OVERLAY_OFFSET = 0.65F;
    private static long lastRenderWarningMs;

    private ChunkOverlayRenderer() {
    }

    public static void register() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(ChunkOverlayRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        try {
            renderSafe(context);
        } catch (Throwable exception) {
            long now = System.currentTimeMillis();
            if (now - lastRenderWarningMs > 5000L) {
                DonutUtilitiesClient.LOGGER.warn("Skipped overlay render to prevent a client crash", exception);
                lastRenderWarningMs = now;
            }
        }
    }

    private static void renderSafe(WorldRenderContext context) {
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null || client.field_1724 == null || !hasVisibleOverlayModule()) {
            return;
        }

        class_4184 camera = client.field_1773.method_19418();
        class_243 cameraPos = camera.method_71156();
        class_243 traceStart = traceStart(camera);
        class_4587 matrices = context.matrices();
        class_4588 fillVertices = context.consumers().method_73477(EspRenderTypes.xrayFill());
        class_4588 lineVertices = context.consumers().method_73477(EspRenderTypes.xrayLines());
        int alpha = alphaFromSettings();
        Set<Long> renderedChunks = new HashSet<>();
        int chunkOverlays = 0;
        int markerBoxes = 0;
        List<BaseMarker> markers = sortedMarkersForRender(client);
        float sharedSusChunkY = sharedSusChunkY(client, markers);

        matrices.method_22903();
        try {
            matrices.method_22904(-cameraPos.field_1352, -cameraPos.field_1351, -cameraPos.field_1350);

            for (BaseMarker marker : markers) {
                if (marker.type() == MarkerType.SUS_CHUNK) {
                    if (!shouldRenderSusChunks()) {
                        continue;
                    }

                    if (chunkOverlays >= susChunkOverlayLimit(client)
                            || overlayOrigin(client).method_10262(marker.pos()) > susChunkRenderDistanceSqr(client)) {
                        continue;
                    }

                    if (drawSusChunk(fillVertices, lineVertices, matrices.method_23760(), client, marker, renderedChunks, alpha, sharedSusChunkY)) {
                        chunkOverlays++;
                    }
                    continue;
                }

                if (markerBoxes >= MAX_MARKER_BOXES || !shouldRenderMarker(marker)
                        || overlayOrigin(client).method_10262(marker.pos()) > markerRenderDistanceSqr(client)) {
                    continue;
                }

                drawMarkerBox(fillVertices, lineVertices, matrices.method_23760(), marker, traceStart);
                markerBoxes++;
            }

            drawEntityEsp(lineVertices, matrices.method_23760(), client, traceStart);
            drawFreecamBodyMarker(fillVertices, lineVertices, matrices.method_23760(), client);
        } finally {
            matrices.method_22909();
        }
    }

    private static List<BaseMarker> sortedMarkersForRender(class_310 client) {
        class_2338 playerPos = client.field_1724.method_24515();
        List<BaseMarker> markers = new ArrayList<>(DonutUtilitiesClient.SCANNER.markers());
        markers.sort(Comparator.comparingInt(ChunkOverlayRenderer::renderPriority)
                .thenComparingDouble(marker -> playerPos.method_10262(marker.pos()))
                .thenComparingInt(marker -> marker.type().ordinal())
                .thenComparingLong(marker -> marker.pos().method_10063()));
        return markers;
    }

    private static int renderPriority(BaseMarker marker) {
        return switch (marker.type()) {
            case STORAGE, BLOCK_ESP -> 0;
            case SUSPICIOUS, LIGHT, BLOCK_ENTITY -> 1;
            case SUS_CHUNK, PRIME_CHUNK, SEED_PATTERN -> 2;
            case HOLE -> 3;
        };
    }

    private static class_243 traceStart(class_4184 camera) {
        class_243 cameraPos = camera.method_71156();
        Vector3fc forward = camera.method_19335();
        return new class_243(cameraPos.field_1352 + forward.x() * 0.45, cameraPos.field_1351 + forward.y() * 0.45, cameraPos.field_1350 + forward.z() * 0.45);
    }

    private static class_2338 overlayOrigin(class_310 client) {
        return FreecamController.active()
                ? class_2338.method_49638(FreecamController.position())
                : client.field_1724.method_24515();
    }

    private static double markerRenderDistanceSqr(class_310 client) {
        int renderBlocks = Math.max(192, client.field_1690.method_42503().method_41753() * 16 + 64);
        renderBlocks = Math.max(renderBlocks, DonutUtilitiesClient.MODULES.espSettings().traceDistance());
        return (double) renderBlocks * renderBlocks;
    }

    private static double susChunkRenderDistanceSqr(class_310 client) {
        int renderBlocks = Math.max(192, client.field_1690.method_42503().method_41753() * 16 + 64);
        return (double) renderBlocks * renderBlocks;
    }

    private static int susChunkOverlayLimit(class_310 client) {
        int renderDistance = Math.max(2, client.field_1690.method_42503().method_41753());
        int fullRenderSquare = (renderDistance * 2 + 1) * (renderDistance * 2 + 1);
        return Math.max(64, Math.min(MAX_CHUNK_OVERLAYS, fullRenderSquare));
    }

    private static boolean drawSusChunk(class_4588 fillVertices, class_4588 lineVertices, class_4587.class_4665 pose,
            class_310 client, BaseMarker marker, Set<Long> renderedChunks, int alpha, float sharedY) {
        class_2338 pos = marker.pos();
        int chunkX = pos.method_10263() >> 4;
        int chunkZ = pos.method_10260() >> 4;
        long chunkKey = (((long) chunkX) << 32) ^ (chunkZ & 0xFFFFFFFFL);
        if (!renderedChunks.add(chunkKey)) {
            return false;
        }

        int minBlockX = chunkX << 4;
        int minBlockZ = chunkZ << 4;
        if (!client.field_1687.method_8393(chunkX, chunkZ)) {
            return false;
        }

        float y = (Float.isNaN(sharedY) ? highestChunkSurface(client, minBlockX, minBlockZ) : sharedY) + FLAT_OVERLAY_OFFSET;
        drawBox(fillVertices, pose, minBlockX, y, minBlockZ,
                minBlockX + 16.0F, y + SURFACE_OVERLAY_THICKNESS, minBlockZ + 16.0F,
                255, 45, 75, Math.max(105, alpha));
        drawOutlinedBox(lineVertices, pose, minBlockX, y, minBlockZ,
                minBlockX + 16.0F, y + SURFACE_OVERLAY_THICKNESS, minBlockZ + 16.0F,
                255, 45, 75, 235);
        return true;
    }

    private static float sharedSusChunkY(class_310 client, List<BaseMarker> markers) {
        if (!shouldRenderSusChunks()) {
            return Float.NaN;
        }

        float lowest = Float.POSITIVE_INFINITY;
        int checked = 0;
        for (BaseMarker marker : markers) {
            if (checked >= susChunkOverlayLimit(client) || marker.type() != MarkerType.SUS_CHUNK
                    || client.field_1724.method_24515().method_10262(marker.pos()) > susChunkRenderDistanceSqr(client)) {
                continue;
            }

            int chunkX = marker.pos().method_10263() >> 4;
            int chunkZ = marker.pos().method_10260() >> 4;
            if (!client.field_1687.method_8393(chunkX, chunkZ)) {
                continue;
            }

            lowest = Math.min(lowest, highestChunkSurface(client, chunkX << 4, chunkZ << 4));
            checked++;
        }
        return lowest == Float.POSITIVE_INFINITY ? Float.NaN : lowest;
    }

    private static float highestChunkSurface(class_310 client, int minBlockX, int minBlockZ) {
        int highest = client.field_1687.method_31607();
        for (int dx = 0; dx < 16; dx += SURFACE_CELL_SIZE) {
            for (int dz = 0; dz < 16; dz += SURFACE_CELL_SIZE) {
                int x = minBlockX + dx + SURFACE_CELL_SIZE / 2;
                int z = minBlockZ + dz + SURFACE_CELL_SIZE / 2;
                highest = Math.max(highest, client.field_1687.method_8624(class_2902.class_2903.field_13202, x, z));
            }
        }
        return highest;
    }

    private static void drawMarkerBox(class_4588 fillVertices, class_4588 lineVertices, class_4587.class_4665 pose, BaseMarker marker,
            class_243 traceStart) {
        class_2338 pos = marker.pos();
        int color = marker.color();
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        int alpha = markerAlpha(marker.type());
        float inflate = marker.type() == MarkerType.STORAGE ? 0.16F
                : marker.type() == MarkerType.SUSPICIOUS || marker.type() == MarkerType.BLOCK_ESP ? 0.12F : 0.05F;
        float inset = marker.type() == MarkerType.HOLE ? 0.02F : -inflate;
        float minX = pos.method_10263() + inset;
        float minY = pos.method_10264() + inset;
        float minZ = pos.method_10260() + inset;
        float maxX = pos.method_10263() + 1.0F - inset;
        float maxY = pos.method_10264() + 1.0F - inset;
        float maxZ = pos.method_10260() + 1.0F - inset;
        int fillAlpha = markerFillAlpha(marker.type(), alpha);
        drawBox(fillVertices, pose, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, fillAlpha);
        if (hasTopHighlight(marker.type())) {
            drawTopHighlight(fillVertices, lineVertices, pose, minX, maxY, minZ, maxX, maxZ, red, green, blue, fillAlpha);
        }
        drawOutlinedBox(lineVertices, pose, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, 245);
        drawTrace(lineVertices, pose, traceStart, center(minX, minY, minZ, maxX, maxY, maxZ), false,
                traceModuleId(marker.type()));
    }

    private static boolean hasTopHighlight(MarkerType type) {
        return type == MarkerType.BLOCK_ESP || type == MarkerType.STORAGE || type == MarkerType.SUSPICIOUS;
    }

    private static void drawTopHighlight(class_4588 fillVertices, class_4588 lineVertices, class_4587.class_4665 pose,
            float minX, float y, float minZ, float maxX, float maxZ, int red, int green, int blue, int fillAlpha) {
        float capY = y + 0.018F;
        float inset = 0.035F;
        float capMinX = minX + inset;
        float capMinZ = minZ + inset;
        float capMaxX = maxX - inset;
        float capMaxZ = maxZ - inset;
        int capAlpha = Math.min(190, Math.max(120, fillAlpha + 65));

        drawFace(fillVertices, pose, capMinX, capY, capMinZ, capMinX, capY, capMaxZ, capMaxX, capY, capMaxZ, capMaxX, capY, capMinZ,
                red, green, blue, capAlpha);
        drawFace(fillVertices, pose, capMinX, capY, capMinZ, capMaxX, capY, capMinZ, capMaxX, capY, capMaxZ, capMinX, capY, capMaxZ,
                red, green, blue, capAlpha);
        drawOutlinedTop(lineVertices, pose, capMinX, capY + 0.002F, capMinZ, capMaxX, capMaxZ, red, green, blue, 255);
    }

    private static void drawEntityEsp(class_4588 lineVertices, class_4587.class_4665 pose, class_310 client,
            class_243 traceStart) {
        boolean players = DonutUtilitiesClient.MODULES.enabled("player_esp");
        boolean mobs = DonutUtilitiesClient.MODULES.enabled("mob_esp");
        if (!players && !mobs) {
            return;
        }

        int count = 0;
        for (class_1297 entity : client.field_1687.method_18112()) {
            if (count >= MAX_ENTITY_BOXES || entity == client.field_1724 || entity.method_5858(client.field_1724) > 16384.0) {
                continue;
            }

            boolean isPlayer = entity instanceof class_1657;
            if ((!players || !isPlayer) && (!mobs || isPlayer || !(entity instanceof class_1309))) {
                continue;
            }

            class_238 box = entity.method_5829().method_1014(0.08);
            drawTrace(lineVertices, pose, traceStart, box.method_1005(), true, isPlayer ? "player_esp" : "mob_esp");
            count++;
        }
    }

    private static void drawFreecamBodyMarker(class_4588 fillVertices, class_4588 lineVertices, class_4587.class_4665 pose, class_310 client) {
        if (!FreecamController.active() || client.field_1724 == null) {
            return;
        }

        class_238 box = client.field_1724.method_5829().method_1014(0.12D);
        drawBox(fillVertices, pose,
                (float) box.field_1323, (float) box.field_1322, (float) box.field_1321,
                (float) box.field_1320, (float) box.field_1325, (float) box.field_1324,
                130, 207, 255, 48);
        drawOutlinedBox(lineVertices, pose,
                (float) box.field_1323, (float) box.field_1322, (float) box.field_1321,
                (float) box.field_1320, (float) box.field_1325, (float) box.field_1324,
                130, 207, 255, 255);

        float y = (float) box.field_1325 + 0.08F;
        drawOutlinedTop(lineVertices, pose, (float) box.field_1323, y, (float) box.field_1321, (float) box.field_1320, (float) box.field_1324,
                255, 240, 90, 255);
    }

    private static void drawTrace(class_4588 vertices, class_4587.class_4665 pose, class_243 start, class_243 target,
            boolean entityTrace, String moduleId) {
        EspSettings settings = DonutUtilitiesClient.MODULES.espSettings();
        if (!settings.traces() || (entityTrace && !settings.entityTraces()) || (!entityTrace && !settings.blockTraces())) {
            return;
        }

        if (start.method_1025(target) > (double) settings.traceDistance() * settings.traceDistance()) {
            return;
        }

        int alpha = settings.traceAlpha() * 255 / 100;
        drawLine(vertices, pose, start, target, settings.traceRed(moduleId), settings.traceGreen(moduleId),
                settings.traceBlue(moduleId), Math.min(255, Math.max(150, alpha)));
    }

    private static String traceModuleId(MarkerType type) {
        return switch (type) {
            case BLOCK_ENTITY -> "block_entity_debug";
            case STORAGE -> "storage_esp";
            case LIGHT -> "light_finder";
            case HOLE -> "hole_esp";
            case BLOCK_ESP -> "block_esp";
            case SUSPICIOUS -> "suspicious_esp";
            case PRIME_CHUNK -> "prime_chunk_finder";
            case SEED_PATTERN -> "seed_chunk_finder";
            case SUS_CHUNK -> "sus_chunk_finder";
        };
    }

    private static void drawTraceBeam(class_4588 vertices, class_4587.class_4665 pose, class_243 start, class_243 target,
            int red, int green, int blue, int alpha) {
        double distance = start.method_1022(target);
        if (distance <= 0.5) {
            return;
        }

        class_243 beamStart = start.method_35590(target, 0.025);
        class_243 direction = target.method_1020(beamStart).method_1029();
        class_243 up = Math.abs(direction.field_1351) > 0.92D ? new class_243(1.0D, 0.0D, 0.0D) : new class_243(0.0D, 1.0D, 0.0D);
        class_243 side = direction.method_1036(up).method_1029().method_1021(0.022D);
        class_243 normal = direction.method_1036(side).method_1029().method_1021(0.022D);

        class_243 s1 = beamStart.method_1019(side).method_1019(normal);
        class_243 s2 = beamStart.method_1019(side).method_1020(normal);
        class_243 s3 = beamStart.method_1020(side).method_1020(normal);
        class_243 s4 = beamStart.method_1020(side).method_1019(normal);
        class_243 e1 = target.method_1019(side).method_1019(normal);
        class_243 e2 = target.method_1019(side).method_1020(normal);
        class_243 e3 = target.method_1020(side).method_1020(normal);
        class_243 e4 = target.method_1020(side).method_1019(normal);

        drawFace(vertices, pose, s1, e1, e2, s2, red, green, blue, alpha);
        drawFace(vertices, pose, s2, e2, e3, s3, red, green, blue, alpha);
        drawFace(vertices, pose, s3, e3, e4, s4, red, green, blue, alpha);
        drawFace(vertices, pose, s4, e4, e1, s1, red, green, blue, alpha);
        drawFace(vertices, pose, s1, s2, s3, s4, red, green, blue, alpha);
        drawFace(vertices, pose, e1, e4, e3, e2, red, green, blue, alpha);
    }

    private static class_243 center(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return new class_243((minX + maxX) * 0.5F, (minY + maxY) * 0.5F, (minZ + maxZ) * 0.5F);
    }

    private static boolean shouldRenderMarker(BaseMarker marker) {
        return switch (marker.type()) {
            case BLOCK_ENTITY -> false;
            case STORAGE -> DonutUtilitiesClient.MODULES.enabled("storage_esp");
            case LIGHT, HOLE -> false;
            case BLOCK_ESP -> DonutUtilitiesClient.MODULES.enabled("block_esp");
            case SUSPICIOUS -> false;
            case PRIME_CHUNK -> DonutUtilitiesClient.MODULES.enabled("prime_chunk_finder");
            case SEED_PATTERN -> DonutUtilitiesClient.MODULES.enabled("seed_chunk_finder");
            case SUS_CHUNK -> false;
        };
    }

    private static boolean shouldRenderSusChunks() {
        return DonutUtilitiesClient.MODULES.enabled("sus_chunk_finder") || DonutUtilitiesClient.MODULES.enabled("rtp_base_finder");
    }

    private static boolean hasVisibleOverlayModule() {
        return shouldRenderSusChunks()
                || DonutUtilitiesClient.MODULES.enabled("block_entity_debug")
                || DonutUtilitiesClient.MODULES.enabled("storage_esp")
                || DonutUtilitiesClient.MODULES.enabled("light_finder")
                || DonutUtilitiesClient.MODULES.enabled("hole_esp")
                || DonutUtilitiesClient.MODULES.enabled("suspicious_esp")
                || DonutUtilitiesClient.MODULES.enabled("block_esp")
                || DonutUtilitiesClient.MODULES.enabled("prime_chunk_finder")
                || DonutUtilitiesClient.MODULES.enabled("seed_chunk_finder")
                || DonutUtilitiesClient.MODULES.enabled("player_esp")
                || DonutUtilitiesClient.MODULES.enabled("mob_esp")
                || FreecamController.active();
    }

    private static int markerAlpha(MarkerType type) {
        return switch (type) {
            case HOLE -> 80;
            case STORAGE, BLOCK_ENTITY -> 105;
            case LIGHT -> 95;
            case SUSPICIOUS, BLOCK_ESP -> 85;
            case PRIME_CHUNK, SEED_PATTERN -> 100;
            case SUS_CHUNK -> 0;
        };
    }

    private static int markerFillAlpha(MarkerType type, int alpha) {
        return switch (type) {
            case STORAGE -> 72;
            case SUSPICIOUS, BLOCK_ESP -> 58;
            case BLOCK_ENTITY -> 48;
            case LIGHT -> 45;
            case HOLE -> 42;
            case PRIME_CHUNK, SEED_PATTERN -> 70;
            case SUS_CHUNK -> 0;
        };
    }

    private static int alphaFromSettings() {
        int percent = DonutUtilitiesClient.MODULES.susChunkSettings().alpha();
        int clamped = Math.max(15, Math.min(100, percent));
        return Math.max(85, Math.min(155, clamped * 2));
    }

    private static void drawBox(class_4588 vertices, class_4587.class_4665 pose, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ, int red, int green, int blue, int alpha) {
        drawFace(vertices, pose, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
        drawFace(vertices, pose, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, red, green, blue, alpha);
        drawFace(vertices, pose, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);
        drawFace(vertices, pose, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, red, green, blue, alpha);
        drawFace(vertices, pose, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, red, green, blue, alpha);
        drawFace(vertices, pose, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
    }

    private static void drawFace(class_4588 vertices, class_4587.class_4665 pose,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4, int red, int green, int blue, int alpha) {
        vertex(vertices, pose, x1, y1, z1, red, green, blue, alpha);
        vertex(vertices, pose, x2, y2, z2, red, green, blue, alpha);
        vertex(vertices, pose, x3, y3, z3, red, green, blue, alpha);
        vertex(vertices, pose, x4, y4, z4, red, green, blue, alpha);
    }

    private static void drawFace(class_4588 vertices, class_4587.class_4665 pose,
            class_243 first, class_243 second, class_243 third, class_243 fourth, int red, int green, int blue, int alpha) {
        drawFace(vertices, pose,
                (float) first.field_1352, (float) first.field_1351, (float) first.field_1350,
                (float) second.field_1352, (float) second.field_1351, (float) second.field_1350,
                (float) third.field_1352, (float) third.field_1351, (float) third.field_1350,
                (float) fourth.field_1352, (float) fourth.field_1351, (float) fourth.field_1350,
                red, green, blue, alpha);
    }

    private static void vertex(class_4588 vertices, class_4587.class_4665 pose, float x, float y, float z,
            int red, int green, int blue, int alpha) {
        vertices.method_56824(pose, x, y, z).method_1336(red, green, blue, alpha);
    }

    private static void drawLine(class_4588 vertices, class_4587.class_4665 pose, class_243 start, class_243 end,
            int red, int green, int blue, int alpha) {
        float x1 = (float) start.field_1352;
        float y1 = (float) start.field_1351;
        float z1 = (float) start.field_1350;
        float x2 = (float) end.field_1352;
        float y2 = (float) end.field_1351;
        float z2 = (float) end.field_1350;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 0.0001F) {
            return;
        }

        float nx = dx / length;
        float ny = dy / length;
        float nz = dz / length;
        vertices.method_56824(pose, x1, y1, z1).method_1336(red, green, blue, alpha).method_60831(pose, nx, ny, nz).method_75298(2.5F);
        vertices.method_56824(pose, x2, y2, z2).method_1336(red, green, blue, alpha).method_60831(pose, nx, ny, nz).method_75298(2.5F);
    }

    private static void drawOutlinedBox(class_4588 vertices, class_4587.class_4665 pose, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ, int red, int green, int blue, int alpha) {
        line(vertices, pose, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
        line(vertices, pose, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
        line(vertices, pose, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
        line(vertices, pose, minX, minY, maxZ, minX, minY, minZ, red, green, blue, alpha);
        line(vertices, pose, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        line(vertices, pose, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        line(vertices, pose, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        line(vertices, pose, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);
        line(vertices, pose, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        line(vertices, pose, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        line(vertices, pose, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
        line(vertices, pose, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
    }

    private static void drawOutlinedTop(class_4588 vertices, class_4587.class_4665 pose, float minX, float y, float minZ,
            float maxX, float maxZ, int red, int green, int blue, int alpha) {
        line(vertices, pose, minX, y, minZ, maxX, y, minZ, red, green, blue, alpha);
        line(vertices, pose, maxX, y, minZ, maxX, y, maxZ, red, green, blue, alpha);
        line(vertices, pose, maxX, y, maxZ, minX, y, maxZ, red, green, blue, alpha);
        line(vertices, pose, minX, y, maxZ, minX, y, minZ, red, green, blue, alpha);
        line(vertices, pose, minX, y, minZ, maxX, y, maxZ, red, green, blue, Math.max(120, alpha - 40));
        line(vertices, pose, maxX, y, minZ, minX, y, maxZ, red, green, blue, Math.max(120, alpha - 40));
    }

    private static void line(class_4588 vertices, class_4587.class_4665 pose, float x1, float y1, float z1,
            float x2, float y2, float z2, int red, int green, int blue, int alpha) {
        drawLine(vertices, pose, new class_243(x1, y1, z1), new class_243(x2, y2, z2), red, green, blue, alpha);
    }
}
