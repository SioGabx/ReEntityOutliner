package net.reentityoutliner.mixin;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.reentityoutliner.Constants;
import net.reentityoutliner.config.ConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class MixinClientCommonPacketListener {

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void onCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (Constants.SERVER_DISABLE_CHANNEL.equals(packet.payload().type().id().toString())) {
            ConfigManager.disableOnServer();
        }
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        ConfigManager.onDisconnect();
    }
}
