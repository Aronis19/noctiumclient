package dev.prchl.donututilities.mixin;

import dev.prchl.donututilities.freecam.FreecamController;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_757.class)
public abstract class GameRendererMixin {
    @Inject(method = "pick", at = @At("TAIL"))
    private void donututilities$usePlayerPickDuringFreecam(float partialTick, CallbackInfo info) {
        if (!FreecamController.active()) {
            return;
        }

        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || client.field_1687 == null) {
            return;
        }

        class_243 from = FreecamController.position();
        class_243 direction = class_243.method_1030(FreecamController.xRot(), FreecamController.yRot());
        class_243 to = from.method_1019(direction.method_1021(client.field_1724.method_55754()));
        class_239 freecamHit = client.field_1687.method_17742(new class_3959(from, to, class_3959.class_3960.field_17559, class_3959.class_242.field_1348, client.field_1724));
        if (freecamHit instanceof class_3965 blockHit
                && freecamHit.method_17783() == class_239.class_240.field_1332
                && client.field_1724.method_56093(blockHit.method_17777(), 0.0D)) {
            client.field_1765 = freecamHit;
        }
        client.field_1692 = null;
    }
}
