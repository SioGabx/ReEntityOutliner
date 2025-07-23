package net.reentityoutliner.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.reentityoutliner.util.MobCategoryColor;
import org.jetbrains.annotations.NotNull;

import static net.reentityoutliner.config.ConfigManager.outlinedEntityTypes;


public class ColorWidget extends Button {

    private MobCategoryColor color;
    private final EntityType<?> entityType;

    //protected net.minecraft.client.gui.components.Button.OnPress pressFunc;

    public ColorWidget(int x, int y, int width, int height, Component message, EntityType<?> entityType) {
        super(x, y, width, height, message, b -> {}, Button.DEFAULT_NARRATION);
        this.entityType = entityType;
        // this.pressFunc = b -> {};
        //https://github.com/Brian-Wuest/MC-Prefab/blob/1e49d3f09fa38e3d851baa10c907d651bf734e8e/Shared/src/com/prefab/gui/controls/ExtendedButton.java#L7
        var settings = outlinedEntityTypes.get(entityType);
        if (settings.outlined){
            onShow();
        }
    }

    public void onShow() {
        this.color = outlinedEntityTypes.get(this.entityType).color;
    }

    public void onPress() {
        onPress(0);
    }

    public void onPress(int button) {
        this.color = this.color.next();
        if (button == 1) {
            this.color = MobCategoryColor.of(entityType.getCategory());
        }
        var settings = outlinedEntityTypes.get(entityType);
        if (settings != null) {
            settings.color = this.color;
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        int color = (255 << 24) | (this.color.red << 16) | (this.color.green << 8) | this.color.blue;
        this.setMessage(Component.literal(this.color.colorName));
        Minecraft minecraft = Minecraft.getInstance();
        context.drawCenteredString(minecraft.font, this.color.colorName, getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, color);
    }
}