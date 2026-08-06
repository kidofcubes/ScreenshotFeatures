package io.github.kidofcubes.screenshotfeatures.mixin;

import io.github.kidofcubes.screenshotfeatures.CameraMatrixManager;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
    @ModifyVariable(method="getNearPlane", argsOnly = true, at = @At("HEAD"), name = "fov")
    public float getNearPlane(float fov){
        if(Configs.IngameTools.FOV_OVERRIDE.getBooleanValue()){
            return (float)Configs.IngameTools.FOV.getDoubleValue();
        }
        return fov;
    }
    @Inject(method="calculateFov", at = @At("HEAD"), cancellable = true)
    public void calculateFov(float partialTicks,CallbackInfoReturnable<Float> cir){
        if(Configs.IngameTools.FOV_OVERRIDE.getBooleanValue()){
            cir.setReturnValue((float)Configs.IngameTools.FOV.getDoubleValue());
        }
    }

    @ModifyVariable(method="createProjectionMatrixForCulling", at=@At("STORE"), argsOnly=false, name="fovForCulling")
    public float createProjectionMatrixForCulling(float g){
        if(Configs.IngameTools.FOV_OVERRIDE.getBooleanValue()){
            return (float)Configs.IngameTools.FOV.getDoubleValue();
        }else{
            return g;
        }
    }
    @Inject(method = "createProjectionMatrixForCulling", at = @At("HEAD"), cancellable=true)
    public void createProjectionMatrixForCulling(CallbackInfoReturnable<Matrix4f> cir){
        if(Configs.CameraMatrix.OVERRIDE_MATRIX.getBooleanValue()){
            cir.setReturnValue(new Matrix4f(CameraMatrixManager.matrix));
        }
    }
}
