package dev.prchl.donututilities.scan;

import net.minecraft.class_2338;

public record BaseMarker(class_2338 pos, MarkerType type, String label, int color, int score, long seenAtTick) {
}
