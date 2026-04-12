package net.zelythia.autotools.config.autoconfig;

import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.AbstractListListEntry;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.impl.ConfigEntryBuilderImpl;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ItemListListEntry extends AbstractListListEntry<String, ItemListListEntry.ItemCell, ItemListListEntry> {
    public ItemListListEntry(Component fieldName, List<String> value, boolean defaultExpanded, Supplier<Optional<Component[]>> tooltipSupplier, Consumer<List<String>> saveConsumer, Supplier<List<String>> defaultValue, Component resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront) {
        super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, (s, itemListListEntry) -> new ItemCell(fieldName, s, itemListListEntry));
    }

    @Override
    public ItemListListEntry self() {
        return this;
    }

    @Override
    public void lateRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.lateRender(graphics, mouseX, mouseY, delta);

        for (ItemCell cell : cells) {
            cell.lateRender(graphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public void save() {
        super.save();
    }

    public static class ItemCell extends AbstractListCell<String, ItemCell, ItemListListEntry> {

        private final @NotNull DropdownBoxEntry<String> dropdownBoxEntry;

        public ItemCell(Component fieldName, @Nullable String value, ItemListListEntry listListEntry) {
            super(value, listListEntry);
            if (value == null) value = "minecraft:air";

            DropdownBoxEntry.SelectionTopCellElement<String> topCellElement = DropdownMenuBuilder.TopCellElementBuilder.of(value, s -> {
                if (s.startsWith("#")) {
                    if (s.indexOf(":") <= 1 || s.indexOf(":") == s.length() - 1) return null;
                    return s;
                }

                try {
                    Identifier identifier = Identifier.parse(s);
                    if (BuiltInRegistries.BLOCK.getOptional(identifier).isPresent()) {
                        return s;
                    }
                } catch (Exception var2) {
                }

                return null;
            });

            DropdownBoxEntry.DefaultSelectionCellCreator<String> cellCreator = new DropdownBoxEntry.DefaultSelectionCellCreator<>() {
                public DropdownBoxEntry.SelectionCellElement<String> create(String selection) {

                    Identifier id;
                    if (selection.startsWith("#")) id = Identifier.parse("minecraft:air");
                    else id = Identifier.parse(selection);

                    return new DropdownBoxEntry.DefaultSelectionCellElement<String>(selection, this.toTextFunction) {
                        private ItemStack getItem(Identifier id){
                            try {
                                return new ItemStack(BuiltInRegistries.BLOCK.getValue(id));
                            }catch (NullPointerException e){
                                return ItemStack.EMPTY;
                            }
                        }

                        @Override
                        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                            this.rendering = true;
                            this.x = x;
                            this.y = y;
                            this.width = width;
                            this.height = height;
                            boolean b = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
                            if (b) {
                                graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, -15132391);
                            }

                            graphics.text(Minecraft.getInstance().font, this.toTextFunction.apply(this.r).getVisualOrderText(), x + 6 + 18, y + 6, b ? -1 : -7829368);
//                            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                            graphics.item(getItem(id), x + 4, y + 2);
                        }
                    };
                }

                public int getCellHeight() {
                    return 20;
                }

                public int getCellWidth() {
                    return 146;
                }

                public int getDropBoxMaxHeight() {
                    return this.getCellHeight() * 7;
                }
            };

            dropdownBoxEntry = ConfigEntryBuilderImpl.create().startDropdownMenu(fieldName, topCellElement, cellCreator)
                    .setSelections(BuiltInRegistries.BLOCK.stream().map(block -> BuiltInRegistries.BLOCK.getKey(block).toString()).collect(Collectors.toSet()))
                    .build();
        }

        @Override
        public String getValue() {
            return dropdownBoxEntry.getValue();
        }

        @Override
        public void updateSelected(boolean isSelected) {
            dropdownBoxEntry.updateSelected(isSelected);
        }

        @Override
        public boolean isRequiresRestart() {
            return dropdownBoxEntry.isRequiresRestart();
        }

        @Override
        public boolean isEdited() {
            return dropdownBoxEntry.isEdited();
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return dropdownBoxEntry.isMouseOver(mouseX, mouseY);
        }

        @Override
        public Optional<Component> getError() {
            return dropdownBoxEntry.getError();
        }

        @Override
        public int getCellHeight() {
            return dropdownBoxEntry.getItemHeight();
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            dropdownBoxEntry.setScreen(listListEntry.getConfigScreen());
            dropdownBoxEntry.setParent((ClothConfigScreen.ListWidget) listListEntry.getParent());
            dropdownBoxEntry.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        }

        public void lateRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            dropdownBoxEntry.lateRender(graphics, mouseX, mouseY, delta);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            dropdownBoxEntry.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
            dropdownBoxEntry.setFocused(true);
            return dropdownBoxEntry.mouseClicked(mouseButtonEvent, bl);
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
            return dropdownBoxEntry.mouseReleased(mouseButtonEvent);
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
            return dropdownBoxEntry.mouseDragged(mouseButtonEvent, d, e);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return dropdownBoxEntry.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        @Override
        public boolean keyPressed(KeyEvent keyEvent) {
            return dropdownBoxEntry.keyPressed(keyEvent);
        }

        @Override
        public boolean keyReleased(KeyEvent keyEvent) {
            return dropdownBoxEntry.keyReleased(keyEvent);
        }

        @Override
        public boolean charTyped(CharacterEvent characterEvent) {
            return dropdownBoxEntry.charTyped(characterEvent);
        }

        @Override
        public @Nullable GuiEventListener getFocused() {
            return dropdownBoxEntry.getFocused();
        }

        @Override
        public boolean isFocused() {
            return dropdownBoxEntry.isFocused();
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            super.setFocused(focused);
            dropdownBoxEntry.setFocused(focused);
        }

        @Override
        public void setFocused(boolean focused) {
            dropdownBoxEntry.setFocused(focused);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return Collections.singletonList(this.dropdownBoxEntry);
        }

        @Override
        public @NotNull NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
            this.dropdownBoxEntry.updateNarration(narrationElementOutput);
        }
    }
}
