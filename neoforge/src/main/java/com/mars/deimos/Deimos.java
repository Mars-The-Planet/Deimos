package com.mars.deimos;

import com.mars.deimos.config.DeimosConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import static com.mars.deimos.Constants.MOD_ID;
import static com.mars.deimos.config.DeimosConfig.DeimosConfigScreen.getScreen;

@Mod(MOD_ID)
public class Deimos {
    public Deimos(IEventBus eventBus) {
        CommonClass.init();
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onPostInit(FMLClientSetupEvent event) {
            ModList.get().forEachModContainer((modid, modContainer) -> {
                if (DeimosConfig.configClass.containsKey(modid)) {
                    modContainer.registerExtensionPoint(IConfigScreenFactory.class, (minecraftClient, screen) -> getScreen(screen, modid));
                }
            });
        }
    }
}
