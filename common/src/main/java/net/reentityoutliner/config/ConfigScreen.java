package net.reentityoutliner.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.reentityoutliner.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static net.reentityoutliner.config.ConfigManager.*;
import static net.reentityoutliner.util.Registries.getAllEntityTypes;

public class ConfigScreen extends Screen {
    private EditBox searchField;
    public static HashMap<String, List<EntityType<?>>> searcher;
    private static String searchText = "";
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
        if (searcher == null) {
            initializePrefixTree();
        }

        final int margin = 35;

        // Search bar
        searchField = new EditBox(this.font, this.width / 2 - 100, 6, 200, 20, Component.literal(searchText));
        searchField.setValue(searchText);
        searchField.setResponder(this::onSearchFieldUpdate);
        //searchField.setBordered(true);

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
                        Component.translatable(groupByCategory ? "button.re-entity-outliner.categories" : "button.re-entity-outliner.no-categories"),
                        (button) -> {
                            groupByCategory = !groupByCategory;
                            this.onSearchFieldUpdate(this.searchField.getValue());
                            button.setMessage(Component.translatable(groupByCategory ? "button.re-entity-outliner.categories" : "button.re-entity-outliner.no-categories"));
                        })
                .bounds(buttonInterval, buttonY, buttonWidth, buttonHeight)
                .build());

        // Deselect Button
        this.addRenderableWidget(Button.builder(
                        Component.translatable("button.re-entity-outliner.deselect"),
                        (button) -> {
                            String text = this.searchField.getValue().toLowerCase().trim();
                            if (searcher.containsKey(text)) {
                                for (EntityType<?> entityType : searcher.get(text)) {
                                    var settings = outlinedEntityTypes.get(entityType);
                                    if (settings != null) settings.outlined = false;
                                }
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
                            String text = this.searchField.getValue().toLowerCase().trim();
                            if (searcher.containsKey(text)) {
                                for (EntityType<?> entityType : searcher.get(text)) {
                                    var settings = outlinedEntityTypes.get(entityType);
                                    if (settings != null) settings.outlined = true;
                                }
                            }
                            double previousScroll = list.getScrollAmount();
                            this.onSearchFieldUpdate(this.searchField.getValue());
                            list.setScrollAmount(previousScroll);
                        })
                .bounds(buttonInterval + (buttonWidth + buttonInterval) * 2, buttonY, buttonWidth, buttonHeight)
                .build());

        // Master Toggle Button
        this.addRenderableWidget(Button.builder(
                        Component.translatable(isOutliningEntities() ? "button.re-entity-outliner.on" : "button.re-entity-outliner.off"),
                        (button) -> {
                            setOutliningEntities(!isOutliningEntities());
                            button.setMessage(Component.translatable(isOutliningEntities() ? "button.re-entity-outliner.on" : "button.re-entity-outliner.off"));
                        })
                .bounds(buttonInterval + (buttonWidth + buttonInterval) * 3, buttonY, buttonWidth, buttonHeight)
                .build());

        // Done Button
        this.addRenderableWidget(Button.builder(
                        Component.translatable("button.re-entity-outliner.done"),
                        (button) -> {
                            if (this.minecraft != null) this.minecraft.setScreen(this.parent);
                        })
                .bounds(buttonInterval + (buttonWidth + buttonInterval) * 4, buttonY, buttonWidth, buttonHeight)
                .build());

        this.setInitialFocus(this.searchField);
        this.onSearchFieldUpdate(this.searchField.getValue());
    }

    private void onSearchFieldUpdate(String text) {
        searchText = text;
        String query = text.toLowerCase().trim();
        list.clearListEntries();

        if (searcher.containsKey(query)) {
            List<EntityType<?>> results = searcher.get(query);
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
            list.addListEntry(EntityListWidget.HeaderEntry.create(null, this.font));
        }

        if (list.getScrollAmount() > list.getMaxScroll()) {
            list.setScrollAmount(list.getMaxScroll());
        }
    }

    private void initializePrefixTree() {
        searcher = new HashMap<>();
        List<EntityType<?>> allResults = new ArrayList<>();
        searcher.put("", allResults);

        List<EntityType<?>> entityTypes = new ArrayList<>(getAllEntityTypes());
        entityTypes.sort(Comparator.comparing(EntityType::getDescriptionId));

        for (EntityType<?> entityType : entityTypes) {
            String name = entityType.getDescription().getString().toLowerCase();
            allResults.add(entityType);
            List<String> prefixes = new ArrayList<>();
            prefixes.add("");

            for (int i = 0; i < name.length(); i++) {
                char character = name.charAt(i);
                for (int p = 0; p < prefixes.size(); p++) {
                    String prefix = prefixes.get(p) + character;
                    prefixes.set(p, prefix);
                    searcher.computeIfAbsent(prefix, k -> new ArrayList<>()).add(entityType);
                }
                if (Character.isWhitespace(character)) {
                    prefixes.add("");
                }
            }
        }
    }

    @Override
    public void removed() {
        save();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        /*
        this.renderBackground(guiGraphics); // Fond sombre de Minecraft

        list.render(guiGraphics, mouseX, mouseY, partialTick);
        searchField.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
         */
        this.renderBackground(guiGraphics);
        list.render(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.searchField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return list.mouseDragged(mouseX, mouseY, button, dragX, dragY) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}