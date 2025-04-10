package net.zelythia.forge;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;

import java.util.List;

@Config(name = "autotools")
public class AutoToolsConfigImpl implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    boolean toggle = false;

    @ConfigEntry.Gui.Tooltip
    boolean disableCreative = true;

    @ConfigEntry.Gui.Tooltip
    boolean keepSlot = false;

    @ConfigEntry.Gui.Tooltip
    boolean preferHotbarTool = true;

    @ConfigEntry.Gui.Tooltip
    boolean preferLowDurability = false;

    @ConfigEntry.Gui.Tooltip
    boolean alwaysPreferFortune = false;

    @ConfigEntry.Gui.Tooltip
    boolean onlySwitchIfNecessary = false;

    @ConfigEntry.Gui.Tooltip
    boolean switchBack = false;

    @ConfigEntry.Gui.Tooltip
    boolean showDPS = false;

    @ConfigEntry.Gui.Tooltip
    boolean changeForEntities = true;

    @ConfigEntry.Gui.Tooltip
    boolean keepAxe = false;

    @ConfigEntry.Gui.Tooltip
    AutoToolsConfig.PreferSilkTouch preferSilkTouch = AutoToolsConfig.PreferSilkTouch.except_ores;

    @ConfigEntry.Gui.Tooltip
    AutoToolsConfig.Enabled enabled = AutoToolsConfig.Enabled.always;

    @ConfigEntry.Gui.Tooltip
    List<Integer> ignoredSlots = List.<Integer>of();
    @ConfigEntry.Gui.Tooltip
    List<Integer> targetSlots = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

    @ConfigEntry.Gui.Tooltip
    double minDurability = 0d;
    @ConfigEntry.Gui.Tooltip
    boolean durabilityCheck = true;

    @ConfigEntry.Gui.Tooltip
    String customTools = "{}";

    @ConfigEntry.Gui.Tooltip
    boolean experimentalBreakDelay = false;


    @Override
    public void validatePostLoad() throws ValidationException {
        ConfigData.super.validatePostLoad();

        ignoredSlots = ignoredSlots.stream().filter(i -> {
            if (i >= 1 && i <= 9) return true;
            AutoTools.LOGGER.warn("Removed Slot: ${} from ignoredSlots", i);
            return false;
        }).toList();

        targetSlots = targetSlots.stream().filter(i -> {
            if (i >= 1 && i <= 9) return true;
            AutoTools.LOGGER.warn("Removed Slot: ${} from targetSlots", i);
            return false;
        }).toList();
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

    public static void save() {
        AutoToolsConfigImpl config = AutoConfig.getConfigHolder(AutoToolsConfigImpl.class).getConfig();

        config.preferSilkTouch = AutoToolsConfig.PREFER_SILK_TOUCH;

        AutoConfig.getConfigHolder(AutoToolsConfigImpl.class).save();
    }
}

