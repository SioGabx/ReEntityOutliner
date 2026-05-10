package net.reentityoutliner.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
    public EntityListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        this.centerListVertically = false;
    }

    public int getRowWidth() {
        return 350;
    }

    protected int getMaxScroll() {
        return super.maxScrollAmount();
    }

    protected double getScrollAmount() {
        return super.scrollAmount();
    }

    public void addListEntry(EntityListWidget.Entry entry) {
        super.addEntry(entry);
    }

    public void clearListEntries() {
        super.clearEntries();
    }

    //Entry definition
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<EntityListWidget.Entry> {
    }

    //Entity entry def 1.21.9
    public static class EntityEntry extends EntityListWidget.Entry {
        private final Checkbox checkbox;
        private final ColorWidget color;
        private final EntityType<?> entityType;
        private final List<Renderable> children = new ArrayList<>();

        public EntityEntry(Checkbox checkbox, ColorWidget color, EntityType<?> entityType) {
            this.checkbox = checkbox;
            this.entityType = entityType;
            this.color = color;

            this.children.add(checkbox);
            var settings = ConfigManager.getOrCreateEntityProperties(entityType);
            if (settings != null && settings.outlined) {
                this.children.add(color);
            }
        }

        public static EntityListWidget.EntityEntry create(EntityType<?> entityType, int width) {
            var settings = ConfigManager.getOrCreateEntityProperties(entityType);
            boolean isChecked = (settings != null && settings.outlined);

            // 155+75=230
            var cb = Checkbox.builder(Component.translatable(entityType.getDescriptionId()), Minecraft.getInstance().font)
                    .pos(width / 2 - 155, 0)
                    .maxWidth(230)
                    .selected(isChecked)
                    .build();

            //tooltips
            Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);

            Component tooltipText = Component.empty()
                    .append(entityType.getDescription())
                    .append(Component.literal("\n" + entityId).withStyle(ChatFormatting.DARK_GRAY));
            cb.setTooltip(Tooltip.create(tooltipText));

            ColorWidget cw = new ColorWidget(width / 2 + 75, 0, 75, 20, Component.empty(), entityType);

            return new EntityEntry(cb, cw, entityType);
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        @Override
        public void renderContent(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float delta) {
            int widgetHeight = 20;
            int centerY = this.getContentY() + (this.getContentHeight() - widgetHeight) / 2;
            this.checkbox.setY(centerY + 1);

            this.checkbox.render(guiGraphics, mouseX, mouseY, delta);
            if (this.children.contains(this.color)) {
                this.color.setY(this.getContentY());
                this.color.render(guiGraphics, mouseX, mouseY, delta);
            }
        }

        @Override
        public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
            var settings = ConfigManager.getOrCreateEntityProperties(this.entityType);
            if (settings == null) {
                Constants.LOG.info("ReEntityOutliner Debug : Settings null for {}", entityType);
                return false;
            }

            // Logique identique à ton vieux code mais adaptée
            if (this.color.isMouseOver(event.x(), event.y()) && this.children.contains(this.color)) {
                return this.color.mouseClicked(event, doubleClick);
            }

            if (this.checkbox.mouseClicked(event, doubleClick)) {
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
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }
    }


    //HEADER ENTRY
    public static class HeaderEntry extends EntityListWidget.Entry {

        private final Font font;
        private final String title;
        private final MobCategory spawnGp;

        private HeaderEntry(MobCategory category, Font font) {
            this.font = font;
            this.spawnGp = category;
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

        public static EntityListWidget.HeaderEntry create(MobCategory category, Font font) {
            return new EntityListWidget.HeaderEntry(category, font);
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float delta) {
            //graphics.fill(k, j, k + l, j + height, 0xFF0000FF); // fond bleu visible

            //Font font = Minecraft.getInstance().font;
            int textWidth = font.width(title);
            int textX = this.getContentX() + (this.getContentWidth() - textWidth) / 2;
            int textY = this.getContentY() + (this.getContentHeight() - font.lineHeight) / 2;

            guiGraphics.drawString(
                    this.font,
                    this.title,
                    textX,
                    textY,
                    0xFFFFFFFF
            );
        }


        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            Constants.LOG.info("HeaderEntry mouseClicked");
            if (event.button() == 0) { // Clic gauche
                // 1. Déterminer si on doit tout cocher ou tout décocher
                boolean allChecked = true;
                for (EntityListWidget.Entry entry : ConfigScreen.list.children()) {
                    if (entry instanceof EntityListWidget.EntityEntry ee && ee.entityType.getCategory() == spawnGp) {
                        if (!ee.checkbox.selected()) {
                            allChecked = false;
                            break;
                        }
                    }
                }

                // 2. Appliquer le changement directement sur la valeur
                boolean targetState = !allChecked;

                for (EntityListWidget.Entry entry : ConfigScreen.list.children()) {
                    if (entry instanceof EntityListWidget.EntityEntry ee && ee.entityType.getCategory() == spawnGp) {
                        // Si la checkbox n'est pas dans l'état voulu, on simule un clic
                        if (ee.checkbox.selected() != targetState) {
                            ee.checkbox.onPress(event);
                        }
                        var props = ConfigManager.getOrCreateEntityProperties(ee.entityType);
                        if (props != null) props.outlined = targetState;
                    }
                }
                return true;
            }
            return super.mouseClicked(event, doubleClick);

        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        @Override
        public String toString() {
            return this.title;
        }
    }
}

