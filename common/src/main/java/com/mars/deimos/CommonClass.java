package com.mars.deimos;

import com.google.common.collect.Lists;
import com.mars.deimos.datagen.DeimosRecipeGenerator;

public class CommonClass {
    public static void init() {
        DeimosRecipeGenerator.createShapedRecipeJson(
                Lists.newArrayList("stick", "minecraft:stone"),
                Lists.newArrayList(
                        "#B",
                        "##"
                ),
                "stone");
    }
}
