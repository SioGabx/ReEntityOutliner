package net.reentityoutliner;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.reentityoutliner.config.ConfigManager;
import net.reentityoutliner.config.ConfigScreen;

import static net.reentityoutliner.config.ConfigManager.*;

public class ReEntityOutliner implements ModInitializer {

    @Override
    public void onInitialize() {
        // Enregistrement des touches (KeyMappings)
        KeyBindingHelper.registerKeyBinding(CONFIG_BIND);
        KeyBindingHelper.registerKeyBinding(OUTLINE_BIND);

        // Définition du chemin de configuration
        ConfigManager.ConfigPath = FabricLoader.getInstance().getConfigDir();

        // Chargement de la config
        ConfigManager.load();

        // Enregistrement du tick client
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(Minecraft client) {
        // Gestion de l'activation/désactivation du surlignage
        while (OUTLINE_BIND.consumeClick()) {
            setOutliningEntities(!isOutliningEntities());
        }

        // Ouverture du menu de configuration
        while (CONFIG_BIND.consumeClick()) {
            // Dans la 1.20.1, setScreen est la méthode standard
            client.setScreen(new ConfigScreen(null));
        }
    }
}