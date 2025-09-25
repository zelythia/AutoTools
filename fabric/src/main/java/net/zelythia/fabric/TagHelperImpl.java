package net.zelythia.fabric;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TagHelperImpl {
    public static Tag.Named<Block> createBlockTag(ResourceLocation resourceLocation) {
        return (Tag.Named<Block>) TagRegistry.block(resourceLocation);
    }

    public static Tag.Named<Item> createItemTag(ResourceLocation resourceLocation) {
        return (Tag.Named<Item>) TagRegistry.item(resourceLocation);
    }
}
