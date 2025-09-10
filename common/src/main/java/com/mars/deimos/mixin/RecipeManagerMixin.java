package com.mars.deimos.mixin;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import static com.mars.deimos.datagen.DeimosRecipeGenerator.RECIPES;

@Mixin(value = RecipeManager.class, priority = 1100)
public class RecipeManagerMixin {
    @Inject(method = "apply*", at = @At("HEAD"))
    public void interceptApply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo info) {
        if(RECIPES != null){
            int i = 0;
            for(JsonElement jsonElement : RECIPES){
                if(jsonElement != null){
                    map.put(new ResourceLocation("deimosgeneratedcrafting" + i), jsonElement);
                    i++;
                }
            }
        }
    }
}
