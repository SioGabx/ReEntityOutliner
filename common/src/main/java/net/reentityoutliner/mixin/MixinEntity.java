package net.reentityoutliner.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.reentityoutliner.config.ConfigManager.*;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow public abstract EntityType<?> getType();

    @Inject(at = @At("HEAD"), method = "getTeamColor()I", cancellable = true)
    private void onGetTeamColor(CallbackInfoReturnable<Integer> info) {
        if (isOutliningEntities()){
        var settings = outlinedEntityTypes.get(this.getType());
        if (settings.outlined) {
                int red = settings.color.red;
                int green = settings.color.green;
                int blue = settings.color.blue;

                int color = (255 << 24) | (red << 16) | (green << 8) | blue;
                info.setReturnValue(color);
                info.cancel();
        }}
    }
}
