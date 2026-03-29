package net.reentityoutliner.config;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.reentityoutliner.util.MobCategoryColor;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.NonNull;

import static net.reentityoutliner.config.ConfigManager.outlinedEntityTypes;

public class ColorWidget extends Button {

    private MobCategoryColor color;
    private final EntityType<?> entityType;

    public ColorWidget(int x, int y, int width, int height, Component message, EntityType<?> entityType) {
        super(x, y, width, height, message, _ -> {
        }, Button.DEFAULT_NARRATION);

        this.entityType = entityType;

        var settings = outlinedEntityTypes.get(entityType);
        if (settings != null && settings.outlined) {
            onShow();
        }
    }

    public void onShow() {
        var settings = outlinedEntityTypes.get(this.entityType);
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

        var settings = outlinedEntityTypes.get(entityType);
        if (settings != null) {
            settings.color = this.color;
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
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

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {

        this.extractDefaultSprite(graphics);
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
        graphics.centeredText(
                minecraft.font,
                this.color.colorName,
                getX() + getWidth() / 2,
                getY() + (getHeight() - 8) / 2,
                colorInt
        );
    }
}