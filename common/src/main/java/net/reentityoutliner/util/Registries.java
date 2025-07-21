package net.reentityoutliner.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.stream.Collectors;

public class Registries {

    public static List<EntityType<?>> getAllEntityTypes() {
        return BuiltInRegistries.ENTITY_TYPE.stream().collect(Collectors.toList());
    }

    public static List<ResourceLocation> getAllEntityTypeIds() {
        return BuiltInRegistries.ENTITY_TYPE.keySet().stream().collect(Collectors.toList());
    }


}
