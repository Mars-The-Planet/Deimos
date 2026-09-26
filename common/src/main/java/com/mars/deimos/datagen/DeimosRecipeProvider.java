package com.mars.deimos.datagen;

import com.google.gson.JsonElement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Recipe;

@FunctionalInterface
public interface DeimosRecipeProvider {
    void generate(HolderLookup<Recipe<?>> existingRecipes, RegistryOps<JsonElement> ops);
}
