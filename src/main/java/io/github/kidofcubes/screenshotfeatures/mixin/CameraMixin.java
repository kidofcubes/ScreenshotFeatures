package io.github.kidofcubes.screenshotfeatures.mixin;

import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public class CameraMixin {
    @ModifyVariable(method="getNearPlane", at = @At("STORE"), index=2)
    public double getNearPlane(double e){
        if(Configs.IngameTools.FOV_OVERRIDE.getBooleanValue()){
            //calculation from vanilla
            return Math.tan(Configs.IngameTools.FOV.getDoubleValue() * (float) (Math.PI / 180.0) / 2.0) * 0.05F;
        }
        return e;
    }
}
