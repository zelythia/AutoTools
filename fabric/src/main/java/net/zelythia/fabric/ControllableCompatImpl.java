package net.zelythia.fabric;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import net.fabricmc.loader.api.FabricLoader;

public class ControllableCompatImpl {
    public static boolean attackDown() {
        if (!FabricLoader.getInstance().isModLoaded("controllable")) return false;

        return Controllable.getController() != null && ButtonBindings.ATTACK.isButtonDown();
    }
}
