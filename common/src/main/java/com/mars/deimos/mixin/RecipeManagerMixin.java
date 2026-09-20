package com.mars.deimos.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mars.deimos.datagen.DeimosRecipeGenerator;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.mars.deimos.Constants.MOD_ID;

@Mixin(value = RecipeManager.class, priority = 1100)
public class RecipeManagerMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Lnet/minecraft/core/HolderLookup;)Lnet/minecraft/world/item/crafting/RecipeMap;"
            )
    )
    private RecipeMap injectDeimosRecipes(HolderLookup<Recipe<?>> originalRecipes, HolderLookup.Provider registries) {
        RecipeMap baseMap = RecipeMap.create(originalRecipes);

        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> byType = ImmutableMultimap.builder();
        ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey = ImmutableMap.builder();

        // Copy the vanilla recipes
        for (RecipeHolder<?> holder : baseMap.values()) {
            byType.put(holder.value().getType(), holder);
            byKey.put(holder.id(), holder);
        }

        // Inject dynamic recipes
        if (DeimosRecipeGenerator.RECIPES != null) {
            RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
            int i = 0;

            for (JsonElement jsonElement : DeimosRecipeGenerator.RECIPES) {
                int finalI = i;
                Recipe.CODEC.parse(ops, jsonElement).ifSuccess((parsed) -> {

                    // Extract the actual Recipe from the Holder$Direct returned by the 26.3 codec
                    Recipe<?> recipeInstance;
                    if (parsed instanceof net.minecraft.core.Holder<?> holder) {
                        recipeInstance = (Recipe<?>) holder.value();
                    } else {
                        recipeInstance = (Recipe<?>) parsed;
                    }

                    ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(
                            Registries.RECIPE,
                            Identifier.fromNamespaceAndPath(MOD_ID, "deimosgeneratedcrafting" + finalI)
                    );

                    RecipeHolder<?> recipeHolder = new RecipeHolder<>(resourceKey, recipeInstance);

                    byType.put(recipeInstance.getType(), recipeHolder);
                    byKey.put(resourceKey, recipeHolder);

                }).ifError((error) -> System.out.print("FAILED TO PARSE DEIMOS RECIPE: " + error));

                i++;
            }
        }

        // Construct the new RecipeMap reflectively
        try {
            java.lang.reflect.Constructor<RecipeMap> constructor = RecipeMap.class.getDeclaredConstructor(Multimap.class, java.util.Map.class);
            constructor.setAccessible(true);
            return constructor.newInstance(byType.build(), byKey.build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct RecipeMap with Deimos recipes", e);
        }
    }
}
