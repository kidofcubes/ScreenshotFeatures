package io.github.kidofcubes.screenshotfeatures.mixin;

import io.github.kidofcubes.screenshotfeatures.config.ConfigTypes.*;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.entity.EntityQueriable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WeatherChangerWorldMixin implements WorldAccess, EntityQueriable<Entity>, AutoCloseable, AttachmentTarget {

    @Inject(method = "isRaining", at = @At("HEAD"), cancellable = true)
    private void isRaining(CallbackInfoReturnable<Boolean> cir){
        if(!Configs.IngameTools.WEATHER_OVERRIDE.getBooleanValue() || (MinecraftClient.getInstance().isInSingleplayer() && !Configs.IngameTools.FORCE_SETTINGS.getBooleanValue())) return;
        cir.setReturnValue((Configs.IngameTools.WEATHER_OVERRIDE_VALUE.getOptionListValue())!=WeatherTypes.CLEAR);
    }

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    private void isThundering(CallbackInfoReturnable<Boolean> cir){
        if(!Configs.IngameTools.WEATHER_OVERRIDE.getBooleanValue() || (MinecraftClient.getInstance().isInSingleplayer() && !Configs.IngameTools.FORCE_SETTINGS.getBooleanValue())) return;
        cir.setReturnValue((Configs.IngameTools.WEATHER_OVERRIDE_VALUE.getOptionListValue())== WeatherTypes.THUNDER);
    }

    @Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
    private void rainGradient(float tickProgress, CallbackInfoReturnable<Float> cir){
        if(!Configs.IngameTools.WEATHER_OVERRIDE.getBooleanValue() || (MinecraftClient.getInstance().isInSingleplayer() && !Configs.IngameTools.FORCE_SETTINGS.getBooleanValue())) return;
        cir.setReturnValue((Configs.IngameTools.WEATHER_OVERRIDE_VALUE.getOptionListValue())!= WeatherTypes.CLEAR ? 1.0f : 0.0f);
    }

    @Inject(method = "getThunderGradient", at = @At("HEAD"), cancellable = true)
    private void thunderGradient(float tickProgress, CallbackInfoReturnable<Float> cir){
        if(!Configs.IngameTools.WEATHER_OVERRIDE.getBooleanValue() || (MinecraftClient.getInstance().isInSingleplayer() && !Configs.IngameTools.FORCE_SETTINGS.getBooleanValue())) return;
        cir.setReturnValue(Configs.IngameTools.WEATHER_OVERRIDE_VALUE.getOptionListValue()== WeatherTypes.THUNDER ? 1.0f : 0.0f);
    }

    @Inject(method = "hasRain", at = @At("HEAD"), cancellable = true)
    private void hasRain(BlockPos pos, CallbackInfoReturnable<Boolean> cir){
        if(Configs.IngameTools.FORCE_RAIN.getOptionListValue() == OptionalBoolean.NO_OP || (MinecraftClient.getInstance().isInSingleplayer() && !Configs.IngameTools.FORCE_SETTINGS.getBooleanValue())) return;
        cir.setReturnValue(Configs.IngameTools.FORCE_RAIN.getOptionListValue() == OptionalBoolean.TRUE);
    }


}
