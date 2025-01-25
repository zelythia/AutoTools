#### \[Warning] AutoTools 3.0 changed the config format breaking all Fabric configs

- New config options: 
  - durabilityCheck: Prevents mining when going under minDurability (#21 by [Solowej2008](https://github.com/Solowej2008))
  - enabled. AutoTools will only work when you are holding: always, tool, no_tool (#23 by [gamelamp](https://github.com/gamelamp) & [Matnns](https://github.com/Matnns))
  - experimentalBreakDelay: Adds an experimental 1 Tick = 50ms delay if toggle is enabled before breaking a block after a tool switch. Enable this if you are experiencing Desyncs like Ghost-Blocks when instant mining (#20)


- Switch to Cloth Config Api on Fabric and built in Neoforge config
- Minimized amount of swaps for switchBack


- Fixed #18: ClientTags don't load correctly in >1.21
- Fixed #26: NullPointerException on Tooltip for Item with reseted ItemAttributes component (by [YoungSoulluoS](https://github.com/YoungSoulluoS))
- Fixed Enchanment Priority Bug on non-enchanted Items
- Fixed wrong DPS calculation