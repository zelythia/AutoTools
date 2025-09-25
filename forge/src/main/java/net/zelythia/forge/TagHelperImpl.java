package net.zelythia.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.zelythia.AutoTools;

public class TagHelperImpl {
    public static Tag.Named<Block> createBlockTag(ResourceLocation resourceLocation) {
        return BlockTags.bind(resourceLocation.toString());
    }

    public static Tag.Named<Item> createItemTag(ResourceLocation resourceLocation) {
        return ItemTags.bind(resourceLocation.toString());
    }
}
