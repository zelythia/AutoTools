package net.zelythia;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AutoToolsConfigScreen extends Screen {
    public final Screen parent;

    public AutoToolsConfigScreen(Screen screen) {
        super(Component.translatable("autotools.configuration.AutoTools"));
        parent = screen;
    }
/*
    @Override
    protected void init() {
        AutoTools.reloadConfig();

        final int y = this.height / 6 - 12;

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.TOGGLE)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.toggle")))
                .create(this.width / 2 - 155, y, 150, 20,
                        Component.translatable("autotools.configuration.toggle"),
                        (cycleButton, boolean_) -> AutoToolsConfig.TOGGLE = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.DISABLECREATIVE)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.disableCreative")))
                .create(this.width / 2 - 155, y + 24, 150, 20,
                        Component.translatable("autotools.configuration.disableCreative"),
                        (cycleButton, boolean_) -> AutoToolsConfig.DISABLECREATIVE = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.KEEPSLOT)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.keepSlot")))
                .create(this.width / 2 - 155, y + 48, 150, 20,
                        Component.translatable("autotools.configuration.keepSlot"),
                        (cycleButton, boolean_) -> AutoToolsConfig.KEEPSLOT = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.PREFER_HOTBAR_TOOL)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.preferHotbarTool")))
                .create(this.width / 2 - 155, y + 72, 150, 20,
                        Component.translatable("autotools.configuration.preferHotbarTool"),
                        (cycleButton, boolean_) -> AutoToolsConfig.PREFER_HOTBAR_TOOL = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.PREFER_LOW_DURABILITY)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.preferLowDurability")))
                .create(this.width / 2 - 155, y + 96, 150, 20,
                        Component.translatable("autotools.configuration.preferLowDurability"),
                        (cycleButton, boolean_) -> AutoToolsConfig.PREFER_LOW_DURABILITY = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.ALWAYS_PREFER_FORTUNE)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.alwaysPreferFortune")))
                .create(this.width / 2 - 155, y + 120, 150, 20,
                        Component.translatable("autotools.configuration.alwaysPreferFortune"),
                        (cycleButton, boolean_) -> AutoToolsConfig.ALWAYS_PREFER_FORTUNE = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.EXPERIMENTAL_BREAK_DELAY)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.experimentalBreakDelay")))
                .create(this.width / 2 - 155, y + 144, 150, 20,
                        Component.translatable("autotools.configuration.experimentalBreakDelay"),
                        (cycleButton, boolean_) -> AutoToolsConfig.EXPERIMENTAL_BREAK_DELAY = boolean_)
        );


        //
        //
        //

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.onlySwitchIfNecessary")))
                .create(this.width / 2 + 5, y, 150, 20,
                        Component.translatable("autotools.configuration.onlySwitchIfNecessary"),
                        (cycleButton, boolean_) -> AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.SWITCH_BACK)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.switchBack")))
                .create(this.width / 2 + 5, y + 24, 150, 20,
                        Component.translatable("autotools.configuration.switchBack"),
                        (cycleButton, boolean_) -> AutoToolsConfig.SWITCH_BACK = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.SHOWDPS)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.showDPS")))
                .create(this.width / 2 + 5, y + 48, 150, 20,
                        Component.translatable("autotools.configuration.showDPS"),
                        (cycleButton, boolean_) -> AutoToolsConfig.SHOWDPS = boolean_)
        );


        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.CHANGE_FOR_ENTITIES)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.changeForEntities")))
                .create(this.width / 2 + 5, y + 72, 150, 20,
                        Component.translatable("autotools.configuration.changeForEntities"),
                        (cycleButton, boolean_) -> AutoToolsConfig.CHANGE_FOR_ENTITIES = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.KEEP_AXE)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.autotools.keepAxe")))
                .create(this.width / 2 + 5, y + 96, 150, 20,
                        Component.translatable("autotools.configuration.keepAxe"),
                        (cycleButton, boolean_) -> AutoToolsConfig.KEEP_AXE = boolean_)
        );

        this.addRenderableWidget(CycleButton.builder(
                        (String string) -> switch (string) {
                            case "always" -> Component.translatable("autotools.configuration.enabled.always");
                            case "tool" -> Component.translatable("autotools.configuration.enabled.tool");
                            case "no_tool" -> Component.translatable("autotools.configuration.enabled.no_tool");
                            default -> Component.translatable("autotools.configuration.error");
                        })
                .withValues("no_tool", "tool", "always")
                .withInitialValue(AutoToolsConfig.ENABLED)
                .withTooltip(s -> Tooltip.create(Component.translatable("ui.desc.autotools.enabled." + AutoToolsConfig.ENABLED)))
                .create(this.width / 2 + 5, y + 120, 150, 20,
                        Component.translatable("autotools.configuration.enabled"),
                        (cycleButton, string) -> {
                            cycleButton.setTooltip(Tooltip.create(Component.translatable("ui.desc.autotools.enabled." + string)));
                            AutoToolsConfig.ENABLED = string;
                        }
                ))
        ;

        //
        //
        //

        this.addRenderableWidget(CycleButton.builder(
                        (String string) -> {
                            return switch (string) {
                                case "always" -> Component.translatable("autotools.configuration.preferSilkTouch.always");
                                case "ores" -> Component.translatable("autotools.configuration.preferSilkTouch.ores");
                                case "except_ores" -> Component.translatable("autotools.configuration.preferSilkTouch.except_ores");
                                case "never" -> Component.translatable("autotools.configuration.preferSilkTouch.never");
                                default -> Component.translatable("autotools.configuration.error");
                            };
                        })
                .withValues("never", "except_ores", "ores", "always")
                .withInitialValue(AutoToolsConfig.PREFER_SILK_TOUCH)
                .withTooltip(s -> Tooltip.create(Component.translatable("ui.desc.autotools.preferSilkTouch." + AutoToolsConfig.PREFER_SILK_TOUCH)))
                .create(this.width / 2 - 155, y + 168, 310, 20,
                        Component.translatable("autotools.configuration.preferSilkTouch"),
                        (cycleButton, string) -> {
                            cycleButton.setTooltip(Tooltip.create(Component.translatable("ui.desc.autotools.preferSilkTouch." + string)));
                            AutoToolsConfig.PREFER_SILK_TOUCH = string;
                        }
                ))
        ;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.onClose();
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xffffff);

        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        AutoToolsConfig.save();
        this.minecraft.setScreen(parent);
    }

 */
}
