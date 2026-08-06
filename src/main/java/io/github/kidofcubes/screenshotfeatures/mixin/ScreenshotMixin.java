package io.github.kidofcubes.screenshotfeatures.mixin;


import com.google.gson.Gson;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.metadata.ScreenshotTagger;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(Screenshot.class)
public class ScreenshotMixin {
    @Inject(
//            method = "method_22691(Lnet/minecraft/client/texture/NativeImage;Ljava/io/File;Ljava/util/function/Consumer;)V",
            method = "lambda$grab$3",
            at = @At(value = "INVOKE", target ="Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void attachMetadata(final NativeImage nativeImage,final File file,final Consumer<Component> consumer,final CallbackInfo ci) {
        if(Configs.Metadata.TAG_SCREENSHOTS.getBooleanValue()){
            try{
                ScreenshotTagger.writeScreenshotTags(file,ScreenshotTagger.getTags(ScreenshotFeatures.client));
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
