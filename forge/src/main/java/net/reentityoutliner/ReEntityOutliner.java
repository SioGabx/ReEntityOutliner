package net.reentityoutliner;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.reentityoutliner.config.ConfigManager;
import net.reentityoutliner.config.ConfigScreen;
import net.minecraftforge.eventbus.api.bus.*;


import static net.reentityoutliner.config.ConfigManager.*;

@Mod(Constants.MOD_ID)
public class ReEntityOutliner {

    public ReEntityOutliner(FMLJavaModLoadingContext modLoadingContext) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello Forge world!");

        ConfigPath = FMLPaths.CONFIGDIR.get();
        ConfigManager.load();

        //Add ConfigScreen screen to config button
        modLoadingContext.registerExtensionPoint(
                net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new ConfigScreen(parent))
        );

        BusGroup busGroup = modLoadingContext.getModBusGroup();
        RegisterKeyMappingsEvent.BUS.addListener(this::registerKeyBinding);

        TickEvent.ClientTickEvent.Pre.BUS.addListener(this::onClientTick);

    }

    private void onClientTick(TickEvent.ClientTickEvent.Pre pre) {
        Minecraft client = Minecraft.getInstance();
        while (CONFIG_BIND.consumeClick()) {
            client.setScreen(new ConfigScreen(null));
        }
        while (OUTLINE_BIND.consumeClick()) {
            setOutliningEntities(!isOutliningEntities());
        }
    }


    public void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(CONFIG_BIND);
        event.register(OUTLINE_BIND);
    }

}
