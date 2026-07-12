package dev.prchl.donututilities.mixin;

import dev.prchl.donututilities.freecam.FreecamController;
import net.minecraft.class_10185;
import net.minecraft.class_241;
import net.minecraft.class_743;
import net.minecraft.class_744;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_743.class)
public abstract class KeyboardInputMixin extends class_744 {
    @Inject(method = "tick", at = @At("TAIL"))
    private void donututilities$freezePlayerMovementDuringFreecam(CallbackInfo info) {
        if (!FreecamController.active()) {
            return;
        }

        field_54155 = class_10185.field_54098;
        field_55868 = class_241.field_1340;
    }
}
