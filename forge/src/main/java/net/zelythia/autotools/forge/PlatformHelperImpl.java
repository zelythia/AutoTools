package net.zelythia.autotools.forge;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBindings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class PlatformHelperImpl {
    public static HashSet<Path> getResourcePaths(String path) {
        HashSet<Path> out = new HashSet<>();

        for (ModFileInfo modFile : ModList.get().getModFiles()) {
            Path modPath = modFile.getFile().findResource(path);
            if (Files.exists(modPath)) {
                out.add(modPath);
            }
        }

        return out;
    }

    public static boolean controllableAttackDown() {
        if (!ModList.get().isLoaded("controllable")) return false;

        return Controllable.getController() != null && ButtonBindings.ATTACK.isButtonDown();
    }

    public static Tag.Named<Block> createBlockTag(ResourceLocation resourceLocation) {
        return BlockTags.createOptional(resourceLocation);
    }

    public static Tag.Named<Item> createItemTag(ResourceLocation resourceLocation) {
        return ItemTags.createOptional(resourceLocation);
    }
}
