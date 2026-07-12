package dev.prchl.donututilities.mixin;

import dev.prchl.donututilities.DonutUtilitiesClient;
import dev.prchl.donututilities.module.ItemInfoModule;
import net.minecraft.class_10039;
import net.minecraft.class_1542;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_5251;
import net.minecraft.class_916;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_916.class)
public abstract class ItemEntityRenderStateMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V", at = @At("RETURN"))
    private void donututilities$itemInfoNameTag(class_1542 item, class_10039 state, float partialTick, CallbackInfo info) {
        if (!DonutUtilitiesClient.MODULES.enabled("item_info") || item.method_6983().method_7960()) {
            return;
        }

        int color = DonutUtilitiesClient.MODULES.itemInfoModule().textColor() & 0x00FFFFFF;
        state.field_53337 = class_2561.method_43470(ItemInfoModule.NAMETAG_MARKER + "  " + ItemInfoModule.label(item.method_6983()) + "  ")
                .method_27694(style -> style.method_27703(class_5251.method_27717(color)));
        state.field_53338 = new class_243(0.0D, item.method_17682() + 1.15D, 0.0D);
    }
}
