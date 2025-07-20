package net.reentityoutliner.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(at = @At("HEAD"), method = "getTeamColor()I", cancellable = true)
    private void onGetTeamColor(CallbackInfoReturnable<Integer> info) {
        Integer color = Color.BLUE.getRGB();
        if (color != null) {
            info.setReturnValue(color);
            info.cancel();
        }
    }
}
