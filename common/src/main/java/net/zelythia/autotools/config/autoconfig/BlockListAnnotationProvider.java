package net.zelythia.autotools.config.autoconfig;

import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.network.chat.TranslatableComponent;

import java.lang.reflect.Field;
import java.util.List;

public class BlockListAnnotationProvider implements GuiProvider {

    @Override
    public List<AbstractConfigListEntry> get(String s, Field field, Object config, Object defaults, GuiRegistryAccess guiRegistryAccess) {
        ItemListListEntry blockList = null;
        try {
            blockList = new ItemListBuilder(new TranslatableComponent("text.cloth-config.reset_value"), new TranslatableComponent(s), (List<String>) field.get(config)).setSaveConsumer(list -> {
                try {
                    field.set(config, list);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }).setDefaultValue((List<String>) field.get(defaults)).build();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return List.of(blockList);
    }
}
