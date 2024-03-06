package net.zelythia.fabric;

import net.zelythia.AutoToolsConfig;
import net.zelythia.fabric.config.SimpleConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class AutoToolsConfigImpl {

    private static final Logger LOGGER = LogManager.getLogger("AutoToolsConfig");
    private static final SimpleConfig config;

    static {
        config = SimpleConfig.of("autotools").provider(AutoToolsConfigImpl::defaultConfig).request();

        AutoToolsConfig.TOGGLE = config.getOrDefault("toggle", true);
        AutoToolsConfig.SHOWDPS = config.getOrDefault("showDPS", true);
        AutoToolsConfig.KEEPSLOT = config.getOrDefault("keepSlot", false);
        AutoToolsConfig.DISABLECREATIVE = config.getOrDefault("disableCreative", true);
        AutoToolsConfig.ALWAYS_PREFER_FORTUNE = config.getOrDefault("alwaysPreferFortune", false);
        AutoToolsConfig.PREFER_SILK_TOUCH = config.getOrDefault("preferSilkTouch", "except_ores");
        AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY = config.getOrDefault("onlySwitchIfNecessary", false);
        AutoToolsConfig.PREFER_HOTBAR_TOOL = config.getOrDefault("preferHotBarTool", true);
        AutoToolsConfig.PREFER_LOW_DURABILITY = config.getOrDefault("preferLowDurability", false);
        AutoToolsConfig.SWITCH_BACK = config.getOrDefault("switchBack", false);
        AutoToolsConfig.CHANGE_FOR_ENTITIES = config.getOrDefault("changeForEntities", true);

        AutoToolsConfig.CUSTOM_TOOLS = config.getOrDefault("customTools", "{}");
    }


    /**
     * Changing what is written in the config by default
     */
    private static String defaultConfig(String filename) {
        return "#AutoTools config\n" +
                "\n" +
                "#AutoTools will always be active and try to get you the best tool. Can be toggled with the set key.\n" +
                "toggle=false\n" +
                "#Disables AutoTools in creative-mode if toggle is enabled\n" +
                "disableCreative=true\n" +
                "#Keeps the selected slot when swapping to a new tool instead of using the vanilla mechanics.\n" +
                "keepSlot=false\n" +
                "#AutoTools will prefer the tool already in your hotbar if multiple tools have the same mining speed, regardless their durability\n" +
                "preferHotBarTool=true\n" +
                "#AutoTools will prefer the tool with the lower durability, instead of the higher one, if they have the same mining speed\n" +
                "preferLowDurability=false\n" +
                "#Autotools will try to always get a tool with Fortune for gravel and leaves\n" +
                "alwaysPreferFortune=false\n" +
                "\n" +
                "#AutoTools only tries to get a new tool if it is needed to break the block\n" +
                "onlySwitchIfNecessary=false  " +
                "#AutoTools will switch back to you previous tool or item you had in your hand before breaking the block\n" +
                "switchBack=false\n" +
                "#Displays the weapons Dps when hovering over it as an tooltip\n" +
                "showDPS=true\n" +
                "#AutoTools will change to the tool with the most DPS when looking at an entity\n" +
                "changeForEntities=true\n" +
                "\n" +
                "#Autotools will prefer Silk Touch:\n" +
                "# never, always, always_ores, except_ores\n" +
                "preferSilkTouch=except_ores\n" +
                "\n" +
                "#Add custom block-tool-configurations in JSON format\n" +
                "#e.g. customTools={\"minecraft:block_id\":\"minecraft:tool_id\"} or customTools={\"minecraft:block_id\":[\"minecraft:tool_id_1\", \"minecraft:tool_id_2\"]}\n" +
                "#When adding multiple tools, the first one has the highest priority\n" +
                "#There are also pre-define lists for tool groups: autotools:pickaxe, autotools:axe, autotools:shovel, autotools.hoe, autotools:sword\n" +
                "#Use \"autotools:disabled\" to disable AutoTools on a certain block\n" +
                "#Also works for entities: \"minecraft:entity_id\":\"minecraft:tool_id\"\n" +
                "customTools={}";

    }

    @Nullable
    public static SimpleConfig getConfig() {
        if (config != null) {
            return config;
        } else {
            LOGGER.error("Config not initialized.( Run 'new AutoToolsConfig()' before using )");
            return null;
        }
    }

    public static void save() {
        if(getConfig() == null) return;
        getConfig().setOrCreate("toggle", AutoToolsConfig.TOGGLE);
        getConfig().setOrCreate("showDPS", AutoToolsConfig.SHOWDPS);
        getConfig().setOrCreate("keepSlot", AutoToolsConfig.KEEPSLOT);
        getConfig().setOrCreate("disableCreative", AutoToolsConfig.DISABLECREATIVE);
        getConfig().setOrCreate("alwaysPreferFortune", AutoToolsConfig.ALWAYS_PREFER_FORTUNE);
        getConfig().setOrCreate("preferSilkTouch", AutoToolsConfig.PREFER_SILK_TOUCH);
        getConfig().setOrCreate("onlySwitchIfNecessary", AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY);
        getConfig().setOrCreate("preferHotBarTool", AutoToolsConfig.PREFER_HOTBAR_TOOL);
        getConfig().setOrCreate("preferLowDurability", AutoToolsConfig.PREFER_LOW_DURABILITY);
        getConfig().setOrCreate("switchBack", AutoToolsConfig.SWITCH_BACK);
        getConfig().setOrCreate("changeForEntities", AutoToolsConfig.CHANGE_FOR_ENTITIES);
    }

    public static void load() {
        config.loadConfig();

        AutoToolsConfig.TOGGLE = config.getOrDefault("toggle", true);
        AutoToolsConfig.SHOWDPS = config.getOrDefault("showDPS", true);
        AutoToolsConfig.KEEPSLOT = config.getOrDefault("keepSlot", false);
        AutoToolsConfig.DISABLECREATIVE = config.getOrDefault("disableCreative", true);
        AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY = config.getOrDefault("onlySwitchIfNecessary", false);
        AutoToolsConfig.ALWAYS_PREFER_FORTUNE = config.getOrDefault("alwaysPreferFortune", false);
        AutoToolsConfig.PREFER_SILK_TOUCH = config.getOrDefault("preferSilkTouch", "except_ores");
        AutoToolsConfig.PREFER_HOTBAR_TOOL = config.getOrDefault("preferHotBarTool", true);
        AutoToolsConfig.PREFER_LOW_DURABILITY = config.getOrDefault("preferLowDurability", false);
        AutoToolsConfig.CUSTOM_TOOLS = config.getOrDefault("customTools", "{}");
        AutoToolsConfig.SWITCH_BACK = config.getOrDefault("switchBack", false);
        AutoToolsConfig.CHANGE_FOR_ENTITIES = config.getOrDefault("changeForEntities", true);
    }
}
