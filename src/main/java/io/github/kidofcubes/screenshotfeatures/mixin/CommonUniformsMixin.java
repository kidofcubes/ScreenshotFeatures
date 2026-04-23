package io.github.kidofcubes.screenshotfeatures.mixin;

import fi.dy.masa.malilib.config.IConfigBase;
import io.github.kidofcubes.screenshotfeatures.CameraMatrixManager;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.config.ConfigAdjustableDouble;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.shaderpack.IdMap;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.joml.Matrix4dc.PROPERTY_PERSPECTIVE;

@Mixin(value=CommonUniforms.class, remap = false)
public class CommonUniformsMixin {
    @Inject(method="addNonDynamicUniforms", at = @At("HEAD"))
    private static void addUniforms(UniformHolder uniforms,IdMap idMap,PackDirectives directives,FrameUpdateNotifier updateNotifier,CallbackInfo ci){
//        ScreenshotFeatures.LOGGER.info("added uniform");
        uniforms.uniform1b(UniformUpdateFrequency.PER_FRAME,"isOrthogonalProjection", () -> {
//            ScreenshotFeatures.LOGGER.info("override matrix? "+Configs.CameraMatrix.OVERRIDE_MATRIX.getBooleanValue());
            if(Configs.CameraMatrix.OVERRIDE_MATRIX.getBooleanValue()){
//                ScreenshotFeatures.LOGGER.info("is orthogonal? "+((CameraMatrixManager.matrix.properties() & PROPERTY_PERSPECTIVE) == 0));
                //assume if not perspective, it's orthogonal
                return (CameraMatrixManager.matrix.properties() & PROPERTY_PERSPECTIVE) == 0;
            }else{
                return false;
            }
        });
        for(IConfigBase config : Configs.ShaderOptions.OPTIONS){
            if(config instanceof ConfigAdjustableDouble configAdjustableDouble){
                uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME,configAdjustableDouble.getName(),configAdjustableDouble::getDoubleValue);
            }
        }

    }
}
