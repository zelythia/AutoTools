package net.zelythia.fabric.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import net.zelythia.config.AutoToolsConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> (AbstractConfigScreen) AutoConfig.getConfigScreen(AutoToolsConfig.class, parent).get();
    }
}
