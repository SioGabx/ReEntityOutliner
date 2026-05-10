package net.reentityoutliner.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Registries {

    // Entity Ban list (without glowing)
    private static final Set<String> BANNED_ENTITY_PATHS = Set.of(
            "experience_orb",
            "area_effect_cloud",
            "interaction",
            "marker",
            "lightning_bolt",
            "ominous_item_spawner",
            "lingering_potion"
    );

    private static boolean isBanned(ResourceLocation id) {
        return id == null || BANNED_ENTITY_PATHS.contains(id.getPath());
    }

    public static List<EntityType<?>> getAllEntityTypes() {
        return BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> !isBanned(BuiltInRegistries.ENTITY_TYPE.getKey(type)))
                .collect(Collectors.toList());
    }
}
