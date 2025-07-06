package net.reentityoutliner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.lwjgl.glfw.GLFW;

import net.reentityoutliner.ui.EntitySelector;
import net.reentityoutliner.ui.ColorWidget.Color;
import net.reentityoutliner.util.EntityTypesSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;

public class ReEntityOutliner implements ClientModInitializer {
    private static final Gson GSON = new Gson();
    public static boolean outliningEntities;

    private static final KeyBinding CONFIG_BIND = new KeyBinding(
            "key.re-entity-outliner.selector",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_SEMICOLON,
            "title.re-entity-outliner.title"
    );

    private static final KeyBinding OUTLINE_BIND = new KeyBinding(
            "key.re-entity-outliner.outline",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "title.re-entity-outliner.title"
    );

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(CONFIG_BIND);
        KeyBindingHelper.registerKeyBinding(OUTLINE_BIND);

        loadConfig();

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }


    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("reentityoutliner.json");
    }

    public static void saveConfig() {
        JsonObject config = new JsonObject();

        JsonArray outlinedEntitiesArray = new JsonArray();

        for (Map.Entry<EntityType<?>, EntityTypesSettings> entry : EntitySelector.outlinedEntityTypes.entrySet()) {
            EntityType<?> entityType = entry.getKey();
            EntityTypesSettings settings = entry.getValue();

            JsonObject entityObj = new JsonObject();
            entityObj.addProperty("entity", EntityType.getId(entityType).toString());

            JsonObject colorObj = new JsonObject();
            colorObj.addProperty("name", settings.color.name());
            colorObj.addProperty("r", settings.color.red);
            colorObj.addProperty("g", settings.color.green);
            colorObj.addProperty("b", settings.color.blue);
            entityObj.add("color", colorObj);

            entityObj.addProperty("outlined", settings.outlined);

            outlinedEntitiesArray.add(entityObj);
        }

        config.add("outlinedEntities", outlinedEntitiesArray);

        try {
            Files.write(getConfigPath(), GSON.toJson(config).getBytes());
        } catch (IOException ex) {
            logException(ex, "Failed to save reentityoutliner config");
        }
    }


    private void loadConfig() {

        for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
            EntitySelector.outlinedEntityTypes.put(entityType, new EntityTypesSettings(Color.of(entityType.getSpawnGroup()), false));
        }

        try {
            // Lecture du fichier en UTF-8 et parsing JSON
            String jsonString = Files.readString(getConfigPath());
            JsonObject config = GSON.fromJson(jsonString, JsonObject.class);

            if (config.has("outlinedEntities")) {
                JsonArray outlinedEntitiesArray = config.getAsJsonArray("outlinedEntities");

                for (JsonElement element : outlinedEntitiesArray) {
                    if (!element.isJsonObject()) continue;

                    JsonObject entityObj = element.getAsJsonObject();

                    String entityId = entityObj.get("entity").getAsString();

                    Optional<EntityType<?>> entityTypeOptional = EntityType.get(entityId);

                    if (entityTypeOptional.isEmpty()) {
                        System.err.printf("[reentityoutliner] Invalid entity type: %s%n", entityId);
                        continue;
                    }

                    EntityType<?> entityType = entityTypeOptional.get();

                    JsonObject colorObj = entityObj.getAsJsonObject("color");
                    String colorName = colorObj.has("name") ? colorObj.get("name").getAsString() : "Unknown";
                    Color color = Color.valueOf(colorName);

                    // Récupérer outlined
                    boolean outlined = entityObj.has("outlined") && entityObj.get("outlined").getAsBoolean();

                    EntityTypesSettings settings = EntitySelector.outlinedEntityTypes.get(entityType);

                    if (settings != null) {
                        settings.color = color;
                        settings.outlined = outlined;
                    }
                }
            }
        } catch (Exception ex) {
            logException(ex, "Failed to load reentityoutliner config");
        }
    }

    private void onEndTick(MinecraftClient client) {
        while (OUTLINE_BIND.wasPressed()) {
            outliningEntities = !outliningEntities;
        }

        if (CONFIG_BIND.isPressed()) {
            client.setScreen(new EntitySelector(null));
        }
    }

    public static void logException(Exception ex, String message) {
        System.err.printf("[reentityoutliner] %s (%s: %s)", message, ex.getClass().getSimpleName(), ex.getLocalizedMessage());
    }
}