package net.zelythia.autotools.config.autoconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.autoconfig.gui.registry.api.GuiTransformer;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.StringListListEntry;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class CustomToolsTransformer implements GuiTransformer {
    @Override
    public List<AbstractConfigListEntry> transform(List<AbstractConfigListEntry> list, String s, Field field, Object config, Object defaults, GuiRegistryAccess guiRegistryAccess) {
        StringListListEntry l = (StringListListEntry) list.get(0);

        l.setCellErrorSupplier(s1 -> {
            try {
                JsonElement jsonElement = JsonParser.parseString("{" + s1 + "}");
                if (!jsonElement.isJsonObject())  return Optional.of(Component.translatable("text.autoconfig.autotools.error.incorrect_json"));
            }catch (Exception e){
                return Optional.of(Component.translatable("text.autoconfig.autotools.error.incorrect_json"));
            }

            return Optional.empty();
        });

        return List.of(l);
    }
}
