package net.zelythia.autotools.fabric;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.nio.file.Path;
import java.util.HashSet;

public class PlatformHelperImpl {
    public static HashSet<Path> getResourcePaths(String path) {
        HashSet<Path> out = new HashSet<>();

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            mod.findPath(path).ifPresent(out::add);
        }

        return out;
    }

    public static boolean controllableAttackDown() {
        return false;
    }

    public static Tag.Named<Block> createBlockTag(ResourceLocation resourceLocation) {
        return (Tag.Named<Block>) TagRegistry.block(resourceLocation);
    }

    public static Tag.Named<Item> createItemTag(ResourceLocation resourceLocation) {
        return (Tag.Named<Item>) TagRegistry.item(resourceLocation);
    }

}
