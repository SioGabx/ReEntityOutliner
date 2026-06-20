package net.reentityoutliner;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.reentityoutliner.config.ConfigManager;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.reentityoutliner.config.ConfigScreen;

import static net.reentityoutliner.config.ConfigManager.*;

public class ReEntityOutliner implements ModInitializer {


    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.

        KeyMappingHelper.registerKeyMapping(CONFIG_BIND);
        KeyMappingHelper.registerKeyMapping(OUTLINE_BIND);
        ConfigPath = FabricLoader.getInstance().getConfigDir();
        ConfigManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(Minecraft client) {
        while (OUTLINE_BIND.consumeClick()) {
            setOutliningEntities(!isOutliningEntities());
        }

        while (CONFIG_BIND.consumeClick()) {
            client.gui.setScreen(new ConfigScreen(null));
        }
    }
}
