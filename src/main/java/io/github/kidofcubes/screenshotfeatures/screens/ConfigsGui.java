package io.github.kidofcubes.screenshotfeatures.screens;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.config.ConfigNamedAdjustableDoubleList;
import io.github.kidofcubes.screenshotfeatures.config.Configs;

import java.util.Collections;
import java.util.List;

public class ConfigsGui extends GuiConfigsBase {
    public static ConfigGuiTab tab = ConfigGuiTab.INGAMETOOLS;

    public ConfigsGui()
    {
        super(10, 50, ScreenshotFeatures.MOD_ID, null, ScreenshotFeatures.MOD_ID+".gui.title.configs", String.format("%s", "version"));
    }

    @Override
    public void initGui() {
        if(tab == ConfigGuiTab.SCREENSHOT_VIEWER){
            GuiBase.openGui(new GuiScreenshotViewer());
            return;
        }else if(tab == ConfigGuiTab.CAMERA_MATRIX_EDITOR){
            GuiBase.openGui(new CameraMatrixEditorGui());
            return;
        }else if(tab == ConfigGuiTab.SHADER_OPTIONS){
            GuiBase.openGui(new CustomUniformsGui(Configs.ShaderOptions.CUSTOM_UNIFORMS));
            return;
        }
        super.initGui();

        this.clearOptions();

        createTabButtons(this, 10, 26);
    }



    public static void createTabButtons(GuiBase gui, int x, int y){
        int rows = 1;

        for (ConfigGuiTab tab : ConfigGuiTab.values())
        {
            int width = gui.getStringWidth(tab.getDisplayName()) + 10;

            if (x >= gui.width - width - 10)
            {
                x = 10;
                y += 22;
                ++rows;
            }

            x += createTabButton(gui, x, y, width, tab);
        }
    }
    public static int createTabButton(GuiBase gui, int x, int y, int width, ConfigGuiTab tab) {
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.getDisplayName());
        button.setEnabled(ConfigsGui.tab != tab);
        gui.addButton(button, new IButtonActionListener() {
            @Override
            public void actionPerformedWithButton(ButtonBase buttonBase, int i) {
                ConfigsGui.tab = tab;
                GuiBase.openGui(new ConfigsGui());
            }
        });

        return button.getWidth() + 2;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs()
    {
        List<? extends IConfigBase> configs;
        ConfigGuiTab tab = ConfigsGui.tab;

        configs = switch(tab){
            case INGAMETOOLS -> Configs.IngameTools.OPTIONS;
            case METADATA -> Configs.Metadata.OPTIONS;
            case SHADER_OPTIONS -> Configs.ShaderOptions.OPTIONS;
//            case ORTHOCAMERAINTEGRATION -> Configs.CameraMatrix.OPTIONS;
            default -> Collections.emptyList();
        };

        return ConfigOptionWrapper.createFor(configs);
    }

    public enum ConfigGuiTab
    {
        INGAMETOOLS ("screenshotfeatures.gui.title.ingametools"),
        METADATA ("screenshotfeatures.gui.title.metadata"),
        SCREENSHOTSAVING ("screenshotfeatures.gui.title.screenshotsaving"),
        SCREENSHOT_VIEWER("screenshotfeatures.gui.title.screenshotviewer"),
//        ORTHOCAMERAINTEGRATION ("screenshotfeatures.gui.title.orthocameraintegration"),
        CAMERA_MATRIX_EDITOR("screenshotfeatures.gui.title.cameramatrixeditor"),
        SHADER_OPTIONS ("screenshotfeatures.gui.title.shaderoptions");

        private final String translationKey;

        private ConfigGuiTab(String translationKey)
        {
            this.translationKey = translationKey;
        }

        public String getDisplayName()
        {
            return StringUtils.translate(this.translationKey);
        }
    }
}
