// Název souboru: ChunkOverlayRenderer.java
package dev.prchl.donututilities.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.class_310; // Použití intermediary mapování pro MinecraftClient
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkPos;
import org.joml.Matrix4f;

/**
 * Třída zajišťující bezpečné klientské vykreslování grafického overlaye.
 */
public class ChunkOverlayRenderer {

    private static ChunkOverlayRenderer instance;
    private final class_310 client;
    private boolean enabled = false;

    public ChunkOverlayRenderer(class_310 client) {
        this.client = client;
        // Registrace do události na konci vykreslovacího cyklu světa
        WorldRenderEvents.END.register(this::onRender);
    }

    /**
     * Statická metoda volaná z hlavní klientské třídy pro inicializaci.
     */
    public static void register() {
        if (instance == null) {
            instance = new ChunkOverlayRenderer(class_310.method_1551());
        }
    }

    /**
     * Statický přístup k instanci pro přepínání stavu z modulů.
     */
    public static ChunkOverlayRenderer getInstance() {
        return instance;
    }

    /**
     * Volá se automaticky hrou při každém renderovacím ticku světa.
     */
    private void onRender(WorldRenderContext context) {
        // Kontrola, zda je overlay aktivní a zda je hráč ve světě
        if (!enabled || client.field_1724 == null || client.field_1687 == null) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) {
            return;
        }

        // Získání aktuální pozice herní kamery
        double cameraX = context.camera().getPos().x;
        double cameraY = context.camera().getPos().y;
        double cameraZ = context.camera().getPos().z;

        // Použijeme standardní vrstvu pro vykreslování debug čar
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());

        matrices.push();
        // Posuneme matici světa tak, aby počátek odpovídal pozici kamery
        matrices.translate(-cameraX, -cameraY, -cameraZ);

        // Získání souřadnic chunku, ve kterém se nachází hráč
        ChunkPos currentChunk = client.field_1724.getChunkPos();

        // Spuštění vykreslení geometrie overlaye
        renderGrid(matrices, buffer, currentChunk);

        matrices.pop();
    }

    /**
     * Vykreslí vizuální hranice (čtverec) kolem specifikovaného chunku.
     */
    private void renderGrid(MatrixStack matrices, VertexConsumer buffer, ChunkPos pos) {
        int startX = pos.getStartX();
        int startZ = pos.getStartZ();
        int endX = pos.getEndX() + 1;
        int endZ = pos.getEndZ() + 1;

        // Vykreslování fixujeme na výškovou úroveň hráče (použití intermediary pole field_1724)
        int heightY = (int) client.field_1724.getY();

        Matrix4f modelMatrix = matrices.peek().getPositionMatrix();

        int r = 255;
        int g = 0;
        int b = 0;
        int a = 255;

        // Vykreslení 4 hran obrysu chunku
        buffer.vertex(modelMatrix, startX, heightY, startZ).color(r, g, b, a).normal(0, 1, 0).next();
        buffer.vertex(modelMatrix, endX, heightY, startZ).color(r, g, b, a).normal(0, 1, 0).next();

        buffer.vertex(modelMatrix, endX, heightY, startZ).color(r, g, b, a).normal(0, 1, 0).next();
        buffer.vertex(modelMatrix, endX, heightY, endZ).color(r, g, b, a).normal(0, 1, 0).next();

        buffer.vertex(modelMatrix, endX, heightY, endZ).color(r, g, b, a).normal(0, 1, 0).next();
        buffer.vertex(modelMatrix, startX, heightY, endZ).color(r, g, b, a).normal(0, 1, 0).next();

        buffer.vertex(modelMatrix, startX, heightY, endZ).color(r, g, b, a).normal(0, 1, 0).next();
        buffer.vertex(modelMatrix, startX, heightY, startZ).color(r, g, b, a).normal(0, 1, 0).next();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}
