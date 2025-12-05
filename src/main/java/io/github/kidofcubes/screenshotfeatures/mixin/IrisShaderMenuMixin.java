package io.github.kidofcubes.screenshotfeatures.mixin;

import com.google.gson.JsonObject;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.metadata.ScreenshotTagger;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.gui.element.ShaderPackSelectionList;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Mixin(value = ShaderPackScreen.class, remap = false)
public abstract class IrisShaderMenuMixin {
    @Shadow
    private ShaderPackSelectionList shaderPackList;

    @Shadow
    public abstract void refreshForChangedPack();

    @Shadow
    private Text notificationDialog;

    @Shadow
    private int notificationDialogTimer;

    @Inject(
            method = "onPackListFilesDrop",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onPackListFilesDrop(List<Path> paths,CallbackInfo callbackInfo) {
        if(paths.size() != 1){
            return;
        }
        Path path = paths.getFirst();
        if(!path.getFileName().toString().endsWith(".png")){
            return;
        }
        try{
            JsonObject tags = ScreenshotTagger.getScreenshotTags(path.toFile());
            if(!tags.has("shader")){
                notificationDialog = Text.of("Screenshot doesn't have appropriate metadata! ");
                notificationDialogTimer = 100;
                callbackInfo.cancel();
                return;
            }
            if(!tags.getAsJsonObject("shader").has("allSettings")){
                notificationDialog = Text.of("Dragged screenshot doesn't have any settings! (Did you enable the shaderpack settings and shaderpack metadata options?)");
                notificationDialogTimer = 100;
                callbackInfo.cancel();
                return;
            }
            if(!tags.getAsJsonObject("shader").has("name")){
                notificationDialog = Text.of("Dragged screenshot doesn't specify the shader name! (Did you enable the shaderpack settings and shaderpack metadata options?)");
                notificationDialogTimer = 100;
                callbackInfo.cancel();
                return;
            }

            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(tags.getAsJsonObject("shader").get("allSettings").getAsString().getBytes(StandardCharsets.UTF_8)));
            String name = tags.getAsJsonObject("shader").get("name").getAsString();

            shaderPackList.select(name);
            if(shaderPackList.getSelectedOrNull()==null || !((ShaderPackSelectionList.ShaderPackEntry)shaderPackList.getSelectedOrNull()).getPackName().equals(name)){
                notificationDialog = Text.of("Shaderpack "+name+" doesn't exist in your shaderpacks folder!");
                notificationDialogTimer = 100;
                callbackInfo.cancel();
                return;
            }
            Iris.clearShaderPackOptionQueue();

            Iris.queueShaderPackOptionsFromProperties(properties);

            //snippet taken from applyChanges (edited to avoid clearing option changes)
            boolean enabled = this.shaderPackList.getTopButtonRow().shadersEnabled;
            boolean previousShadersEnabled = Iris.getIrisConfig().areShadersEnabled();
            if (enabled != previousShadersEnabled) {
                IrisApi.getInstance().getConfig().setShadersEnabledAndApply(enabled);
            }

            this.shaderPackList.setApplied((ShaderPackSelectionList.ShaderPackEntry)shaderPackList.getSelectedOrNull());
            Iris.getIrisConfig().setShaderPackName(name);
            IrisApi.getInstance().getConfig().setShadersEnabledAndApply(shaderPackList.getTopButtonRow().shadersEnabled);
            refreshForChangedPack();

            notificationDialog = Text.of("Loaded Shaderpack options from screenshot successfully!");
            notificationDialogTimer = 100;
        }catch(Exception e){
            notificationDialog = Text.of("Error occurred, check logs for details! "+e.getMessage());
            notificationDialogTimer = 100;
            ScreenshotFeatures.LOGGER.warn("Error loading/applying shaderpack options from screenshot: ",e);
        }
        callbackInfo.cancel();


    }
}