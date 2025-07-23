package net.reentityoutliner;


import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.reentityoutliner.config.ConfigManager;
import net.reentityoutliner.config.ConfigScreen;
import org.lwjgl.glfw.GLFW;

import static net.reentityoutliner.config.ConfigManager.*;

@Mod(Constants.MOD_ID)
public class ReEntityOutliner {

    public ReEntityOutliner(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");

        ConfigPath = FMLPaths.CONFIGDIR.get();
        ConfigManager.load();

        var modContainer = ModList.get().getModContainerById(Constants.MOD_ID).orElseThrow();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (mc, screen) -> new ConfigScreen(screen));
        NeoForge.EVENT_BUS.register(this);
        eventBus.register(ReEntityOutliner.ModBusEvents.class);
    }

    public static class ModBusEvents {

        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(CONFIG_BIND);
            event.register(OUTLINE_BIND);
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (CONFIG_BIND.consumeClick()) {
            client.setScreen(new ConfigScreen(null));
        }
        while (OUTLINE_BIND.consumeClick()) {
            setOutliningEntities(!isOutliningEntities());
        }
    }

}
