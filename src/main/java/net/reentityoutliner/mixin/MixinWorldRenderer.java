package net.reentityoutliner.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.reentityoutliner.ReEntityOutliner;
import net.reentityoutliner.ui.EntitySelector;
import net.reentityoutliner.ui.ColorWidget.Color;
import net.reentityoutliner.util.EntityTypesSettings;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;

/**/
@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {


    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (ReEntityOutliner.outliningEntities
                && vertexConsumers instanceof OutlineVertexConsumerProvider outlineVertexConsumers) {

            EntityTypesSettings settings = EntitySelector.outlinedEntityTypes.get(entity.getType());
            if (settings != null && settings.outlined) {

                Color color = EntitySelector.outlinedEntityTypes.get(entity.getType()).color;
                outlineVertexConsumers.setColor(color.red, color.green, color.blue, 255);

                if (entity.getType() == EntityType.PLAYER) {
                    PlayerEntity player = (PlayerEntity) entity;
                    AbstractTeam team = player.getScoreboardTeam();
                    if (team != null && team.getColor().getColorValue() != null) {
                        int argbInt = team.getColor().getColorValue();
                        int alpha = (argbInt >> 24) & 0xFF;
                        int red = (argbInt >> 16) & 0xFF;
                        int green = (argbInt >> 8) & 0xFF;
                        int blue = argbInt & 0xFF;
                        outlineVertexConsumers.setColor(red, green, blue, alpha);
                    }
                }
            }

        }
    }
}
