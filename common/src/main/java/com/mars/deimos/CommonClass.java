package com.mars.deimos;

import com.google.common.collect.Lists;
import com.mars.deimos.datagen.DeimosRecipeGenerator;
import com.mars.deimos.platform.Services;
import com.mars.deimos.platform.services.IPlatformHelper;

public class CommonClass {
    public static final IPlatformHelper PLATFORM = Services.load(IPlatformHelper.class);
    public static void init() {
        DeimosRecipeGenerator.createShapelessRecipeJson(Lists.newArrayList("paper", "#minecraft:metal_nuggets "), "name_tag", 1);
    }
}
