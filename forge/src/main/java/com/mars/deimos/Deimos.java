package com.mars.deimos;

import com.mars.deimos.config.DeimosConfig;
import com.mars.deimos.packets.SyncPacket;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.*;

import static com.mars.deimos.Constants.*;
import static com.mars.deimos.config.DeimosConfigScreenClass.getScreen;

@Mod(MOD_ID)
public class Deimos extends ServerClass {
    public static SimpleChannel channel;

    public Deimos() {
        CommonClass.init();
        channel = ChannelBuilder.named(PACKET_IDENTIFIER)
                .clientAcceptedVersions(Channel.VersionTest.exact(1))
                .serverAcceptedVersions(Channel.VersionTest.exact(1))
                .networkProtocolVersion(1)
                .simpleChannel();

        channel.messageBuilder(SyncPacket.class)
                .encoder(SyncPacket::write)
                .decoder(SyncPacket::new)
                .consumerMainThread((packet,ctx) -> {
                    if(ctx.isClientSide()) {
                        ctx.enqueueWork(() -> {
                            ClientClass.applyServerConfig(packet, ctx.getConnection()::disconnect);
                        });
                        ctx.setPacketHandled(true);
                    }
                }).direction(PacketFlow.CLIENTBOUND).add();
        channel.build();

        serverinit(false);
    }


    @Override
    public void sendToPlayer(Connection connection) {
        channel.send(new SyncPacket(DeimosConfig.configClass), connection);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onPostInit(FMLClientSetupEvent event) {
            ModList.get().forEachModContainer((modid, modContainer) -> {
                if (DeimosConfig.configClass.containsKey(modid)) {
                    modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                            new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> getScreen(parent, modid)));
                }
            });
        }

        @SubscribeEvent
        public static void onInit(FMLClientSetupEvent event) {
            ClientClass.clientinit();
        }

    }
}
