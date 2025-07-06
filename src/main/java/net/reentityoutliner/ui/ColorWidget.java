package net.reentityoutliner.ui;

import net.reentityoutliner.util.EntityTypesSettings;

import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ColorWidget extends PressableWidget {
    private Color color;
    private final EntityType<?> entityType;

    private ColorWidget(int x, int y, int width, int height, Text message, EntityType<?> entityType) {
        super(x, y, width, height, message);
        this.entityType = entityType;

        EntityTypesSettings settings = EntitySelector.outlinedEntityTypes.get(this.entityType);
        if (settings != null && settings.outlined) {
            onShow();
        }
    }

    public ColorWidget(int x, int y, int width, int height, EntityType<?> entityType) {
        this(x, y, width, height, Text.translatable("options.chat.color"), entityType);
    }

    public void onShow() {
        this.color = EntitySelector.outlinedEntityTypes.get(this.entityType).color;
    }

    public void onPress() {
        onPress(0);
    }

    public void onPress(int button) {
        this.color = this.color.next();
        if (button == 1) {
            this.color = Color.of(entityType.getSpawnGroup());
        }
        EntityTypesSettings settings = EntitySelector.outlinedEntityTypes.get(this.entityType);
        if (settings != null) {
            settings.color = this.color;
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        super.renderWidget(context, mouseX, mouseY, delta);
        int color = (255 << 24) | (this.color.red << 16) | (this.color.green << 8) | this.color.blue;
        this.setMessage(this.color.colorName);
        this.drawMessage(context, minecraftClient.textRenderer, color);
    }


    public enum Color {
        WHITE(255, 255, 255, Text.of("WHITE")),
        BLACK(0, 0, 0, Text.of("BLACK")),
        RED(255, 0, 0, Text.of("RED")),
        ORANGE(255, 127, 0, Text.of("ORANGE")),
        YELLOW(255, 255, 0, Text.of("YELLOW")),
        GREEN(0, 255, 0, Text.of("GREEN")),
        BLUE(0, 0, 255, Text.of("BLUE")),
        PURPLE(127, 0, 127, Text.of("PURPLE")),
        PINK(255, 155, 182, Text.of("PINK")),
        CYAN(0, 255, 255, Text.of("CYAN"));

        public final int red;
        public final int green;
        public final int blue;
        public final Text colorName;

        private static final Map<SpawnGroup, Color> spawnGroupColors = Map.of(
                SpawnGroup.AMBIENT, Color.PURPLE,
                SpawnGroup.AXOLOTLS, Color.PINK,
                SpawnGroup.CREATURE, Color.YELLOW,
                SpawnGroup.MISC, Color.WHITE,
                SpawnGroup.MONSTER, Color.RED,
                SpawnGroup.UNDERGROUND_WATER_CREATURE, Color.ORANGE,
                SpawnGroup.WATER_AMBIENT, Color.GREEN,
                SpawnGroup.WATER_CREATURE, Color.BLUE
        );

        private static final Color[] colors = Color.values();

        Color(int red, int green, int blue, Text colorName) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.colorName = colorName;
        }

        public static Color of(SpawnGroup group) {
            return spawnGroupColors.get(group);
        }

        public Color next() {
            return get((this.ordinal() + 1) % colors.length);
        }

        public Color get(int index) {
            return colors[index];
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
