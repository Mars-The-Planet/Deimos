package com.mars.deimos.datagen;

import com.google.gson.JsonElement;
import com.mars.deimos.mixin.HolderReferenceAccessor;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

import static com.mars.deimos.Constants.LOG;
import static com.mars.deimos.Constants.MOD_ID;

public class DeimosRecipeHolders {
    private static final HolderOwner<Recipe<?>> OWNER = new HolderOwner<>() {};

    @SuppressWarnings("unchecked")
    public static List<Holder.Reference<Recipe<?>>> build(HolderLookup.Provider registries) {
        if (DeimosRecipeGenerator.RECIPES == null) return List.of();

        RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
        List<Holder.Reference<Recipe<?>>> result = new ArrayList<>();
        int i = 0;

        for (JsonElement json : DeimosRecipeGenerator.RECIPES) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(
                    Registries.RECIPE,
                    Identifier.fromNamespaceAndPath(MOD_ID, "deimosgeneratedcrafting" + i++));

            Recipe.CODEC.parse(ops, json)
                    .ifSuccess(parsed -> {
                        Recipe<?> recipe = parsed instanceof Holder<?> h ? (Recipe<?>) h.value() : (Recipe<?>) parsed;
                        Holder.Reference<Recipe<?>> ref = Holder.Reference.createStandAlone(OWNER, key);
                        ((HolderReferenceAccessor<Recipe<?>>) (Object) ref).deimos$bindValue(recipe);
                        result.add(ref);
                    })
                    .ifError(err -> LOG.error("Failed to parse Deimos recipe {}: {}", key.identifier(), err.message()));
        }
        return result;
    }
}
