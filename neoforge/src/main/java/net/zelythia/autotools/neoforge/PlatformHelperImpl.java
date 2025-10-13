package net.zelythia.autotools.neoforge;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class PlatformHelperImpl {
    public static HashSet<Path> getResourcePaths(String path) {
        HashSet<Path> out = new HashSet<>();

        for (IModFileInfo modFile : ModList.get().getModFiles()) {
            Path modPath = modFile.getFile().findResource(path);
            if (Files.exists(modPath)) {
                out.add(modPath);
            }
        }

        return out;
    }
}
