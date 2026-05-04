# AutoTools

[![modrinth](https://img.shields.io/modrinth/dt/minecraft_autotools?style=for-the-badge&logo=modrinth&label=Modrinth&color=1bd96a)](https://modrinth.com/mod/minecraft_autotools)
[![modrinth](https://cf.way2muchnoise.eu/522205.svg?badge_style=for_the_badge)]([https://modrinth.com/mod/minecraft_autotools](https://www.curseforge.com/minecraft/mc-mods/autotools))

AutoTools looks through your whole inventory and finds the best tool to mine a block or attack a mob. The tool is
determined by mining speed, mining level, enchantments, and the DPS. the Mod features a high level of customisation with
many config options. For blocks where SilkTouch is preferred
see [silk_touch.json](https://github.com/zelythia/AutoTools/blob/1.21.6/blockLists/silk_touch.json)
and for
Fortune [fortune.json](https://github.com/zelythia/AutoTools/blob/1.21.6/blockLists/fortune.json).
Shears will always pre preferred
for [shears.json](https://github.com/zelythia/AutoTools/blob/1.21.6/blockLists/shears.json).

All config options can conveniently be edited using the ClothConfig UI:

- #### Selection Config \[default]:
    - **toggle** \[false]: AutoTools will always be active and try to get you the best tool. Can be toggled with the set
      key.
    - **disableCreative** \[true]: Disables AutoTools in creative-mode if toggle is enabled.
    - **keepSlot** \[false]: Keeps the selected slot when swapping to a new tool instead of using the vanilla mechanics.
    - **hotbarOnly** \[false]: AutoTools will only select tool from you hotbar
    - **preserveDurability** \[true]: AutoTools will switch to an item with no durability if no suitable tool has been found
    - **hotbarOnly** \[false]: AutoTools will only select tools from you hotbar
    - **preferHotBarTool** \[true]: AutoTools will prefer the tool already in your hotbar if multiple tools have the same mining speed, regardless their durability.
    - **preferLowDurability** \[false]: AutoTools will prefer the tool with the lower durability, instead of the higher one, if they have the same mining speed and enchantments.
    - **preserveDurability** \[true]: AutoTools will switch to an item with no durability if no suitable tool has been found
    - **switchBack** \[false]: AutoTools will switch back to the item you had in your hand before breaking the block
    - **showDPS** \[true]: Displays the weapons Damage when hovering over it as a tooltip
    - **changeForEntities** \[true]: AutoTools will change to the tool with the most DPS when looking at an entity
    - **keepAxe** \[false]: AutoTools won't change to a better weapon(e.g. a sword) when holding an axe
    - **enabled** \[always]:
        - always: AutoTools will always be active
        - tool: AutoTools will only be active while holding a tool
        - no_tool: AutoTools will only be active while not holding a tool
    - **ignoredSlots** \[\[]]: AutoTools won't do anything if the currently selected one of these
    - **targetSlots** \[\[1,2,3,4,5,6,7,8,9]]: AutoTools only puts tools in these slots
    - **ignoredInventorySlots** \[\[]]: AutoTools won't select tools from these slots in your inventory. See [Inventory Slots](https://raw.githubusercontent.com/zelythia/AutoTools/refs/heads/26.1.2/images/inventory_slots.png)
    - **minDurability** \[0.0]: If < 1: Seen as a percentage: Tools below minDurability won't be selected  
      Else: Seen as durability: tools will be selected until at minDurability (e.g. set to 1 to never break a tool)
    - **durabilityCheck** \[true]: Prevents mining when going under minDurability
    - **experimentalBreakDelay** \[false]: Adds an experimental 1 Tick = 50ms delay if toggle is enabled before breaking
      a block after a tool switch. Enable this if you are experiencing Desyncs like Ghost-Blocks when instant mining.

- #### Block Behaviour:
    - **onlySwitchIfNecessary** \[false]: AutoTools only tries to get a new tool if it is needed to break the block
    - **alwaysPreferFortune** \[false]: Autotools will use Fortune for Gravel and Leaves
    - **preferSilkTouch** \[except_ores]:
        - [always](https://github.com/zelythia/AutoTools/blob/1.20.4/common/src/main/resources/data/autotools/tags/blocks/silk_touch_setting_always.json):
          Autotools additionally will use SilkTouch for: grass, stone, ores, leaves, snow
        - [ores](https://github.com/zelythia/AutoTools/blob/1.20.4/common/src/main/resources/data/autotools/tags/blocks/silk_touch_setting_always_ores.json):
          AutoTools additionally will use SilkTouch for ores
        - [except_ores](https://github.com/zelythia/AutoTools/blob/1.20.4/common/src/main/resources/data/autotools/tags/blocks/silk_touch_setting_always_exc_ores.json):
          Autotools additionally will SilkTouch for: grass, stone, leaves, snow
        - never: Autotools will only use SilkTouch if its required to mine a block.
    - **customTools**
        - Add custom tool-configurations in JSON format (e.g. customTools={"minecraft:block_id":"minecraft:tool_id"}
          or customBlocks={"minecraft:block_id":["minecraft:tool_id_1", "minecraft:tool_id_2"]})
        - **Note**: On **Forge** and **NeoForge** you have the use customTools="{\"minecraft:block_id\":\"minecraft:
          tool_id\"}"
        - When adding multiple tools, the first one has the highest priority
          There are also pre-define lists for tool groups: autotools:pickaxe, autotools:axe, autotools:shovel,
          autotools:hoe, autotools:sword
        - To completely disable AutoTools for a block use: "autotools:disabled"
        - This works on both blocks and entities (e.g. "minecraft:stone" or "minecraft:sheep")

### Compatability

There is a version available for the Forge, NeoForge and Fabric mod loader (ModMenu integration when using the Fabric
version)  
AutoTools requires [ClothConfig](https://modrinth.com/mod/cloth-config).

AutoTools is entirely client-side and works on servers and with many third-party mods.

AutoTools also works with [Controllable](https://github.com/MrCrayfish/Controllable)

---

### For Developers:

#### Item Compatability:

Tools: Should implement Item.isCorrectToolForDrops(BlockState) and Item.getDestroySpeed(Item, BlockState). Additionally,
Blocks should correctly implement requiresCorrectToolForDrops  (Mojmap Mappings)

Weapons: Items need their attack damage (and attack speed) stored as AttributeModifiers

#### Mod Integration:

If you want to depend on AutoTools in your own project, use either one of these configs. A list of available packages can be found at:

https://maven.zelythia.net/#/releases/net/zelythia/autotools-common or https://github.com/zelythia/AutoTools/packages/2683389/versions


```
repositories {
    maven {
        name = "Zelythia Releases"
        url = uri("https://maven.zelythia.net/releases")
    }
}
```

According to the [GitHub Documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package):
```
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/zelythia/AutoTools")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
        }
   }
}
```

