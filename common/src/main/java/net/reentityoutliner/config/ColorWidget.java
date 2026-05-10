package net.reentityoutliner.config;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.EntityType;
import net.reentityoutliner.util.MobCategoryColor;
import org.jetbrains.annotations.NotNull;


public class ColorWidget extends Button {

    private MobCategoryColor color;
    private final EntityType<?> entityType;


    public ColorWidget(int x, int y, int width, int height, Component message, EntityType<?> entityType) {
        super(x, y, width, height, message, b -> {}, Button.DEFAULT_NARRATION);
        this.entityType = entityType;
        // this.pressFunc = b -> {};
        //https://github.com/Brian-Wuest/MC-Prefab/blob/1e49d3f09fa38e3d851baa10c907d651bf734e8e/Shared/src/com/prefab/gui/controls/ExtendedButton.java#L7
        var settings = ConfigManager.getOrCreateEntityProperties(entityType);
        if (settings != null && settings.outlined) {
            onShow();
        }
    }

    public void onShow() {
        var settings = ConfigManager.getOrCreateEntityProperties(this.entityType);
        if (settings != null) {
            this.color = settings.color;
        }
    }

    private void cycleColor(boolean reset) {
        if (this.color == null) return;

        if (reset) {
            this.color = MobCategoryColor.of(entityType.getCategory());
        } else {
            this.color = this.color.next();
        }

        var settings = ConfigManager.getOrCreateEntityProperties(entityType);
        if (settings != null) {
            settings.color = this.color;
        }
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
        if (!this.active || !this.visible) return false;
        super.mouseClicked(event, doubleClick);
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        if (this.isMouseOver(mouseX, mouseY)) {
            this.onPress(event.buttonInfo()); // VANILLA → son + focus OK
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            if (button == 0) {
                this.cycleColor(false);

            } else if (button == 1) {
                this.cycleColor(true); // clic droit
            }
            return true;
        }

        return false;
    }


    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(graphics, mouseX, mouseY, delta);
        if (this.isMouseOver(mouseX, mouseY)) {
            // épaisseur du contour
            int thickness = 1;
            // haut
            graphics.fill(getX(), getY(), getRight(), getY() + thickness, CommonColors.WHITE);
            // bas
            graphics.fill(getX(), getBottom() - thickness, getRight(), getBottom(), CommonColors.WHITE);
            // gauche
            graphics.fill(getX(), getY(), getX() + thickness, getBottom(), CommonColors.WHITE);
            // droite
            graphics.fill(getRight() - thickness, getY(), getRight(), getBottom(), CommonColors.WHITE);

            graphics.requestCursor(isActive() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
        }


        if (this.color == null) return;
        int colorInt = (255 << 24) | (this.color.red << 16) | (this.color.green << 8) | this.color.blue;

        this.setMessage(Component.literal(this.color.colorName));

        Minecraft minecraft = Minecraft.getInstance();
        graphics.drawCenteredString(
                minecraft.font,
                this.color.colorName,
                getX() + getWidth() / 2,
                getY() + (getHeight() - 8) / 2,
                colorInt
        );
    }
}