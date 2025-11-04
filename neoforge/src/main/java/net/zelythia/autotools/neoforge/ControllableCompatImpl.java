package net.zelythia.autotools.neoforge;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import net.neoforged.fml.ModList;

public class ControllableCompatImpl {
    public static boolean attackDown() {
        if (!ModList.get().isLoaded("controllable")) return false;

        return Controllable.getController() != null && ButtonBindings.ATTACK.isButtonDown();
    }
}
