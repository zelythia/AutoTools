package net.zelythia.autotools.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.zelythia.autotools.AutoTools;
import net.zelythia.autotools.config.autoconfig.BlockList;
import net.zelythia.autotools.config.autoconfig.HotbarSlots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Config(name = "autotools")
public class AutoToolsConfig extends PartitioningSerializer.GlobalData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public GeneralConfig general = new GeneralConfig();

    @ConfigEntry.Category("block_lists")
    @ConfigEntry.Gui.TransitiveObject
    public BlockLists blockLists = new BlockLists();

    public static GeneralConfig get() {
        return AutoConfig.getConfigHolder(AutoToolsConfig.class).getConfig().general;
    }

    public static BlockLists blockLists() {
        return AutoConfig.getConfigHolder(AutoToolsConfig.class).getConfig().blockLists;
    }

    public static void save() {
        AutoConfig.getConfigHolder(AutoToolsConfig.class).save();
    }

    public enum PreferSilkTouch {
        never, always, ores, except_ores,
    }

    public enum Enabled {
        always, tool, no_tool
    }

    @Config(name = "lists")
    public static class BlockLists implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        @BlockList
        public List<String> silktouch = new ArrayList<>(Arrays.asList(
                "minecraft:ender_chest", "minecraft:glowstone", "minecraft:sea_lantern", "minecraft:campfire", "minecraft:soul_campfire", "minecraft:blue_ice", "minecraft:ice", "minecraft:packed_ice", "minecraft:bookshelf", "minecraft:bee_nest", "minecraft:beehive", "minecraft:turtle_egg", "minecraft:melon",
                "minecraft:brown_mushroom_block", "minecraft:red_mushroom_block", "minecraft:mushroom_stem",
                "minecraft:twisting_vines", "minecraft:twisting_vines_plant", "minecraft:weeping_vines", "minecraft:weeping_vines_plant",
                "minecraft:glass", "minecraft:red_stained_glass", "minecraft:lime_stained_glass", "minecraft:pink_stained_glass", "minecraft:gray_stained_glass", "minecraft:cyan_stained_glass", "minecraft:blue_stained_glass", "minecraft:white_stained_glass", "minecraft:brown_stained_glass", "minecraft:green_stained_glass", "minecraft:black_stained_glass", "minecraft:orange_stained_glass", "minecraft:yellow_stained_glass", "minecraft:purple_stained_glass", "minecraft:magenta_stained_glass",
                "minecraft:glass_pane", "minecraft:red_stained_glass_pane", "minecraft:lime_stained_glass_pane", "minecraft:pink_stained_glass_pane", "minecraft:gray_stained_glass_pane", "minecraft:cyan_stained_glass_pane", "minecraft:blue_stained_glass_pane", "minecraft:light_blue_stained_glass", "minecraft:light_gray_stained_glass", "minecraft:white_stained_glass_pane", "minecraft:brown_stained_glass_pane", "minecraft:green_stained_glass_pane", "minecraft:black_stained_glass_pane", "minecraft:orange_stained_glass_pane", "minecraft:yellow_stained_glass_pane", "minecraft:purple_stained_glass_pane", "minecraft:magenta_stained_glass_pane", "minecraft:light_blue_stained_glass_pane", "minecraft:light_gray_stained_glass_pane",
                "#minecraft:corals", "#minecraft:coral_blocks"
        ));

        @ConfigEntry.Gui.Tooltip
        @BlockList
        public List<String> silktouch_setting_always = new ArrayList<>(Arrays.asList(
                "minecraft:grass_block", "minecraft:mycelium", "minecraft:podzol", "minecraft:warped_nylium", "minecraft:crimson_nylium", "minecraft:stone", "minecraft:gravel",
                "minecraft:gold_ore", "minecraft:iron_ore", "minecraft:coal_ore", "minecraft:diamond_ore", "minecraft:emerald_ore", "minecraft:redstone_ore", "minecraft:nether_gold_ore", "minecraft:lapis_ore", "minecraft:nether_quartz_ore", "minecraft:gilded_blackstone",
                "#minecraft:leaves", "minecraft:snow_block", "minecraft:snow"
        ));

        @ConfigEntry.Gui.Tooltip
        @BlockList
        public List<String> silktouch_setting_exc_ores = new ArrayList<>(Arrays.asList(
                "minecraft:grass_block", "minecraft:podzol", "minecraft:mycelium", "minecraft:gravel",
                "minecraft:stone",
                "minecraft:warped_nylium", "minecraft:crimson_nylium",
                "#minecraft:leaves", "minecraft:snow_block", "minecraft:snow"
        ));

        @ConfigEntry.Gui.Tooltip
        @BlockList
        public List<String> silktouch_setting_always_ores = new ArrayList<>(Arrays.asList(
                "minecraft:gravel",
                "minecraft:gold_ore", "minecraft:iron_ore", "minecraft:coal_ore", "minecraft:diamond_ore", "minecraft:emerald_ore", "minecraft:redstone_ore", "minecraft:nether_gold_ore", "minecraft:lapis_ore", "minecraft:nether_quartz_ore", "minecraft:gilded_blackstone"
        ));


        @ConfigEntry.Gui.Tooltip
        @BlockList
        public List<String> fortune = new ArrayList<>(Arrays.asList(
                "minecraft:gold_ore", "minecraft:iron_ore", "minecraft:coal_ore", "minecraft:diamond_ore", "minecraft:emerald_ore", "minecraft:redstone_ore", "minecraft:nether_gold_ore", "minecraft:lapis_ore", "minecraft:nether_quartz_ore",
                "minecraft:gilded_blackstone",
                "minecraft:nether_wart", "minecraft:sweet_berry_bush", "minecraft:wheat", "minecraft:carrots", "minecraft:potatoes", "minecraft:beetroots",
                "minecraft:glowstone", "minecraft:melon", "minecraft:sea_lantern", "minecraft:twisting_vines", "minecraft:twisting_vines_plant", "minecraft:weeping_vines", "minecraft:weeping_vines_plant"
        ));

        @ConfigEntry.Gui.Tooltip
        @BlockList
        public List<String> fortune_setting = new ArrayList<>(Arrays.asList("minecraft:gravel", "#minecraft:leaves"));


        @ConfigEntry.Gui.Tooltip
        @BlockList
        public List<String> shears = new ArrayList<>(Arrays.asList(
                "minecraft:dead_bush", "minecraft:fern", "minecraft:large_fern", "#minecraft:leaves", "minecraft:nether_sprouts", "minecraft:seagrass", "minecraft:tall_seagrass", "minecraft:grass", "minecraft:tall_grass", "minecraft:tripwire", "minecraft:twisting_vines", "minecraft:weeping_vines", "minecraft:vine", "minecraft:cobweb"
        ));


        @ConfigEntry.Gui.Tooltip
        @BlockList
        public List<String> do_not_swap_unless_ench = new ArrayList<>(Arrays.asList(
                "minecraft:dead_bush", "minecraft:fern", "minecraft:large_fern", "#minecraft:leaves", "minecraft:nether_sprouts", "minecraft:seagrass", "minecraft:tall_seagrass", "minecraft:grass", "minecraft:tall_grass", "minecraft:tripwire", "minecraft:twisting_vines", "minecraft:weeping_vines", "minecraft:vine", "minecraft:cobweb",
                "minecraft:nether_wart", "minecraft:sweet_berry_bush", "minecraft:wheat", "minecraft:carrots", "minecraft:potatoes", "minecraft:beetroots",
                "minecraft:twisting_vines", "minecraft:twisting_vines_plant", "minecraft:weeping_vines", "minecraft:weeping_vines_plant",
                "#minecraft:corals", "#minecraft:coral_blocks"
        ));


        @ConfigEntry.Gui.Tooltip
        public boolean enable_datapacks = true;

        @ConfigEntry.Gui.Tooltip
        public List<String> customTools = new ArrayList<>();
    }

    @Config(name = "general")
    public static class GeneralConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public boolean toggle = false;

        @ConfigEntry.Gui.Tooltip
        public boolean disableCreative = true;

        @ConfigEntry.Gui.Tooltip
        public boolean keepSlot = false;

        @ConfigEntry.Gui.Tooltip
        public boolean hotbarOnly = false;

        @ConfigEntry.Gui.Tooltip
        public boolean preferHotbarTool = true;

        @ConfigEntry.Gui.Tooltip
        public boolean preferLowDurability = false;

        @ConfigEntry.Gui.Tooltip
        public boolean preserveDurability = true;

        @ConfigEntry.Gui.Tooltip
        public boolean alwaysPreferFortune = false;

        @ConfigEntry.Gui.Tooltip
        public boolean onlySwitchIfNecessary = false;

        @ConfigEntry.Gui.Tooltip
        public boolean switchBack = false;

        @ConfigEntry.Gui.Tooltip
        public boolean showDPS = false;

        @ConfigEntry.Gui.Tooltip
        public boolean changeForEntities = true;

        @ConfigEntry.Gui.Tooltip
        public boolean keepAxe = false;

        @ConfigEntry.Gui.Tooltip
        public PreferSilkTouch preferSilkTouch = PreferSilkTouch.except_ores;

        @ConfigEntry.Gui.Tooltip
        public Enabled enabled = Enabled.always;

        @HotbarSlots
        @ConfigEntry.Gui.Tooltip
        public List<Integer> ignoredSlots = new ArrayList<>();
        @HotbarSlots(allowEmpty = false)
        @ConfigEntry.Gui.Tooltip
        public List<Integer> targetSlots = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
        @ConfigEntry.Gui.Tooltip
        public List<Integer> ignoredInventorySlots = new ArrayList<>();

        @ConfigEntry.Gui.Tooltip
        public double minDurability = 0d;
        @ConfigEntry.Gui.Tooltip
        public boolean durabilityCheck = true;

        @ConfigEntry.Gui.Tooltip
        public boolean experimentalBreakDelay = false;

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
    }
}