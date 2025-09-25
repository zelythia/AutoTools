package net.zelythia;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TagHelper {

    @ExpectPlatform
    public static Tag.Named<Block> createBlockTag(ResourceLocation resourceLocation){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Tag.Named<Item> createItemTag(ResourceLocation resourceLocation){
        throw new AssertionError();
    }


}
