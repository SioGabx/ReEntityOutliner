package net.reentityoutliner.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.reentityoutliner.Constants;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static net.reentityoutliner.config.ConfigManager.*;
import static net.reentityoutliner.config.ConfigManager.isOutliningEntities;
import static net.reentityoutliner.util.Registries.getAllEntityTypes;

public class ConfigScreen extends Screen {
    private EditBox searchField;
    public static HashMap<String, List<EntityType<?>>> searcher; // Prefix -> arr of results
    private static String searchText = "";
    public static boolean groupByCategory = true;
    public static EntityListWidget list;
    private Minecraft client;
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Entity Selector"));
        this.parent = parent;
    }

    protected void init() {
        Constants.LOG.info("Init config screen");
        client = super.minecraft;
        if (searcher == null) {
            initializePrefixTree();
        }

        final int margin = 35;
        //searchbar
        searchField = new EditBox(this.font, this.width / 2 - 100, 6, 200, 20, Component.literal(searchText));
        this.searchField.setValue(searchText);
        this.searchField.setResponder(this::onSearchFieldUpdate);
        this.addRenderableWidget(searchField);
        //scrolllist
        list = new EntityListWidget(this.client, this.width, this.height - margin * 2, margin, 25);
        this.addRenderableWidget(list);
        //buttons
        int buttonWidth = 80;
        int buttonHeight = 20;
        int numberOfButtonOnInterface = 5;
        int buttonInterval = (this.width - numberOfButtonOnInterface * buttonWidth) / (numberOfButtonOnInterface + 1);
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
                            String text = this.searchField.getValue();
                            if (searcher.containsKey(text)) {
                                List<EntityType<?>> results = searcher.get(text);
                                for (EntityType<?> entityType : results) {
                                    var settings = outlinedEntityTypes.get(entityType);
                                    if (settings != null) settings.outlined = false;
                                }
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
                            String text = this.searchField.getValue();
                            if (searcher.containsKey(text)) {
                                List<EntityType<?>> results = searcher.get(text);
                                for (EntityType<?> entityType : results) {
                                    var settings = outlinedEntityTypes.get(entityType);
                                    if (settings != null) settings.outlined = true;
                                }
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
                            if (this.minecraft != null) this.minecraft.setScreen(this.parent);
                        })
                .size(buttonWidth, buttonHeight)
                .pos(buttonInterval + (buttonWidth + buttonInterval) * 4, buttonY)
                .build());

        this.setInitialFocus(this.searchField);
        this.onSearchFieldUpdate(this.searchField.getValue());
    }


    private void onSearchFieldUpdate(String text) {
        searchText = text;
        text = text.toLowerCase().trim();
        list.clearListEntries();

        if (searcher.containsKey(text)) {
            List<EntityType<?>> results = searcher.get(text);
            // Splits results into categories and separates them with headers
            if (groupByCategory) {
                HashMap<MobCategory, List<EntityType<?>>> resultsByCategory = new HashMap<>();

                for (EntityType<?> entityType : results) {
                    MobCategory category = entityType.getCategory();
                    if (!resultsByCategory.containsKey(category)) {
                        resultsByCategory.put(category, new ArrayList<>());
                    }

                    resultsByCategory.get(category).add(entityType);
                }
                for (MobCategory category : MobCategory.values()) {
                    if (resultsByCategory.containsKey(category)) {
                        if (client!= null)
                            list.addListEntry(EntityListWidget.HeaderEntry.create(category, client.font));
                        for (EntityType<?> entityType : resultsByCategory.get(category))
                            list.addListEntry(EntityListWidget.EntityEntry.create(entityType, this.width));
                    }
                }
            } else {
                for (EntityType<?> entityType : results)
                    list.addListEntry(EntityListWidget.EntityEntry.create(entityType, this.width));
            }
        } else {// If there are no results, let the user know
            if (client != null)
                list.addListEntry(EntityListWidget.HeaderEntry.create(null, client.font));
        }

        if (list.getScrollAmount() > list.getMaxScroll()) {
            list.setScrollAmount(list.getMaxScroll());
        }
    }

    private void initializePrefixTree() {
        searcher = new HashMap<>();


        // Initialize no-text results
        List<EntityType<?>> allResults = new ArrayList<>();
        searcher.put("", allResults);

        List<EntityType<?>> entityTypes = new ArrayList<>(getAllEntityTypes());
        entityTypes.sort(Comparator.comparing(EntityType::getDescriptionId));
        // Add each entity type to everywhere it belongs in the prefix "tree"
        for (EntityType<?> entityType : entityTypes) {
            String name = entityType.getDescription().getString().toLowerCase();
            allResults.add(entityType);
            List<String> prefixes = new ArrayList<>();
            prefixes.add("");
            // By looping over the name's length, we add to every possible prefix
            for (int i = 0; i < name.length(); i++) {
                char character = name.charAt(i);
                // Loop over every prefix
                for (int p = 0; p < prefixes.size(); p++) {
                    String prefix = prefixes.get(p) + character;
                    prefixes.set(p, prefix);
                    // Get results for current prefix
                    List<EntityType<?>> results;
                    if (searcher.containsKey(prefix)) {
                        results = searcher.get(prefix);
                    } else {
                        results = new ArrayList<>();
                        searcher.put(prefix, results);
                    }

                    results.add(entityType);
                }
                // Add another prefix to allow searching by second/third/... word
                if (Character.isWhitespace(character)) {
                    prefixes.add("");
                }
            }
        }
    }

    public void removed() {
        save();
    }

    public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
super.extractRenderState(context, mouseX, mouseY, delta);
list.extractRenderState(context, mouseX, mouseY, delta);
        this.setFocused(this.searchField);
        this.searchField.extractRenderState(context, mouseX, mouseY, delta);
    }
    public boolean mouseDragged(net.minecraft.client.input.@NotNull MouseButtonEvent mouseButtonEvent, double dx, double dy) {
        return list.mouseDragged(mouseButtonEvent, dx, dy);
    }

}
