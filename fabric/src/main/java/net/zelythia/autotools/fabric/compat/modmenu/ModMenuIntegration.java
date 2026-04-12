package net.zelythia.autotools.fabric.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import net.zelythia.autotools.config.AutoToolsConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> (AbstractConfigScreen) AutoConfigClient.getConfigScreen(AutoToolsConfig.class, parent).get();
    }
}
