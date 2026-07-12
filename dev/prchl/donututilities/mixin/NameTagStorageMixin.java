package dev.prchl.donututilities.mixin;

import dev.prchl.donututilities.module.ItemInfoModule;
import net.minecraft.class_11689;
import net.minecraft.class_12075;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_11689.class_12050.class)
public abstract class NameTagStorageMixin {
    private static final ThreadLocal<Boolean> DONUTUTILITIES_ITEM_INFO = ThreadLocal.withInitial(() -> false);

    @Inject(method = "add", at = @At("HEAD"))
    private void donututilities$captureItemInfoTag(class_4587 matrices, class_243 attachment, int yOffset, class_2561 text,
                                                   boolean visibleThroughWalls, int light, double distanceToCameraSq,
                                                   class_12075 cameraState, CallbackInfo info) {
        DONUTUTILITIES_ITEM_INFO.set(text != null && text.getString().contains(ItemInfoModule.NAMETAG_MARKER));
    }

    @ModifyVariable(method = "add", at = @At("HEAD"), argsOnly = true)
    private class_2561 donututilities$stripItemInfoMarker(class_2561 text) {
        if (text == null || !text.getString().contains(ItemInfoModule.NAMETAG_MARKER)) {
            return text;
        }
        return class_2561.method_43470(text.getString().replace(ItemInfoModule.NAMETAG_MARKER, ""))
                .method_27696(text.method_10866());
    }

    @ModifyArg(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeStorage$NameTagSubmit;<init>(Lorg/joml/Matrix4f;FFLnet/minecraft/network/chat/Component;IIID)V"), index = 6)
    private int donututilities$itemInfoBackground(int backgroundColor) {
        return DONUTUTILITIES_ITEM_INFO.get() ? 0x8D151820 : backgroundColor;
    }
}
