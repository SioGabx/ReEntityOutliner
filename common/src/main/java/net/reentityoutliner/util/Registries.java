package net.reentityoutliner.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Registries {

    // Liste des entités bannies (celles qui n'ont pas de rendu ou ne devraient pas être surlignées)
    private static final Set<String> BANNED_ENTITY_PATHS = Set.of(
            "experience_orb",
            "area_effect_cloud",
            "interaction",
            "marker",
            "lightning_bolt",
            "ominous_item_spawner", // Note : cette entité est apparue en 1.21, elle sera ignorée en 1.20.1
            "lingering_potion"
    );

    private static boolean isBanned(ResourceLocation id) {
        // En 1.20.1, on utilise ResourceLocation au lieu d'Identifier
        return id == null || BANNED_ENTITY_PATHS.contains(id.getPath());
    }

    public static List<EntityType<?>> getAllEntityTypes() {
        // BuiltInRegistries est bien présent en 1.20.1
        return BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> !isBanned(BuiltInRegistries.ENTITY_TYPE.getKey(type)))
                .collect(Collectors.toList());
    }
}