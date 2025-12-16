package io.github.kidofcubes.screenshotfeatures.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.config.ConfigTypes.*;

import java.io.File;

public class Configs implements IConfigHandler {

    private static final String CONFIG_FILE_NAME = ScreenshotFeatures.MOD_ID+".json";

    private static <T extends ConfigBase<?>> T autoCommentAndNameWithGroup(T thing, String group){
        thing.setComment(String.format("%s.config.%s.comment.%s",ScreenshotFeatures.MOD_ID,group,thing.getName()));
        thing.setTranslatedName(String.format("%s.config.%s.name.%s",ScreenshotFeatures.MOD_ID,group,thing.getName()));
        return thing;
    }

    public static class IngameTools {
        private static final ImmutableList.Builder<IConfigBase> OPTIONS_BUILDER = ImmutableList.builder();
        public static final ImmutableList<IConfigBase> OPTIONS;

        private static final ImmutableList.Builder<IHotkey> HOTKEYS_BUILDER = ImmutableList.builder();
        public static final ImmutableList<IHotkey> HOTKEYS;

        private static <T extends ConfigBase<?>> T setupConfig(T thing){
            autoCommentAndNameWithGroup(thing, "ingametools");
            OPTIONS_BUILDER.add(thing);
            if(thing instanceof IHotkey iHotkey){
                HOTKEYS_BUILDER.add(iHotkey);
            }
            return thing;
        }


        public static final ConfigHotkey OPEN_CONFIG = setupConfig(new ConfigHotkey("openConfig", ""));

        public static final ConfigBooleanHotkeyed DOF_LOCK = setupConfig(new ConfigBooleanHotkeyed("dofLock", false, ""));
        public static final ConfigBooleanHotkeyed DOF_OVERRIDE = setupConfig(new ConfigBooleanHotkeyed("dofOverride", false, ""));
        public static final ConfigDouble DOF_OVERRIDE_VALUE = setupConfig(new ConfigDouble("dofOverrideValue", 1.0, -Double.MAX_VALUE,Double.MAX_VALUE));
        public static final ConfigDouble DOF_STEP = setupConfig(new ConfigDouble("dofStep", 1.0, -Double.MAX_VALUE,Double.MAX_VALUE));
        public static final ConfigHotkey DOF_MODIFIER = setupConfig(new ConfigHotkey("dofModifier", "LEFT", KeybindSettings.MODIFIER_INGAME_EMPTY));
        public static final ConfigBooleanHotkeyed TIME_OVERRIDE = setupConfig(new ConfigBooleanHotkeyed("timeOverride", false, ""));
        public static final ConfigInteger TIME_OVERRIDE_VALUE = setupConfig(new ConfigInteger("timeOverrideValue", 0));
        public static final ConfigInteger TIME_STEP = setupConfig(new ConfigInteger("timeStep", 50)); //todo ConfigLong
        public static final ConfigHotkey TIME_MODIFIER = setupConfig(new ConfigHotkey("timeModifier", "RIGHT", KeybindSettings.MODIFIER_INGAME_EMPTY));
        public static final ConfigBooleanHotkeyed WEATHER_OVERRIDE = setupConfig(new ConfigBooleanHotkeyed("weatherOverride", false, ""));
        public static final ConfigOptionList WEATHER_OVERRIDE_VALUE = setupConfig(new ConfigOptionList("weatherOverrideValue", WeatherTypes.CLEAR));
        public static final ConfigHotkey CYCLE_WEATHER_OVERRIDE = setupConfig(new ConfigHotkey("weatherOverrideCycle", ""));
        public static final ConfigBooleanHotkeyed FORCE_PRECIPITATION = setupConfig(new ConfigBooleanHotkeyed("precipitationForce", false, ""));
        public static final ConfigOptionList PRECIPITATION_FORCE_TYPE = setupConfig(new ConfigOptionList("precipitationForceType", PrecipitationType.NO_OP, ""));
        public static final ConfigOptionList FORCE_RAIN = setupConfig(new ConfigOptionList("rainForce", OptionalBoolean.NO_OP));
        public static final ConfigBoolean ALLOW_MWHEEL_CHANGE_VALUE = setupConfig(new ConfigBoolean("allowMWheelChangeValue", true));
        public static final ConfigDouble MWHEEL_MULTIPLIER = setupConfig(new ConfigDouble("mWheelMultiplier", 1.0, -Double.MAX_VALUE, Double.MAX_VALUE));
        public static final ConfigHotkey INCREASE_VALUE = setupConfig(new ConfigHotkey("increaseValue", "UP", KeybindSettings.PRESS_ALLOWEXTRA));
        public static final ConfigHotkey DECREASE_VALUE = setupConfig(new ConfigHotkey("decreaseValue", "DOWN", KeybindSettings.PRESS_ALLOWEXTRA));
        public static final ConfigHotkey LARGE_VALUE_MODIFIER = setupConfig(new ConfigHotkey("largeValueModifier", "LEFT_CONTROL", KeybindSettings.MODIFIER_INGAME));
        public static final ConfigHotkey SMALL_VALUE_MODIFIER = setupConfig(new ConfigHotkey("smallValueModifier", "LEFT_SHIFT", KeybindSettings.MODIFIER_INGAME));
        public static final ConfigDouble LARGE_VALUE_MULTIPLIER = setupConfig(new ConfigDouble("largeValueMultiplier", 10.0, -Double.MAX_VALUE,Double.MAX_VALUE));
        public static final ConfigDouble SMALL_VALUE_MULTIPLIER = setupConfig(new ConfigDouble("smallValueMultiplier", 0.1, -Double.MAX_VALUE,Double.MAX_VALUE));

        public static final ConfigBoolean FORCE_SETTINGS = setupConfig(new ConfigBoolean("forceSettings", false));

        static {
            OPTIONS = OPTIONS_BUILDER.build();
            HOTKEYS = HOTKEYS_BUILDER.build();
        }
    }

    public static class Metadata {
        private static final ImmutableList.Builder<IConfigBase> OPTIONS_BUILDER = ImmutableList.builder();
        public static final ImmutableList<IConfigBase> OPTIONS;

        private static <T extends ConfigBase<?>> T setupConfig(T thing){
            autoCommentAndNameWithGroup(thing, "metadata");
            OPTIONS_BUILDER.add(thing);
            return thing;
        }

        public static final ConfigBoolean TAG_SCREENSHOTS = setupConfig(new ConfigBoolean( "tagScreenshots", true));
        public static final ConfigBoolean LOCATION = setupConfig(new ConfigBoolean( "location", true));
        public static final ConfigBoolean CAMERA_DATA = setupConfig(new ConfigBoolean( "cameraData", true));
        public static final ConfigBoolean WORLD_DATA = setupConfig(new ConfigBoolean( "worldData", true));
        public static final ConfigBoolean WORLD_SEED = setupConfig(new ConfigBoolean( "worldSeed", true));
        public static final ConfigBoolean RESOURCE_PACKS = setupConfig(new ConfigBoolean( "resourcePacks", true));
        public static final ConfigBoolean MOD_LIST = setupConfig(new ConfigBoolean( "modList", false));
        public static final ConfigBoolean SHADER_PACK_NAME = setupConfig(new ConfigBoolean( "shaderPackName", true));
        public static final ConfigBoolean SHADER_PACK_HASH = setupConfig(new ConfigBoolean( "shaderPackHash", true));
        public static final ConfigBoolean SHADER_PACK_COMMIT = setupConfig(new ConfigBoolean( "shaderPackCommit", true));
        public static final ConfigBoolean SHADER_PACK_DIFF = setupConfig(new ConfigBoolean( "shaderPackDiff", true));
        public static final ConfigBoolean SHADER_PACK_SETTINGS = setupConfig(new ConfigBoolean( "shaderPackSettings", true));
        public static final ConfigBoolean MC_VERSION = setupConfig(new ConfigBoolean( "mcVersion", true));

        static {
            OPTIONS = OPTIONS_BUILDER.build();
        }
    }

    public static class ScreenshotSaving {
        private static final ImmutableList.Builder<IConfigBase> OPTIONS_BUILDER = ImmutableList.builder();
        public static final ImmutableList<IConfigBase> OPTIONS;

        private static <T extends ConfigBase<?>> T setupConfig(T thing){
            autoCommentAndNameWithGroup(thing, "screenshotsaving");
            OPTIONS_BUILDER.add(thing);
            return thing;
        }


        public static final ConfigBoolean USE_CUSTOM_SCREENSHOT_DIRECTORY = setupConfig(new ConfigBoolean( "useCustomScreenshotDirectory", false));
        public static final ConfigString SCREENSHOT_DIRECTORY = setupConfig(new ConfigString( "customScreenshotDirectory", "./screenshots/"));
        public static final ConfigBoolean RENAME_SCREENSHOTS = setupConfig(new ConfigBoolean( "renameScreenshots", true));
        public static final ConfigString SCREENSHOT_NAMING_SCHEMA = setupConfig(new ConfigString( "screenshotNamingSchema", "<datetime>-tagged"));

        static {
            OPTIONS = OPTIONS_BUILDER.build();
        }
    }

    public static class CameraMatrix {
        private static final ImmutableList.Builder<IConfigBase> OPTIONS_BUILDER = ImmutableList.builder();
        public static final ImmutableList<IConfigBase> OPTIONS;

        private static final ImmutableList.Builder<IHotkey> HOTKEYS_BUILDER = ImmutableList.builder();
        public static final ImmutableList<IHotkey> HOTKEYS;

        private static <T extends ConfigBase<?>> T setupConfig(T thing){
            autoCommentAndNameWithGroup(thing, "cameramatrix");
            OPTIONS_BUILDER.add(thing);
            if(thing instanceof IHotkey iHotkey){
                HOTKEYS_BUILDER.add(iHotkey);
            }
            return thing;
        }


        public static final ConfigBooleanHotkeyed PULL_MATRIX = setupConfig(new ConfigBooleanHotkeyed("pullMatrix", false, ""));
        public static final ConfigBooleanHotkeyed OVERRIDE_MATRIX = setupConfig(new ConfigBooleanHotkeyed("overrideMatrix", false, ""));
        public static final ConfigDouble MATRIX_WIDTH = setupConfig(new ConfigDouble("matrixDesiredWidth", 1600, ""));
        public static final ConfigDouble MATRIX_HEIGHT = setupConfig(new ConfigDouble("matrixDesiredHeight", 900, ""));
        public static final ConfigDouble MATRIX_PERSPECTIVE_SETTINGS_DISTANCE = setupConfig(new ConfigDouble("matrixSettingDistance", 1024, ""));
        public static final ConfigDouble MATRIX_NEAR = setupConfig(new ConfigDouble("matrixNear", 0.01,  -Double.MAX_VALUE, Double.MAX_VALUE, ""));
        public static final ConfigDouble MATRIX_FAR = setupConfig(new ConfigDouble("matrixFar", 8192, ""));
        public static final ConfigOptionList MATRIX_SETTINGS_APPLY = setupConfig(new ConfigOptionList("matrixSettingsApply", MatrixSettingsApplyType.WIDTH));
        public static final ConfigBooleanHotkeyed ALWAYS_APPLY_MATRIX = setupConfig(new ConfigBooleanHotkeyed("alwaysApplyMatrix", false, ""));

        static {
            OPTIONS = OPTIONS_BUILDER.build();
            HOTKEYS = HOTKEYS_BUILDER.build();
        }
    }

    public static void loadFromFile() {
        File configFile = new File(FileUtils.getConfigDirectoryAsPath().toFile(), CONFIG_FILE_NAME);

        if (configFile.exists() && configFile.isFile() && configFile.canRead())
        {
            JsonElement element = JsonUtils.parseJsonFile(configFile);

            if (element != null && element.isJsonObject())
            {
                JsonObject root = element.getAsJsonObject();

                ConfigUtils.readConfigBase(root, "IngameTools", Configs.IngameTools.OPTIONS);
                ConfigUtils.readConfigBase(root, "Metadata", Configs.Metadata.OPTIONS);
                ConfigUtils.readConfigBase(root, "ScreenshotSaving", Configs.ScreenshotSaving.OPTIONS);
                ConfigUtils.readConfigBase(root, "CameraMatrix", CameraMatrix.OPTIONS);
            }
        }
    }

    public static void saveToFile() {
        File dir = FileUtils.getConfigDirectoryAsPath().toFile();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs())
        {
            JsonObject root = new JsonObject();

            ConfigUtils.writeConfigBase(root, "IngameTools", Configs.IngameTools.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Metadata", Configs.Metadata.OPTIONS);
            ConfigUtils.writeConfigBase(root, "ScreenshotSaving", Configs.ScreenshotSaving.OPTIONS);
            ConfigUtils.writeConfigBase(root, "CameraMatrix", CameraMatrix.OPTIONS);

            JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
        }
    }

    @Override
    public void load() {
        loadFromFile();
    }

    @Override
    public void save() {
        saveToFile();
    }
}
