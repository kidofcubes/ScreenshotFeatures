package io.github.kidofcubes.screenshotfeatures.mixin;


import com.google.gson.Gson;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.metadata.ScreenshotTagger;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
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

@Mixin(ScreenshotRecorder.class)
public class ScreenshotMixin {
    //inject into the lambda in takeScreenshot in saveScreenshot
    @ModifyVariable(
            method = "method_68157(Ljava/io/File;Ljava/lang/String;Ljava/util/function/Consumer;Lnet/minecraft/client/texture/NativeImage;)V",
            at = @At("STORE"),
            index = 4)
    private static File directoryInject(final File f) {
        String location;
        if (!Configs.ScreenshotSaving.USE_CUSTOM_SCREENSHOT_DIRECTORY.getBooleanValue()) {
            location = "./screenshots/";
        } else {
            location = Configs.ScreenshotSaving.SCREENSHOT_DIRECTORY.getStringValue() + "/";
        }
        try {
            return new File(location);
        }catch (Exception e){
            ScreenshotFeatures.LOGGER.error("Error checking save location for screenshot: ",e);
            ScreenshotFeatures.client.player.sendMessage(Text.translatable("screenshotfeatures.messages.errorInvalidScreenshotLocation"),false);
            return new File("./screenshots/");
        }
    }

    @ModifyVariable(
            method = "saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;ILjava/util/function/Consumer;)V",
            argsOnly = true,
            at = @At("HEAD"),
            index = 1
    )
    private static String filenameInject(final String orig) {
        if(!Configs.ScreenshotSaving.RENAME_SCREENSHOTS.getBooleanValue()){
            return orig;
        }
        final String filename;
        if (orig == null) {
//            filename = FileNameTemplateProcessor.format(Configs.ScreenshotSaving.SCREENSHOT_NAMING_SCHEMA.getStringValue());
            filename = orig;
        } else {
            filename = orig;
        }
        final String dir;
        if (!Configs.ScreenshotSaving.USE_CUSTOM_SCREENSHOT_DIRECTORY.getBooleanValue()) {
            dir = "./screenshots/";
        } else {
            dir = Configs.ScreenshotSaving.SCREENSHOT_DIRECTORY.getStringValue() + "/";
        }

        int i = 1;
        String tmp = filename;
        while (true) {
            final Path p;
            try {
                 p = Path.of(dir + tmp + ".png");
            }catch (Exception e){
                ScreenshotFeatures.LOGGER.error("Error checking save file location for screenshot: ",e);
                ScreenshotFeatures.client.player.sendMessage(Text.translatable("screenshotfeatures.messages.errorInvalidScreenshotLocation"),false);
                return orig;
            }
            if (!Files.exists(p)) {
                break;
            }
            tmp = filename + "_(" + i + ")";
            ++i;
        }
        return tmp + ".png";
    }

    // inject into the lambda in Util.getServiceWorker.execute()
    // this is to create any necessary subdirectories for the screenshots.
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Inject(
            method = "method_22691(Lnet/minecraft/client/texture/NativeImage;Ljava/io/File;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void createDirs(final NativeImage _nativeImage, final File file, final Consumer<Text> _consumer, final CallbackInfo _ci) {
        file.getParentFile().mkdirs();
    }

    @Inject(
            method = "method_22691(Lnet/minecraft/client/texture/NativeImage;Ljava/io/File;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private static void attachMetadata(final NativeImage nativeImage, final File file, final Consumer<Text> consumer, final CallbackInfo ci) {
//        PNGMetadataManipulator.attachMetadata(file, GameMeta.getMetadata(ScreenshotFeatures.client));
        if(Configs.Metadata.TAG_SCREENSHOTS.getBooleanValue()){
            try{
                ScreenshotTagger.writeScreenshotTags(file,ScreenshotTagger.getTags(ScreenshotFeatures.client));
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
