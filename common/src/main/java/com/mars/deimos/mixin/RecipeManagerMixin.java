package com.mars.deimos.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mars.deimos.datagen.DeimosRecipeGenerator;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static com.mars.deimos.Constants.MOD_ID;

@Mixin(value = RecipeManager.class, priority = 1100)
public class RecipeManagerMixin {
    @Shadow @Final
    private HolderLookup.Provider registries;

    @Inject(method = "prepare", at = @At(value = "TAIL"), cancellable = true)
    private void interceptPrepare(ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfoReturnable<RecipeMap> cir,
                                  @Local LocalRef<List<RecipeHolder<?>>> list) {
        if(DeimosRecipeGenerator.RECIPES != null){
            int i = 0;
            for(JsonElement jsonElement : DeimosRecipeGenerator.RECIPES){
                int finalI = i;
                Recipe.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), jsonElement).ifSuccess((parsed) -> {
                    ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MOD_ID, "deimosgeneratedcrafting" + finalI));
                    RecipeHolder<?> recipeHolder = new RecipeHolder<>(resourceKey, parsed);
                    List<RecipeHolder<?>> currentList = list.get();
                    currentList.add(recipeHolder);
                    list.set(currentList);
                    cir.setReturnValue(RecipeMap.create(list.get()));
                }).ifError((error) -> System.out.print("FAILED: " + error));
                i++;
            }
        }
    }
}
