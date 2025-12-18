package io.github.kidofcubes.screenshotfeatures.mixin;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GuiConfigsBase.class, remap = false)
public abstract class GuiConfigsBaseMixin extends GuiListBase<GuiConfigsBase.ConfigOptionWrapper,WidgetConfigOption,WidgetListConfigOptions> implements IKeybindConfigGui {
    private GuiConfigsBaseMixin(int listX,int listY){
        super(listX,listY);
    }

    @Inject(method = "onKeyTyped", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    public void onKeyTyped(KeyInput input,CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(super.onKeyTyped(input));
        cir.cancel();
    }
}
