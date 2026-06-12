<center><p><img src="https://i.imgur.com/sFx7o7p.png" alt="Title" /></p></center>
<center><h5 style="text-align: center;"><strong> 
<a href="https://discord.gg/2CUh6gMuCt"><img src="https://img.shields.io/discord/1027252425960198165?color=5b6ee1&amp;label=Discord&amp;style=for-the-badge&amp;logo=discord&amp;logoColor=white" alt="Discord" /></a> 
<a href="https://patreon.com/MarsThePlanet"><img src="https://img.shields.io/endpoint.svg?url=https%3A%2F%2Fshieldsio-patreon.vercel.app%2Fapi%3Fusername%3DMarsThePlanet%26type%3Dpatrons&amp;style=for-the-badge" alt="Support me on Patreon" /></a> 
<a href="https://www.curseforge.com/minecraft/mc-mods/deimos-fabric-forge-neoforge" ><img src="https://img.shields.io/curseforge/dt/1158094?color=F16436&amp;logo=curseforge&amp;logoColor=white&amp;label=Curseforge&amp;style=for-the-badge" alt="CurseForge" /></a>
<a href="https://modrinth.com/mod/deimos" ><img src="https://img.shields.io/modrinth/dt/deimos?style=for-the-badge&amp;logo=modrinth&amp;logoColor=white&amp;label=modrinth&amp;color=00AF5C" alt="Modrinth" /></a>
</strong></h5></center>
<center><p style="text-align: center;">Deimos is a data generation and configuration Minecraft library. With it, you can generate config files and display them in-game natively on Forge and Neoforge or with the help of <a href="https://www.curseforge.com/minecraft/mc-mods/modmenu">Mod Menu</a> on Fabric. Deimos allows you to create new recipes when the game starts, which makes them configurable. This also means you don't have to use JSON files, and changing Minecraft versions becomes significantly easier and less painful.</p>
<p style="text-align: center;">The configuration part of this library is based on&nbsp;<a href="https://www.curseforge.com/minecraft/mc-mods/midnightlib">MidnightLib</a> by <a href="https://www.curseforge.com/members/motschen/projects">Motschen</a>.</p>
<p style="text-align: center;">I made this mod to simplify my mods' development and allow me to use just one config library across all mod loaders and Minecraft versions. So if you want to see some examples of how to use this library in the wild you can check out the <a href="https://modrinth.com/user/MarsThePlanet">mods I made</a>.</p>
<p style="text-align: center;">
<a href="https://modrinth.com/mod/laser-bridges-and-doors"><img alt="Laser Bridges & Doors " src="https://media.forgecdn.net/avatars/thumbnails/1195/185/64/64/638770736607588151_animated.gif" /></a> 
<a href="https://modrinth.com/mod/wishful-recipes"><img alt="Wishful Recipes" src="https://media.forgecdn.net/avatars/thumbnails/1821/680/64/64/639150887771985409.png" /></a> 
<a href="https://modrinth.com/mod/more-music-discs"><img alt="More Music Discs " src="https://media.forgecdn.net/avatars/thumbnails/369/955/64/64/637539954760044673.png" /></a> 
<a href="https://modrinth.com/mod/server-side-horror"><img alt="Server-Side Horror" src="https://media.forgecdn.net/avatars/thumbnails/1380/4/64/64/638895109610009518.png" /></a> 
<a href="https://modrinth.com/mod/exp-counter"><img alt="EXP Counter" src="https://media.forgecdn.net/avatars/thumbnails/1179/179/64/64/638752204531779528.png" /></a> 
<a href="https://modrinth.com/mod/fire-arrows-ignite-fire"><img alt="Fire Arrows Ignite Fire" src="https://i.imgur.com/MSoi1ek.png" /></a> 
<a href="https://modrinth.com/mod/flat-exp-costs"><img alt="Flat EXP Costs" src="https://media.forgecdn.net/avatars/thumbnails/1314/641/64/64/638854870713185428.png" /></a> 
<a href="https://modrinth.com/mod/lava-turns-sand-into-glass"><img alt="Lava Turns Sand into Glass" src="https://media.forgecdn.net/avatars/thumbnails/1179/182/64/64/638752210189574617.png" /></a> 
<a href="https://modrinth.com/mod/cooked-carrots"><img alt="Cooked Carrots" src="https://i.imgur.com/tFPjbhV.png" /></a> 
<a href="https://modrinth.com/mod/husks-drop-sand"><img alt="Husks Drop Sand" src="https://media.forgecdn.net/avatars/thumbnails/376/68/64/64/637552001848895633.png" /></a> 
</p>
<p style="text-align: center;"></p></center>
<h1>For developers</h1>
<p>Below are the instructions for setting up and using Deimos. For more details about this library, be sure to check out its <a href="https://github.com/Mars-The-Planet/Deimos">GitHub page</a>.</p>
<h2 id="setup">Setup</h2>
<p>You can either use my <a href="https://github.com/Mars-The-Planet/Multiloader-Templates-with-Deimos">IntelliJ templates</a> which generate a new MultiLoader project with Deimos preconfigured, or follow these instructions to set it up manually:</p>
<h3 id="in-your-build-gradle-">In build.gradle</h3>
<pre><code class="lang-groovy"><span class="hljs-section">repositories</span> {
    <span class="hljs-section">maven</span> {
        url = <span class="hljs-string">"https://api.modrinth.com/maven"</span>
    }
}
</code></pre>
<h4 id="forge-and-neoforge-">Forge and Neoforge:</h4>
<pre><code class="lang-groovy"><span class="hljs-section">dependencies</span> {
    implementation <span class="hljs-string">"maven.modrinth:deimos:<span class="hljs-variable">${project.deimos_version}</span>"</span>
}
</code></pre>
<h4 id="fabric-">Fabric:</h4>
<pre><code class="lang-groovy"><span class="hljs-section">dependencies</span> {
    modImplementation <span class="hljs-string">"maven.modrinth:deimos:<span class="hljs-variable">${project.deimos_version}</span>"</span>
    <span class="hljs-comment">//if you want to use modmenu</span>
    modCompileOnly <span class="hljs-string">"com.terraformersmc:modmenu:<span class="hljs-variable">${project.modmenu_version}</span>"</span>
}
</code></pre>
<p>if you want to use the mod menu functionality you need to add a new repository:</p>
<pre><code class="lang-groovy"><span class="hljs-section">repositories</span> {
    <span class="hljs-section">maven</span> {
        name = <span class="hljs-string">"Terraformers"</span>
        url = <span class="hljs-string">"https://maven.terraformersmc.com/"</span>
    }
}
</code></pre>
<p>You can find the specific Deimos version you need on <a href="https://modrinth.com/mod/deimos/versions">Modrinth</a></p>
<h2 id="how-to-use-it">How to use it</h2>
<h3 id="creating-configs">Creating configs</h3>
<p>You can add configs in a class that extends DeimosConfig:</p>
<pre><code class="lang-java">public <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">TestConfig</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">DeimosConfig</span> </span>{
    <span class="hljs-meta">@Entry</span> public static int test_int = <span class="hljs-number">6</span>;
    <span class="hljs-meta">@Entry</span> public static <span class="hljs-type">List</span>&lt;<span class="hljs-type">String</span>&gt; test_string_list = <span class="hljs-type">Lists</span>.newArrayList(
            <span class="hljs-string">"minecraft:acacia_planks"</span>, <span class="hljs-string">"minecraft:andesite"</span>);
}
</code></pre>
<p>And then in your initialize method you need to call DeimosConfig.init like this:</p>
<pre><code class="lang-java"><span class="hljs-selector-tag">DeimosConfig</span><span class="hljs-selector-class">.init</span>(<span class="hljs-selector-tag">MOD_ID</span>, <span class="hljs-selector-tag">TestConfig</span><span class="hljs-selector-class">.class</span>);
</code></pre>
<h3 id="adding-new-recipes">Adding new recipes</h3>
<p>To add new recipes you call methods from DeimosRecipeGenerator in your initialize method. You can add shapeless crafting, shaped crafting, smelting, smoking, blasting, campfire and stone cutting recipes. Here are some examples:</p>
<pre><code class="lang-java">DeimosRecipeGenerator.createSmeltingJson(TestConfig.test_string_list.<span class="hljs-keyword">get</span>(<span class="hljs-number">0</span>), TestConfig.test_string_list.<span class="hljs-keyword">get</span>(<span class="hljs-number">1</span>), TestConfig.test_int, <span class="hljs-number">0.5</span>F);

DeimosRecipeGenerator.createShapedRecipeJson(
Lists.<span class="hljs-keyword">new</span><span class="hljs-type">ArrayList</span>(<span class="hljs-string">'#'</span>),
Lists.<span class="hljs-keyword">new</span><span class="hljs-type">ArrayList</span>(ResourceLocation.parse(<span class="hljs-string">"sand"</span>)),
Lists.<span class="hljs-keyword">new</span><span class="hljs-type">ArrayList</span>(<span class="hljs-string">"item"</span>),
Lists.<span class="hljs-keyword">new</span><span class="hljs-type">ArrayList</span>(
<span class="hljs-string">"# "</span>,
<span class="hljs-string">" #"</span>
),
ResourceLocation.parse(<span class="hljs-string">"stone"</span>), <span class="hljs-number">1</span>);
</code></pre>
<p>Notice that you can use values from your config file. If the player changes them and restarts the game, the recipes will also change. 
This even works with modded items.</p>
<p style="text-align: center;"><a href="https://www.patreon.com/marstheplanet" rel="nofollow"> <img src="https://i.imgur.com/rJIRiP5.png" /></a></p>