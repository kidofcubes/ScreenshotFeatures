package io.github.kidofcubes.screenshotfeatures.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.ProjectionType;
import io.github.kidofcubes.screenshotfeatures.CameraMatrixManager;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import org.joml.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.joml.Matrix4dc.PROPERTY_AFFINE;
import static org.joml.Matrix4dc.PROPERTY_PERSPECTIVE;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method ="renderLevel", at = @At(value="INVOKE", target ="Lnet/minecraft/client/renderer/PerspectiveProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;", shift = At.Shift.BEFORE))
    private void overrideMatrix(CallbackInfo ci, @Local(name = "matrix4f") LocalRef<Matrix4f> matrix4f) {

        if(Configs.CameraMatrix.PULL_MATRIX.getBooleanValue()){
            CameraMatrixManager.matrix = new Matrix4d(matrix4f.get());
        }
        if(Configs.CameraMatrix.OVERRIDE_MATRIX.getBooleanValue()){
            matrix4f.set(new Matrix4f(CameraMatrixManager.matrix));
        }
    }


    @Inject(method ="getProjectionMatrix", at = @At(value = "HEAD"), cancellable = true)
    public void getBasicProjectionMatrix(float fovDegrees,CallbackInfoReturnable<Matrix4f> cir) {
        if(Configs.CameraMatrix.OVERRIDE_MATRIX.getBooleanValue()){
            cir.setReturnValue(new Matrix4f(CameraMatrixManager.matrix));
        }
    }

    @ModifyArg(method ="renderLevel", at = @At(value = "INVOKE", target ="Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V"), index = 1)
    ProjectionType setMatrixOption(ProjectionType projectionType) {
        if(Configs.CameraMatrix.OVERRIDE_MATRIX.getBooleanValue()){
            if((CameraMatrixManager.matrix.properties() & PROPERTY_PERSPECTIVE) > 0){
                return ProjectionType.PERSPECTIVE;
            }else if((CameraMatrixManager.matrix.properties() & PROPERTY_AFFINE) > 0){
                return ProjectionType.ORTHOGRAPHIC;
            }
        }
        return projectionType;
    }
    @Final
    @Shadow
    private Camera mainCamera;

    @Inject(method ="renderLevel", at = @At(value = "INVOKE", target ="Lnet/minecraft/client/Camera;update(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", shift = At.Shift.AFTER), cancellable = true)
    public void thing(DeltaTracker renderTickCounter,CallbackInfo ci){
        if(Configs.CameraMatrix.OVERRIDE_MATRIX.getBooleanValue()){
            Vector3d translation = new Vector3d(0,0,Configs.CameraMatrix.ORTHOGONAL_OFFSET.getDoubleValue()).rotate(new Quaterniond(mainCamera.rotation()));
            ((CameraAccessor)mainCamera).setPosInvoker(mainCamera.position().add(translation.x, translation.y, translation.z));
        }
    }
}
