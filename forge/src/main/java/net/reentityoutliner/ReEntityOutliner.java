package net.reentityoutliner;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.reentityoutliner.config.ConfigManager;
import net.reentityoutliner.config.ConfigScreen;

import static net.reentityoutliner.config.ConfigManager.*;

@Mod(Constants.MOD_ID)
public class ReEntityOutliner {

    public ReEntityOutliner() {
        // Le Mod Event Bus (pour l'enregistrement des touches)
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Initialisation de la config
        ConfigPath = FMLPaths.CONFIGDIR.get();
        ConfigManager.load();

        // Enregistrement du bouton "Configuration" dans la liste des mods de Forge
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new ConfigScreen(parent))
        );

        // Enregistrement des KeyBindings sur le bus du MOD
        modEventBus.addListener(this::registerKeyBinding);

        // Enregistrement du Tick sur le bus de FORGE (Global)
        MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onClientTick(TickEvent.ClientTickEvent event) {
        // On vérifie que le tick est en phase END (pour éviter de traiter deux fois par tick)
        if (event.phase == TickEvent.Phase.END) {
            Minecraft client = Minecraft.getInstance();

            // On vérifie que le client n'est pas nul (sécurité)
            if (client.player == null) return;

            while (CONFIG_BIND.consumeClick()) {
                client.setScreen(new ConfigScreen(null));
            }

            while (OUTLINE_BIND.consumeClick()) {
                setOutliningEntities(!isOutliningEntities());
            }
        }
    }

    public void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(CONFIG_BIND);
        event.register(OUTLINE_BIND);
    }
}