package com.mars.deimos;

import com.mars.deimos.config.DeimosConfig;
import com.mars.deimos.packets.SyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.network.Connection;

public class Deimos extends ServerClass implements ModInitializer, ClientModInitializer {

    @Override
    public void onInitialize() {
        CommonClass.init();
        serverinit(false);
        PayloadTypeRegistry.configurationS2C().register(SyncPacket.PACKET_TYPE, SyncPacket.CODEC);
    }

    @Override
    public void onInitializeClient() {
        ClientClass.clientinit();
        ClientConfigurationNetworking.registerGlobalReceiver(SyncPacket.PACKET_TYPE, (CustomPayload, Context) -> Context.client().execute(() -> {
            SyncPacket sp = (SyncPacket) CustomPayload;
            ClientClass.applyServerConfig(sp, Context.responseSender()::disconnect);
        }));
    }

    @Override
    public void sendToPlayer(Connection connection) {
        connection.send(ServerConfigurationNetworking.createS2CPacket(new SyncPacket(DeimosConfig.configClass)));
    }
}
