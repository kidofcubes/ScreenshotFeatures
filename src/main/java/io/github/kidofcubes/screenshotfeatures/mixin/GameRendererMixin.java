package io.github.kidofcubes.screenshotfeatures.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.kidofcubes.screenshotfeatures.screens.CameraMatrixEditorGui;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RawProjectionMatrix;set(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;", shift = At.Shift.BY))
    void debug(RenderTickCounter renderTickCounter,CallbackInfo ci, @Local(name = "h") float h, @Local(name = "matrix4f") Matrix4f matrix4f) {
        CameraMatrixEditorGui.matrix = matrix4f;
    }
}
