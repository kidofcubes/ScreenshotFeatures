package io.github.kidofcubes.screenshotfeatures.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.config.gui.ButtonPressDirtyListenerSimple;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerButton;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerTextField;
import fi.dy.masa.malilib.config.gui.ConfigOptionListenerResetConfig;
import fi.dy.masa.malilib.config.options.BooleanHotkeyGuiWrapper;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonBoolean;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import io.github.kidofcubes.screenshotfeatures.config.ConfigDoubleHotkeyed;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = WidgetConfigOption.class,remap = false)
public abstract class WidgetConfigOptionMixin extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper> {
    private WidgetConfigOptionMixin(int x,int y,int width,int height,WidgetListConfigOptionsBase<?,?> parent,GuiConfigsBase.ConfigOptionWrapper entry,int listIndex){
        super(x,y,width,height,parent,entry,listIndex);
    }
    @Final
    @Shadow
    protected IKeybindConfigGui host;

    @Shadow
    protected abstract void addConfigTextFieldEntry(int x,int y,int resetX,int configWidth,int configHeight,IConfigValue config);

    // TODO(Ravel): remapper for com.llamalad7.mixinextras.expression.Expression is not implemented
// TODO(Ravel): remapper for com.llamalad7.mixinextras.expression.Expression is not implemented
    @Expression("? instanceof ?")
    @Inject(method="addConfigOption", at=@At(value="MIXINEXTRAS:EXPRESSION", ordinal = 0), cancellable = true)
    void customConfigOptions(int x,int y,int labelWidth,int configWidth,IConfigBase config,CallbackInfo ci,@Local(name = "configHeight") int configHeight){
        if (config instanceof ConfigDoubleHotkeyed configDoubleHotkeyed) {
//            this.addDoubleAndModifierAndHotkeyWidgets(x, y, configWidth, configDoubleHotkeyed, configDoubleHotkeyed.getKeybind());
            int mainWidth = 88;
            int keybindX = x+mainWidth+-2;
            int keybindWidth = 64;
            int resetX = x + configWidth + 2;
            this.addConfigTextFieldEntry(x, y, resetX, mainWidth, configHeight, (IConfigValue)config);
            ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(keybindX, y, keybindWidth, 20, configDoubleHotkeyed.getKeybind(), this.host);

            GuiTextFieldGeneric field = this.createTextField(x, y + 1, configWidth - 4, configHeight - 3);
            field.setMaxLength(this.maxTextfieldTextLength);
            field.setValue(configDoubleHotkeyed.getStringValue());
            ButtonGeneric resetButton = this.createResetButton(x, y, configDoubleHotkeyed);
            ConfigOptionChangeListenerTextField listenerChange = new ConfigOptionChangeListenerTextField(configDoubleHotkeyed, field, resetButton);
            this.addTextField(field, listenerChange);

            this.addButton(keybindButton, this.host.getButtonPressListener());
            ci.cancel();
        }
    }

    void addDoubleAndModifierAndHotkeyWidgets(int x,int y,int configWidth,IConfigDouble doubleConfig,IKeybind keybind) {

//        int booleanBtnWidth = 60;
//        ConfigButtonBoolean booleanButton = new ConfigButtonBoolean(x, y, booleanBtnWidth, 20, doubleConfig);
//        x += booleanBtnWidth + 2;
//        configWidth -= booleanBtnWidth + 2 + 22;
//        ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(x, y, configWidth, 20, keybind, this.host);
//        x += configWidth + 2;
//        this.addWidget(new WidgetKeybindSettings(x, y, 20, 20, keybind, doubleConfig.getName(), this.parent, this.host.getDialogHandler()));
//        x += 22;
//        ButtonGeneric resetButton = this.createResetButton(x, y, resettableConfig);
//        ConfigOptionChangeListenerButton booleanChangeListener = new ConfigOptionChangeListenerButton(resettableConfig, resetButton, (ButtonPressDirtyListenerSimple)null);
//        WidgetConfigOption.HotkeyedBooleanResetListener resetListener = new WidgetConfigOption.HotkeyedBooleanResetListener(resettableConfig, booleanButton, keybindButton, resetButton, this.host);
//        IKeybindConfigGui var10000 = this.host;
//        Objects.requireNonNull(resetListener);
//        var10000.addKeybindChangeListener(resetListener::updateButtons);
//        this.addButton(booleanButton, booleanChangeListener);
//        this.addButton(keybindButton, this.host.getButtonPressListener());
//        this.addButton(resetButton, resetListener);
    }
}
