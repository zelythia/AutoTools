package net.zelythia.autotools.config.autoconfig;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.AbstractListListEntry;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.impl.ConfigEntryBuilderImpl;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    public void lateRender(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.lateRender(matrices, mouseX, mouseY, delta);

        for (ItemCell cell : cells) {
            cell.lateRender(matrices, mouseX, mouseY, delta);
        }
    }

    @Override
    public void mouseMoved(double d, double e) {
        super.mouseMoved(d, e);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        return this.getChildAt(d, e).filter((guiEventListener) -> guiEventListener.mouseScrolled(d, e, f)).isPresent();
    }


    @Override
    public List<? extends GuiEventListener> children() {
        return super.children();
    }

    @Override
    public void save() {
        super.save();
    }


    public static class FixedDropdownBoxEntry<T> extends DropdownBoxEntry<T>{
        public FixedDropdownBoxEntry(Component fieldName, @NotNull Component resetButtonKey, @Nullable Supplier tooltipSupplier, boolean requiresRestart, @Nullable Supplier defaultValue, @Nullable Consumer saveConsumer, @Nullable Iterable selections, @NotNull SelectionTopCellElement topRenderer, @NotNull SelectionCellCreator cellCreator) {
            super(fieldName, resetButtonKey, tooltipSupplier, requiresRestart, defaultValue, saveConsumer, selections, topRenderer, cellCreator);
            super.selectionElement = new FixedSelectionElement<>(this, new Rectangle(0, 0, 150, 20), new FixedDefaultDropdownMenuElement<>(selections == null ? ImmutableList.of() : ImmutableList.copyOf(selections)), topRenderer, cellCreator);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.selectionElement.isMouseOver(mouseX, mouseY);
        }

        public static class FixedSelectionElement<T> extends DropdownBoxEntry.SelectionElement<T>{

            public FixedSelectionElement(DropdownBoxEntry<T> entry, Rectangle bounds, DropdownBoxEntry.DropdownMenuElement<T> menu, DropdownBoxEntry.SelectionTopCellElement<T> topRenderer, DropdownBoxEntry.SelectionCellCreator<T> cellCreator) {
                super(entry, bounds, menu, topRenderer, cellCreator);
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                return this.bounds.contains(mouseX, mouseY) || this.menu.isExpanded() && this.menu.isMouseOver(mouseX, mouseY);
            }
        }

        public static class  FixedDefaultDropdownMenuElement<T> extends DropdownBoxEntry.DefaultDropdownMenuElement<T>{
            public FixedDefaultDropdownMenuElement(@NotNull ImmutableList<T> selections) {
                super(selections);
            }

            @Override
            public void lateRender(PoseStack matrices, int mouseX, int mouseY, float delta) {
                super.lateRender(matrices, mouseX, mouseY, delta);
            }
        }
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
                    ResourceLocation identifier = new ResourceLocation(s);
                    if (Registry.BLOCK.getOptional(identifier).isPresent()) {
                        return s;
                    }
                } catch (Exception var2) {
                }

                return null;
            });

            DropdownBoxEntry.SelectionCellCreator<String> cellCreator = new DropdownBoxEntry.DefaultSelectionCellCreator<String>() {
                public DropdownBoxEntry.SelectionCellElement<String> create(String selection) {

                    ResourceLocation resourceLocation;
                    if (selection.startsWith("#")) resourceLocation = ResourceLocation.tryParse("minecraft:air");
                    else resourceLocation = ResourceLocation.tryParse(selection);

                    final ItemStack s = new ItemStack(Registry.BLOCK.get(resourceLocation));


                    return new DropdownBoxEntry.DefaultSelectionCellElement<String>(selection, this.toTextFunction) {
                        public void render(PoseStack matrices, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                            this.rendering = true;
                            this.x = x;
                            this.y = y;
                            this.width = width;
                            this.height = height;
                            boolean b = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
                            if (b) {
                                fill(matrices, x + 1, y + 1, x + width - 1, y + height - 1, -15132391);
                            }

                            Minecraft.getInstance().font.drawShadow(matrices, ((Component)this.toTextFunction.apply(this.r)).getVisualOrderText(), (float)(x + 6 + 18), (float)(y + 6), b ? 16777215 : 8947848);
                            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                            itemRenderer.renderGuiItem(s, x + 4, y + 2);
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

            dropdownBoxEntry = new FixedDropdownBoxEntry<>(fieldName,  ConfigEntryBuilderImpl.create().getResetButtonKey(), null, false, null, null, Registry.BLOCK.stream().map(block -> Registry.BLOCK.getKey(block).toString()).collect(Collectors.toSet()), topCellElement, cellCreator);
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
        public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            dropdownBoxEntry.setScreen(listListEntry.getConfigScreen());
            dropdownBoxEntry.setParent((ClothConfigScreen.ListWidget) listListEntry.getParent());
            dropdownBoxEntry.render(poseStack, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        }

        public void lateRender(PoseStack matrices, int mouseX, int mouseY, float delta) {
            dropdownBoxEntry.lateRender(matrices, mouseX, mouseY, delta);
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            dropdownBoxEntry.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return dropdownBoxEntry.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return dropdownBoxEntry.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return dropdownBoxEntry.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double d, double e, double f) {
            return dropdownBoxEntry.mouseScrolled(d, e, f);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return dropdownBoxEntry.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return dropdownBoxEntry.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return dropdownBoxEntry.charTyped(codePoint, modifiers);
        }

        @Override
        public @Nullable GuiEventListener getFocused() {
            return dropdownBoxEntry.getFocused();
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            super.setFocused(focused);
            dropdownBoxEntry.setFocused(focused);
        }

        @Override
        public boolean changeFocus(boolean bl) {
            return dropdownBoxEntry.changeFocus(bl);
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return Collections.singletonList(this.dropdownBoxEntry);
        }
    }
}
