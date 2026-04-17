package io.github.kidofcubes.screenshotfeatures.mixin;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.config.gui.ButtonPressDirtyListenerSimple;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerTextField;
import fi.dy.masa.malilib.config.gui.ConfigOptionListenerResetConfig;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldType;
import io.github.kidofcubes.screenshotfeatures.config.ConfigAdjustableDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = WidgetConfigOption.class,remap = false)
public abstract class WidgetConfigOptionMixin extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper> {
    private WidgetConfigOptionMixin(int x,int y,int width,int height,WidgetListConfigOptionsBase<?,?> parent,GuiConfigsBase.ConfigOptionWrapper entry,int listIndex){
        super(x,y,width,height,parent,entry,listIndex);
    }
    @Final
    @Shadow
    protected IKeybindConfigGui host;

    @Expression("? instanceof ?")
    @Inject(method="addConfigOption", at=@At(value="MIXINEXTRAS:EXPRESSION", ordinal = 0), cancellable = true)
    void customConfigOptions(int x,int y,int labelWidth,int configWidth,IConfigBase config,CallbackInfo ci,@Local(name = "configHeight") int configHeight){
        if (config instanceof ConfigAdjustableDouble configAdjustableDouble) {
            int mainWidth = 98;
            int keybindX = x+mainWidth+4;
            int keybindWidth = 80;
            int keybindSettingsX = keybindX + keybindWidth + 2;
            int keybindSettingsWidth = 20;
            int resetX = keybindSettingsX + keybindSettingsWidth + 2;

            GuiTextFieldGeneric field = this.createTextField(x, y+1, mainWidth, configHeight - 2);
            field.setMaxLength(this.maxTextfieldTextLength);
            field.setValue(configAdjustableDouble.getStringValue());

            ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(keybindX, y, keybindWidth, configHeight, configAdjustableDouble.getKeybind(), this.host);

            ButtonGeneric resetButton = this.createResetButton(resetX, y, configAdjustableDouble);
            ConfigOptionListenerResetConfig listenerReset = new ConfigOptionListenerResetConfig(configAdjustableDouble, new ConfigOptionListenerResetConfig.ConfigResetterTextField(configAdjustableDouble, field), resetButton, (ButtonPressDirtyListenerSimple)null);

            ConfigOptionChangeListenerTextField listenerChange = new ConfigOptionChangeListenerTextField(configAdjustableDouble, field, resetButton);

            this.addTextField(field, listenerChange, TextFieldType.DOUBLE);
            this.addButton(keybindButton, (a,b) -> {});
//            this.addButton(keybindButton, host.getButtonPressListener());
//            this.addWidget(new WidgetKeybindSettings(keybindSettingsX, y+1, keybindSettingsWidth, 20, configAdjustableDouble.getKeybind(), configAdjustableDouble.getName(), this.parent, this.host.getDialogHandler()));
            this.addButton(resetButton, listenerReset);

//            WidgetConfigOption.HotkeyedBooleanResetListener resetListener = new WidgetConfigOption.HotkeyedBooleanResetListener(resettableConfig, booleanButton, keybindButton, resetButton, this.host);
//            this.host.addKeybindChangeListener();


            ci.cancel();
        }
    }
}