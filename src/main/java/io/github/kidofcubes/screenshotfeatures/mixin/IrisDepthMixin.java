package io.github.kidofcubes.screenshotfeatures.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.irisshaders.iris.gl.program.ProgramBuilder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.pathways.CenterDepthSampler;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Mixin(value = CenterDepthSampler.class, remap = false)
public abstract class IrisDepthMixin {

    @Redirect(method ="<init>", at = @At(value = "NEW", target = "java/lang/String", ordinal = 0))
    private String fshShaderOverride(byte[] bytes, Charset charset) {
        try {
            String fsh = new String(IOUtils.toByteArray((InputStream) Objects.requireNonNull(ScreenshotFeatures.class.getResourceAsStream("/centerDepthOverride.fsh"))), StandardCharsets.UTF_8);
            return fsh;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(method ="<init>", at = @At(value = "INVOKE", target ="Lnet/irisshaders/iris/gl/program/ProgramBuilder;build()Lnet/irisshaders/iris/gl/program/Program;"))
    private void addUniform(CallbackInfo ci, @Local ProgramBuilder builder){
        builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "lockedValue" ,() -> {
            if(Configs.IngameTools.DOF_OVERRIDE.getBooleanValue()){
                return linearToDepth((Minecraft.getInstance().options.renderDistance().get() * 16.0f), Configs.IngameTools.DOF_OVERRIDE_VALUE.getDoubleValue());
            }else if(Configs.IngameTools.DOF_LOCK.getBooleanValue()){
                return -1.0f;
            }else{
                return 0.0f;
            }
        });
    }

    @Unique
    private static final float near = 0.05F; //i think it is anyway

    //taken from a shader somewhere, need to properly update later
    float linearToDepth(double far, double linear){
        return (float)(((IrisDepthMixin.near *far)-(linear*far))/(linear*(IrisDepthMixin.near -far)));
    }
}