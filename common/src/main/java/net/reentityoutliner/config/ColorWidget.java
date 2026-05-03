package net.reentityoutliner.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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

    public ColorWidget(int x, int y, int width, int height, Component message, EntityType<?> entityType) {
        // En Java 17 (Minecraft 1.20.1), le pattern `_ -> {}` n'est pas supporté. On utilise `btn -> {}`.
        super(x, y, width, height, message, btn -> {
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) return false;

        // isMouseOver remplace la vérification de l'Event vanilla
        if (this.isMouseOver(mouseX, mouseY)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());

            if (button == 0) { // Clic gauche
                this.cycleColor(false);
            } else if (button == 1) { // Clic droit
                this.cycleColor(true);
            }

            // Appliquer le focus manuellement puisque l'on bypass le super.mouseClicked()
            this.setFocused(true);
            return true;
        }

        return false;
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Rendu de l'arrière-plan classique du bouton (remplace extractDefaultSprite)
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        // Variables de positionnement (getRight() et getBottom() n'existent pas directement en 1.20.1)
        int x = this.getX();
        int y = this.getY();
        int right = x + this.width;
        int bottom = y + this.height;

        if (this.isHoveredOrFocused()) {
            int thickness = 1;
            int white = 0xFFFFFFFF; // Remplace CommonColors.WHITE

            // haut
            guiGraphics.fill(x, y, right, y + thickness, white);
            // bas
            guiGraphics.fill(x, bottom - thickness, right, bottom, white);
            // gauche
            guiGraphics.fill(x, y, x + thickness, bottom, white);
            // droite
            guiGraphics.fill(right - thickness, y, right, bottom, white);

            // NB: La modification du pointeur (CursorTypes.POINTING_HAND) a été retirée
            // car ce n'est pas supporté nativement par le système vanilla de la 1.20.1.
        }
    }

    @Override
    public void renderString(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int color) {
        // En 1.20.1, c'est la méthode parfaite pour injecter notre texte coloré
        // par dessus l'arrière-plan du widget généré par renderWidget()
        if (this.color == null) return;

        int colorInt = (255 << 24) | (this.color.red << 16) | (this.color.green << 8) | this.color.blue;
        this.setMessage(Component.literal(this.color.colorName));

        guiGraphics.drawCenteredString(
                font,
                this.getMessage(),
                this.getX() + this.width / 2,
                this.getY() + (this.height - 8) / 2,
                colorInt
        );
    }
}