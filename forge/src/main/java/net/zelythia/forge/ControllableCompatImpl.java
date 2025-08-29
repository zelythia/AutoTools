package net.zelythia.forge;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBindings;
import net.minecraftforge.fml.ModList;

public class ControllableCompatImpl {
    public static boolean attackDown() {
        if (!ModList.get().isLoaded("controllable")) return false;

        return Controllable.getController() != null && ButtonBindings.ATTACK.isButtonDown();
    }
}
