package io.github.kidofcubes.screenshotfeatures.integrations;

import com.google.gson.Gson;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.metadata.ScreenshotTagger;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
//import me.ramidzkh.fabrishot.event.ScreenshotSaveCallback;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.util.Map;

public class FabrishotIntegration {
    public static boolean fabrishotPresent() {
        return FabricLoader.getInstance().isModLoaded("fabrishot");
    }
    public static void register(){
        if(fabrishotPresent()) {
//            ScreenshotSaveCallback.EVENT.register(path -> {
//                if(Configs.Metadata.TAG_SCREENSHOTS.getBooleanValue()){
//                    try{
//                        ScreenshotTagger.writeScreenshotTags(path.toFile(),ScreenshotTagger.getTags(ScreenshotFeatures.client));
//                    }catch(IOException e){
//                        e.printStackTrace();
//                    }
//                }
////                PNGMetadataManipulator.attachMetadata(path.toFile(), GameMeta.getMetadata(ScreenshotFeatures.client));
//            });
//            ScreenshotFeatures.LOGGER.info("Fabrishot loaded, hooking!");
        }else{
            ScreenshotFeatures.LOGGER.info("Fabrishot not loaded, not hooking!");
        }
    }
}
