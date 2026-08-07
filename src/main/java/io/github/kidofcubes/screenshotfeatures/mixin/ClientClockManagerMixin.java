package io.github.kidofcubes.screenshotfeatures.mixin;

import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.minecraft.client.ClientClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientClockManager.class)
public class ClientClockManagerMixin {
    @Inject(method="getTotalTicks", at = @At("HEAD"), cancellable = true)
    private void getTotalTicks(CallbackInfoReturnable<Long> cir){
        if(Configs.IngameTools.TIME_OVERRIDE.getBooleanValue()){
            cir.setReturnValue((long)Configs.IngameTools.TIME_OVERRIDE_VALUE.getIntegerValue());
        }
    }
}
