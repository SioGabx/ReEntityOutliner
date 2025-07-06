package net.reentityoutliner.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import net.reentityoutliner.ReEntityOutliner;
import net.reentityoutliner.util.EntityTypesSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.registry.Registries;

public class EntitySelector extends Screen {
    protected final Screen parent;

    private TextFieldWidget searchField;
    public static EntityListWidget list;
    public static boolean groupByCategory = true;
    private static String searchText = "";
    public static HashMap<String, List<EntityType<?>>> searcher; // Prefix -> arr of results
    public static final HashMap<EntityType<?>, EntityTypesSettings> outlinedEntityTypes = new HashMap<>();

    public EntitySelector(Screen parent) {
        super(Text.translatable("title.re-entity-outliner.selector"));
        this.parent = parent;
    }


    protected void init() {
        if (searcher == null) {
            initializePrefixTree();
        }
        int margin = 35;
        list = new EntityListWidget(this.client, this.width, this.height - margin * 2, margin, 25);
        this.addSelectableChild(list);

        // Create search field
        this.searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 6, 200, 20, Text.of(searchText));
        this.searchField.setText(searchText);
        this.searchField.setChangedListener(this::onSearchFieldUpdate);
        this.addSelectableChild(searchField);

        // Create buttons
        int buttonWidth = 80;
        int buttonHeight = 20;
        int numberOfButtonOnInterface = 5;
        int buttonInterval = (this.width - numberOfButtonOnInterface * buttonWidth) / (numberOfButtonOnInterface + 1);
        int buttonY = this.height - 16 - (buttonHeight / 2);

        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable(groupByCategory ? "button.re-entity-outliner.categories" : "button.re-entity-outliner.no-categories"),
                        (button) -> {
                            groupByCategory = !groupByCategory;
                            this.onSearchFieldUpdate(this.searchField.getText());
                            button.setMessage(Text.translatable(groupByCategory ? "button.re-entity-outliner.categories" : "button.re-entity-outliner.no-categories"));
                        }
                ).size(buttonWidth, buttonHeight).position(buttonInterval, buttonY).build()
        );


        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable("button.re-entity-outliner.deselect"),
                        (button) -> {
                            String text = this.searchField.getText();
                            if (searcher.containsKey(text)) {
                                List<EntityType<?>> results = searcher.get(text);
                                for (EntityType<?> entityType : results) {
                                    EntityTypesSettings settings = EntitySelector.outlinedEntityTypes.get(entityType);
                                    if (settings != null) {
                                        settings.outlined = false;
                                    }
                                }
                            }

                            double previousScroll = list.getScrollY();
                            this.onSearchFieldUpdate(this.searchField.getText());
                            list.setScrollY(previousScroll);
                        }
                ).size(buttonWidth, buttonHeight).position(buttonInterval + (buttonWidth + buttonInterval), buttonY).build()
        );


        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable("button.re-entity-outliner.select"),
                        (button) -> {
                            String text = this.searchField.getText();
                            if (searcher.containsKey(text)) {
                                List<EntityType<?>> results = searcher.get(text);
                                for (EntityType<?> entityType : results) {
                                    EntityTypesSettings settings = EntitySelector.outlinedEntityTypes.get(entityType);
                                    if (settings != null) {
                                        settings.outlined = true;
                                    }
                                }
                            }
                            double previousScroll = list.getScrollY();
                            this.onSearchFieldUpdate(this.searchField.getText());
                            list.setScrollY(previousScroll);
                        }
                ).size(buttonWidth, buttonHeight).position(buttonInterval + (buttonWidth + buttonInterval) * 2, buttonY).build()
        );
        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable(ReEntityOutliner.outliningEntities ? "button.re-entity-outliner.on" : "button.re-entity-outliner.off"),
                        (button) -> {
                            ReEntityOutliner.outliningEntities = !ReEntityOutliner.outliningEntities;
                            button.setMessage(Text.translatable(ReEntityOutliner.outliningEntities ? "button.re-entity-outliner.on" : "button.re-entity-outliner.off"));
                        }
                ).size(buttonWidth, buttonHeight).position(buttonInterval + (buttonWidth + buttonInterval) * 3, buttonY).build()
        );


        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable("button.re-entity-outliner.done"),
                        (button) -> {
                            if (this.client != null) {
                                this.client.setScreen(null);
                            }
                        }
                ).size(buttonWidth, buttonHeight).position(buttonInterval + (buttonWidth + buttonInterval) * 4, buttonY).build()
        );

        this.setInitialFocus(this.searchField);
        this.onSearchFieldUpdate(this.searchField.getText());
    }

    // Initializes the prefix tree used for searching in the entity selector screen
    private void initializePrefixTree() {
        EntitySelector.searcher = new HashMap<>();

        // Initialize no-text results
        List<EntityType<?>> allResults = new ArrayList<>();
        EntitySelector.searcher.put("", allResults);

        // Get sorted list of entity types
        List<EntityType<?>> entityTypes = new ArrayList<>();
        for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
            entityTypes.add(entityType);
        }
        entityTypes.sort(Comparator.comparing(o -> o.getName().getString()));

        // Add each entity type to everywhere it belongs in the prefix "tree"
        for (EntityType<?> entityType : entityTypes) {

            String name = entityType.getName().getString().toLowerCase();
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
                    if (EntitySelector.searcher.containsKey(prefix)) {
                        results = EntitySelector.searcher.get(prefix);
                    } else {
                        results = new ArrayList<>();
                        EntitySelector.searcher.put(prefix, results);
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


    // Callback provided to TextFieldWidget triggered when its text updates
    private void onSearchFieldUpdate(String text) {
        searchText = text;
        text = text.toLowerCase().trim();

        list.clearListEntries();

        if (searcher.containsKey(text)) {
            List<EntityType<?>> results = searcher.get(text);

            // Splits results into categories and separates them with headers
            if (groupByCategory) {
                HashMap<SpawnGroup, List<EntityType<?>>> resultsByCategory = new HashMap<>();

                for (EntityType<?> entityType : results) {
                    SpawnGroup category = entityType.getSpawnGroup();
                    if (!resultsByCategory.containsKey(category)) {
                        resultsByCategory.put(category, new ArrayList<>());
                    }

                    resultsByCategory.get(category).add(entityType);
                }

                for (SpawnGroup category : SpawnGroup.values()) {
                    if (resultsByCategory.containsKey(category)) {
                        if (this.client != null) {
                            list.addListEntry(EntityListWidget.HeaderEntry.create(category, this.client.textRenderer, this.width, 25));
                        }

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
        } else { // If there are no results, let the user know
            if (this.client != null) {
                list.addListEntry(EntityListWidget.HeaderEntry.create(null, this.client.textRenderer, this.width, 25));
            }
        }

        // This prevents an overscroll when the user is already scrolled down and the results list is shortened
        if (list.getMaxScrollY() < list.getScrollY()){
            list.setScrollY(list.getMaxScrollY());
        }
    }

    // Called when config screen is escaped
    public void removed() {
        ReEntityOutliner.saveConfig();
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render buttons
        super.render(context, mouseX, mouseY, delta);
        // Render scrolling list
        list.render(context, mouseX, mouseY, delta);
        // Render our search bar
        this.setFocused(this.searchField);
        //this.searchField.setTextFieldFocused(true);
        this.searchField.render(context, mouseX, mouseY, delta);
    }


    // Sends mouseDragged event to the scrolling list
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return list.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
