package net.zelythia.clientTags.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.tags.Tag;

import java.nio.file.Path;
import java.util.HashSet;

public class ClientTagsLoaderImpl {
    public static HashSet<Path> getResourcePaths(String path) {
        HashSet<Path> out = new HashSet<>();

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            mod.findPath(path).ifPresent(out::add);
        }

        return out;
    }
}
