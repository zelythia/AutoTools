package net.zelythia;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class AutoToolsConfig {
    public static boolean TOGGLE;
    public static boolean SHOWDPS;
    public static boolean KEEPSLOT;
    public static boolean DISABLECREATIVE;
    public static String PREFER_SILK_TOUCH;
    public static boolean ALWAYS_PREFER_FORTUNE;
    public static boolean ONLY_SWITCH_IF_NECESSARY;
    public static boolean PREFER_HOTBAR_TOOL;
    public static boolean PREFER_LOW_DURABILITY;
    public static boolean SWITCH_BACK;
    public static boolean CHANGE_FOR_ENTITIES;

    public static String CUSTOM_TOOLS;

    @ExpectPlatform
    static void save() {
        throw new AssertionError();
    }

    @ExpectPlatform
    static void load() {
        throw new AssertionError();
    }
}
