package com.mars.deimos;

import com.mars.deimos.config.DeimosConfig;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.mars.deimos.Constants.MOD_ID;
import static com.mars.deimos.config.DeimosConfig.DeimosConfigScreen.getScreen;

@Mod(MOD_ID)
public class Deimos {
    public Deimos(FMLJavaModLoadingContext context) {
        CommonClass.init();

        var modBusGroup = context.getModBusGroup();
        FMLClientSetupEvent.getBus(modBusGroup).addListener(Deimos::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        ModList.forEachModContainer((modid, modContainer) -> {
            if (DeimosConfig.configClass.containsKey(modid)) {
                modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> getScreen(parent, modid)
                ));
            }
        });
    }
}
