package com.mars.deimos;

import com.mars.deimos.config.DeimosConfig;
import com.mars.deimos.packets.SyncPacket;
import net.minecraft.network.Connection;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.mars.deimos.Constants.MOD_ID;
import static com.mars.deimos.config.DeimosConfigScreenClass.getScreen;

@Mod(MOD_ID)
public class Deimos extends ServerClass {
    public Deimos(IEventBus eventBus) {
        CommonClass.init();
        serverinit(true);
        eventBus.addListener(this::registerConfigTask);
        eventBus.addListener(this::registerPackets);
    }

    public void registerConfigTask(final RegisterConfigurationTasksEvent event) {
        event.register(new SyncTask(event.getListener()));
    }

    public void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1").executesOn(HandlerThread.MAIN);
        registrar.configurationToClient(SyncPacket.PACKET_TYPE, SyncPacket.CODEC, (packet,ctx) -> {
            if(ctx.flow().isClientbound()) {
                ctx.enqueueWork(() -> {
                    SyncPacket sp = (SyncPacket) packet;
                    ClientClass.applyServerConfig(sp, ctx.connection()::disconnect);
                });
            }
        });
    }

    @Override
    public void sendToPlayer(Connection connection) {}


    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onPostInit(FMLClientSetupEvent event) {
            ModList.get().forEachModContainer((modid, modContainer) -> {
                if (DeimosConfig.configClass.containsKey(modid)) {
                    modContainer.registerExtensionPoint(IConfigScreenFactory.class, (minecraftClient, screen) -> getScreen(screen, modid));
                }
            });

            ClientClass.clientinit();
        }
    }
}
