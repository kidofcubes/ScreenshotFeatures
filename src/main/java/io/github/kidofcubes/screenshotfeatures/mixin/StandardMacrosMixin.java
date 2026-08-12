package io.github.kidofcubes.screenshotfeatures.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.irisshaders.iris.gl.shader.StandardMacros;
import net.irisshaders.iris.helpers.StringPair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value=StandardMacros.class, remap = false)
public class StandardMacrosMixin {
    @Shadow
    private static void define(List<StringPair> defines,String key){}

    @Shadow
    private static void define(List<StringPair> defines,String key,String value){}
    @Inject(method = "createStandardEnvironmentDefines", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/shader/StandardMacros;define(Ljava/util/List;Ljava/lang/String;Ljava/lang/String;)V", ordinal = 0))
    private static void injectScreenshotFeaturesDefine(CallbackInfoReturnable<ImmutableList<StringPair>> cir,@Local(name="standardDefines") ArrayList<StringPair> standardDefines) {
        define(standardDefines, "SCREENSHOT_FEATURES");
        for(var entry: Configs.CustomUniforms.CUSTOM_UNIFORMS.getEntries()){
            define(standardDefines, "SCREENSHOT_FEATURES_CUSTOM_UNIFORM_"+entry.getName());
        }
    }

}
