package net.zelythia.clientTags.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class ClientTagsLoaderImpl {
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
}
