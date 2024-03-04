# AutoTools

- AutoTools looks through your whole inventory and finds the **best tool** to mine a block or attack a mob
- AutoTools looks a mining speed, mining level, enchantments, and DPS to determine the best tool
- AutoTools has many **config**-options to configure it to your liking:
    - toggle: AutoTools will always be active and try to get you the best tool. Can be toggled with the set key.
    - disableCreative: Disables AutoTools in creative-mode if toggle is enabled.
    - keepSlot: Keeps the selected slot when swapping to a new tool instead of using the vanilla mechanics.
    - preferHotBarTool: AutoTools will prefer the tool already in your hotbar if multiple tools have the same mining speed, regardless their durability.
    - preferLowDurability: AutoTools will prefer the tool with the lower durability, instead of the higher one, if they have the same mining speed.
    - alwaysPreferFortune: Autotools will try to always get a tool with Fortune for gravel and leaves
    - preferSilkTouch: Autotools will prefer Silk Touch: never, always, always_ores, except_ores.
    - onlySwitchIfNecessary: AutoTools only tries to get a new tool if it is needed to break the block
    - switchBack: AutoTools will switch back to you previous tool or item you had in your hand before breaking the block
    - showDPS: Displays the weapons Dps when hovering over it as an tooltip
    - changeForEntities: AutoTools will change to the tool with the most DPS when looking at an entity
    - customTools
        - Add custom tool-configurations in JSON format (e.g. customTools={"minecraft:block_id":"minecraft:tool_id"} or customBlocks={"minecraft:block_id":["minecraft:tool_id_1", "minecraft:tool_id_2"]})
        - **Note**: On **Forge** you have the use customTools="{\"minecraft:block_id\":\"minecraft:tool_id\"}"
        - When adding multiple tools, the first one has the highest priority
          There are also pre-define lists for tool groups: autotools:pickaxe, autotools:axe, autotools:shovel, autotools:hoe, autotools:sword
        - This works on both blocks and entites (e.g. for entites: "minecraft:sheep")

### Compatability

There is a version available for the Forge and Fabric mod loader (ModMenu integration when using the Fabric version)  
This mod is entirely client-side and works on servers and with many third-party mods.

### Requirements

This mod requires the **Fabric API** when using it with the fabric mod loader. (Does not need to be installed separately)

---

### For Developers:

Tools: Should implement Item.isCorrectToolForDrops(BlockState) and Item.getDestroySpeed(Item, BlockState). Additionally, Blocks should correctly implement requiresCorrectToolForDrops

Weapons: Items need their attack damage (and attack speed) stored as AttributeModifiers
