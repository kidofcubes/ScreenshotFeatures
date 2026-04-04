package io.github.kidofcubes.screenshotfeatures.mixin;

import io.github.kidofcubes.screenshotfeatures.config.ConfigTypes.*;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.entity.UUIDLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class WeatherChangerLevelMixin implements LevelAccessor, UUIDLookup<Entity>, AutoCloseable, AttachmentTarget {

    @Inject(method ="isRaining", at = @At("HEAD"), cancellable = true)
    private void isRaining(CallbackInfoReturnable<Boolean> cir){
        if(!Configs.IngameTools.WEATHER_OVERRIDE.getBooleanValue() || (Minecraft.getInstance().isLocalServer() && !Configs.IngameTools.FORCE_SETTINGS.getBooleanValue())) return;
        cir.setReturnValue((Configs.IngameTools.WEATHER_OVERRIDE_VALUE.getOptionListValue())!=WeatherTypes.CLEAR);
    }

    @Inject(method ="isThundering", at = @At("HEAD"), cancellable = true)
    private void isThundering(CallbackInfoReturnable<Boolean> cir){
        if(!Configs.IngameTools.WEATHER_OVERRIDE.getBooleanValue() || (Minecraft.getInstance().isLocalServer() && !Configs.IngameTools.FORCE_SETTINGS.getBooleanValue())) return;
        cir.setReturnValue((Configs.IngameTools.WEATHER_OVERRIDE_VALUE.getOptionListValue())== WeatherTypes.THUNDER);
    }

    @Inject(method ="getRainLevel", at = @At("HEAD"), cancellable = true)
    private void rainGradient(float tickProgress, CallbackInfoReturnable<Float> cir){
        if(!Configs.IngameTools.WEATHER_OVERRIDE.getBooleanValue() || (Minecraft.getInstance().isLocalServer() && !Configs.IngameTools.FORCE_SETTINGS.getBooleanValue())) return;
        cir.setReturnValue((Configs.IngameTools.WEATHER_OVERRIDE_VALUE.getOptionListValue())!= WeatherTypes.CLEAR ? 1.0f : 0.0f);
    }

    @Inject(method ="getThunderLevel", at = @At("HEAD"), cancellable = true)
    private void thunderGradient(float tickProgress, CallbackInfoReturnable<Float> cir){
        if(!Configs.IngameTools.WEATHER_OVERRIDE.getBooleanValue() || (Minecraft.getInstance().isLocalServer() && !Configs.IngameTools.FORCE_SETTINGS.getBooleanValue())) return;
        cir.setReturnValue(Configs.IngameTools.WEATHER_OVERRIDE_VALUE.getOptionListValue()== WeatherTypes.THUNDER ? 1.0f : 0.0f);
    }

    @Inject(method ="isRainingAt", at = @At("HEAD"), cancellable = true)
    private void hasRain(BlockPos pos,CallbackInfoReturnable<Boolean> cir){
        if(Configs.IngameTools.FORCE_RAIN.getOptionListValue() == OptionalBoolean.NO_OP || (Minecraft.getInstance().isLocalServer() && !Configs.IngameTools.FORCE_SETTINGS.getBooleanValue())) return;
        cir.setReturnValue(Configs.IngameTools.FORCE_RAIN.getOptionListValue() == OptionalBoolean.TRUE);
    }


}
