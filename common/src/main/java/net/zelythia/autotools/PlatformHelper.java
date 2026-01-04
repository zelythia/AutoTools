package net.zelythia.autotools;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.nio.file.Path;
import java.util.HashSet;

public class PlatformHelper {

    @ExpectPlatform
    public static HashSet<Path> getResourcePaths(String path) {
        throw new AssertionError();
    }


    @ExpectPlatform
    public static boolean controllableAttackDown() {
        throw new AssertionError();
    }


    @ExpectPlatform
    public static Tag.Named<Block> createBlockTag(ResourceLocation resourceLocation){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Tag.Named<Item> createItemTag(ResourceLocation resourceLocation){
        throw new AssertionError();
    }

}
