package net.zelythia.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.zelythia.AutoTools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Config(name = "autotools")
public class AutoToolsConfig extends PartitioningSerializer.GlobalData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    GeneralConfig general = new GeneralConfig();

    @ConfigEntry.Category("silktouch")
    @ConfigEntry.Gui.TransitiveObject
    SilkTouchConfig silkTouch = new SilkTouchConfig();

    @ConfigEntry.Category("fortune")
    @ConfigEntry.Gui.TransitiveObject
    FortuneConfig fortune = new FortuneConfig();


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface BlockList {
    }



    @Config(name = "silktouch")
    public static class SilkTouchConfig implements ConfigData {
        @BlockList
        List<String> silktouch = List.of(
                "minecraft:ender_chest", "minecraft:glowstone", "minecraft:sea_lantern", "minecraft:campfire", "minecraft:soul_campfire", "minecraft:blue_ice", "minecraft:ice", "minecraft:packed_ice", "minecraft:bookshelf", "minecraft:bee_nest", "minecraft:beehive", "minecraft:turtle_egg", "minecraft:melon",
                "minecraft:brown_mushroom_block", "minecraft:red_mushroom_block", "minecraft:mushroom_stem",
                "minecraft:twisting_vines", "minecraft:twisting_vines_plant", "minecraft:weeping_vines", "minecraft:weeping_vines_plant",
                "minecraft:glass", "minecraft:tinted_glass", "minecraft:red_stained_glass", "minecraft:lime_stained_glass", "minecraft:pink_stained_glass", "minecraft:gray_stained_glass", "minecraft:cyan_stained_glass", "minecraft:blue_stained_glass", "minecraft:white_stained_glass", "minecraft:brown_stained_glass", "minecraft:green_stained_glass", "minecraft:black_stained_glass", "minecraft:orange_stained_glass", "minecraft:yellow_stained_glass", "minecraft:purple_stained_glass", "minecraft:magenta_stained_glass",
                "minecraft:glass_pane", "minecraft:red_stained_glass_pane", "minecraft:lime_stained_glass_pane", "minecraft:pink_stained_glass_pane", "minecraft:gray_stained_glass_pane", "minecraft:cyan_stained_glass_pane", "minecraft:blue_stained_glass_pane", "minecraft:light_blue_stained_glass", "minecraft:light_gray_stained_glass", "minecraft:white_stained_glass_pane", "minecraft:brown_stained_glass_pane", "minecraft:green_stained_glass_pane", "minecraft:black_stained_glass_pane", "minecraft:orange_stained_glass_pane", "minecraft:yellow_stained_glass_pane", "minecraft:purple_stained_glass_pane", "minecraft:magenta_stained_glass_pane", "minecraft:light_blue_stained_glass_pane", "minecraft:light_gray_stained_glass_pane",
                "#minecraft:corals", "#minecraft:coral_blocks",
                "minecraft:sculk", "minecraft:sculk_catalyst", "minecraft:sculk_sensor", "minecraft:calibrated_sculk_sensor", "minecraft:sculk_shrieker", "minecraft:sculk_vein",
                "minecraft:chiseled_bookshelf", "minecraft:decorated_pot",
                "minecraft:creaking_heart");

        @BlockList
        List<String> silktouch_setting_always = List.of(
                "minecraft:grass_block", "minecraft:mycelium", "minecraft:podzol", "minecraft:warped_nylium", "minecraft:crimson_nylium", "minecraft:stone", "minecraft:deepslate", "minecraft:gravel",
                "minecraft:gold_ore", "minecraft:iron_ore", "minecraft:coal_ore", "minecraft:copper_ore", "minecraft:diamond_ore", "minecraft:emerald_ore", "minecraft:redstone_ore", "minecraft:nether_gold_ore", "minecraft:lapis_ore", "minecraft:nether_quartz_ore", "minecraft:gilded_blackstone", "minecraft:deepslate_coal_ore", "minecraft:deepslate_gold_ore", "minecraft:deepslate_iron_ore", "minecraft:deepslate_copper_ore", "minecraft:deepslate_diamond_ore", "minecraft:deepslate_emerald_ore", "minecraft:deepslate_redstone_ore", "minecraft:deepslate_lapis_ore",
                "#minecraft:leaves", "minecraft:snow_block", "minecraft:snow",
                "minecraft:pale_hanging_moss", "minecraft:bush", "minecraft:short_dry_grass", "minecraft:tall_dry_grass");

        @BlockList
        List<String> silktouch_setting_exc_ores = List.of(
                "minecraft:grass_block", "minecraft:podzol", "minecraft:mycelium", "minecraft:gravel",
                "minecraft:stone", "minecraft:deepslate",
                "minecraft:warped_nylium", "minecraft:crimson_nylium",
                "#minecraft:leaves", "minecraft:snow_block", "minecraft:snow",
                "minecraft:pale_hanging_moss", "minecraft:bush", "minecraft:short_dry_grass", "minecraft:tall_dry_grass");

        @BlockList
        List<String> silktouch_settings_always_ores = List.of(
                "minecraft:gravel",
                "minecraft:gold_ore", "minecraft:iron_ore", "minecraft:coal_ore", "minecraft:copper_ore", "minecraft:diamond_ore", "minecraft:emerald_ore", "minecraft:redstone_ore", "minecraft:nether_gold_ore", "minecraft:lapis_ore", "minecraft:nether_quartz_ore", "minecraft:gilded_blackstone", "minecraft:deepslate_coal_ore", "minecraft:deepslate_gold_ore", "minecraft:deepslate_iron_ore", "minecraft:deepslate_copper_ore", "minecraft:deepslate_diamond_ore", "minecraft:deepslate_emerald_ore", "minecraft:deepslate_redstone_ore", "minecraft:deepslate_lapis_ore");
    }

    @Config(name = "fortune")
    public static class FortuneConfig implements ConfigData {
        @BlockList
        List<String> fortune = List.of(
                "minecraft:gold_ore", "minecraft:iron_ore", "minecraft:coal_ore", "minecraft:copper_ore", "minecraft:diamond_ore", "minecraft:emerald_ore", "minecraft:redstone_ore", "minecraft:nether_gold_ore", "minecraft:lapis_ore", "minecraft:nether_quartz_ore", "minecraft:deepslate_coal_ore", "minecraft:deepslate_gold_ore", "minecraft:deepslate_iron_ore", "minecraft:deepslate_copper_ore", "minecraft:deepslate_diamond_ore", "minecraft:deepslate_emerald_ore", "minecraft:deepslate_redstone_ore", "minecraft:deepslate_lapis_ore",
                "minecraft:amethyst_cluster", "minecraft:gilded_blackstone",
                "minecraft:nether_wart", "minecraft:sweet_berry_bush", "minecraft:wheat", "minecraft:carrots", "minecraft:potatoes", "minecraft:beetroots",
                "minecraft:glowstone", "minecraft:melon", "minecraft:sea_lantern", "minecraft:twisting_vines", "minecraft:twisting_vines_plant", "minecraft:weeping_vines", "minecraft:weeping_vines_plant");

        @BlockList
        List<String> fortune_settings = List.of("minecraft:gravel", "#minecraft:leaves");
    }

    @Config(name = "general")
    public static class GeneralConfig implements ConfigData {
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
        net.zelythia.AutoToolsConfig.PreferSilkTouch preferSilkTouch = net.zelythia.AutoToolsConfig.PreferSilkTouch.except_ores;

        @ConfigEntry.Gui.Tooltip
        net.zelythia.AutoToolsConfig.Enabled enabled = net.zelythia.AutoToolsConfig.Enabled.always;

        @ConfigEntry.Gui.Tooltip
        List<Integer> ignoredSlots = List.<Integer>of();
        @ConfigEntry.Gui.Tooltip
        List<Integer> targetSlots = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

        @ConfigEntry.Gui.Tooltip
        double minDurability = 0d;
        @ConfigEntry.Gui.Tooltip
        boolean durabilityCheck = true;

        @ConfigEntry.Gui.Tooltip
        List<String> customTools = List.of();

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
    }
}