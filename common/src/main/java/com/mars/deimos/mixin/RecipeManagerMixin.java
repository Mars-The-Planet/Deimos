package com.mars.deimos.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mars.deimos.datagen.AppendingHolderLookup;
import com.mars.deimos.datagen.DeimosRecipeGenerator;
import com.mars.deimos.datagen.DeimosRecipeHolders;
import com.mars.deimos.datagen.DeimosRecipeProvider;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = RecipeManager.class, priority = 1100)
public class RecipeManagerMixin {

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Lnet/minecraft/core/HolderLookup;)Lnet/minecraft/world/item/crafting/RecipeMap;"))
    private RecipeMap deimos$injectRecipes(HolderLookup<Recipe<?>> original, Operation<RecipeMap> operation, @Local(argsOnly = true) HolderLookup.Provider registries) {
        RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);

        DeimosRecipeGenerator.RECIPES = new ArrayList<>();
        for (DeimosRecipeProvider provider : DeimosRecipeGenerator.PROVIDERS) {
            provider.generate(original, ops);
        }

        List<Holder.Reference<Recipe<?>>> extra = DeimosRecipeHolders.build(registries);
        return extra.isEmpty()
                ? operation.call(original)
                : operation.call(new AppendingHolderLookup<>(original, extra));
    }
}
