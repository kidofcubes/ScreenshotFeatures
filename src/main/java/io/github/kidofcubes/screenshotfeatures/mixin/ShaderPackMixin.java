package io.github.kidofcubes.screenshotfeatures.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.include.IncludeProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ShaderPack.class, remap = false)
public class ShaderPackMixin {
    @Inject(method="lambda$new$8", at=@At(value="INVOKE", target="Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;"))
    private static void injectUniforms(List<String> disabledPrograms,IncludeProcessor includeProcessor,Iterable<StringPair> finalEnvironmentDefines1,AbsolutePackPath path,CallbackInfoReturnable<String> cir,@Local(name="source") LocalRef<String> source){
//        source

    }
}
