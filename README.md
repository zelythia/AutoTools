# AutoTools

AutoTools looks through your whole inventory and finds the best tool to mine a block or attack a mob. The tool is
determined by mining speed, mining level, enchantments, and the DPS. the Mod features a high level of customisation with
many config options. For blocks where SilkTouch is preferred
see [silk_touch.json](https://github.com/zelythia/AutoTools/blob/1.16.5/common/src/main/resources/data/autotools/tags/blocks/silk_touch.json)
and for
Fortune [fortune.json](https://github.com/zelythia/AutoTools/blob/1.16.5/common/src/main/resources/data/autotools/tags/blocks/fortune.json).
Shears will always pre preferred
for [shears.json](https://github.com/zelythia/AutoTools/blob/1.16.5/common/src/main/resources/data/autotools/tags/blocks/shears.json)

- #### Selection Config \[default]:
    - **toggle** \[false]: AutoTools will always be active and try to get you the best tool. Can be toggled with the set
      key.
    - **disableCreative** \[true]: Disables AutoTools in creative-mode if toggle is enabled.
    - **keepSlot** \[false]: Keeps the selected slot when swapping to a new tool instead of using the vanilla mechanics.
    - **preferHotBarTool** \[true]: AutoTools will prefer the tool already in your hotbar if multiple tools have the same
      mining
      speed, regardless their durability.
    - **preferLowDurability** \[false]: AutoTools will prefer the tool with the lower durability, instead of the higher one,
      if they
      have the same mining speed and enchantments.
    - **switchBack** \[false]: AutoTools will switch back to the item you had in your hand before breaking the block
    - **showDPS** \[true]: Displays the weapons Damage when hovering over it as a tooltip
    - **changeForEntities** \[true]: AutoTools will change to the tool with the most DPS when looking at an entity

- #### Block Behaviour:
    - **onlySwitchIfNecessary** \[false]: AutoTools only tries to get a new tool if it is needed to break the block
    - **alwaysPreferFortune** \[false]: Autotools will use Fortune for Gravel and Leaves
    - **preferSilkTouch** \[except_ores]:
        - [always](https://github.com/zelythia/AutoTools/blob/1.16.5/common/src/main/resources/data/autotools/tags/blocks/silk_touch_setting_always.json):
          Autotools additionally will use SilkTouch for: grass, stone, ores, leaves, snow
        - [always_ores](https://github.com/zelythia/AutoTools/blob/1.16.5/common/src/main/resources/data/autotools/tags/blocks/silk_touch_setting_always_ores.json):
          AutoTools additionally will use SilkTouch for ores
        - [except_ores](https://github.com/zelythia/AutoTools/blob/1.16.5/common/src/main/resources/data/autotools/tags/blocks/silk_touch_setting_always_exc_ores.json):
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
AutoTools is entirely client-side and works on servers and with many third-party mods.

---

### For Developers:

Tools: Should implement Item.isCorrectToolForDrops(BlockState) and Item.getDestroySpeed(Item, BlockState). Additionally,
Blocks should correctly implement requiresCorrectToolForDrops

Weapons: Items need their attack damage (and attack speed) stored as AttributeModifiers
