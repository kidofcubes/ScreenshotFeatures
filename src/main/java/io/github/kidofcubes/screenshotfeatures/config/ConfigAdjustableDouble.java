package io.github.kidofcubes.screenshotfeatures.config;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.config.IConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.hotkeys.*;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;

public class ConfigAdjustableDouble extends ConfigDouble implements IHotkey {
    public static final Codec<ConfigAdjustableDouble> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    PrimitiveCodec.STRING.fieldOf("name").forGetter(ConfigBase::getName),
                    PrimitiveCodec.DOUBLE.fieldOf("defaultValue").forGetter(ConfigDouble::getDefaultDoubleValue),
                    PrimitiveCodec.DOUBLE.fieldOf("value").forGetter(ConfigDouble::getDoubleValue),
                    PrimitiveCodec.STRING.fieldOf("defaultHotkey").forGetter((get) -> get.keybind.getDefaultStringValue()),
                    PrimitiveCodec.DOUBLE.fieldOf("minValue").forGetter(ConfigDouble::getDoubleValue),
                    PrimitiveCodec.DOUBLE.fieldOf("maxValue").forGetter(ConfigDouble::getDoubleValue),
//                    PrimitiveCodec.DOUBLE.fieldOf("multiplier").forGetter(ConfigDouble::getDoubleValue),
                    KeybindSettings.CODEC.fieldOf("keybindSettings").forGetter((get) -> get.keybind.getSettings()),
                    PrimitiveCodec.STRING.fieldOf("comment").forGetter((get) -> get.comment),
                    PrimitiveCodec.STRING.fieldOf("prettyName").forGetter((get) -> get.prettyName),
                    PrimitiveCodec.STRING.fieldOf("translatedName").forGetter((get) -> get.translatedName)
            ).apply(instance, ConfigAdjustableDouble::new)
    );
    protected final IKeybind keybind;
    protected double step;

    public ConfigAdjustableDouble(String name,double defaultValue,String defaultHotkey) {
        this(name, defaultValue, defaultHotkey, Double.MIN_VALUE, Double.MAX_VALUE, 1.0, KeybindSettings.DEFAULT, name + " Comment?", StringUtils.splitCamelCase(name), name);
    }

    public ConfigAdjustableDouble(String name,double defaultValue,String defaultHotkey, double multiplier) {
        this(name, defaultValue, defaultHotkey, Double.MIN_VALUE, Double.MAX_VALUE, multiplier, KeybindSettings.DEFAULT, name + " Comment?", StringUtils.splitCamelCase(name), name);
    }

    public ConfigAdjustableDouble(String name,double defaultValue,String defaultHotkey,double minValue,double maxValue) {
        this(name, defaultValue, defaultHotkey, minValue, maxValue, 1.0, KeybindSettings.DEFAULT, name + " Comment?", StringUtils.splitCamelCase(name), name);
    }

    public ConfigAdjustableDouble(String name,double defaultValue,String defaultHotkey,double minValue,double maxValue,double multiplier) {
        this(name, defaultValue, defaultHotkey, minValue, maxValue, multiplier, KeybindSettings.DEFAULT, name + " Comment?", StringUtils.splitCamelCase(name), name);
    }

    public ConfigAdjustableDouble(String name,double defaultValue,String defaultHotkey,double minValue,double maxValue,double multiplier,String comment) {
        this(name, defaultValue, defaultHotkey, minValue, maxValue, multiplier, KeybindSettings.DEFAULT, comment, StringUtils.splitCamelCase(name), name);
    }

    public ConfigAdjustableDouble(String name,double defaultValue,String defaultHotkey,double minValue,double maxValue,double multiplier,String comment,String prettyName) {
        this(name, defaultValue, defaultHotkey, minValue, maxValue, multiplier, KeybindSettings.DEFAULT, comment, prettyName, name);
    }

    public ConfigAdjustableDouble(String name,double defaultValue,String defaultHotkey,double minValue,double maxValue,double multiplier,String comment,String prettyName,String translatedName) {
        this(name, defaultValue, defaultHotkey, minValue, maxValue, multiplier,  KeybindSettings.DEFAULT, comment, prettyName, translatedName);
    }

    public ConfigAdjustableDouble(String name,double defaultValue,String defaultHotkey,KeybindSettings settings) {
        this(name, defaultValue, defaultHotkey, Double.MIN_VALUE, Double.MAX_VALUE, 1.0, settings, name + " Comment?", StringUtils.splitCamelCase(name), name);
    }

    public ConfigAdjustableDouble(String name,double defaultValue,String defaultHotkey,double minValue,double maxValue,double multiplier,KeybindSettings settings,String comment) {
        this(name, defaultValue, defaultHotkey, minValue, maxValue, multiplier, settings, comment, StringUtils.splitCamelCase(name), name);
    }

    public ConfigAdjustableDouble(String name,double defaultValue,String defaultHotkey,double minValue,double maxValue,double multiplier,KeybindSettings settings,String comment,String prettyName) {
        this(name, defaultValue, defaultHotkey, minValue, maxValue, multiplier, settings, comment, prettyName, name);
    }

    public ConfigAdjustableDouble(String name,double defaultValue,String defaultHotkey,double minValue,double maxValue,double multiplier,KeybindSettings settings,String comment,String prettyName,String translatedName) {
        super(name, defaultValue, minValue, maxValue, comment, prettyName, translatedName);
        this.keybind = KeybindMulti.fromStorageString(defaultHotkey, settings);
        ScreenshotFeatures.adjustableValues.add(this);
        this.step = multiplier;
//        this.keybind.setCallback((keyAction,iKeybind) -> {});
    }

    private ConfigAdjustableDouble(String name,double defaultValue,double value,String defaultHotkey,double minValue,double maxValue,KeybindSettings settings,String comment,String prettyName,String translatedName) {
        this(name, defaultValue, defaultHotkey, minValue, maxValue, 1.0, settings, comment, prettyName, translatedName);
        this.setDoubleValue(value);
    }

    @Override
    public IKeybind getKeybind() {
        return this.keybind;
    }

    public String getDefaultHotkey() {
        return this.keybind.getDefaultStringValue();
    }
    @Override
    public boolean isModified() {
        return super.isModified() || this.getKeybind().isModified();
    }

    @Override
    public boolean isDirty() {
        return this.getKeybind().isDirty() || super.isDirty();
    }

    @Override
    public void markDirty(){
        super.markDirty();
        this.getKeybind().markDirty();
    }

    @Override
    public void markClean(){
        super.markClean();
        this.getKeybind().markClean();
    }

    @Override
    public void checkIfClean(){
        if (this.isDirty())
        {
            this.markClean();
            this.onValueChanged();
        }
    }

    @Override
    public void resetToDefault() {
        super.resetToDefault();
//        this.keybind.resetToDefault();
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        try {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (JsonUtils.hasDouble(obj, "value")) {
                    super.setValueFromJsonElement(obj.get("value"));
                }

                if (JsonUtils.hasObject(obj, "hotkey")) {
                    JsonObject hotkeyObj = obj.getAsJsonObject("hotkey");
                    this.keybind.setValueFromJsonElement(hotkeyObj);
                }
            } else {
                super.setValueFromJsonElement(element);
            }
        } catch (Exception e) {
            MaLiLib.LOGGER.warn("Failed to set config value for '{}' from the JSON element '{}'", this.getName(), element, e);
        }
    }

    @Override
    public JsonElement getAsJsonElement() {
        JsonObject obj = new JsonObject();
        obj.add("value", super.getAsJsonElement());
        obj.add("hotkey", this.getKeybind().getAsJsonElement());
        return obj;
    }
}

