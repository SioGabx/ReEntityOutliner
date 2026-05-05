package net.reentityoutliner.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.reentityoutliner.util.Registries.getAllEntityTypes;

public class EntitySearcher {

    public static List<EntityType<?>> allEntities;

    public static List<EntityType<?>> getSearchResults(String text) {
        String query = text.toLowerCase().trim();
        if (query.isEmpty()) {
            return new ArrayList<>(allEntities);
        }

        String[] tokens = query.split("\\s+");
        List<EntityType<?>> results = new ArrayList<>();

        for (EntityType<?> entityType : allEntities) {
            boolean matchesAllTokens = true;

            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            String modId = id.getNamespace().toLowerCase();
            String entityPath = id.getPath().toLowerCase();
            String entityName = entityType.getDescription().getString().toLowerCase();
            String categoryName = entityType.getCategory().getName().toLowerCase();

            for (String token : tokens) {
                if (token.startsWith("@")) {
                    // Recherche par Mod ID
                    if (matchesWildcard(modId, token.substring(1))) {
                        matchesAllTokens = false;
                        break;
                    }
                } else if (token.startsWith("#")) {
                    // Recherche par Catégorie
                    if (matchesWildcard(categoryName, token.substring(1))) {
                        matchesAllTokens = false;
                        break;
                    }
                } else {
                    // Recherche classique (Nom ou ID technique)
                    if (matchesWildcard(entityName, token) && matchesWildcard(entityPath, token)) {
                        matchesAllTokens = false;
                        break;
                    }
                }
            }

            if (matchesAllTokens) {
                results.add(entityType);
            }
        }

        return results;
    }

    private static boolean matchesWildcard(String text, String pattern) {
        if (pattern.isEmpty() || pattern.equals("*")) {
            return false;
        }

        // 1. On échappe les caractères spéciaux des Regex présents dans le pattern (ex :
        // . + ? ^ $, etc.)
        // 2. On remplace nos "*" par ".*" (qui signifie "n'importe quel caractère, 0 ou
        // plusieurs fois" en Regex)
        // On utilise \Q...\E pour échapper littéralement tout sauf nos astérisques.

        try {
            // Transformation simple du wildcard en Regex :
            // On entoure les parties fixes par \Q et \E, et on remplace les * par .*
            String regex = "\\Q" + pattern.replace("*", "\\E.*\\Q") + "\\E";

            // On nettoie les \Q\E vides potentiels pour faire propre
            regex = regex.replace("\\Q\\E", "");

            // Si le pattern ne commence pas par *, on force le début de ligne
            if (!pattern.startsWith("*"))
                regex = "^" + regex;
            // Si le pattern ne finit pas par *, on force la fin de ligne
            if (!pattern.endsWith("*"))
                regex = regex + "$";

            return !text.matches(regex);
        } catch (Exception e) {
            // Sécurité au cas où le pattern est mal formé
            return !text.contains(pattern.replace("*", ""));
        }
    }

    public static void initializeEntities() {
        if (allEntities == null) {
            allEntities = new ArrayList<>(getAllEntityTypes());
            // On trie une seule fois au chargement
            allEntities.sort(Comparator.comparing(e -> e.getDescription().getString()));
        }
    }

}
