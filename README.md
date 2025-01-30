![Title](https://i.imgur.com/sFx7o7p.png)

[![Discord](https://img.shields.io/discord/1027252425960198165?color=5b6ee1&label=Discord&style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/2CUh6gMuCt) [![Support me on ko-fi](https://img.shields.io/badge/ko--fi-donate-FF5E5B?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/mars_) [![CurseForge](https://img.shields.io/curseforge/dt/1158094?color=F16436&logo=curseforge&logoColor=white&label=Curseforge&style=for-the-badge)](https://www.curseforge.com/minecraft/mc-mods/deimos-fabric-forge-neoforge) [![Modrinth](https://img.shields.io/modrinth/dt/deimos?style=for-the-badge&logo=modrinth&logoColor=white&label=modrinth&color=00AF5C)](https://modrinth.com/mod/deimos)

Deimos is a data generation and configuration Minecraft library. With it, you can generate config files and display them in-game natively on Forge and Neoforge or with the help of the [Mod Menu](https://modrinth.com/mod/modmenu) on Fabric. Deimos allows you to create new recipes when the game starts, which makes them configurable. This also means you don't have to use JSON files, and changing Minecraft versions becomes significantly easier and less painful.

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
## How to use it?
### Creating configs
You can add configs in a class that extends DeimosConfig. 
```java

public class TestConfig extends DeimosConfig {
    @Entry public static int test_int = 6;
    @Entry public static List<String> test_string_list = Lists.newArrayList(
            "minecraft:acacia_planks", "minecraft:andesite");
}
```
And t
### Adding new recipes
Notice that you can use values from your config file and if the player changes and restarts the game the recipe also changes. 