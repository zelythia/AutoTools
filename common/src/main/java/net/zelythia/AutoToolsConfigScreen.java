package net.zelythia;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AutoToolsConfigScreen extends Screen {
    public final Screen parent;

    public AutoToolsConfigScreen(Screen screen) {
        super(Component.translatable("ui.title"));
        parent = screen;
    }

    @Override
    protected void init() {
        AutoToolsConfig.load();
        AutoTools.loadCustomItems();

        final int y = this.height / 6 - 12;

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.TOGGLE)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.toggle")))
                .create(this.width / 2 - 155, y, 150, 20,
                        Component.translatable("ui.config.toggle"),
                        (cycleButton, boolean_) -> AutoToolsConfig.TOGGLE = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.DISABLECREATIVE)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.disableCreative")))
                .create(this.width / 2 - 155, y + 24, 150, 20,
                        Component.translatable("ui.config.disableCreative"),
                        (cycleButton, boolean_) -> AutoToolsConfig.DISABLECREATIVE = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.KEEPSLOT)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.keepSlot")))
                .create(this.width / 2 - 155, y + 48, 150, 20,
                        Component.translatable("ui.config.keepSlot"),
                        (cycleButton, boolean_) -> AutoToolsConfig.KEEPSLOT = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.PREFER_HOTBAR_TOOL)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.preferHotbarTool")))
                .create(this.width / 2 - 155, y + 72, 150, 20,
                        Component.translatable("ui.config.preferHotbarTool"),
                        (cycleButton, boolean_) -> AutoToolsConfig.PREFER_HOTBAR_TOOL = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.PREFER_LOW_DURABILITY)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.preferLowDurability")))
                .create(this.width / 2 - 155, y + 96, 150, 20,
                        Component.translatable("ui.config.preferLowDurability"),
                        (cycleButton, boolean_) -> AutoToolsConfig.PREFER_LOW_DURABILITY = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.ALWAYS_PREFER_FORTUNE)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.preferFortune")))
                .create(this.width / 2 - 155, y + 120, 150, 20,
                        Component.translatable("ui.config.preferFortune"),
                        (cycleButton, boolean_) -> AutoToolsConfig.ALWAYS_PREFER_FORTUNE = boolean_)
        );


        //
        //
        //

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.onlyNecessary")))
                .create(this.width / 2 + 5, y, 150, 20,
                        Component.translatable("ui.config.onlyNecessary"),
                        (cycleButton, boolean_) -> AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.SWITCH_BACK)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.switchBack")))
                .create(this.width / 2 + 5, y + 24, 150, 20,
                        Component.translatable("ui.config.switchBack"),
                        (cycleButton, boolean_) -> AutoToolsConfig.SWITCH_BACK = boolean_)
        );

        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.SHOWDPS)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.showDPS")))
                .create(this.width / 2 + 5, y + 48, 150, 20,
                        Component.translatable("ui.config.showDPS"),
                        (cycleButton, boolean_) -> AutoToolsConfig.SHOWDPS = boolean_)
        );


        this.addRenderableWidget(CycleButton.onOffBuilder(AutoToolsConfig.CHANGE_FOR_ENTITIES)
                .withTooltip(aBoolean -> Tooltip.create(Component.translatable("ui.desc.changeForEntities")))
                .create(this.width / 2 + 5, y + 72, 150, 20,
                        Component.translatable("ui.config.changeForEntities"),
                        (cycleButton, boolean_) -> AutoToolsConfig.CHANGE_FOR_ENTITIES = boolean_)
        );

        //
        //
        //

        this.addRenderableWidget(CycleButton.builder(
                        (String string) -> {
                            return switch (string) {
                                case "always" -> Component.translatable("ui.config.preferSilkTouch.always");
                                case "always_ores" -> Component.translatable("ui.config.preferSilkTouch.always_ores");
                                case "except_ores" -> Component.translatable("ui.config.preferSilkTouch.except_ores");
                                case "never" -> Component.translatable("ui.config.preferSilkTouch.never");
                                default -> Component.translatable("ui.config.error");
                            };
                        })
                .withValues("never", "except_ores", "always_ores", "always")
                .withInitialValue(AutoToolsConfig.PREFER_SILK_TOUCH)
                .withTooltip(s -> Tooltip.create(Component.translatable("ui.desc.preferSilkTouch." + AutoToolsConfig.PREFER_SILK_TOUCH)))
                .create(this.width / 2 - 155, y + 144, 310, 20,
                        Component.translatable("ui.config.preferSilkTouch"),
                        (cycleButton, string) -> AutoToolsConfig.PREFER_SILK_TOUCH = string
                ))
        ;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.onClose();
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 15, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        AutoToolsConfig.save();
        this.minecraft.setScreen(parent);
    }
}
