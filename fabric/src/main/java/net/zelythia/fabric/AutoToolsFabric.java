package net.zelythia.fabric;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import me.shedaniel.clothconfig2.gui.entries.AbstractListListEntry;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.gui.entries.StringListListEntry;
import me.shedaniel.clothconfig2.impl.ConfigEntryBuilderImpl;
import me.shedaniel.clothconfig2.impl.builders.AbstractListBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;
import net.zelythia.ControllableCompat;
import net.zelythia.TooltipHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AutoToolsFabric implements ClientModInitializer {
    private static final KeyMapping KEY_AUTOTOOLS = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.autotools.get_tool", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category"));
    private static final KeyMapping KEY_SILKTOUCH = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.autotools.silktouch", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.autotools.category"));
    private boolean keyPressed = false;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(AutoToolsConfigImpl.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));  //TODO switch to Jankson after Lists are fixed

        AutoConfig.getConfigHolder(AutoToolsConfigImpl.class).registerSaveListener((configHolder, autoToolsConfig) -> {
            AutoTools.reloadConfig();
            return InteractionResult.SUCCESS;
        });

        GuiRegistry registry = AutoConfig.getGuiRegistry(AutoToolsConfigImpl.class);

        registry.registerAnnotationProvider((s, field, config, defaults, guiRegistryAccess) -> {
            ItemListListEntry blockList = null;
            try {
                blockList = new ItemListBuilder(Component.translatable("text.cloth-config.reset_value"), Component.translatable(s), (List<String>) field.get(config)).setSaveConsumer(list -> {
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
        }, BlockList.class);

        registry.registerPredicateTransformer((list, s, field, o, o1, guiRegistryAccess) -> {

            StringListListEntry l = (StringListListEntry) list.getFirst();

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
        }, field -> field.getName() == "customTools");

        AutoTools.init();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (AutoToolsConfig.TOGGLE) {
                //Changing the toggle setting:
                //When toggling the keybinding should only be reacted to once per press
                if (KEY_AUTOTOOLS.consumeClick()) {
                    if (!keyPressed) {
                        AutoTools.toggle = !AutoTools.toggle;
                        client.player.displayClientMessage(AutoTools.toggle ? Component.translatable("chat.enabled_autotools") : Component.translatable("chat.disabled_autotools"), false);
                        keyPressed = true;
                    }
                    //resetting the keyPressed-count
                    while (KEY_AUTOTOOLS.consumeClick()) {
                        keyPressed = true;
                    }
                } else {
                    keyPressed = false;
                }
            } else {
                if (KEY_AUTOTOOLS.consumeClick()) {
                    AutoTools.startedMining = false;
                    AutoTools.getCorrectTool(client.hitResult, client);
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (AutoToolsConfig.SWITCH_BACK) {
                if (Minecraft.getInstance().options.keyAttack.isDown() || ControllableCompat.attackDown()) {
                    AutoTools.startedMining = true;
                } else {
                    //Detecting switchBack for entities when using toggle, switching back otherwise if the key is released
                    if (AutoToolsConfig.TOGGLE || AutoTools.startedMining) {
                        AutoTools.switchBack();
                    }
                }
            }

            if (KEY_SILKTOUCH.consumeClick()) {
                AutoToolsConfig.PreferSilkTouch[] values = AutoToolsConfig.PreferSilkTouch.values();
                AutoToolsConfig.PREFER_SILK_TOUCH = values[(AutoToolsConfig.PREFER_SILK_TOUCH.ordinal() + 1) % values.length];

                client.player.displayClientMessage(Component.translatable("chat.cycle_silktouch").append(Component.translatable("autotools.configuration.preferSilkTouch." + AutoToolsConfig.PREFER_SILK_TOUCH)), false);

                AutoToolsConfig.save();
            }
        });


        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            TooltipHelper.applyTooltip(stack, lines);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            AutoTools.swaps.clear();
        });
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface BlockList {
    }

    static class ItemListListEntry extends AbstractListListEntry<String, ItemListListEntry.ItemCell, ItemListListEntry> {
        public ItemListListEntry(Component fieldName, List<String> value, boolean defaultExpanded, Supplier<Optional<Component[]>> tooltipSupplier, Consumer<List<String>> saveConsumer, Supplier<List<String>> defaultValue, Component resetButtonKey, boolean requiresRestart, boolean deleteButtonEnabled, boolean insertInFront) {
            super(fieldName, value, defaultExpanded, tooltipSupplier, saveConsumer, defaultValue, resetButtonKey, requiresRestart, deleteButtonEnabled, insertInFront, (s, itemListListEntry) -> new ItemCell(fieldName, s, itemListListEntry));
        }

        @Override
        public ItemListListEntry self() {
            return this;
        }

        @Override
        public void lateRender(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            super.lateRender(graphics, mouseX, mouseY, delta);

            for (ItemCell cell : cells) {
                cell.lateRender(graphics, mouseX, mouseY, delta);
            }
        }

        @Override
        public void save() {
            super.save();
        }

        public static class ItemCell extends AbstractListListEntry.AbstractListCell<String, ItemCell, ItemListListEntry> {

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
                        ResourceLocation identifier = ResourceLocation.parse(s);
                        if (BuiltInRegistries.BLOCK.getOptional(identifier).isPresent()) {
                            return s;
                        }
                    } catch (Exception var2) {
                    }

                    return null;
                });

                DropdownBoxEntry.DefaultSelectionCellCreator<String> cellCreator = new DropdownBoxEntry.DefaultSelectionCellCreator<>() {
                    public DropdownBoxEntry.SelectionCellElement<String> create(String selection) {

                        ResourceLocation resourceLocation;
                        if (selection.startsWith("#")) resourceLocation = ResourceLocation.parse("minecraft:air");
                        else resourceLocation = ResourceLocation.parse(selection);

                        final ItemStack s = new ItemStack(BuiltInRegistries.BLOCK.getValue(resourceLocation));


                        return new DropdownBoxEntry.DefaultSelectionCellElement<String>(selection, this.toTextFunction) {
                            public void render(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
                                this.rendering = true;
                                this.x = x;
                                this.y = y;
                                this.width = width;
                                this.height = height;
                                boolean b = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
                                if (b) {
                                    graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, -15132391);
                                }

                                graphics.drawString(Minecraft.getInstance().font, this.toTextFunction.apply(this.r).getVisualOrderText(), x + 6 + 18, y + 6, b ? -1 : -7829368);
                                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                                graphics.renderItem(s, x + 4, y + 2);
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
            public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
                dropdownBoxEntry.setScreen(listListEntry.getConfigScreen());
                dropdownBoxEntry.setParent((ClothConfigScreen.ListWidget) listListEntry.getParent());
                dropdownBoxEntry.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
            }

            public void lateRender(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
                dropdownBoxEntry.lateRender(graphics, mouseX, mouseY, delta);
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
            public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
                return dropdownBoxEntry.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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

    class ItemListBuilder extends AbstractListBuilder<String, ItemListListEntry, ItemListBuilder> {
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
            entry.setTooltipSupplier(() -> (Optional) this.getTooltipSupplier().apply(entry.getValue()));
            entry.setAddTooltip(this.getAddTooltip());
            entry.setRemoveTooltip(this.getRemoveTooltip());
            if (this.errorSupplier != null) {
                entry.setErrorSupplier(() -> (Optional) this.errorSupplier.apply(entry.getValue()));
            }

            return this.finishBuilding(entry);
        }
    }


}
