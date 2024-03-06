package net.zelythia.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import net.zelythia.AutoToolsConfig;

public class AutoToolsConfigImpl {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> TOGGLE;
    private static final ForgeConfigSpec.ConfigValue<Boolean> SHOWDPS;
    private static final ForgeConfigSpec.ConfigValue<Boolean> KEEPSLOT;
    private static final ForgeConfigSpec.ConfigValue<Boolean> DISABLECREATIVE;
    private static final ForgeConfigSpec.ConfigValue<String> PREFER_SILK_TOUCH;
    private static final ForgeConfigSpec.ConfigValue<Boolean> ALWAYS_PREFER_FORTUNE;
    private static final ForgeConfigSpec.ConfigValue<Boolean> ONLY_SWITCH_IF_NECESSARY;
    private static final ForgeConfigSpec.ConfigValue<Boolean> PREFER_HOTBAR_TOOL;
    private static final ForgeConfigSpec.ConfigValue<Boolean> PREFER_LOW_DURABILITY;
    private static final ForgeConfigSpec.ConfigValue<Boolean> SWITCH_BACK;
    private static final ForgeConfigSpec.ConfigValue<Boolean> CHANGE_FOR_ENTITIES;

    private static final ForgeConfigSpec.ConfigValue<String> CUSTOM_TOOLS;

    static {
        BUILDER.push("AutoTools");

        TOGGLE = BUILDER.comment("AutoTools will always be active and try to get you the best tool. Can be toggled with the set key.")
                .define("toggle", false);
        DISABLECREATIVE = BUILDER.comment("Disables AutoTools in creative if toggle is enabled")
                .define("disableCreative", true);
        KEEPSLOT = BUILDER.comment("Keeps the selected slot when swapping to a new tool instead of using the vanilla mechanics")
                .define("keepSlot", false);
        PREFER_HOTBAR_TOOL = BUILDER.comment("utoTools will prefer the tool already in your hotbar if multiple tools have the same mining speed, regardless their durability")
                .define("preferHotBarTool", true);
        PREFER_LOW_DURABILITY = BUILDER.comment("AutoTools will prefer the tool with the lower durability, instead of the higher one, if they have the same mining speed")
                .define("preferLowDurability", false);
        ALWAYS_PREFER_FORTUNE = BUILDER.comment("Autotools will try to always get a tool with Fortune for gravel and leaves")
                .define("alwaysPreferFortune", false);
        BUILDER.comment("");

        ONLY_SWITCH_IF_NECESSARY = BUILDER.comment("AutoTools only tries to get a new tool if it is needed to break the block")
                .define("onlySwitchIfNecessary", false);
        SWITCH_BACK = BUILDER.comment("AutoTools will switch back to you previous tool or item you had in your hand before breaking the block")
                .define("switchBack", false);
        SHOWDPS = BUILDER.comment("Displays the weapons Dps when hovering over it.")
                .define("showDPS", true);
        CHANGE_FOR_ENTITIES = BUILDER.comment("AutoTools will change to the tool with the most DPS when looking at an entity.")
                .define("changeForEntities", true);
        BUILDER.comment("");

        PREFER_SILK_TOUCH = BUILDER.comment("Autotools will prefer Silk Touch: never, always, always_ores, except_ores")
                .define("preferSilkTouch", "except_ores");
        BUILDER.comment("");

        CUSTOM_TOOLS = BUILDER.comment("Add custom block-tool-configurations in JSON format\n" +
                        "e.g. customTools={\\\"minecraft:block_id\\\":\\\"minecraft:tool_id\\\"} or customTools={\\\"minecraft:block_id\\\":[\\\"minecraft:tool_id_1\\\", \\\"minecraft:tool_id_2\\\"]}\n" +
                        "When adding multiple tools, the first one has the highest priority\n" +
                        "There are also pre-define lists for tool groups: autotools:pickaxe, autotools:axe, autotools:shovel, autotools.hoe, autotools:sword\ne" +
                        "Use \"autotools:disabled\" to disable AutoTools on a certain block\n" +
                        "Also works for entities: \"minecraft:entity_id\":\"minecraft:tool_id")
                .define("customTools", "{}");

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void save() {
        TOGGLE.set(AutoToolsConfig.TOGGLE);
        SHOWDPS.set(AutoToolsConfig.SHOWDPS);
        KEEPSLOT.set(AutoToolsConfig.KEEPSLOT);
        DISABLECREATIVE.set(AutoToolsConfig.DISABLECREATIVE);
        PREFER_SILK_TOUCH.set(AutoToolsConfig.PREFER_SILK_TOUCH);
        ALWAYS_PREFER_FORTUNE.set(AutoToolsConfig.ALWAYS_PREFER_FORTUNE);
        ONLY_SWITCH_IF_NECESSARY.set(AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY);
        PREFER_HOTBAR_TOOL.set(AutoToolsConfig.PREFER_HOTBAR_TOOL);
        PREFER_LOW_DURABILITY.set(AutoToolsConfig.PREFER_LOW_DURABILITY);
        SWITCH_BACK.set(AutoToolsConfig.SWITCH_BACK);
        CHANGE_FOR_ENTITIES.set(AutoToolsConfig.CHANGE_FOR_ENTITIES);

        SPEC.save();
    }

    public static void load() {
        AutoToolsConfig.TOGGLE = TOGGLE.get();
        AutoToolsConfig.SHOWDPS = SHOWDPS.get();
        AutoToolsConfig.KEEPSLOT = KEEPSLOT.get();
        AutoToolsConfig.DISABLECREATIVE = DISABLECREATIVE.get();
        AutoToolsConfig.PREFER_SILK_TOUCH = PREFER_SILK_TOUCH.get();
        AutoToolsConfig.ALWAYS_PREFER_FORTUNE = ALWAYS_PREFER_FORTUNE.get();
        AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY = ONLY_SWITCH_IF_NECESSARY.get();
        AutoToolsConfig.PREFER_HOTBAR_TOOL = PREFER_HOTBAR_TOOL.get();
        AutoToolsConfig.PREFER_LOW_DURABILITY = PREFER_LOW_DURABILITY.get();
        AutoToolsConfig.CUSTOM_TOOLS = CUSTOM_TOOLS.get();
        AutoToolsConfig.SWITCH_BACK = SWITCH_BACK.get();
        AutoToolsConfig.CHANGE_FOR_ENTITIES = CHANGE_FOR_ENTITIES.get();
    }
}
