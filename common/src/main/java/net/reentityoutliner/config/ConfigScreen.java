package net.reentityoutliner.config;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.reentityoutliner.Constants;
import net.reentityoutliner.util.EntitySearcher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.reentityoutliner.config.ConfigManager.*;
import static net.reentityoutliner.config.ConfigManager.isOutliningEntities;

public class ConfigScreen extends Screen {
    private EditBox searchField;
    private static String searchText = "*";
    public static boolean groupByCategory = true;
    public static EntityListWidget list;
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Entity Selector"));
        this.parent = parent;
    }

    protected void init() {
        Constants.LOG.info("Init config screen");
        EntitySearcher.initializeEntities();

        Minecraft client = super.minecraft;

        // Search bar
        searchField = new EditBox(this.font, this.width / 2 - 100, 6, 200, 20, Component.literal(searchText)) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                var val = super.mouseClicked(mouseX, mouseY, button);
                SetSearchFieldCursorPosition();
                return val;
            }

            @Override
            public void setFocused(boolean p_265520_) {
                super.setFocused(p_265520_);
                SetSearchFieldCursorPosition();
            }
        };
        searchField.setValue(searchText);
        searchField.setHint(Component.translatable("gui.re-entity-outliner.search_hint"));
        searchField.setResponder(this::onSearchFieldUpdate);
        this.addRenderableWidget(searchField);

        // Bouton d'aide (?) à côté de la barre de recherche
        Style styleTitre = Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true);
        Style styleArg = Style.EMPTY.withColor(ChatFormatting.YELLOW);
        Style styleExemple = Style.EMPTY.withColor(ChatFormatting.GRAY);
        this.addRenderableWidget(Button.builder(Component.literal("?"), (button) -> Util.getPlatform().openUri("https://github.com/SioGabx/ReEntityOutliner/wiki/How-to-search"))
                .bounds(this.width / 2 + 105, 6, 20, 20) // Positionné juste à droite de la barre (200/2 + 5px d'écart)
                .tooltip(net.minecraft.client.gui.components.Tooltip.create(
                        Component.empty()
                                .append(Component.translatable("gui.re-entity-outliner.search_help.title").withStyle(styleTitre))
                                .append("\n\n")
                                .append(Component.translatable("gui.re-entity-outliner.search_help.exact",
                                        Component.literal("zombie").withStyle(styleArg)))
                                .append("\n")
                                .append(Component.translatable("gui.re-entity-outliner.search_help.end-with",
                                        Component.literal("zombie*").withStyle(styleArg)))
                                .append("\n")
                                .append(Component.translatable("gui.re-entity-outliner.search_help.start-with",
                                        Component.literal("*zombie").withStyle(styleArg)))
                                .append("\n")
                                .append(Component.translatable("gui.re-entity-outliner.search_help.contains",
                                        Component.literal("*zombie*").withStyle(styleArg)))
                                .append("\n")
                                .append(Component.translatable("gui.re-entity-outliner.search_help.mod",
                                        Component.literal("@mod").withStyle(styleArg)))
                                .append("\n")
                                .append(Component.translatable("gui.re-entity-outliner.search_help.category",
                                        Component.literal("#category").withStyle(styleArg)))
                                .append("\n")
                                .append(Component.translatable("gui.re-entity-outliner.search_help.example",
                                        Component.literal("@minecraft #monster *zombie*")).withStyle(styleExemple))
                ))
                .build());

        //scrolllist
        final int margin = 35;
        list = new EntityListWidget(client, this.width, this.height - margin * 2, margin, 25);
        this.addRenderableWidget(list);
        //buttons
        int buttonWidth = 80;
        int buttonHeight = 20;
        int numberOfButtons = 5;
        int buttonInterval = (this.width - numberOfButtons * buttonWidth) / (numberOfButtons + 1);
        int buttonY = this.height - 16 - (buttonHeight / 2);


        this.addRenderableWidget(Button.builder(
                        Component.translatable(groupByCategory ? "button.re-entity-outliner.categories" : "button.re-entity-outliner.no-categories"),
                        (button) -> {
                            groupByCategory = !groupByCategory;
                            this.onSearchFieldUpdate(this.searchField.getValue());
                            button.setMessage(Component.translatable(groupByCategory ? "button.re-entity-outliner.categories" : "button.re-entity-outliner.no-categories"));
                        })
                .size(buttonWidth, buttonHeight)
                .pos(buttonInterval, buttonY)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("button.re-entity-outliner.deselect"),
                        (button) -> {
                            List<EntityType<?>> currentResults = EntitySearcher.getSearchResults(this.searchField.getValue());
                            for (EntityType<?> entityType : currentResults) {
                                var settings = ConfigManager.getOrCreateEntityProperties(entityType);
                                if (settings != null)
                                    settings.outlined = false;
                            }
                            double previousScroll = list.getScrollAmount();
                            this.onSearchFieldUpdate(this.searchField.getValue());
                            list.setScrollAmount(previousScroll);
                        })
                .size(buttonWidth, buttonHeight)
                .pos(buttonInterval + (buttonWidth + buttonInterval), buttonY)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("button.re-entity-outliner.select"),
                        (button) -> {
                            List<EntityType<?>> currentResults = EntitySearcher.getSearchResults(this.searchField.getValue());
                            for (EntityType<?> entityType : currentResults) {
                                var settings = ConfigManager.getOrCreateEntityProperties(entityType);
                                if (settings != null)
                                    settings.outlined = true;
                            }
                            double previousScroll = list.getScrollAmount();
                            this.onSearchFieldUpdate(this.searchField.getValue());
                            list.setScrollAmount(previousScroll);
                        })
                .size(buttonWidth, buttonHeight)
                .pos(buttonInterval + (buttonWidth + buttonInterval) * 2, buttonY)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable(isOutliningEntities() ? "button.re-entity-outliner.on" : "button.re-entity-outliner.off"),
                        (button) -> {
                            setOutliningEntities(!isOutliningEntities());
                            button.setMessage(Component.translatable(isOutliningEntities() ? "button.re-entity-outliner.on" : "button.re-entity-outliner.off"));
                        })
                .size(buttonWidth, buttonHeight)
                .pos(buttonInterval + (buttonWidth + buttonInterval) * 3, buttonY)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("button.re-entity-outliner.done"),
                        (button) -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(this.parent);
                            }
                        })
                .size(buttonWidth, buttonHeight)
                .pos(buttonInterval + (buttonWidth + buttonInterval) * 4, buttonY)
                .build());

        this.setInitialFocus(this.searchField);
        SetSearchFieldCursorPosition();
        this.onSearchFieldUpdate(this.searchField.getValue());
    }

    private void SetSearchFieldCursorPosition() {
        if (this.searchField.getValue().equals("*")) {
            this.searchField.setCursorPosition(0);
        } else {
        this.searchField.setCursorPosition(this.searchField.getValue().length());

    }
        this.searchField.setHighlightPos(this.searchField.getCursorPosition());
    }

    private void onSearchFieldUpdate(String text) {
        searchText = text;
        list.clearListEntries();

        List<EntityType<?>> results = EntitySearcher.getSearchResults(text);

        if (!results.isEmpty()) {
            if (groupByCategory) {
                HashMap<MobCategory, List<EntityType<?>>> resultsByCategory = new HashMap<>();
                for (EntityType<?> entityType : results) {
                    resultsByCategory.computeIfAbsent(entityType.getCategory(), k -> new ArrayList<>()).add(entityType);
                }

                for (MobCategory category : MobCategory.values()) {
                    if (resultsByCategory.containsKey(category)) {
                        list.addListEntry(EntityListWidget.HeaderEntry.create(category, this.font));
                        for (EntityType<?> entityType : resultsByCategory.get(category)) {
                            list.addListEntry(EntityListWidget.EntityEntry.create(entityType, this.width));
                        }
                    }
                }
            } else {
                for (EntityType<?> entityType : results) {
                    list.addListEntry(EntityListWidget.EntityEntry.create(entityType, this.width));
                }
            }
        } else {
            list.addListEntry(EntityListWidget.HeaderEntry.create(null, this.font)); // Afficher un header vide ou un message "Aucun résultat"
        }

        if (list.getScrollAmount() > list.getMaxScroll()) {
            list.setScrollAmount(list.getMaxScroll());
        }
    }

    public void removed() {
        save();
    }

    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        list.render(context, mouseX, mouseY, delta);
        this.searchField.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return list.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

}
