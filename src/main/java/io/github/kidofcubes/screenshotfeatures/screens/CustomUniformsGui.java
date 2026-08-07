package io.github.kidofcubes.screenshotfeatures.screens;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.config.ConfigNamedAdjustableDoubleList;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.minecraft.client.input.CharacterEvent;

import java.util.ArrayList;
import java.util.List;

public class CustomUniformsGui extends GuiConfigsBase {

    private final ConfigNamedAdjustableDoubleList listManager;
    private GuiMode guiMode = GuiMode.NORMAL;
    private GuiTextFieldGeneric nameField;

    public CustomUniformsGui(ConfigNamedAdjustableDoubleList listManager) {
        super(10, 50, ScreenshotFeatures.MOD_ID, null,
                ScreenshotFeatures.MOD_ID + ".gui.title.customuniforms");
        this.listManager = listManager;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        // Tab buttons from the main config GUI
        ConfigsGui.createTabButtons(this, 10, 26);

        // Management buttons below the tabs
        int buttonY = 50;
        int buttonX = 10;

        switch (guiMode) {
            case NORMAL -> {
                buttonX += addButtonAt(buttonX, buttonY, "Add Entry", new AddEntryListener());
                addButtonAt(buttonX, buttonY, "Remove Entry", new RemoveEntryListener());
            }
            case ADD_ENTRY -> {
                nameField = new GuiTextFieldGeneric(buttonX, buttonY, 150, 20, this.font);
                nameField.setMaxLength(64);
                nameField.setSuggestion("Uniform name...");
                this.addTextField(nameField, null);

                buttonX += 155;
                buttonX += addButtonAt(buttonX, buttonY, "Confirm Add", new ConfirmAddListener());
                addButtonAt(buttonX, buttonY, "Cancel", new CancelListener());
            }
            case REMOVE_ENTRY -> {
                nameField = new GuiTextFieldGeneric(buttonX, buttonY, 150, 20, this.font);
                nameField.setMaxLength(64);
                nameField.setSuggestion("Uniform name...");
                this.addTextField(nameField, null);

                buttonX += 155;
                buttonX += addButtonAt(buttonX, buttonY, "Confirm Remove", new ConfirmRemoveListener());
                addButtonAt(buttonX, buttonY, "Cancel", new CancelListener());
            }
        }
    }

    private int addButtonAt(int x, int y, String labelKey, IButtonActionListener listener) {
        String label = StringUtils.translate(ScreenshotFeatures.MOD_ID + ".gui.button." + labelKey.toLowerCase().replace(" ", ""));
        // Fallback if no translation is defined
        if (label.equals(ScreenshotFeatures.MOD_ID + ".gui.button." + labelKey.toLowerCase().replace(" ", ""))) {
            label = labelKey;
        }
        int width = this.getStringWidth(label) + 10;
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, label);
        this.addButton(button, listener);
        return button.getWidth() + 4;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<IConfigBase> entries = listManager.getEntriesAsConfigBases();
        if (entries.isEmpty()) {
            // Show a label indicating no entries
            List<ConfigOptionWrapper> wrappers = new ArrayList<>();
            wrappers.add(new ConfigOptionWrapper("No custom uniforms defined. Click 'Add Entry' to create one."));
            return wrappers;
        }
        return ConfigOptionWrapper.createFor(entries);
    }

    private void switchToMode(GuiMode mode) {
        this.guiMode = mode;
        // Re-initialize the GUI to reflect the new mode
        this.initGui();
    }

    private enum GuiMode {
        NORMAL,
        ADD_ENTRY,
        REMOVE_ENTRY
    }

    @Override
    public boolean onCharTyped(CharacterEvent input){
        if(this.nameField.active){
            return this.nameField.charTyped(input);
        }
        return super.onCharTyped(input);
    }

    // --- Action Listeners ---

    private class AddEntryListener implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase buttonBase, int mouseButton) {
            switchToMode(GuiMode.ADD_ENTRY);
        }
    }

    private class RemoveEntryListener implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase buttonBase, int mouseButton) {
            switchToMode(GuiMode.REMOVE_ENTRY);
        }
    }

    private class CancelListener implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase buttonBase, int mouseButton) {
            switchToMode(GuiMode.NORMAL);
        }
    }

    private class ConfirmAddListener implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase buttonBase, int mouseButton) {
            if (nameField != null) {
                String name = nameField.getValue().trim();
                if (!name.isEmpty()) {
                    ConfigNamedAdjustableDoubleList.NamedEntry entry = listManager.addEntry(name);
                    if (entry != null) {
                        // Successfully added, save config
                        Configs.saveToFile();
                    }
                }
            }
            switchToMode(GuiMode.NORMAL);
        }
    }

    private class ConfirmRemoveListener implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase buttonBase, int mouseButton) {
            if (nameField != null) {
                String name = nameField.getValue().trim();
                if (!name.isEmpty()) {
                    listManager.removeEntry(name);
                    // Save config after removal
                    Configs.saveToFile();
                }
            }
            switchToMode(GuiMode.NORMAL);
        }
    }
}
