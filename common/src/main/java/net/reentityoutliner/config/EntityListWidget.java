package net.reentityoutliner.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.reentityoutliner.config.ConfigManager.outlinedEntityTypes;

public class EntityListWidget extends ContainerObjectSelectionList<EntityListWidget.Entry>{
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
            var settings = outlinedEntityTypes.get(entityType);
            if (settings != null && settings.outlined) {
                this.children.add(color);
            }
        }

        public static EntityListWidget.EntityEntry create(EntityType<?> entityType, int width) {
            var settings = outlinedEntityTypes.get(entityType);

            return new EntityListWidget.EntityEntry(
                    Checkbox.builder(Component.translatable(entityType.getDescriptionId()), Minecraft.getInstance().font)
                            .pos(width / 2 - 155, 0)
                            .selected(settings != null && settings.outlined).build(),
                    new ColorWidget(width / 2 + 75, 0, 75, 20,Component.empty(), entityType),
                    entityType
            );
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        @Override
        public void renderContent(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float delta) {
            this.checkbox.setY(this.getContentY());
            this.checkbox.render(guiGraphics, mouseX, mouseY, delta);
            if (this.children.contains(this.color)) {
                this.color.setY(this.getContentY());
                this.color.render(guiGraphics, mouseX, mouseY, delta);
            }
        }

        @Override
        public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
            var settings = outlinedEntityTypes.get(this.entityType);
            if (settings != null && settings.outlined) {
                if (this.color.isMouseOver(event.x(), event.y())) {
                    this.color.onPress(event.button());
                } else {
                    settings.outlined = false;
                    this.checkbox.onPress(event.buttonInfo());
                    this.children.remove(this.color);
                }
            } else {
                if (settings != null) {
                    settings.outlined = true;
                }
                this.color.onShow();
                this.checkbox.onPress(event.buttonInfo());
                this.children.add(this.color);
            }
            return true;
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
                StringBuilder title = new StringBuilder();
                for (String term : category.getName().split("\\p{Punct}|\\s")) {
                    title.append(StringUtils.capitalize(term)).append(" ");
                }
                this.title = title.toString().trim();
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
            //int textX = this.getContentX() + (guiGraphics.guiWidth() - textWidth) / 2;
            //int textY = this.getContentY() + (guiGraphics.guiHeight() - font.lineHeight) / 2;

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
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override

        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        //public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (event.button() == 0) {
                boolean allAlreadySelected = true;
                for (int i = 0; i < 2; i++) {
                    for (EntityListWidget.Entry entry : ConfigScreen.list.children()) {
                        if (entry instanceof EntityListWidget.EntityEntry entityEntry) {
                            if (entityEntry.entityType.getCategory() == spawnGp) {
                                boolean isChecked = entityEntry.checkbox.selected();
                                if ((!isChecked && i == 0) || (isChecked && i == 1)) {
                                    allAlreadySelected = false;
                                    entityEntry.mouseClicked(event, doubleClick);
                                }
                            }
                        }
                    }
                    if (!allAlreadySelected) break;
                }
            }
            return super.mouseClicked(event, doubleClick);
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

