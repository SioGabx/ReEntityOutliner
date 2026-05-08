package net.reentityoutliner.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.reentityoutliner.Constants;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EntityListWidget extends ContainerObjectSelectionList<EntityListWidget.Entry> {

    public EntityListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
        // Correction cruciale : top et bottom définissent la zone de rendu
        super(minecraft, width, height, top, bottom, itemHeight);
        this.centerListVertically = false;
        // En 1.20.1, on peut désactiver le rendu du fond par défaut si on veut gérer celui du Screen
        this.setRenderBackground(false);
        this.setRenderHeader(false, 0);
    }

    @Override
    public int getRowWidth() {
        return 350; // Plus petit que 400 pour éviter de coller aux bords
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + 170; // Positionne la barre à droite du RowWidth
    }

    public void addListEntry(Entry entry) {
        super.addEntry(entry);
    }

    public void clearListEntries() {
        super.clearEntries();
    }

    // --- CLASSES INTERNES ---

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<EntityListWidget.Entry> { }

    public static class EntityEntry extends Entry {
        private final Checkbox checkbox;
        private final ColorWidget color;
        private final EntityType<?> entityType;
        private final List<GuiEventListener> children = new ArrayList<>();

        private EntityEntry(Checkbox checkbox, ColorWidget color, EntityType<?> entityType) {
            this.checkbox = checkbox;
            this.entityType = entityType;
            this.color = color;
            this.children.add(checkbox);

            var settings = ConfigManager.getOrCreateEntityProperties(entityType);
            if (settings != null && settings.outlined) {
                this.children.add(color);
            }
        }

        public static EntityEntry create(EntityType<?> entityType, int width) {
            var settings = ConfigManager.getOrCreateEntityProperties(entityType);
            boolean isChecked = (settings != null && settings.outlined);

            // 155+75=230
            Checkbox cb = new Checkbox(
                    width / 2 - 155, 0, 230, 20,
                    entityType.getDescription(),
                    isChecked,
                    true
            );
            //tooltips
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            
            Component tooltipText = Component.empty()
                    .append(entityType.getDescription())
                    .append(Component.literal("\n" + entityId).withStyle(ChatFormatting.DARK_GRAY));
            cb.setTooltip(Tooltip.create(tooltipText));

            ColorWidget cw = new ColorWidget(width / 2 + 75, 0, 75, 20, Component.empty(), entityType);

            return new EntityEntry(cb, cw, entityType);
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            this.checkbox.setY(top);
            this.checkbox.render(guiGraphics, mouseX, mouseY, partialTick);

            if (this.children.contains(this.color)) {
                this.color.setY(top);
                this.color.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            var settings = ConfigManager.getOrCreateEntityProperties(this.entityType);
            if (settings == null) {
                Constants.LOG.info("ReEntityOutliner Debug : Settings null for {}", entityType);
                return false;
            }

            // Logique identique à ton vieux code mais adaptée
            if (this.color.isMouseOver(mouseX, mouseY) && this.children.contains(this.color)) {
                return this.color.mouseClicked(mouseX, mouseY, button);
            }

            if (this.checkbox.mouseClicked(mouseX, mouseY, button)) {
                settings.outlined = this.checkbox.selected();
                if (settings.outlined) {
                    this.color.onShow();
                    if (!this.children.contains(this.color)) this.children.add(this.color);
                } else {
                    this.children.remove(this.color);
                }
                return true;
            }
            return false;
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() { return this.children; }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() { return List.of(checkbox, color); }
    }

    public static class HeaderEntry extends Entry {
        private final Font font;
        private final String title;

        public HeaderEntry(MobCategory category, Font font) {
            this.font = font;
            if (category != null) {
                String title = category.getName();
                //to Pascal_Case
                String output = Arrays.stream(title.split("_"))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                        .collect(Collectors.joining("_"));
                this.title = StringUtils.capitalize(output);
            } else {
                this.title = Component.translatable("gui.re-entity-outliner.no_results").getString();
            }
        }

        public static HeaderEntry create(MobCategory category, Font font) {
            return new HeaderEntry(category, font);
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            guiGraphics.drawCenteredString(font, title, left + width / 2, top + (height / 2) - 4, 0xFFFFFF);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() { return List.of(); }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() { return List.of(); }
    }
}