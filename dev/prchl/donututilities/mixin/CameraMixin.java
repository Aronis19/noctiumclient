package dev.prchl.donututilities.mixin;

import dev.prchl.donututilities.DonutUtilitiesClient;
import dev.prchl.donututilities.freecam.FreecamController;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4184.class)
public abstract class CameraMixin {
    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void setPosition(class_243 position);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "setup", at = @At("TAIL"))
    private void donututilities$applyFreecam(class_1937 level, class_1297 entity, boolean detached, boolean thirdPersonReverse,
            float partialTick, CallbackInfo info) {
        if (!FreecamController.active()) {
            return;
        }

        FreecamController.updateForRender(class_310.method_1551(), entity.method_5705(partialTick), entity.method_5695(partialTick),
                DonutUtilitiesClient.MODULES.freecamModule().speed());
        setPosition(FreecamController.position());
        setRotation(FreecamController.yRot(), FreecamController.xRot());
        detached = true;
    }
}
