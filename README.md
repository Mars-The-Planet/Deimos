![Title](https://i.imgur.com/sFx7o7p.png)

[![Discord](https://img.shields.io/discord/1027252425960198165?color=5b6ee1&label=Discord&style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/2CUh6gMuCt) [![Support me on ko-fi](https://img.shields.io/badge/ko--fi-donate-FF5E5B?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/mars_) [![CurseForge](https://img.shields.io/curseforge/dt/1158094?color=F16436&logo=curseforge&logoColor=white&label=Curseforge&style=for-the-badge)](https://www.curseforge.com/minecraft/mc-mods/deimos-fabric-forge-neoforge) [![Modrinth](https://img.shields.io/modrinth/dt/deimos?style=for-the-badge&logo=modrinth&logoColor=white&label=modrinth&color=00AF5C)](https://modrinth.com/mod/deimos)

Deimos is a data generation and configuration Minecraft library. With it, you can generate config files and display them in-game natively on Forge and Neoforge or with the help of the [Mod Menu](https://modrinth.com/mod/modmenu) on Fabric. Deimos allows you to create new recipes when the game starts, which makes them configurable. This also means you don't have to use JSON files, and changing Minecraft versions becomes significantly easier and less painful.

I made this mod to simplify my mods' development and allow me to use just one config library across all mod loaders and Minecraft versions. So if you want to see some examples of how to use this library in the wild you can check out the [mods I made](https://modrinth.com/user/MarsThePlanet)

The configuration part of this library is based on [MidnightLib](https://www.curseforge.com/minecraft/mc-mods/midnightlib) by [Motschen](https://www.curseforge.com/members/motschen/projects).

## Setup:
### In your build.gradle:
```groovy
repositories {
    maven {
        url = "https://api.modrinth.com/maven"
    }
}
```
#### Forge and Neoforge:
```groovy
dependencies {
    implementation "maven.modrinth:deimos:${project.deimos_version}"
}
```
#### Fabric:
if you want to use the mod menu functionality you need to add a new repository:
```groovy
repositories {
    maven {
        name = "Terraformers"
        url = "https://maven.terraformersmc.com/"
    }
}
```
```groovy
dependencies {
    modImplementation "maven.modrinth:deimos:${project.deimos_version}"
    //if you want to use modmenu
    modCompileOnly "com.terraformersmc:modmenu:${project.modmenu_version}"
}
```
You can find the specific Deimos version you need on [Modrinth](https://modrinth.com/mod/deimos/versions)
## How to use it
### Creating configs
You can add configs in a class that extends DeimosConfig:
```java
public class TestConfig extends DeimosConfig {
    @Entry public static int test_int = 6;
    @Entry public static List<String> test_string_list = Lists.newArrayList(
            "minecraft:acacia_planks", "minecraft:andesite");
}
```
And then in your initialize method you need to call DeimosConfig.init like this:
```java
DeimosConfig.init(MOD_ID, TestConfig.class);
```
### Adding new recipes
To add new recipes you call methods from DeimosRecipeGenerator in your initialize method. You can add shapeless crafting, shaped crafting, smelting, smoking, blasting, campfire and stone cutting recipes. Here are some examples:
```java
DeimosRecipeGenerator.createSmeltingJson(TestConfig.test_string_list.get(0), TestConfig.test_string_list.get(1), TestConfig.test_int, 0.5F);

DeimosRecipeGenerator.createShapedRecipeJson(
        Lists.newArrayList('#'),
        Lists.newArrayList(ResourceLocation.parse("sand")),
        Lists.newArrayList("item"),
        Lists.newArrayList(
                "# ",
                " #"
        ),
        ResourceLocation.parse("stone"), 1);
```
Notice that you can use values from your config file. If the player changes them and restarts the game, the recipes will also change. 
This even works with moded items.
