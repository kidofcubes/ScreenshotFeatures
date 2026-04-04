package io.github.kidofcubes.screenshotfeatures.mixin;

import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.ClientLevelData.class)
public class TimeChangerMixin {
    @Inject(
            method ="getDayTime",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if(Configs.IngameTools.TIME_OVERRIDE.getBooleanValue()) {
            cir.setReturnValue((long) Configs.IngameTools.TIME_OVERRIDE_VALUE.getIntegerValue());
        }
    }
}
