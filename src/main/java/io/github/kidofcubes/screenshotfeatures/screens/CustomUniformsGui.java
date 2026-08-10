package io.github.kidofcubes.screenshotfeatures.screens;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.widgets.*;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.config.ConfigNamedAdjustableDoubleList;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.option.MergedStringOption;
import net.irisshaders.iris.shaderpack.option.OptionSet;
import net.irisshaders.iris.shaderpack.option.ShaderPackOptions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

import java.lang.reflect.Field;
import java.util.*;

public class CustomUniformsGui extends GuiConfigsBase {

    final ConfigNamedAdjustableDoubleList listManager;

    private static Field fieldSearchBox;
    private static boolean reflectionInitialized = false;
    private static boolean reflectionFailed = false;
    private WidgetDropDownList<String> dropdown = null;

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

//        dropdown = new WidgetDropDownList<>(10, 70, 200, 20, height-120, 100, List.of("1","2","3","4","5","6","7","8","9","10"));
        List<String> options = new ArrayList<>(getFloatDefines().keySet());
        List<String> translatedNames = new ArrayList<>(options.size());
        for(int i=0;i<options.size();i++){
            String key = options.get(i);
            String translatedName = StringUtils.translate("option."+key);
            if(translatedName.equals("option."+key)){
                translatedNames.add("");
            }else{
                translatedNames.add(translatedName);
//                options.set(i,translatedName+"|"+key);
            }
        }
        int longest = 10;
        for(int i=0;i<options.size();i++){
            longest=Math.max(longest, this.font.width(translatedNames.get(i)));
        }
        int spaceWidth = this.font.width(" ");
        longest=longest+(spaceWidth*4);
//        List<String> paddedOptions = new ArrayList<>();
        Map<String, String> keyToPaddedDisplay = new HashMap<>();
        int totalLongest = 10;
        for(int i=0;i<options.size();i++){
            int spaces = ((longest-this.font.width(translatedNames.get(i)))/spaceWidth);
            String total = translatedNames.get(i)+(" ".repeat(spaces))+"|    "+options.get(i);
            keyToPaddedDisplay.put(options.get(i), total);

//            String total = String.format("%-"+(longest+3)+"s",translatedNames.get(i))+"|   "+options.get(i);
            totalLongest=Math.max(totalLongest,this.font.width(total));
//            paddedOptions.add(total);
        }
        ;



        dropdown = new WidgetDropDownList<>(75, 70, totalLongest+10, 20, height-120, 100, options){
            @Override
            protected void setSelectedEntry(int index){
                String key = filteredEntries.get(index);
                var pack = Iris.getCurrentPack();
                double defaultValue = 0.0;
                if(pack.isPresent()){
                    String defaultString = pack.get().getShaderPackOptions().getOptionValues().getStringValueOrDefault(key);
                    try{
                        defaultValue=Double.parseDouble(defaultString);
                    }catch(NumberFormatException _){}
                }
                interactedAddedNewEntry(key, defaultValue, true);
//                super.setSelectedEntry(index);
            }

            @Override
            protected String getDisplayString(String entry){
                return keyToPaddedDisplay.get(entry);
            }
        };
        this.addWidget(dropdown);
    }

    private int mouseX=0;
    private int mouseY=0;
    @Override
    public void drawContents(GuiContext ctx,int mouseX,int mouseY,float partialTicks){
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        super.drawContents(ctx,mouseX,mouseY,partialTicks);
    }

    @Override
    public boolean onCharTyped(CharacterEvent input){
        if(dropdown.isMouseOver(mouseX,mouseY)){
            return dropdown.onCharTyped(input);
        }
        return super.onCharTyped(input);
    }

    private int addButtonAt(int x,int y,String label,IButtonActionListener listener) {
        int width = this.getStringWidth(label) + 10;
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, label);
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
            for(int i=0;i<=6;i++){
                wrappers.add(new ConfigOptionWrapper(StringUtils.translate(ScreenshotFeatures.MOD_ID+".gui.customuniforms.placeholdertext"+i)));
            }
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

    private void interactedAddedNewEntry(String key, double value, boolean override){
        ConfigNamedAdjustableDoubleList.NamedEntry entry = listManager.addEntry(key, override);
        if (entry != null) {
            entry.setValue(value);
            String translated = StringUtils.getTranslatedOrFallback("option."+key+".comment", "???");

            // comment code taken from iris
            // Strip any trailing "."s
            if (translated.endsWith(".")) {
                translated = translated.substring(0, translated.length() - 1);
            }
            // Split comment body into lines by separator ". "
            List<MutableComponent> splitByPeriods = Arrays.stream(translated.split("\\. [ ]*")).map(Component::literal).toList();
            // Line wrap
            List<FormattedCharSequence> lines = new ArrayList<>();
            for (MutableComponent text : splitByPeriods) {
                lines.addAll(this.font.split(text, 300));
            }
            StringBuilder total = new StringBuilder();
            total.append(ChatFormatting.PREFIX_CODE).append(ChatFormatting.BOLD);
            total.append(key);
//            total.append(ChatFormatting.PREFIX_CODE).append(ChatFormatting.RESET);
            total.append(ChatFormatting.PREFIX_CODE); //doesn't need the reset char here for some reason??
            total.append("\n");
            for(FormattedCharSequence formattedCharSequence: lines){
                total.append("\n");
                formattedCharSequence.accept((_,_,codePoint) -> {
                    total.appendCodePoint(codePoint);
                    return true;
                });
            }
            total.deleteCharAt(0);
            entry.getConfig().setComment(total.toString());


            clearSearchBarText();
            Configs.saveToFile();
            refreshList();
            dropdown.setSelectedEntry(null);
        }
    }

    // --- Action Listeners ---

    private class AddEntryListener implements IButtonActionListener {
        @Override
        public void actionPerformedWithButton(ButtonBase buttonBase, int mouseButton) {
            String name = getSearchBarText().trim();
            if (!name.isEmpty()) {
                interactedAddedNewEntry(name, 0.0, false);
            }
        }
    }
    public static Map<String,Float> getFloatDefines() {
        if(Iris.getCurrentPack().isPresent()){
            ShaderPackOptions options = Iris.getCurrentPack().get().getShaderPackOptions();
            OptionSet optionSet = options.getOptionSet();
            Map<String,Float> floatDefines = new HashMap<>();
            for(Map.Entry<String,MergedStringOption> entry : optionSet.getStringOptions().entrySet()){
                String key = entry.getKey();
//                boolean isNumber = false;
//                for(String allowedValue: value.getOption().getAllowedValues()){
//                    if(allowedValue.matches("-?\\d+(\\.\\d+)?")){
//                        isNumber=true;
//                        break;
//                    }
//                }
//                if(isNumber){
                    try {
                        floatDefines.put(key,Float.valueOf(options.getOptionValues().getStringValueOrDefault(key)));
                    }catch(NumberFormatException _){}
//                }
            }
            return floatDefines;
        }else{
            return new HashMap<>();
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
