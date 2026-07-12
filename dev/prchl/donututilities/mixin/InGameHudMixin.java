package dev.prchl.donututilities.mixin;

import dev.prchl.donututilities.render.DonutHudRenderer;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_329.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void donututilities$renderHud(class_332 graphics, class_9779 tickCounter, CallbackInfo info) {
        DonutHudRenderer.render(graphics, tickCounter);
    }
}
