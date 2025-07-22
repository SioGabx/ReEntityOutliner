package net.reentityoutliner.config;

import net.reentityoutliner.Constants;
import net.reentityoutliner.util.EntityTypesProperties;
import net.reentityoutliner.util.Registries;
import net.reentityoutliner.util.MobCategoryColor;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;

public class ConfigManager {
    public static final HashMap<EntityType<?>, EntityTypesProperties> outlinedEntityTypes = new HashMap<>();
    public static boolean outliningEntities = false;
    public static String getReadableName(EntityType<?> type) {
        return type.getDescription().getString();
    }

    public static void load() {
        for (EntityType<?> entityType : Registries.getAllEntityTypes()) {
            var color = MobCategoryColor.of(entityType.getCategory());
            outlinedEntityTypes.put(entityType, new EntityTypesProperties(color, false));
            Constants.LOG.info(getReadableName(entityType));

        }
    }

    public static void save(){

    }
}


