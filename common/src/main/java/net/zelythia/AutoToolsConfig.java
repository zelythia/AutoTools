package net.zelythia;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.List;

public class AutoToolsConfig {
    public enum PreferSilkTouch {
        never, always, ores, except_ores,
    }

    public enum Enabled {
        always, tool, no_tool
    }

    public static boolean TOGGLE;
    public static boolean SHOWDPS;
    public static boolean KEEPSLOT;
    public static boolean DISABLECREATIVE;
    public static PreferSilkTouch PREFER_SILK_TOUCH;
    public static boolean ALWAYS_PREFER_FORTUNE;
    public static boolean ONLY_SWITCH_IF_NECESSARY;
    public static boolean PREFER_HOTBAR_TOOL;
    public static boolean PREFER_LOW_DURABILITY;
    public static boolean SWITCH_BACK;
    public static boolean CHANGE_FOR_ENTITIES;
    public static boolean KEEP_AXE;
    public static boolean DURABILITY_CHECK;

    public static String CUSTOM_TOOLS;
    public static List<? extends Integer> IGNORED_SLOTS;
    public static List<? extends Integer> TARGET_SLOTS;
    public static double MIN_DURABILITY;
    public static Enabled ENABLED;

    public static boolean EXPERIMENTAL_BREAK_DELAY;

    @ExpectPlatform
    public static void load() {
        throw new AssertionError();
    }
}
