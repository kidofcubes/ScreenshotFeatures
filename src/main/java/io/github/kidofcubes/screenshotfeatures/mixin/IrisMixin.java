package io.github.kidofcubes.screenshotfeatures.mixin;


import io.github.kidofcubes.screenshotfeatures.integrations.ShaderIntegration;
import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.file.Path;
import java.util.Map;

@Mixin(value = Iris.class, remap = false)
public abstract class IrisMixin {
    @Inject(
            method = "loadExternalShaderpack",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static void loadShaderPack(String name,CallbackInfoReturnable<Boolean> cir,Path shaderPackRoot,Path shaderPackConfigTxt,Path shaderPackPath,boolean isZip,Map<String,String> changedConfigs){
        if(cir.getReturnValue()==true){ //in case of failure
            ShaderIntegration.onShaderPackLoad(shaderPackRoot,shaderPackPath,isZip);
        }
    }

    @Inject(
            method = "setShadersDisabled",
            at = @At("HEAD")
    )
    private static void unloadShaderPack(CallbackInfo ci){
        ShaderIntegration.onShaderPackUnload();
    }
}
