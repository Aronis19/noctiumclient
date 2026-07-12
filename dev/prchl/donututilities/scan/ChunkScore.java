package dev.prchl.donututilities.scan;

import net.minecraft.class_1923;

public final class ChunkScore {
    private final class_1923 pos;
    private int score;
    private int storage;
    private int undergroundStorage;
    private int lights;
    private int suspicious;
    private int blockEntities;
    private int kelpSamples;

    public ChunkScore(class_1923 pos) {
        this.pos = pos;
    }

    public void storage() {
        storage++;
        score += 6;
    }

    public void undergroundStorage() {
        storage++;
        undergroundStorage++;
        // A single storage block below Y=0 is already a strong stash signal.
        score += 18;
    }

    public void light() {
        lights++;
        score += 4;
    }

    public void suspicious() {
        suspicious++;
        score += 2;
    }

    public void suspicious(int weight) {
        suspicious++;
        score += Math.max(1, weight);
    }

    public void blockEntity() {
        blockEntities++;
        score += 3;
    }

    public void kelp() {
        if (kelpSamples < 2) {
            kelpSamples++;
            score += 1;
        }
    }

    public class_1923 pos() {
        return pos;
    }

    public int score() {
        return score;
    }

    public int storageCount() {
        return storage;
    }

    public int undergroundStorageCount() {
        return undergroundStorage;
    }

    public int lightsCount() {
        return lights;
    }

    public int suspiciousCount() {
        return suspicious;
    }

    public int blockEntityCount() {
        return blockEntities;
    }

    public String label() {
        return "score " + score + " | st " + storage + " | ust " + undergroundStorage + " | li " + lights + " | sus " + suspicious + " | be " + blockEntities;
    }
}
