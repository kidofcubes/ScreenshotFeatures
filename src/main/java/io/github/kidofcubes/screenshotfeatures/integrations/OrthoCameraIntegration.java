package io.github.kidofcubes.screenshotfeatures.integrations;

import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.fabricmc.loader.api.FabricLoader;

public class OrthoCameraIntegration {
    public static boolean OrthoCameraPresent() {
        return FabricLoader.getInstance().isModLoaded("orthocamera");
    }
    public static void register(){
        if(!OrthoCameraPresent()) {
            ScreenshotFeatures.LOGGER.info("OrthoCamera not loaded, not hooking!");
            return;
        }

        ScreenshotFeatures.LOGGER.info("OrthoCamera loaded, hooking!");

        Configs.OrthoCameraIntegration.CONFIG_ENABLED.setValueChangeCallback(config -> {
            if(config.getBooleanValue()){
                Configs.OrthoCameraIntegration.ORTHO_ENABLED.markDirty();
                Configs.OrthoCameraIntegration.FIXED_CAMERA.markDirty();
                Configs.OrthoCameraIntegration.X_SCALE.markDirty();
                Configs.OrthoCameraIntegration.Y_SCALE.markDirty();
                Configs.OrthoCameraIntegration.MIN_DISTANCE.markDirty();
                Configs.OrthoCameraIntegration.MAX_DISTANCE.markDirty();
            }
        });

//        Configs.OrthoCameraIntegration.ORTHO_ENABLED.setValueChangeCallback(config -> {
//            if(!Configs.OrthoCameraIntegration.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.enabled = Configs.OrthoCameraIntegration.ORTHO_ENABLED.getBooleanValue();
//        });
//        Configs.OrthoCameraIntegration.FIXED_CAMERA.setValueChangeCallback(config -> {
//            if(!Configs.OrthoCameraIntegration.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.fixed = Configs.OrthoCameraIntegration.FIXED_CAMERA.getBooleanValue();
//        });
//
//        Configs.OrthoCameraIntegration.X_SCALE.setValueChangeCallback(config -> {
//            if(!Configs.OrthoCameraIntegration.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.scale_x = Configs.OrthoCameraIntegration.X_SCALE.getFloatValue();
//        });
//        Configs.OrthoCameraIntegration.Y_SCALE.setValueChangeCallback(config -> {
//            if(!Configs.OrthoCameraIntegration.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.scale_y = Configs.OrthoCameraIntegration.Y_SCALE.getFloatValue();
//        });
//
//        Configs.OrthoCameraIntegration.MIN_DISTANCE.setValueChangeCallback(config -> {
//            if(!Configs.OrthoCameraIntegration.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.min_distance = Configs.OrthoCameraIntegration.MIN_DISTANCE.getFloatValue();
//        });
//        Configs.OrthoCameraIntegration.MAX_DISTANCE.setValueChangeCallback(config -> {
//            if(!Configs.OrthoCameraIntegration.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.max_distance = Configs.OrthoCameraIntegration.MAX_DISTANCE.getFloatValue();
//        });

        Configs.OrthoCameraIntegration.CONFIG_ENABLED.markDirty();
    }
}
