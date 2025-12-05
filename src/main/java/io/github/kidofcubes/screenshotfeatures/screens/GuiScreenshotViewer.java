package io.github.kidofcubes.screenshotfeatures.screens;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.metadata.ScreenshotTagger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class GuiScreenshotViewer extends GuiListBase<GuiScreenshotViewer.MetadataEntry, GuiScreenshotViewer.MetadataEntryWidget, WidgetListBase<GuiScreenshotViewer.MetadataEntry, GuiScreenshotViewer.MetadataEntryWidget>> implements ISelectionListener<GuiScreenshotViewer.MetadataEntry> {
    public GuiScreenshotViewer() {
        this(10,88);
    }
    public GuiScreenshotViewer(int listX, int listY) {
        super(listX, listY);
    }
    @Override
    public void initGui()
    {
        ConfigsGui.tab = ConfigsGui.ConfigGuiTab.SCREENSHOTVIEWER;

        super.initGui();

        title = StringUtils.translate("screenshotfeatures.gui.title.screenshotviewer");

        this.clearWidgets();
        this.clearButtons();
        this.createTabButtons();
        this.getListWidget().refreshEntries();
        this.addLabel(18,53,-1,14,0xFFFFFFFF,StringUtils.translate("screenshotfeatures.gui.description.screenshotviewer0"));
        this.addLabel(18,68,-1,14,0xFFFFFFFF,StringUtils.translate("screenshotfeatures.gui.description.screenshotviewer1"));
    }
    private final List<MetadataEntry> metadata = new ArrayList<>();

    @Override
    public void onFilesDropped(List<Path> paths) {
        Path path = paths.get(0);
        if(!path.getFileName().toString().endsWith(".png")){
            return;
        }
        metadata.clear();
        try{
            JsonObject values = ScreenshotTagger.getScreenshotTags(path.toFile());
            visit(values, new ArrayList<>(), (jsonPath,value) -> {
                metadata.add(new MetadataEntry(String.join(".",jsonPath),value.toString()));
            });
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        this.getListWidget().refreshEntries();
    }
    private void visit(JsonObject jsonObject, List<String> path, BiConsumer<List<String>,JsonElement> visitor){
        for(Map.Entry<String,JsonElement> entry: jsonObject.entrySet()){
            path.add(entry.getKey());
            if(entry.getValue().isJsonObject()){
                visit(entry.getValue().getAsJsonObject(), path, visitor);
            }else{
                visitor.accept(path,entry.getValue());
            }
            path.removeLast();
        }
    }


    private void createTabButtons(){
        int x = 10;
        int y = 26;
        int rows = 1;

        for (ConfigsGui.ConfigGuiTab tab : ConfigsGui.ConfigGuiTab.values())
        {
            int width = this.getStringWidth(tab.getDisplayName()) + 10;

            if (x >= this.width - width - 10)
            {
                x = 10;
                y += 22;
                ++rows;
            }

            x += this.createTabButton(x, y, width, tab);
        }
    }
    protected int createTabButton(int x, int y, int width, ConfigsGui.ConfigGuiTab tab) {
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.getDisplayName());
        button.setEnabled(ConfigsGui.tab != tab);
        this.addButton(button, new IButtonActionListener() {
            @Override
            public void actionPerformedWithButton(ButtonBase buttonBase, int i) {
                ConfigsGui.tab = tab;
                GuiBase.openGui(new ConfigsGui());
            }
        });

        return button.getWidth() + 2;
    }

    @Override
    protected WidgetListBase<MetadataEntry, MetadataEntryWidget> createListWidget(int listX, int listY) {
        return new MetaDataEntryWidgetList(listX,listY,getBrowserWidth(),getBrowserHeight(),this) {

        };
    }
    public class MetaDataEntryWidgetList extends WidgetListBase<MetadataEntry, MetadataEntryWidget> {
        public MetaDataEntryWidgetList(int x, int y, int width, int height, @Nullable ISelectionListener<MetadataEntry> selectionListener) {
            super(x, y, width, height, selectionListener);
            browserEntryHeight = 20;
        }

        @Override
        protected Collection<MetadataEntry> getAllEntries() {
            return metadata;
        }

        @Override
        protected MetadataEntryWidget createListEntryWidget(int x, int y, int listIndex, boolean isOdd, MetadataEntry entry) {
            return new MetadataEntryWidget(x,y,browserEntryWidth,getBrowserEntryHeightFor(entry),entry,listIndex);
        }
    }


    @Override
    protected int getBrowserWidth() {
        return this.width - 20;
    }

    @Override
    protected int getBrowserHeight() {
        return this.height - this.getListY() - 6;
    }

    @Override
    public void onSelectionChange(@Nullable GuiScreenshotViewer.MetadataEntry metadataEntry) {

    }

    public static record MetadataEntry(String key, String value){}
    public static class MetadataEntryWidget extends WidgetListEntryBase<MetadataEntry> {

        public MetadataEntryWidget(int x, int y, int width, int height, @Nullable GuiScreenshotViewer.MetadataEntry entry, int listIndex) {
            super(x, y, width, height, entry, listIndex);
            ButtonGeneric button = new ButtonGeneric(x, y, -1, 20, entry.key,entry.value);
            this.addButton(button, new IButtonActionListener() {
                @Override
                public void actionPerformedWithButton(ButtonBase buttonBase, int i) {
                    ScreenshotFeatures.client.keyboard.setClipboard(entry.value);

                }
            });
//            int color = TextColor.parse("#ffffff").getOrThrow().getRgb();
//            this.addLabel(x,y,width,height, color, entry.key+"\n"+entry.value);
            this.addLabel(x+button.getWidth()+1,y,-1,20, 0xFFFFFFFF, entry.value);
//            this.getStringWidth()
//            this.drawCenteredString(this.,x,y,,"what test stuff");
        }
    }
}
