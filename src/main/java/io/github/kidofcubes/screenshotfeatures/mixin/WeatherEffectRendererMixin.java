package io.github.kidofcubes.screenshotfeatures.mixin;

import io.github.kidofcubes.screenshotfeatures.config.ConfigTypes;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherEffectRendererMixin {
    @Redirect(method ="extractRenderState", at = @At(value = "INVOKE", target ="Lnet/minecraft/client/multiplayer/ClientLevel;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
    private Biome.Precipitation getPrecipitation(ClientLevel instance,BlockPos pos){
        if(Configs.IngameTools.FORCE_PRECIPITATION.getBooleanValue()){
            if(Configs.IngameTools.PRECIPITATION_FORCE_TYPE.getOptionListValue() == ConfigTypes.PrecipitationType.RAIN){
                return (Biome.Precipitation.RAIN);
            }else if(Configs.IngameTools.PRECIPITATION_FORCE_TYPE.getOptionListValue() == ConfigTypes.PrecipitationType.SNOW){
                return (Biome.Precipitation.SNOW);
            }else{
                return (Biome.Precipitation.NONE);
            }
        }else{
            return instance.getPrecipitationAt(pos);
        }
    }
}
