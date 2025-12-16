package io.github.kidofcubes.screenshotfeatures.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.kidofcubes.screenshotfeatures.CameraMatrixManager;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @ModifyVariable(method = "renderWorld", at = @At(value = "LOAD", target = "Lnet/minecraft/client/render/RawProjectionMatrix;set(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
    Matrix4f modifyProjectionMatrix(Matrix4f projectionMatrix, @Local(name = "h") float fov) {
        if(Configs.CameraMatrix.OVERRIDE_MATRIX.getBooleanValue()){
            return new Matrix4f(CameraMatrixManager.matrix);
        }
        return projectionMatrix;
    }

    @ModifyArg(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RawProjectionMatrix;set(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"), index = 0)
//    void getProjectionMatrix(RenderTickCounter renderTickCounter,CallbackInfo ci,@Local(name = "h") float fov, @Local(name = "matrix4f") Matrix4f projectionMatrix) {
    Matrix4f getProjectionMatrix(Matrix4f projectionMatrix,@Local(name = "h") float fov) {
//        if(Configs.CameraMatrix.OVERRIDE_MATRIX.getBooleanValue()){
//            return CameraMatrixEditorGui.matrix;
//        }
        if(Configs.CameraMatrix.PULL_MATRIX.getBooleanValue()){
            CameraMatrixManager.matrix = new Matrix4d(projectionMatrix);
        }
        return projectionMatrix;
    }

//    @Inject(method = "getProjectionMatrix")
}
