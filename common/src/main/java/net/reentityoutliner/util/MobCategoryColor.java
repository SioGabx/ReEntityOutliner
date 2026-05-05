package net.reentityoutliner.util;

import net.minecraft.world.entity.MobCategory;

import java.util.Map;

public enum MobCategoryColor {
    WHITE(255, 255, 255, "WHITE"),
    BLACK(0, 0, 0, "BLACK"),
    RED(255, 0, 0, "RED"),
    ORANGE(255, 127, 0, "ORANGE"),
    YELLOW(255, 255, 0, "YELLOW"),
    GREEN(0, 255, 0, "GREEN"),
    BLUE(0, 0, 255, "BLUE"),
    PURPLE(127, 0, 127, "PURPLE"),
    PINK(255, 155, 182, "PINK"),
    CYAN(0, 255, 255, "CYAN");

    public final int red;
    public final int green;
    public final int blue;
    public final String colorName;

    private static final Map<MobCategory, MobCategoryColor> spawnGroupColors = Map.of(
            MobCategory.AMBIENT, MobCategoryColor.PURPLE,
            MobCategory.AXOLOTLS, MobCategoryColor.PINK,
            MobCategory.CREATURE, MobCategoryColor.YELLOW,
            MobCategory.MISC, MobCategoryColor.WHITE,
            MobCategory.MONSTER, MobCategoryColor.RED,
            MobCategory.UNDERGROUND_WATER_CREATURE, MobCategoryColor.ORANGE,
            MobCategory.WATER_AMBIENT, MobCategoryColor.GREEN,
            MobCategory.WATER_CREATURE, MobCategoryColor.BLUE
    );

    private static final MobCategoryColor[] colors = MobCategoryColor.values();

    MobCategoryColor(int red, int green, int blue, String colorName) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.colorName = colorName;
    }

    public static MobCategoryColor of(MobCategory group) {
        return spawnGroupColors.getOrDefault(group, MobCategoryColor.WHITE);
    }

    public MobCategoryColor next() {
        return get((this.ordinal() + 1) % colors.length);
    }

    public MobCategoryColor get(int index) {
        return colors[index];
    }
}
