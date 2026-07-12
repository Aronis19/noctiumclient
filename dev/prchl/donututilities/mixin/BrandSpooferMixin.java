package dev.prchl.donututilities.mixin;

import dev.prchl.donututilities.DonutUtilitiesClient;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBrandRetriever.class)
public abstract class BrandSpooferMixin {
    @Inject(method = "getClientModName", at = @At("HEAD"), cancellable = true)
    private static void donututilities$spoofClientBrand(CallbackInfoReturnable<String> cir) {
        if (DonutUtilitiesClient.MODULES.enabled("brand_spoofer")) {
            cir.setReturnValue(DonutUtilitiesClient.MODULES.brandSpooferSettings().brand());
        }
    }
}
