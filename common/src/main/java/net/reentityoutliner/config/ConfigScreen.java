package net.reentityoutliner.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.reentityoutliner.Constants;
import net.reentityoutliner.util.EntitySearcher;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.reentityoutliner.config.ConfigManager.*;

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

    @Override
    protected void init() {
        Constants.LOG.info("Init config screen");

        EntitySearcher.initializeEntities();

        final int margin = 35;

        // Search bar
        searchField = new EditBox(this.font, this.width / 2 - 100, 6, 200, 20, Component.literal(searchText)) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                // Appelle d'abord le comportement de base pour que le clic fonctionne
                super.onClick(mouseX, mouseY);

                // Ta logique personnalisée
                if (this.getValue().equals("*")) {
                    this.setCursorPosition(0);
                    this.setHighlightPos(0);
                }
            }
        };
        searchField.setValue(searchText);
        searchField.setHint(Component.translatable("gui.re-entity-outliner.search_hint"));
        searchField.setResponder(this::onSearchFieldUpdate);

        // searchField.setBordered(true);

        this.addRenderableWidget(searchField);

        // Scroll list
        // Minecraft, Largeur, Hauteur, Top, Bottom, ItemHeight
        list = new EntityListWidget(this.minecraft, this.width, this.height, margin, this.height - margin, 25);
        this.addRenderableWidget(list);

        // Buttons positioning
        int buttonWidth = 80;
        int buttonHeight = 20;
        int numberOfButtons = 5;
        int buttonInterval = (this.width - numberOfButtons * buttonWidth) / (numberOfButtons + 1);
        int buttonY = this.height - 16 - (buttonHeight / 2);

        // Category Toggle Button
        this.addRenderableWidget(Button.builder(
                Component.translatable(groupByCategory ? "button.re-entity-outliner.categories"
                        : "button.re-entity-outliner.no-categories"),
                (button) -> {
                    groupByCategory = !groupByCategory;
                    this.onSearchFieldUpdate(this.searchField.getValue());
                    button.setMessage(Component.translatable(groupByCategory ? "button.re-entity-outliner.categories"
                            : "button.re-entity-outliner.no-categories"));
                })
                .bounds(buttonInterval, buttonY, buttonWidth, buttonHeight)
                .build());

        // Deselect Button
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
                .bounds(buttonInterval + (buttonWidth + buttonInterval), buttonY, buttonWidth, buttonHeight)
                .build());

        // Select Button
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
                .bounds(buttonInterval + (buttonWidth + buttonInterval) * 2, buttonY, buttonWidth, buttonHeight)
                .build());

        // Master Toggle Button
        this.addRenderableWidget(Button.builder(
                Component.translatable(
                        isOutliningEntities() ? "button.re-entity-outliner.on" : "button.re-entity-outliner.off"),
                (button) -> {
                    setOutliningEntities(!isOutliningEntities());
                    button.setMessage(Component.translatable(
                            isOutliningEntities() ? "button.re-entity-outliner.on" : "button.re-entity-outliner.off"));
                })
                .bounds(buttonInterval + (buttonWidth + buttonInterval) * 3, buttonY, buttonWidth, buttonHeight)
                .build());

        // Done Button
        this.addRenderableWidget(Button.builder(
                Component.translatable("button.re-entity-outliner.done"),
                (button) -> {
                    if (this.minecraft != null)
                        this.minecraft.setScreen(this.parent);
                })
                .bounds(buttonInterval + (buttonWidth + buttonInterval) * 4, buttonY, buttonWidth, buttonHeight)
                .build());

        this.setInitialFocus(this.searchField);
        if (this.searchField.getValue() == "*") {
            this.searchField.setCursorPosition(0);
             this.searchField.setHighlightPos(0);
        }
        this.onSearchFieldUpdate(this.searchField.getValue());
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

    @Override
    public void removed() {
        save();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        list.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.searchField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return list.mouseDragged(mouseX, mouseY, button, dragX, dragY)
                || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}