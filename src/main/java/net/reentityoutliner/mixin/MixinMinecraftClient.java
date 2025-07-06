package net.reentityoutliner.mixin;

import net.reentityoutliner.ReEntityOutliner;
import net.reentityoutliner.ui.EntitySelector;
import net.reentityoutliner.util.EntityTypesSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void outlineEntities(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        if (ReEntityOutliner.outliningEntities) {
            EntityTypesSettings settings = EntitySelector.outlinedEntityTypes.get(entity.getType());
            if (settings != null && settings.outlined) {
                ci.setReturnValue(true);
            }
        }
    }
}