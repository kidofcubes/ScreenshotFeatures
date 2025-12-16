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

//        Configs.CameraMatrix.CONFIG_ENABLED.setValueChangeCallback(config -> {
//            if(config.getBooleanValue()){
//                Configs.CameraMatrix.ORTHO_ENABLED.markDirty();
//                Configs.CameraMatrix.FIXED_CAMERA.markDirty();
//                Configs.CameraMatrix.X_SCALE.markDirty();
//                Configs.CameraMatrix.Y_SCALE.markDirty();
//                Configs.CameraMatrix.MIN_DISTANCE.markDirty();
//                Configs.CameraMatrix.MAX_DISTANCE.markDirty();
//            }
//        });

//        Configs.CameraMatrix.ORTHO_ENABLED.setValueChangeCallback(config -> {
//            if(!Configs.CameraMatrix.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.enabled = Configs.CameraMatrix.ORTHO_ENABLED.getBooleanValue();
//        });
//        Configs.CameraMatrix.FIXED_CAMERA.setValueChangeCallback(config -> {
//            if(!Configs.CameraMatrix.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.fixed = Configs.CameraMatrix.FIXED_CAMERA.getBooleanValue();
//        });
//
//        Configs.CameraMatrix.X_SCALE.setValueChangeCallback(config -> {
//            if(!Configs.CameraMatrix.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.scale_x = Configs.CameraMatrix.X_SCALE.getFloatValue();
//        });
//        Configs.CameraMatrix.Y_SCALE.setValueChangeCallback(config -> {
//            if(!Configs.CameraMatrix.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.scale_y = Configs.CameraMatrix.Y_SCALE.getFloatValue();
//        });
//
//        Configs.CameraMatrix.MIN_DISTANCE.setValueChangeCallback(config -> {
//            if(!Configs.CameraMatrix.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.min_distance = Configs.CameraMatrix.MIN_DISTANCE.getFloatValue();
//        });
//        Configs.CameraMatrix.MAX_DISTANCE.setValueChangeCallback(config -> {
//            if(!Configs.CameraMatrix.CONFIG_ENABLED.getBooleanValue()){ return; }
//            OrthoCamera.CONFIG.max_distance = Configs.CameraMatrix.MAX_DISTANCE.getFloatValue();
//        });

//        Configs.CameraMatrix.CONFIG_ENABLED.markDirty();
    }
}
