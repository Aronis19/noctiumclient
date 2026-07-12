package dev.prchl.donututilities.mixin;

import dev.prchl.donututilities.DonutUtilitiesClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "aqys.melodify.client.HUDRenderer", remap = false)
public abstract class MelodifyHudMixin {
    @ModifyConstant(method = "onHudRender", remap = false, constant = @Constant(stringValue = "Melodify"), require = 0)
    private static String donututilities$radioTitle(String original) {
        return "Radio";
    }

    @ModifyConstant(method = "onHudRender", remap = false, constant = @Constant(stringValue = "Press K to connect Spotify"), require = 0)
    private static String donututilities$radioConnectHint(String original) {
        return "Press " + DonutUtilitiesClient.MODULES.radioModule().keyName() + " to connect Spotify";
    }
}
