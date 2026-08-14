package io.github.kidofcubes.screenshotfeatures.mixin;

import com.google.common.collect.ImmutableSet;
import net.irisshaders.iris.shaderpack.option.OptionAnnotatedSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = OptionAnnotatedSource.class, remap = false)
public interface OptionAnnotatedSourceMixin {
    @Accessor("VALID_CONST_OPTION_NAMES")
    static ImmutableSet<String> screenshotfeatures$VALID_CONST_OPTION_NAMES(){
        throw new AssertionError("Untransformed @Accessor");
    }
}
