package net.zelythia.autotools.fabric;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

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
        if (!FabricLoader.getInstance().isModLoaded("controllable")) return false;

        return Controllable.getController() != null && ButtonBindings.ATTACK.isButtonDown();
    }
}
