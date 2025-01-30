package com.mars.deimos.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.HashMap;
import java.util.Map;

import static com.mars.deimos.Constants.MOD_ID;
import static com.mars.deimos.config.DeimosConfig.DeimosConfigScreen.getScreen;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> getScreen(parent, MOD_ID);
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        HashMap<String, ConfigScreenFactory<?>> map = new HashMap<>();
        DeimosConfig.configClass.forEach((modid, cClass) -> {
            map.put(modid, parent -> getScreen(parent, modid));
        });
        return map;
    }
}
