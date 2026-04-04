package io.github.kidofcubes.screenshotfeatures.mixin;

import io.github.kidofcubes.screenshotfeatures.config.ConfigTypes;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherChangerWeatherEffectRendererMixin {
    @Redirect(method ="render", at = @At(value = "FIELD", target ="Lnet/minecraft/client/renderer/state/WeatherRenderState;intensity:F"))
    private float getRainGradient(WeatherRenderState instance){
        if(Configs.IngameTools.FORCE_PRECIPITATION.getBooleanValue()){
            return 1.0f;
        }else{
            return instance.intensity;
        }
    }
    @Redirect(method ="getPrecipitationAt", at = @At(value = "INVOKE", target ="Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
    private Biome.Precipitation getPrecipitation(Biome instance,BlockPos pos,int seaLevel){
        if(instance.hasPrecipitation() || Configs.IngameTools.FORCE_PRECIPITATION.getBooleanValue()){
            if(Configs.IngameTools.PRECIPITATION_FORCE_TYPE.getOptionListValue() == ConfigTypes.PrecipitationType.CLEAR){
                return (Biome.Precipitation.NONE);
            }else if(Configs.IngameTools.PRECIPITATION_FORCE_TYPE.getOptionListValue() == ConfigTypes.PrecipitationType.RAIN){
                return (Biome.Precipitation.RAIN);
            }else if(Configs.IngameTools.PRECIPITATION_FORCE_TYPE.getOptionListValue() == ConfigTypes.PrecipitationType.SNOW){
                return (Biome.Precipitation.SNOW);
            }
        }else{
            return (Biome.Precipitation.NONE);
        }
        return instance.getPrecipitationAt(pos,seaLevel);
    }
}
