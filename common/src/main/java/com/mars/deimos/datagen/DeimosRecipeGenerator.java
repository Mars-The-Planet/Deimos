package com.mars.deimos.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeimosRecipeGenerator {
    public static List<JsonObject> RECIPES = new ArrayList<>();

    public static void createItemConvertorJson(String input, String output, int count) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(input.contains("#") ? input : Identifier.parse(input).toString());
        json.add("ingredients", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", (Identifier.parse(output)).toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createShapelessRecipeJson(ArrayList<String> input, String output, int count){
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonArray ingredientsArray = new JsonArray();
        for(String item : input){
            ingredientsArray.add(item.contains("#") ? item : Identifier.parse(item).toString());
        }
        json.add("ingredients", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", (Identifier.parse(output)).toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createShapedRecipeJson(ArrayList<String> inputs, ArrayList<String> pattern, String output, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");
        JsonArray jsonArray = new JsonArray();
        for(String line : pattern){
            jsonArray.add(line);
        }
        json.add("pattern", jsonArray);

        JsonObject keyList = new JsonObject();

        ArrayList<String> keys = new ArrayList<>();
        int keyCounter = 0;
        for (String row : pattern) {
            for (int i = 0; i < row.length(); i++) {
                String potentialKey = String.valueOf(row.charAt(i));
                if (!Objects.equals(potentialKey, " ") && !keys.contains(potentialKey)) {
                    keyList.addProperty(potentialKey, (Identifier.parse(inputs.get(keyCounter))).toString());
                    keys.add(potentialKey);
                    keyCounter++;
                }
            }
        }

        json.add("key", keyList);

        JsonObject result = new JsonObject();
        result.addProperty("id", (Identifier.parse(output)).toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createShapedRecipeJson(ArrayList<String> items, ArrayList<String> pattern, String output) {
        createShapedRecipeJson(items, pattern, output, 1);
    }

    @Deprecated
    public static void createShapedRecipeJson(ArrayList<Character> keys, ArrayList<String> items, ArrayList<String> pattern, String output, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");
        JsonArray jsonArray = new JsonArray();
        for(String line : pattern){
            jsonArray.add(line);
        }
        json.add("pattern", jsonArray);

        JsonObject keyList = new JsonObject();

        for (int i = 0; i < keys.size(); ++i) {
            keyList.addProperty(keys.get(i) + "", items.get(i).contains("#") ? items.get(i) : Identifier.parse(items.get(i)).toString());
        }

        json.add("key", keyList);

        JsonObject result = new JsonObject();
        result.addProperty("id", (Identifier.parse(output)).toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    @Deprecated
    public static void createShapedRecipeJson(ArrayList<Character> keys, ArrayList<String> items, ArrayList<String> type, ArrayList<String> pattern, String output, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");
        JsonArray jsonArray = new JsonArray();
        for(String line : pattern){
            jsonArray.add(line);
        }
        json.add("pattern", jsonArray);

        JsonObject keyList = new JsonObject();

        for (int i = 0; i < keys.size(); ++i) {
            if(type.get(i).matches("tag"))
                keyList.addProperty(keys.get(i) + "", (Identifier.parse("#" + items.get(i))).toString());
            else
                keyList.addProperty(keys.get(i) + "", (Identifier.parse(items.get(i))).toString());
        }

        json.add("key", keyList);

        JsonObject result = new JsonObject();
        result.addProperty("id", (Identifier.parse(output)).toString());
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

        json.addProperty("ingredient", input.contains("#") ? input : Identifier.parse(input).toString());

        JsonObject result = new JsonObject();
        result.addProperty("id", (Identifier.parse(output)).toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createBlastingJson(String input, String output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:blasting");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        json.addProperty("ingredient", input.contains("#") ? input : Identifier.parse(input).toString());

        JsonObject result = new JsonObject();
        result.addProperty("id", Identifier.parse(output).toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createSmokingJson(String input, String output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:smoking");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        json.addProperty("ingredient", input.contains("#") ? input : Identifier.parse(input).toString());

        JsonObject result = new JsonObject();
        result.addProperty("id", (Identifier.parse(output)).toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createCampfireCookingJson(String input, String output, int cookingTime) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:campfire_cooking");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);

        json.addProperty("ingredient", input.contains("#") ? input : Identifier.parse(input).toString());

        JsonObject result = new JsonObject();
        result.addProperty("id", (Identifier.parse(output)).toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    @Deprecated
    public static void createCampfireCookingJson(String input, String output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:campfire_cooking");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        json.addProperty("ingredient", Identifier.parse(input).toString());

        JsonObject result = new JsonObject();
        result.addProperty("id", (Identifier.parse(output)).toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createStoneCuttingJson(String input, String output, int count) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:stonecutting");

        json.addProperty("ingredient", input.contains("#") ? input : Identifier.parse(input).toString());

        JsonObject result = new JsonObject();
        result.addProperty("id", (Identifier.parse(output)).toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createItemConvertorJson(Identifier input, Identifier output, int count) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonArray ingredientsArray = new JsonArray();
        ingredientsArray.add(input.toString());
        json.add("ingredients", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createShapelessRecipeJson(ArrayList<Identifier> input, ArrayList<String> type, Identifier output, int count){
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonArray ingredientsArray = new JsonArray();
        int i = 0;
        for(Identifier item : input){
            if(type.get(i).matches("tag"))
                ingredientsArray.add("#" + item.toString());
            else
                ingredientsArray.add(item.toString());
            i++;
        }
        json.add("ingredients", ingredientsArray);

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createShapedRecipeJson(ArrayList<Character> keys, ArrayList<Identifier> items, ArrayList<String> type, ArrayList<String> pattern, Identifier output, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");
        JsonArray jsonArray = new JsonArray();
        for(String line : pattern){
            jsonArray.add(line);
        }
        json.add("pattern", jsonArray);

        JsonObject keyList = new JsonObject();

        for (int i = 0; i < keys.size(); ++i) {
            if(type.get(i).matches("tag"))
                keyList.addProperty(keys.get(i) + "", "#" + items.get(i).toString());
            else
                keyList.addProperty(keys.get(i) + "", items.get(i).toString());
        }

        json.add("key", keyList);

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createSmeltingJson(Identifier input, Identifier output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:smelting");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        json.addProperty("ingredient", input.toString());

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createBlastingJson(Identifier input, Identifier output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:blasting");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        json.addProperty("ingredient", input.toString());

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createSmokingJson(Identifier input, Identifier output, int cookingTime, float experience) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:smoking");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);
        json.addProperty("experience", experience);

        json.addProperty("ingredient", input.toString());

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createCampfireCookingJson(Identifier input, Identifier output, int cookingTime) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:campfire_cooking");
        json.addProperty("category", "misc");
        json.addProperty("cookingtime", cookingTime);

        json.addProperty("ingredient", input.toString());

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        json.add("result", result);

        RECIPES.add(json);
    }

    public static void createStoneCuttingJson(Identifier input, Identifier output, int count) {
        JsonObject json = new JsonObject();

        json.addProperty("type", "minecraft:stonecutting");

        json.addProperty("ingredient", input.toString());

        JsonObject result = new JsonObject();
        result.addProperty("id", output.toString());
        result.addProperty("count", count);
        json.add("result", result);

        RECIPES.add(json);
    }
}
