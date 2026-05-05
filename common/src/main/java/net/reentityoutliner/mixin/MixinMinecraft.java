package net.reentityoutliner.mixin;

import net.minecraft.world.entity.Entity;
import net.reentityoutliner.Constants;
import net.minecraft.client.Minecraft;
import net.reentityoutliner.config.ConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.reentityoutliner.config.ConfigManager.*;
import static net.reentityoutliner.config.ConfigManager.isOutliningEntities;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    
    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(CallbackInfo info) {
        Constants.LOG.info("Loaded {}", Constants.MOD_NAME);
    }


    @Inject(at = @At("HEAD"), method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z", cancellable = true)
    public void onShouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (isOutliningEntities()){
            var settings = ConfigManager.getOrCreateEntityProperties(entity.getType());
            if (settings != null && settings.outlined) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }
}