package io.github.kidofcubes.screenshotfeatures.screens;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
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
        if(tab == ConfigGuiTab.SCREENSHOTVIEWER){
            GuiBase.openGui(new GuiScreenshotViewer());
            return;
        }
        super.initGui();

        this.clearOptions();

        int x = 10;
        int y = 26;

        for (ConfigGuiTab tab : ConfigGuiTab.values())
        {
            x += this.createButton(x, y, -1, tab) + 2;
        }
    }

    private int createButton(int x, int y, int width, ConfigGuiTab tab)
    {
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.getDisplayName());
        button.setEnabled(ConfigsGui.tab != tab);
        this.addButton(button, new ButtonListener(tab, this));

        return button.getWidth();
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs()
    {
        List<? extends IConfigBase> configs;
        ConfigGuiTab tab = ConfigsGui.tab;

        if (tab == ConfigGuiTab.INGAMETOOLS) {
            configs = Configs.IngameTools.OPTIONS;
        } else if (tab==ConfigGuiTab.METADATA) {
            configs = Configs.Metadata.OPTIONS;
        }else if (tab==ConfigGuiTab.SCREENSHOTSAVING) {
            configs = Configs.ScreenshotSaving.OPTIONS;
        } else if (tab==ConfigGuiTab.ORTHOCAMERAINTEGRATION) {
            configs = Configs.OrthoCameraIntegration.OPTIONS;
        }else{
            return Collections.emptyList();
        }

        return ConfigOptionWrapper.createFor(configs);
    }

    private static class ButtonListener implements IButtonActionListener
    {
        private final ConfigsGui parent;
        private final ConfigGuiTab tab;

        public ButtonListener(ConfigGuiTab tab, ConfigsGui parent) {
            this.tab = tab;
            this.parent = parent;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            ConfigsGui.tab = this.tab;
            if(this.tab == ConfigGuiTab.SCREENSHOTVIEWER){
                GuiBase.openGui(new GuiScreenshotViewer());
                return;
            }

            this.parent.reCreateListWidget(); // apply the new config width
            this.parent.getListWidget().resetScrollbarPosition();
            this.parent.initGui();
        }
    }

    public enum ConfigGuiTab
    {
        INGAMETOOLS ("screenshotfeatures.gui.title.ingametools"),
        METADATA ("screenshotfeatures.gui.title.metadata"),
        SCREENSHOTSAVING ("screenshotfeatures.gui.title.screenshotsaving"),
        SCREENSHOTVIEWER ("screenshotfeatures.gui.title.screenshotviewer"),
        ORTHOCAMERAINTEGRATION ("screenshotfeatures.gui.title.orthocameraintegration");

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
