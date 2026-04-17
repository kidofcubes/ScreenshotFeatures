package io.github.kidofcubes.screenshotfeatures.mixin;

import io.github.kidofcubes.screenshotfeatures.CameraMatrixManager;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import me.cortex.voxy.client.core.VoxyRenderSystem;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VoxyRenderSystem.class, remap = false)
public class VoxyRenderSystemMixin {
    @Inject(method="computeProjectionMat", at=@At("HEAD"), cancellable=true)
    private static void computeProjectionMat(Matrix4fc base,CallbackInfoReturnable<Matrix4f> cir) {
        if(Configs.CameraMatrix.OVERRIDE_MATRIX.getBooleanValue()){
            Matrix4f matrix = new Matrix4f(CameraMatrixManager.matrix);
            Matrix4f extraProjection = matrix.invert(new Matrix4f()).mul(base);
            cir.setReturnValue(extraProjection.mulLocal(new Matrix4f(matrix)));
        }
    }
}
