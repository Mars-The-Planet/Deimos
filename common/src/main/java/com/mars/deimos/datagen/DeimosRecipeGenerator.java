package com.mars.deimos.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class DeimosRecipeGenerator {
    public static List<JsonObject> RECIPES = new ArrayList<>();

    public static void createItemConvertorJson(String input, String output, int count) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", (ResourceLocation.parse(input)).toString());
        (ResourceLocation.parse(input)).toString();

        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredients", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", (ResourceLocation.parse(output)).toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createShapelessRecipeJson(ArrayList<String> input, ArrayList<String> type, String output, int count){
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonArray ingredientsArray = new JsonArray();
        int i = 0;
        for(String item : input){
            JsonObject ingredients = new JsonObject();
            ingredients.addProperty(type.get(i), item.toString());
            ingredientsArray.add(ingredients);
            i++;
        }
        json.add("ingredients", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", (ResourceLocation.parse(output)).toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createShapedRecipeJson(ArrayList<Character> keys, ArrayList<String> items, ArrayList<String> type, ArrayList<String> pattern, String output, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");
        JsonArray jsonArray = new JsonArray();
        for(String line : pattern){
            jsonArray.add(line);
        }
        json.add("pattern", jsonArray);

        JsonObject individualKey;
        JsonObject keyList = new JsonObject();

        for (int i = 0; i < keys.size(); ++i) {
            individualKey = new JsonObject();
            individualKey.addProperty(type.get(i), (ResourceLocation.parse(items.get(i))).toString());
            keyList.add(keys.get(i) + "", individualKey);
        }

        json.add("key", keyList);

        JsonObject result = new JsonObject();
        result.addProperty("id", (ResourceLocation.parse(output)).toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createSmeltingJson(String input, String output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:smelting");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", (ResourceLocation.parse(input)).toString());
        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredient", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", (ResourceLocation.parse(output)).toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createBlastingJson(String input, String output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:blasting");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", (ResourceLocation.parse(input)).toString());
        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredient", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", (ResourceLocation.parse(output)).toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createSmokingJson(String input, String output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:smoking");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", (ResourceLocation.parse(input)).toString());
        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredient", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", (ResourceLocation.parse(output)).toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createCampfireCookingJson(String input, String output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:campfire_cooking");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", (ResourceLocation.parse(input)).toString());
        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredient", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", (ResourceLocation.parse(output)).toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createStoneCuttingJson(String input, String output, int count) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:stonecutting");
        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", (ResourceLocation.parse(input)).toString());

        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredient", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", (ResourceLocation.parse(output)).toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createItemConvertorJson(ResourceLocation input, ResourceLocation output, int count) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", input.toString());

        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredients", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createShapelessRecipeJson(ArrayList<ResourceLocation> input, ArrayList<String> type, ResourceLocation output, int count){
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonArray ingredientsArray = new JsonArray();
        int i = 0;
        for(ResourceLocation item : input){
            JsonObject ingredients = new JsonObject();
            ingredients.addProperty(type.get(i), item.toString());
            ingredientsArray.add(ingredients);
            i++;
        }
        json.add("ingredients", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createShapedRecipeJson(ArrayList<Character> keys, ArrayList<ResourceLocation> items, ArrayList<String> type, ArrayList<String> pattern, ResourceLocation output, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");
        JsonArray jsonArray = new JsonArray();
        for(String line : pattern){
            jsonArray.add(line);
        }
        json.add("pattern", jsonArray);

        JsonObject individualKey;
        JsonObject keyList = new JsonObject();

        for (int i = 0; i < keys.size(); ++i) {
            individualKey = new JsonObject();
            individualKey.addProperty(type.get(i), items.get(i).toString());
            keyList.add(keys.get(i) + "", individualKey);
        }

        json.add("key", keyList);

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createSmeltingJson(ResourceLocation input, ResourceLocation output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:smelting");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", input.toString());
        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredient", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createBlastingJson(ResourceLocation input, ResourceLocation output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:blasting");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", input.toString());
        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredient", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createSmokingJson(ResourceLocation input, ResourceLocation output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:smoking");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", input.toString());
        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredient", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createCampfireCookingJson(ResourceLocation input, ResourceLocation output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:campfire_cooking");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", input.toString());
        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredient", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createStoneCuttingJson(ResourceLocation input, ResourceLocation output, int count) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:stonecutting");
        JsonObject ingredients = new JsonObject();
        ingredients.addProperty("item", input.toString());

        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(ingredients);
        json.add("ingredient", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }
}
