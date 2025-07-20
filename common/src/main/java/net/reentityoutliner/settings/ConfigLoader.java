package net.reentityoutliner.settings;

import net.reentityoutliner.Constants;
import net.reentityoutliner.common.EntityTypesProperties;
import net.reentityoutliner.common.Registries;
import net.reentityoutliner.common.MobCategoryColor;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;

public class ConfigLoader {
    public static final HashMap<EntityType<?>, EntityTypesProperties> outlinedEntityTypes = new HashMap<>();
    public static String getReadableName(EntityType<?> type) {
        return type.getDescription().getString();
    }

    public static void load(){
          for (EntityType<?> entityType : Registries.getAllEntityTypes()) {
              var color = MobCategoryColor.of(entityType.getCategory());
            outlinedEntityTypes.put(entityType, new EntityTypesProperties(color, false));
              Constants.LOG.info(getReadableName(entityType));

          }
    }
}


