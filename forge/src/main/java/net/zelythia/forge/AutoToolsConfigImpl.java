package net.zelythia.forge;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Config(name = "autotools")
public class AutoToolsConfigImpl implements ConfigData {
    @Comment("AutoTools will always be active and try to get you the best tool. Can be toggled with the set key.")
    boolean toggle = false;
    @Comment("Disables AutoTools in creative-mode if toggle is disabled")
    boolean disableCreative = true;
    @Comment("Keeps the selected slot when swapping to a new tool instead of using the vanilla mechanics")
    boolean keepSlot = false;
    @Comment("AutoTools will prefer the tool already in your hotbar if multiple tools have the same mining speed, regardless their durability")
    boolean preferHotbarTool = true;
    @Comment("AutoTools will prefer the tool with the lower durability, instead of the higher one, if they have the same mining speed")
    boolean preferLowDurability = false;
    @Comment("Autotools will use Fortune for Gravel and Leaves")
    boolean alwaysPreferFortune = false;
    @Comment("AutoTools will only switch items if you can't mine a block with your current one")
    boolean onlySwitchIfNecessary = false;
    @Comment("AutoTools will switch back to the previous tool or item you had in your hand before breaking the block")
    boolean switchBack = false;
    @Comment("Displays the weapons Dps when hovering over it")
    boolean showDPS = false;
    @Comment("AutoTools will change to the tool with the most DPS when looking at an entity")
    boolean changeForEntities = true;
    @Comment("AutoTools won't change to a better weapon(e.g. a sword) when holding an axe")
    boolean keepAxe = false;

    @Comment("Autotools additionally will prefer Silk Touch even if it isn't required to mine a block: ")
    AutoToolsConfig.PreferSilkTouch preferSilkTouch = AutoToolsConfig.PreferSilkTouch.except_ores;
    @Comment("AutoTools will only work and swap to the best tool if you are already holding: ")
    AutoToolsConfig.Enabled enabled = AutoToolsConfig.Enabled.always;

    @Comment("AutoTools won't do anything if the currently selected one of these:")
    List<Integer> ignoredSlots = new ArrayList<>();
    @Comment("AutoTools only puts tools in these slots:")
    List<Integer> targetSlots = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

    @Comment("If < 1: Seen as a percentage: Tools below minDurability won't be selected\nElse: Seen as durability: tools will be selected until at minDurability (e.g. set to 1 to never break a tool)")
    double minDurability = 0d;
    @Comment("Prevents mining when going under minDurability")
    boolean durabilityCheck = true;


    @Comment("Add custom block-tool-configurations in JSON format\ne.g. {\"minecraft:block_id\":\"minecraft:tool_id\"} or customTools={\"minecraft:block_id\":[\"minecraft:tool_id_1\", \"minecraft:tool_id_2\"]}\n When adding multiple tools, the first one has the highest priority.\nThere are also pre-define lists for tool groups: autotools:pickaxe, autotools:axe, autotools:shovel, autotools.hoe, autotools:sword\nUse \"autotools:disabled\" to disable AutoTools on a certain block.\nAlso works for entities: \"minecraft:entity_id\":\"minecraft:tool_id\"")
    String customTools = "{}";

    @Comment("Adds an experimental 1 Tick = 50ms delay if toggle is enabled before breaking a block after a tool switch. Enable this if you are experiencing Desyncs like Ghost-Blocks when instant mining.")
    boolean experimentalBreakDelay = false;


    @Override
    public void validatePostLoad() throws ValidationException {
        ConfigData.super.validatePostLoad();

        ignoredSlots = ignoredSlots.stream().filter(i -> {
            if (i >= 1 && i <= 9) return true;
            AutoTools.LOGGER.warn("Removed Slot: ${} from ignoredSlots", i);
            return false;
        }).collect(Collectors.toList());

        targetSlots = targetSlots.stream().filter(i -> {
            if (i >= 1 && i <= 9) return true;
            AutoTools.LOGGER.warn("Removed Slot: ${} from targetSlots", i);
            return false;
        }).collect(Collectors.toList());
    }

    public static void load() {
        AutoTools.swaps.clear();

        AutoToolsConfigImpl config = AutoConfig.getConfigHolder(AutoToolsConfigImpl.class).getConfig();

        AutoToolsConfig.TOGGLE = config.toggle;
        AutoToolsConfig.DISABLECREATIVE = config.disableCreative;
        AutoToolsConfig.KEEPSLOT = config.keepSlot;
        AutoToolsConfig.PREFER_HOTBAR_TOOL = config.preferHotbarTool;
        AutoToolsConfig.PREFER_LOW_DURABILITY = config.preferLowDurability;
        AutoToolsConfig.ALWAYS_PREFER_FORTUNE = config.alwaysPreferFortune;
        AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY = config.onlySwitchIfNecessary;
        AutoToolsConfig.SWITCH_BACK = config.switchBack;
        AutoToolsConfig.SHOWDPS = config.showDPS;
        AutoToolsConfig.CHANGE_FOR_ENTITIES = config.changeForEntities;
        AutoToolsConfig.KEEP_AXE = config.keepAxe;

        AutoToolsConfig.PREFER_SILK_TOUCH = config.preferSilkTouch;
        AutoToolsConfig.ENABLED = config.enabled;

        AutoToolsConfig.IGNORED_SLOTS = config.ignoredSlots;
        AutoToolsConfig.TARGET_SLOTS = config.targetSlots;

        AutoToolsConfig.MIN_DURABILITY = config.minDurability;
        AutoToolsConfig.DURABILITY_CHECK = config.durabilityCheck;

        AutoToolsConfig.CUSTOM_TOOLS = config.customTools;

        AutoToolsConfig.EXPERIMENTAL_BREAK_DELAY = config.experimentalBreakDelay;

    }
}

