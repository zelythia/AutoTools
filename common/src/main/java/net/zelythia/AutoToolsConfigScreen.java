package net.zelythia;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Arrays;
import java.util.Optional;

public class AutoToolsConfigScreen extends OptionsSubScreen {

    public static final CycleOption<Boolean> TOGGLE = CycleOption.createOnOff(
            "ui.config.toggle",
            new TranslatableComponent("ui.desc.toggle"),
            gameOptions1 -> AutoToolsConfig.TOGGLE,
            (gameOptions, option, toggle) -> AutoToolsConfig.TOGGLE = toggle
    );

    public static final CycleOption<Boolean> SHOWDPS = CycleOption.createOnOff(
            "ui.config.showDPS",
            new TranslatableComponent("ui.desc.showDPS"),
            gameOptions1 -> AutoToolsConfig.SHOWDPS,
            (gameOptions, option, showDPS) -> AutoToolsConfig.SHOWDPS = showDPS
    );

    public static final CycleOption<Boolean> KEEPSLOT = CycleOption.createOnOff(
            "ui.config.keepSlot",
            new TranslatableComponent("ui.desc.keepSlot"),
            gameOptions1 -> AutoToolsConfig.KEEPSLOT,
            (gameOptions, option, keepSlot) -> AutoToolsConfig.KEEPSLOT = keepSlot
    );

    public static final CycleOption<Boolean> DISABLECREATIVE = CycleOption.createOnOff(
            "ui.config.disableCreative",
            new TranslatableComponent("ui.desc.disableCreative"),
            gameOptions1 -> AutoToolsConfig.DISABLECREATIVE,
            (gameOptions, option, disableCreative) -> AutoToolsConfig.DISABLECREATIVE = disableCreative
    );

    public static final CycleOption<Boolean> ALWAYS_PREFER_FORTUNE = CycleOption.createOnOff(
            "ui.config.preferFortune",
            new TranslatableComponent("ui.desc.preferFortune"),
            gameOptions -> AutoToolsConfig.ALWAYS_PREFER_FORTUNE,
            (gameOptions, option, preferFortune) -> AutoToolsConfig.ALWAYS_PREFER_FORTUNE = preferFortune
    );

    public static final CycleOption<String> PREFER_SILK_TOUCH = CycleOption.create(
            "ui.config.preferSilkTouch",
            () -> { //List of elements
                String[] options = {"never", "except_ores", "always_ores", "always",};
                return Arrays.stream(options).toList();
            },
            (string) -> { //Supplier
                return switch (string) {
                    case "always" -> new TranslatableComponent("ui.config.preferSilkTouch.always");
                    case "always_ores" -> new TranslatableComponent("ui.config.preferSilkTouch.always_ores");
                    case "except_ores" -> new TranslatableComponent("ui.config.preferSilkTouch.except_ores");
                    case "never" -> new TranslatableComponent("ui.config.preferSilkTouch.never");
                    default -> new TranslatableComponent("ui.config.error");
                };
            },
            options -> AutoToolsConfig.PREFER_SILK_TOUCH,  //Getter
            (options, option, string) -> AutoToolsConfig.PREFER_SILK_TOUCH = string //Setter
    ).setTooltip(minecraft -> (preferSilkTouch) -> minecraft.font.split(new TranslatableComponent("ui.desc.preferSilkTouch." + AutoToolsConfig.PREFER_SILK_TOUCH), 200));

    public static final CycleOption<Boolean> ONLY_SWITCH_IF_NECESSARY = CycleOption.createOnOff(
            "ui.config.onlyNecessary",
            new TranslatableComponent("ui.desc.onlyNecessary"),
            gameOptions1 -> AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY,
            (gameOptions, option, switchNecessary) -> AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY = switchNecessary
    );

    public static final CycleOption<Boolean> PREFER_HOTBAR_TOOL = CycleOption.createOnOff(
            "ui.config.preferHotbarTool",
            new TranslatableComponent("ui.desc.preferHotbarTool"),
            gameOptions1 -> AutoToolsConfig.PREFER_HOTBAR_TOOL,
            (gameOptions, option, switchNecessary) -> AutoToolsConfig.PREFER_HOTBAR_TOOL = switchNecessary
    );

    public static final CycleOption<Boolean> PREFER_LOW_DURABILITY = CycleOption.createOnOff(
            "ui.config.preferLowDurability",
            new TranslatableComponent("ui.desc.preferLowDurability"),
            gameOptions1 -> AutoToolsConfig.PREFER_LOW_DURABILITY,
            (gameOptions, option, switchNecessary) -> AutoToolsConfig.PREFER_LOW_DURABILITY = switchNecessary
    );

    public static final CycleOption<Boolean> SWITCH_BACK = CycleOption.createOnOff(
            "ui.config.switchBack",
            new TranslatableComponent("ui.desc.switchBack"),
            gameOptions -> AutoToolsConfig.SWITCH_BACK,
            (gameOptions, option, switchBack) -> AutoToolsConfig.SWITCH_BACK = switchBack
    );

    public static final CycleOption<Boolean> CHANGE_FOR_ENTITIES = CycleOption.createOnOff(
            "ui.config.changeForEntities",
            new TranslatableComponent("ui.desc.changeForEntities"),
            gameOptions -> AutoToolsConfig.CHANGE_FOR_ENTITIES,
            (gameOptions, option, change) -> AutoToolsConfig.CHANGE_FOR_ENTITIES = change
    );


    public AutoToolsConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, new TextComponent("AutoTools Config"));
    }

    @Override
    protected void init() {
        AutoToolsConfig.load();
        AutoTools.loadCustomItems();

        int y = this.height / 6 - 12;
        this.addRenderableWidget(TOGGLE.createButton(this.options, this.width / 2 - 155, y, 150));
        this.addRenderableWidget(DISABLECREATIVE.createButton(this.options, this.width / 2 - 155, y + 24, 150));
        this.addRenderableWidget(KEEPSLOT.createButton(this.options, this.width / 2 - 155, y + 48, 150));
        this.addRenderableWidget(PREFER_HOTBAR_TOOL.createButton(this.options, this.width / 2 - 155, y + 72, 150));
        this.addRenderableWidget(PREFER_LOW_DURABILITY.createButton(this.options, this.width / 2 - 155, y + 96, 150));
        this.addRenderableWidget(ALWAYS_PREFER_FORTUNE.createButton(this.options, this.width / 2 - 155, y + 120, 150));

        this.addRenderableWidget(ONLY_SWITCH_IF_NECESSARY.createButton(this.options, this.width / 2 + 5, y, 150));
        this.addRenderableWidget(SWITCH_BACK.createButton(this.options, this.width / 2 + 5, y + 24, 150));
        this.addRenderableWidget(SHOWDPS.createButton(this.options, this.width / 2 + 5, y + 48, 150));
        this.addRenderableWidget(CHANGE_FOR_ENTITIES.createButton(this.options, this.width / 2 + 5, y + 72, 150));

        this.addRenderableWidget(PREFER_SILK_TOUCH.createButton(this.options, this.width / 2 - 155, y + 144, 310));

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, (p_96827_) -> this.onClose()));
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 15, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);

        //Drawing tooltips to the screen
        Optional<GuiEventListener> hoveredElement = getChildAt(mouseX, mouseY);
        if (hoveredElement.isPresent() && hoveredElement.get() instanceof TooltipAccessor) {
            this.renderTooltip(matrices, ((TooltipAccessor) hoveredElement.get()).getTooltip(), mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        AutoToolsConfig.save();
        this.minecraft.setScreen(this.lastScreen);
    }
}
