package io.github.kidofcubes.screenshotfeatures.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessorMixin {
    @Invoker("getFov")
    float getFovThing(Camera camera,float tickProgress,boolean changingFov);
}
