package com.mars.deimos;

import com.mars.deimos.config.DeimosConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.mars.deimos.Constants.MOD_ID;
import static com.mars.deimos.config.DeimosConfig.DeimosConfigScreen.getScreen;

@Mod(Constants.MOD_ID)
public class Deimos {
    public Deimos() {
        CommonClass.init();
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onPostInit(FMLClientSetupEvent event) {
            ModList.get().forEachModContainer((modid, modContainer) -> {
                if (DeimosConfig.configClass.containsKey(modid)) {
                    modContainer.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () ->
                            new ConfigGuiHandler.ConfigGuiFactory((client, parent) -> getScreen(parent, modid)));
                }
            });
        }
    }
}
