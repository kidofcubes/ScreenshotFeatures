package io.github.kidofcubes.screenshotfeatures.screens;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.gui.widgets.WidgetSearchBar;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.config.ConfigNamedAdjustableDoubleList;
import io.github.kidofcubes.screenshotfeatures.config.Configs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CustomUniformsGui extends GuiConfigsBase {

    final ConfigNamedAdjustableDoubleList listManager;

    private static Field fieldSearchBox;
    private static boolean reflectionInitialized = false;
    private static boolean reflectionFailed = false;

    public CustomUniformsGui(ConfigNamedAdjustableDoubleList listManager) {
        super(10, 90, ScreenshotFeatures.MOD_ID, null,
                ScreenshotFeatures.MOD_ID + ".gui.title.customuniforms");
        this.listManager = listManager;
    }

    @Override
    protected boolean useKeybindSearch() {
        return true;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        // Tab buttons from the main config GUI
        ConfigsGui.createTabButtons(this, 10, 26);

        addButtonAt(10, 70, StringUtils.translate(ScreenshotFeatures.MOD_ID+".gui.customuniforms.buttons.addentry"), new AddEntryListener());
    }

    private int addButtonAt(int x, int y, String label, IButtonActionListener listener) {
        int width = this.getStringWidth(label) + 10;
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, label);
        button.setHoverStrings(StringUtils.translate(ScreenshotFeatures.MOD_ID+".gui.customuniforms.buttons.addentry.hover"));
        this.addButton(button, listener);
        return button.getWidth() + 4;
    }

    @Override
    protected WidgetListConfigOptions createListWidget(int listX, int listY) {
        return new CustomUniformsListWidget(listX, listY,
                this.getBrowserWidth(), this.getBrowserHeight(), this.getConfigWidth(), 0.f, false, this);
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<IConfigBase> entries = listManager.getEntriesAsConfigBases();
        if (entries.isEmpty()) {
            List<ConfigOptionWrapper> wrappers = new ArrayList<>();
            wrappers.add(new ConfigOptionWrapper(StringUtils.translate(ScreenshotFeatures.MOD_ID+".gui.customuniforms.placeholdertext0")));
            wrappers.add(new ConfigOptionWrapper(StringUtils.translate(ScreenshotFeatures.MOD_ID+".gui.customuniforms.placeholdertext1")));
            wrappers.add(new ConfigOptionWrapper(StringUtils.translate(ScreenshotFeatures.MOD_ID+".gui.customuniforms.placeholdertext2")));
            return wrappers;
        }
        return ConfigOptionWrapper.createFor(entries);
    }

    /**
     * Gets the raw search bar text via reflection on WidgetSearchBar.searchBox.
     */
    private String getSearchBarText() {
        try {
            if (this.getListWidget() == null) return "";
            WidgetSearchBar searchBar = this.getListWidget().getSearchBarWidget();
            if (searchBar == null) return "";

            if (!reflectionInitialized) {
                initReflection();
            }
            if (reflectionFailed) return "";

            net.minecraft.client.gui.components.EditBox searchBox =
                    (net.minecraft.client.gui.components.EditBox) fieldSearchBox.get(searchBar);
            if (searchBox == null) return "";

            return searchBox.getValue();
        } catch (Exception e) {
            reflectionFailed = true;
            ScreenshotFeatures.LOGGER.warn("Failed to access search bar text via reflection", e);
            return "";
        }
    }

    /**
     * Clears the search bar text via reflection.
     */
    private void clearSearchBarText() {
        try {
            if (this.getListWidget() == null) return;
            WidgetSearchBar searchBar = this.getListWidget().getSearchBarWidget();
            if (searchBar == null) return;

            if (!reflectionInitialized) {
                initReflection();
            }
            if (reflectionFailed) return;

            net.minecraft.client.gui.components.EditBox searchBox =
                    (net.minecraft.client.gui.components.EditBox) fieldSearchBox.get(searchBar);
            if (searchBox == null) return;

            searchBox.setValue("");
        } catch (Exception e) {
            reflectionFailed = true;
            ScreenshotFeatures.LOGGER.warn("Failed to clear search bar text via reflection", e);
        }
    }

    private static void initReflection() {
        try {
            // WidgetSearchBar has protected field: searchBox (GuiTextFieldGeneric extends EditBox)
            fieldSearchBox = WidgetSearchBar.class.getDeclaredField("searchBox");
            fieldSearchBox.setAccessible(true);
            reflectionInitialized = true;
        } catch (Exception e) {
            reflectionFailed = true;
            ScreenshotFeatures.LOGGER.warn("Failed to initialize reflection for search bar access", e);
        }
    }

    void refreshList() {
        if (this.getListWidget() != null) {
            this.getListWidget().refreshEntries();
        }
    }

    // --- Action Listeners ---

    private class AddEntryListener implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase buttonBase, int mouseButton) {
            String name = getSearchBarText().trim();
            if (!name.isEmpty()) {
                ConfigNamedAdjustableDoubleList.NamedEntry entry = listManager.addEntry(name, false);
                if (entry != null) {
                    clearSearchBarText();
                    Configs.saveToFile();
                    refreshList();
                }
            }
        }
    }

    /**
     * Custom WidgetConfigOption that adds an X (delete) button next to each config row.
     */
    private static class CustomUniformsConfigOption extends WidgetConfigOption {
        public CustomUniformsConfigOption(int x, int y, int width, int height,
                                          int labelWidth, int configWidth,
                                          ConfigOptionWrapper wrapper, int listIndex,
                                          IKeybindConfigGui host,
                                          WidgetListConfigOptionsBase<?, ?> parent,
                                          CustomUniformsGui parentGui) {
            super(x, y, width, height, labelWidth, configWidth,
                    wrapper, listIndex, host, parent);

            // Add X button for config entries (not label-only entries)
            if (wrapper.getType() == ConfigOptionWrapper.Type.CONFIG && wrapper.getConfig() != null) {
                String entryName = wrapper.getConfig().getName();
                int btnX = x + width - 22;
//                int btnX = x + 300;
                int btnY = y + 1;
                ButtonGeneric deleteBtn = new ButtonGeneric(btnX, btnY, -1, 18, "X");
                this.addButton(deleteBtn, new DeleteEntryListener(parentGui, entryName));
                btnX-=100;
                ButtonGeneric toggleOverride = new ButtonGeneric(btnX, btnY, -1, 18, "override: "+parentGui.listManager.getEntry(entryName).override){
                    @Override
                    public void updateDisplayString() {
                        this.displayString = "override: "+parentGui.listManager.getEntry(entryName).override;
                    }
                };
                this.addButton(toggleOverride,(_,_) -> {
                    parentGui.listManager.getEntry(entryName).override = !parentGui.listManager.getEntry(entryName).override;
                    toggleOverride.updateDisplayString();
                });
            }
        }
    }

    /**
     * Custom list widget that uses CustomUniformsConfigOption with X buttons.
     */
    private static class CustomUniformsListWidget extends WidgetListConfigOptions {
        private final CustomUniformsGui parentGui;

        public CustomUniformsListWidget(int x, int y, int width, int height, int configWidth,
                                        float zLevel, boolean useKeybindSearch, CustomUniformsGui parent) {
            super(x, y, width, height, configWidth, zLevel, useKeybindSearch, parent);
            this.parentGui = parent;
        }

        @Override
        protected WidgetConfigOption createListEntryWidget(int x, int y, int listIndex,
                                                           boolean isOdd, ConfigOptionWrapper wrapper) {
            return new CustomUniformsConfigOption(x, y, this.browserEntryWidth,
                    this.browserEntryHeight, this.maxLabelWidth, this.configWidth,
                    wrapper, listIndex, this.parent, this, parentGui);
        }
    }

    /**
     * Listener for the X (delete) button on each row.
     */
    private static class DeleteEntryListener implements IButtonActionListener {
        private final CustomUniformsGui gui;
        private final String entryName;

        DeleteEntryListener(CustomUniformsGui gui, String entryName) {
            this.gui = gui;
            this.entryName = entryName;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase buttonBase, int mouseButton) {
            if (gui.listManager.removeEntry(entryName)) {
                Configs.saveToFile();
                gui.refreshList();
            }
        }
    }
}
