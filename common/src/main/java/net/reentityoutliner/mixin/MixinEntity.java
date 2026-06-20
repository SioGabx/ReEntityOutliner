package net.reentityoutliner.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.TeamColor;
import net.reentityoutliner.config.ConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static net.reentityoutliner.config.ConfigManager.*;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public abstract EntityType<?> getType();

    @Inject(at = @At("HEAD"), method = "getTeamColor()I", cancellable = true)
    private void onGetTeamColor(CallbackInfoReturnable<Integer> info) {
        if (isOutliningEntities()) {
            var settings = ConfigManager.getOrCreateEntityProperties(this.getType());
            if (settings != null && settings.outlined) {
                //Get generic colors for entity
                int red = settings.color.red;
                int green = settings.color.green;
                int blue = settings.color.blue;

                //Make outline to the color of the player
                if (this.getType() ==  EntityTypes.PLAYER) {
                    if (((Object)this) instanceof Player player) {
                        var team = player.getTeam();
                        if (team != null) {
                            Optional<TeamColor> TeamColorValue = team.getColor();
                            var colorValue = TeamColorValue.get();
                            if (colorValue != null) {
                                int argbInt = colorValue.rgb();
                                red = (argbInt >> 16) & 0xFF;
                                green = (argbInt >> 8) & 0xFF;
                                blue = argbInt & 0xFF;
                            }
                        }
                    }
                }

                //Apply
                int color = (255 << 24) | (red << 16) | (green << 8) | blue;
                info.setReturnValue(color);
                info.cancel();
            }
        }
    }
}
