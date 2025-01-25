package net.zelythia.neoforge;

import com.google.gson.JsonParser;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;

import java.util.List;

public class AutoToolsConfigImpl {


    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue TOGGLE;
    private static final ModConfigSpec.BooleanValue SHOWDPS;
    private static final ModConfigSpec.BooleanValue KEEPSLOT;
    private static final ModConfigSpec.BooleanValue DISABLECREATIVE;
    private static final ModConfigSpec.BooleanValue ALWAYS_PREFER_FORTUNE;
    private static final ModConfigSpec.BooleanValue ONLY_SWITCH_IF_NECESSARY;
    private static final ModConfigSpec.BooleanValue PREFER_HOTBAR_TOOL;
    private static final ModConfigSpec.BooleanValue PREFER_LOW_DURABILITY;
    private static final ModConfigSpec.BooleanValue SWITCH_BACK;
    private static final ModConfigSpec.BooleanValue CHANGE_FOR_ENTITIES;
    private static final ModConfigSpec.BooleanValue KEEP_AXE;
    private static final ModConfigSpec.BooleanValue DURABILITY_CHECK;
    private static final ModConfigSpec.BooleanValue EXPERIMENTAL_BREAK_DELAY;

    private static final ModConfigSpec.EnumValue<AutoToolsConfig.PreferSilkTouch> PREFER_SILK_TOUCH;
    private static final ModConfigSpec.EnumValue<AutoToolsConfig.Enabled> ENABLED;

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> IGNORED_SLOTS;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> TARGET_SLOTS;

    private static final ModConfigSpec.ConfigValue<Double> MIN_DURABILITY;

    private static final ModConfigSpec.ConfigValue<String> CUSTOM_TOOLS;


    static {
        BUILDER.push("AutoTools");

        TOGGLE = BUILDER.comment("AutoTools will always be active and try to get you the best tool. Can be toggled with the set key.")
                .define("toggle", false);
        DISABLECREATIVE = BUILDER.comment("Disables AutoTools in creative if toggle is enabled")
                .define("disableCreative", true);
        KEEPSLOT = BUILDER.comment("Keeps the selected slot when swapping to a new tool instead of using the vanilla mechanics")
                .define("keepSlot", false);
        PREFER_HOTBAR_TOOL = BUILDER.comment("AutoTools will prefer the tool already in your hotbar if multiple tools have the same mining speed, regardless their durability")
                .define("preferHotbarTool", true);
        PREFER_LOW_DURABILITY = BUILDER.comment("AutoTools will prefer the tool with the lower durability, instead of the higher one, if they have the same mining speed")
                .define("preferLowDurability", false);
        ALWAYS_PREFER_FORTUNE = BUILDER.comment("Autotools will try to always get a tool with Fortune for gravel and leaves")
                .define("alwaysPreferFortune", false);
        BUILDER.comment(" ");

        ONLY_SWITCH_IF_NECESSARY = BUILDER.comment("AutoTools only tries to get a new tool if it is needed to break the block")
                .define("onlySwitchIfNecessary", false);
        SWITCH_BACK = BUILDER.comment("AutoTools will switch back to you previous tool or item you had in your hand before breaking the block")
                .define("switchBack", false);
        SHOWDPS = BUILDER.comment("Displays the weapons Dps when hovering over it.")
                .define("showDPS", true);
        CHANGE_FOR_ENTITIES = BUILDER.comment("AutoTools will change to the tool with the most DPS when looking at an entity.")
                .define("changeForEntities", true);
        KEEP_AXE = BUILDER.comment("AutoTools won't change to a better weapon(e.g. a sword) when holding an axe")
                .define("changeForEntities", false);
        BUILDER.comment(" ");

        PREFER_SILK_TOUCH = BUILDER.comment("Autotools additionally will prefer Silk Touch even if it isn't required to mine a block: never, always, ores, except_ores")
                .defineEnum("preferSilkTouch", AutoToolsConfig.PreferSilkTouch.except_ores);
        BUILDER.comment(" ");

        IGNORED_SLOTS = BUILDER.comment("AutoTools won't do anything if the currently selected slot is in ignoredSlots")
                .defineListAllowEmpty("ignoredSlots", List.of(), () -> 1, o -> {
                    if (o instanceof Integer i) {
                        return i >= 1 && i <= 9;
                    }
                    return false;
                });

        TARGET_SLOTS = BUILDER.comment("AutoTools only puts tools in these slots:")
                .defineListAllowEmpty("targetSlots", List.of(1,2,3,4,5,6,7,8,9), () -> 1, o -> {
                    if (o instanceof Integer i) {
                        return i >= 1 && i <= 9;
                    }
                    return false;
                });


        MIN_DURABILITY = BUILDER.comment("If < 1: Seen as a percentage: Tools below minDurability won't be selected\\Else: Seen as durability: tools will be selected until at minDurability (e.g. set to 1 to never break a tool)")
                .define("minDurability", 0d);
        DURABILITY_CHECK = BUILDER.comment("Prevents mining when going under minDurability")
                .define("durabilityCheck", true);
        BUILDER.comment(" ");

        ENABLED = BUILDER.comment("AutoTools will only work and swap to the best tool if you are already holding: always, tool, no_tool")
                .defineEnum("enabled", AutoToolsConfig.Enabled.always);

        CUSTOM_TOOLS = BUILDER.comment("Add custom block-tool-configurations in JSON format\n" +
                        "e.g. customTools={\\\"minecraft:block_id\\\":\\\"minecraft:tool_id\\\"} or customTools={\\\"minecraft:block_id\\\":[\\\"minecraft:tool_id_1\\\", \\\"minecraft:tool_id_2\\\"]}\n" +
                        "When adding multiple tools, the first one has the highest priority\n" +
                        "There are also pre-define lists for tool groups: autotools:pickaxe, autotools:axe, autotools:shovel, autotools.hoe, autotools:sword\n" +
                        "Use \"autotools:disabled\" to disable AutoTools on a certain block\n" +
                        "Also works for entities: \"minecraft:entity_id\":\"minecraft:tool_id")
                .define("customTools", "{}", o -> {
                    if(o instanceof String s){
                        try {
                            return JsonParser.parseString(s).isJsonObject();
                        }
                        catch (Exception e){
                            return false;
                        }
                    }
                    return false;
                });

        EXPERIMENTAL_BREAK_DELAY = BUILDER.comment("Adds an experimental 1 Tick = 50ms delay if toggle is enabled before breaking a block after a tool switch.\nEnable this if you are experiencing Desyncs like Ghost-Blocks when instant mining.")
                .define("experimentalBreakDelay", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void load() {
        AutoTools.swaps.clear();

        AutoToolsConfig.TOGGLE = TOGGLE.get();
        AutoToolsConfig.SHOWDPS = SHOWDPS.get();
        AutoToolsConfig.KEEPSLOT = KEEPSLOT.get();
        AutoToolsConfig.DISABLECREATIVE = DISABLECREATIVE.get();
        AutoToolsConfig.PREFER_SILK_TOUCH = PREFER_SILK_TOUCH.get();
        AutoToolsConfig.ALWAYS_PREFER_FORTUNE = ALWAYS_PREFER_FORTUNE.get();
        AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY = ONLY_SWITCH_IF_NECESSARY.get();
        AutoToolsConfig.PREFER_HOTBAR_TOOL = PREFER_HOTBAR_TOOL.get();
        AutoToolsConfig.PREFER_LOW_DURABILITY = PREFER_LOW_DURABILITY.get();
        AutoToolsConfig.SWITCH_BACK = SWITCH_BACK.get();
        AutoToolsConfig.CHANGE_FOR_ENTITIES = CHANGE_FOR_ENTITIES.get();
        AutoToolsConfig.KEEP_AXE = KEEP_AXE.get();
        AutoToolsConfig.ENABLED = ENABLED.get();
        AutoToolsConfig.DURABILITY_CHECK = DURABILITY_CHECK.get();

        AutoToolsConfig.CUSTOM_TOOLS = CUSTOM_TOOLS.get();
        AutoToolsConfig.IGNORED_SLOTS = IGNORED_SLOTS.get();
        AutoToolsConfig.TARGET_SLOTS = TARGET_SLOTS.get();
        AutoToolsConfig.MIN_DURABILITY = MIN_DURABILITY.get();

        AutoToolsConfig.EXPERIMENTAL_BREAK_DELAY = EXPERIMENTAL_BREAK_DELAY.get();
    }
}
