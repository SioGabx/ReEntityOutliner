NEW : Updated to 26+ (NeoForge / Forge + Fabric version)

# Re:Entity Outliner
Re:Entity Outliner is a clientside mod that allows you to select entity types to outline, making them visible through obstructions (like an entity xray / entity ESP) at any distance by using a glowing effect.

<a href="https://legacy.curseforge.com/minecraft/mc-mods/re-entity-outliner"><img alt="CurseForge page" src="https://img.shields.io/curseforge/dt/1079322?logo=curseforge"></a>
<a href="https://github.com/SioGabx/EntityOutliner/releases"><img alt="GitHub all releases" src="https://img.shields.io/github/downloads/SioGabx/EntityOutliner/total?color=%2316d68a&logo=github"></a>

## Why Use It?
This mod will help with:
<details>
  <summary>Finding passive mobs</summary>
  
  by outlining them.
  
  ![Image of outlined bees](https://i.imgur.com/jqhVLSX.png "It's hard to find bees!")
</details>

<details>
  <summary>Finding unlit caves</summary>
  
  by outlining zombies, creepers, skeletons, and spiders.
  
  ![Gif showing how outlining monsters can reveal unlit caves](https://i.imgur.com/owNj5BE.gif "Great for when you've reached a dead end in your cave!")
</details>

<details>
  <summary>Fighting other players</summary>
  
  by outlining players.
  
  ![Image of outlined players](https://i.imgur.com/TiEldyM.png "Even works while they're sneaking!")
</details>

<details>
  <summary>Finding your death location</summary>
  
  by outlining items and experience orbs.
  
  ![Image of outlined items/xp orbs of death location](https://i.imgur.com/sOzk89i.png "Tombstone mods are cool too!")
</details>

<details>
  <summary>Wither skeleton skull hunting</summary>
  
  by outlining wither skeletons
  
  ![Image of outlined wither skeletons](https://i.imgur.com/cc4rhaY.png "I actually like the grind for wither skeleton skulls!")
</details>

<details>
  <summary>Finding mineshafts</summary>
  
  by outlining cave spiders and minecarts with chests.
  
  ![Image of outlined chest minecarts](https://i.imgur.com/36rMnDc.png "I hate cave spiders!")
</details>

And many more!

---

## Features

### **Entity Selector**
![GIF demonstrating use of the entity selector screen](https://i.imgur.com/XozyBa4.gif "Advanced search engine!")

The selector screen allows you to outline any entity in the game. It features a powerful search engine to filter through hundreds of entities (including those added by other mods).

#### **🔍 How to Search**
The search is **case-insensitive** and supports multiple keywords (tokens) separated by spaces. An entity must match **all** keywords to be displayed.

* **Basic Search**: Type the name or technical ID (e.g., `cow` or `zombie`).
* **Mod Filter (`@`)**: Filter by mod ID.
    * *Example:* `@minecraft` shows only vanilla entities.
* **Category Filter (`#`)**: Filter by entity category.
    * *Example:* `#monster` shows only hostile mobs.
* **Wildcards (`*`)**: Use an asterisk for partial matches.
    * *Example:* `zom*` (starts with), `*eye` (ends with), or `*skeleton*` (contains).

| Pattern | Search Type | Example |
| :--- | :--- | :--- |
| `name` | Name or ID | `creeper` |
| `@modid` | Mod Origin | `@minecraft` |
| `#category` | Entity Type | `#monster` |
| `*` | Wildcard | `zom*` |

> **Note**: If a Player is on a team, their outline color matches their team color, overriding the manual selection.

### **Controls**
![Image of the keybind selector](https://i.imgur.com/au39Ov1.png)

Custom keybinds are provided to open the entity selector and toggle the outline globally.

---

## Installation

### [On Fabric](https://docs.fabricmc.net/players/installing-mods) : 
1. Install [Fabric](https://fabricmc.net/use/)
2. Drop the [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) jar into the mods folder
3. Drop the Re:Entity Outliner FABRIC .jar into the mods folder

### On NeoForge : (from 1.21.8+)
1. Install [NeoForge](https://neoforged.net/)
2. Drop the Re:Entity Outliner NEOFORGE .jar into the mods folder

### On Forge : (from 1.20.1 to 1.21.11)
1. Install [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/)
2. Drop the Re:Entity Outliner FORGE .jar into the mods folder

---

## License
MIT. Feel free to use this mod in any modpack.

## Credits
Re:Entity Outliner is a continuation of **adamviola's** initial mod: [Entity Outliner](https://www.curseforge.com/minecraft/mc-mods/entity-outliner).