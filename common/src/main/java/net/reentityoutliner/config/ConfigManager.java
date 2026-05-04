package net.reentityoutliner.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.reentityoutliner.Constants;
import net.reentityoutliner.util.EntityTypesProperties;
import net.reentityoutliner.util.MobCategoryColor;
import net.reentityoutliner.util.Registries;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class ConfigManager {

    public static final HashMap<EntityType<?>, EntityTypesProperties> outlinedEntityTypes = new HashMap<>();

    private static boolean outliningEntities = false;

    public static boolean isOutliningEntities() {
        return outliningEntities;
    }

    public static void setOutliningEntities(boolean value) {
        outliningEntities = value;
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            String message = outliningEntities ? "gui.re-entity-outliner.outline.now-on" : "gui.re-entity-outliner.outline.now-off";
            // En 1.20.1, displayClientMessage avec le booléen 'true' permet d'afficher dans l'ActionBar (au-dessus de la barre d'inventaire)
            client.player.displayClientMessage(Component.translatable(message, Constants.MOD_NAME), true);
        }
    }

    public static Path ConfigPath = null;
    private static final Gson GSON = new Gson();

    // En 1.20.1, les catégories de KeyMappings sont simplement des chaînes de caractères (clés de traduction).
    // Plus besoin d'utiliser un objet de type Category.register().
    public static final String KEYBINDING_CATEGORY = "Re:Entity Outliner";

    public static final KeyMapping CONFIG_BIND = new KeyMapping(
            "key.re-entity-outliner.selector",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SEMICOLON,
            KEYBINDING_CATEGORY
    );

    public static final KeyMapping OUTLINE_BIND = new KeyMapping(
            "key.re-entity-outliner.outline",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            KEYBINDING_CATEGORY
    );

    private static Path getConfigPath() {
        return ConfigPath.resolve("reentityoutliner.json");
    }

    public static void save() {
        JsonObject config = new JsonObject();
        JsonArray outlinedEntitiesArray = new JsonArray();

        for (var entry : outlinedEntityTypes.entrySet()) {
            EntityType<?> entityType = entry.getKey();
            var settings = entry.getValue();

            JsonObject entityObj = new JsonObject();
            // En 1.20.1, on passe par BuiltInRegistries pour récupérer le nom de l'entité
            entityObj.addProperty("entity", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());

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
            Constants.LOG.error("Failed to save reentityoutliner config");
            Constants.LOG.error(ex.getMessage());
        }
    }

    public static EntityTypesProperties getOrCreateEntityProperties(EntityType<?> entityType) {
        return outlinedEntityTypes.computeIfAbsent(entityType, type -> {
            var color = MobCategoryColor.of(type.getCategory());
            return new EntityTypesProperties(color, false);
        });
    }

    public static void load() {
        for (EntityType<?> entityType : Registries.getAllEntityTypes()) {
            getOrCreateEntityProperties(entityType);
        }

        try {
            if (!Files.exists(getConfigPath())) return;

            String jsonString = Files.readString(getConfigPath());
            JsonObject config = GSON.fromJson(jsonString, JsonObject.class);

            if (config != null && config.has("outlinedEntities")) {
                JsonArray outlinedEntitiesArray = config.getAsJsonArray("outlinedEntities");

                for (JsonElement element : outlinedEntitiesArray) {
                    if (!element.isJsonObject()) continue;

                    JsonObject entityObj = element.getAsJsonObject();
                    String entityId = entityObj.get("entity").getAsString();

                    EntityType<?> entityType = null;
                    for (EntityType<?> type : outlinedEntityTypes.keySet()) {
                        // En 1.20.1, récupération via BuiltInRegistries
                        if (BuiltInRegistries.ENTITY_TYPE.getKey(type).toString().equals(entityId)) {
                            entityType = type;
                            break;
                        }
                    }

                    if (entityType == null) {
                        System.err.printf("[reentityoutliner] No match found for entity: %s%n", entityId);
                        continue;
                    }

                    JsonObject colorObj = entityObj.getAsJsonObject("color");
                    String colorName = colorObj.has("name") ? colorObj.get("name").getAsString() : "Unknown";
                    MobCategoryColor color = MobCategoryColor.valueOf(colorName);

                    boolean outlined = entityObj.has("outlined") && entityObj.get("outlined").getAsBoolean();
                    var settings = ConfigManager.getOrCreateEntityProperties(entityType);
                    if (settings != null) {
                        settings.color = color;
                        settings.outlined = outlined;
                    }
                }
            }
        } catch (Exception ex) {
            Constants.LOG.error("Failed to load reentityoutliner config");
            Constants.LOG.error(ex.getMessage());
        }
    }
}