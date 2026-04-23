package net.zelythia.autotools.config.autoconfig;

import me.shedaniel.clothconfig2.impl.builders.AbstractListBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ItemListBuilder extends AbstractListBuilder<String, ItemListListEntry, ItemListBuilder> {
    private Function<ItemListListEntry, ItemListListEntry.ItemCell> createNewInstance;

    public ItemListBuilder(Component resetButtonKey, Component fieldNameKey, List<String> value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }

    public Function<String, Optional<Component>> getCellErrorSupplier() {
        return super.getCellErrorSupplier();
    }

    public ItemListBuilder setCreateNewInstance(Function<ItemListListEntry, ItemListListEntry.ItemCell> createNewInstance) {
        this.createNewInstance = createNewInstance;
        return this;
    }

    public @NotNull ItemListListEntry build() {
        ItemListListEntry entry = new ItemListListEntry(this.getFieldNameKey(), this.value, this.isExpanded(), null, this.getSaveConsumer(), this.defaultValue, this.getResetButtonKey(), this.isRequireRestart(), this.isDeleteButtonEnabled(), this.isInsertInFront());
        if (this.createNewInstance != null) {
            entry.setCreateNewInstance(this.createNewInstance);
        }

        entry.setInsertButtonEnabled(this.isInsertButtonEnabled());
        entry.setCellErrorSupplier(this.cellErrorSupplier);
        entry.setTooltipSupplier(() -> this.getTooltipSupplier().apply(entry.getValue()));
        entry.setAddTooltip(this.getAddTooltip());
        entry.setRemoveTooltip(this.getRemoveTooltip());
        if (this.errorSupplier != null) {
            entry.setErrorSupplier(() -> this.errorSupplier.apply(entry.getValue()));
        }

        return this.finishBuilding(entry);
    }
}
