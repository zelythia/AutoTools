package net.zelythia.autotools.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;

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

    public static boolean controllableAttackDown() {
//        if (!ModList.get().isLoaded("controllable")) return false;
//
//        return Controllable.getController() != null && ButtonBindings.ATTACK.isButtonDown();

        return false;
    }
}
