package dev.prchl.donututilities.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.prchl.donututilities.DonutUtilitiesClient;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_290;
import net.minecraft.class_2960;

public final class EspRenderTypes {
    private static final RenderPipeline XRAY_FILL_PIPELINE = RenderPipeline.builder(class_10799.field_56860)
            .withLocation(class_2960.method_60655(DonutUtilitiesClient.MOD_ID, "xray_esp_fill"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_27382)
            .build();

    private static final RenderPipeline XRAY_LINE_PIPELINE = RenderPipeline.builder(class_10799.field_56859)
            .withLocation(class_2960.method_60655(DonutUtilitiesClient.MOD_ID, "xray_esp_lines"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withVertexFormat(class_290.field_63455, VertexFormat.class_5596.field_27377)
            .build();

    private static final class_1921 XRAY_FILL = class_1921.method_75940("donututilities_xray_fill",
            class_12247.method_75927(XRAY_FILL_PIPELINE)
                    .method_75929(class_1921.field_64009)
                    .method_75938());

    private static final class_1921 XRAY_LINES = class_1921.method_75940("donututilities_xray_lines",
            class_12247.method_75927(XRAY_LINE_PIPELINE)
                    .method_75929(class_1921.field_64009)
                    .method_75938());

    private EspRenderTypes() {
    }

    public static class_1921 xrayFill() {
        return XRAY_FILL;
    }

    public static class_1921 xrayLines() {
        return XRAY_LINES;
    }
}
