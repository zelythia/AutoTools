package net.zelythia;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.BooleanOption;
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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AutoToolsConfigScreen extends OptionsSubScreen {

    public static final BooleanOption TOGGLE = new BooleanOption(
            "ui.config.toggle",
            new TranslatableComponent("ui.desc.toggle"),
            gameOptions -> AutoToolsConfig.TOGGLE,
            (gameOptions, toggle) -> AutoToolsConfig.TOGGLE = toggle
    );

    public static final BooleanOption SHOWDPS = new BooleanOption(
            "ui.config.showDPS",
            new TranslatableComponent("ui.desc.showDPS"),
            gameOptions1 -> AutoToolsConfig.SHOWDPS,
            (gameOptions, showDPS) -> AutoToolsConfig.SHOWDPS = showDPS
    );

    public static final BooleanOption KEEPSLOT = new BooleanOption(
            "ui.config.keepSlot",
            new TranslatableComponent("ui.desc.keepSlot"),
            gameOptions1 -> AutoToolsConfig.KEEPSLOT,
            (gameOptions, keepSlot) -> AutoToolsConfig.KEEPSLOT = keepSlot
    );

    public static final BooleanOption DISABLECREATIVE = new BooleanOption(
            "ui.config.disableCreative",
            new TranslatableComponent("ui.desc.disableCreative"),
            gameOptions1 -> AutoToolsConfig.DISABLECREATIVE,
            (gameOptions, disableCreative) -> AutoToolsConfig.DISABLECREATIVE = disableCreative
    );

    public static final BooleanOption ALWAYS_PREFER_FORTUNE = new BooleanOption(
            "ui.config.preferFortune",
            new TranslatableComponent("ui.desc.preferFortune"),
            gameOptions -> AutoToolsConfig.ALWAYS_PREFER_FORTUNE,
            (gameOptions, preferFortune) -> AutoToolsConfig.ALWAYS_PREFER_FORTUNE = preferFortune
    );

    public static final CycleOption PREFER_SILK_TOUCH = new CycleOption(
            "ui.config.preferSilkTouch",
            (options, integer) -> {
                switch (AutoToolsConfig.PREFER_SILK_TOUCH) {
                    case "never":
                        AutoToolsConfig.PREFER_SILK_TOUCH = "except_ores";
                        break;
                    case "except_ores":
                        AutoToolsConfig.PREFER_SILK_TOUCH = "always_ores";
                        break;
                    case "always_ores":
                        AutoToolsConfig.PREFER_SILK_TOUCH = "always";
                        break;
                    case "always":
                        AutoToolsConfig.PREFER_SILK_TOUCH = "never";
                        break;

                }
            },
            (options, cycleOption) -> {
                cycleOption.setTooltip(Minecraft.getInstance().font.split(new TranslatableComponent("ui.desc.preferSilkTouch." + AutoToolsConfig.PREFER_SILK_TOUCH), 200));
                return new TranslatableComponent("options.generic_value", new TranslatableComponent("ui.config.preferSilkTouch"), new TranslatableComponent("ui.config.preferSilkTouch." + AutoToolsConfig.PREFER_SILK_TOUCH));
            }
    );

    public static final BooleanOption ONLY_SWITCH_IF_NECESSARY = new BooleanOption(
            "ui.config.onlyNecessary",
            new TranslatableComponent("ui.desc.onlyNecessary"),
            gameOptions1 -> AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY,
            (gameOptions, switchNecessary) -> AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY = switchNecessary
    );

    public static final BooleanOption PREFER_HOTBAR_TOOL = new BooleanOption(
            "ui.config.preferHotbarTool",
            new TranslatableComponent("ui.desc.preferHotbarTool"),
            gameOptions1 -> AutoToolsConfig.PREFER_HOTBAR_TOOL,
            (gameOptions, switchNecessary) -> AutoToolsConfig.PREFER_HOTBAR_TOOL = switchNecessary
    );

    public static final BooleanOption PREFER_LOW_DURABILITY = new BooleanOption(
            "ui.config.preferLowDurability",
            new TranslatableComponent("ui.desc.preferLowDurability"),
            gameOptions1 -> AutoToolsConfig.PREFER_LOW_DURABILITY,
            (gameOptions, switchNecessary) -> AutoToolsConfig.PREFER_LOW_DURABILITY = switchNecessary
    );

    public static final BooleanOption SWITCH_BACK = new BooleanOption(
            "ui.config.switchBack",
            new TranslatableComponent("ui.desc.switchBack"),
            gameOptions -> AutoToolsConfig.SWITCH_BACK,
            (gameOptions, switchBack) -> AutoToolsConfig.SWITCH_BACK = switchBack
    );

    public static final BooleanOption CHANGE_FOR_ENTITIES = new BooleanOption(
            "ui.config.changeForEntities",
            new TranslatableComponent("ui.desc.changeForEntities"),
            gameOptions -> AutoToolsConfig.CHANGE_FOR_ENTITIES,
            (gameOptions, switchBack) -> AutoToolsConfig.CHANGE_FOR_ENTITIES = switchBack
    );


    public AutoToolsConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, new TextComponent("AutoTools Config"));
    }


    @Override
    protected void init() {
        AutoToolsConfig.load();
        AutoTools.loadCustomItems();

        int y = this.height / 6 - 12;
        this.addButton(TOGGLE.createButton(this.options, this.width / 2 - 155, y, 150));
        this.addButton(DISABLECREATIVE.createButton(this.options, this.width / 2 - 155, y + 24, 150));
        this.addButton(KEEPSLOT.createButton(this.options, this.width / 2 - 155, y + 48, 150));
        this.addButton(PREFER_HOTBAR_TOOL.createButton(this.options, this.width / 2 - 155, y + 72, 150));
        this.addButton(PREFER_LOW_DURABILITY.createButton(this.options, this.width / 2 - 155, y + 96, 150));
        this.addButton(ALWAYS_PREFER_FORTUNE.createButton(this.options, this.width / 2 - 155, y + 120, 150));

        this.addButton(ONLY_SWITCH_IF_NECESSARY.createButton(this.options, this.width / 2 + 5, y, 150));
        this.addButton(SWITCH_BACK.createButton(this.options, this.width / 2 + 5, y + 24, 150));
        this.addButton(SHOWDPS.createButton(this.options, this.width / 2 + 5, y + 48, 150));
        this.addButton(CHANGE_FOR_ENTITIES.createButton(this.options, this.width / 2 + 5, y + 72, 150));

        this.addButton(PREFER_SILK_TOUCH.createButton(this.options, this.width / 2 - 155, y + 144, 310));

        this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, (p_96827_) -> this.onClose()));
    }

    @Override
    public void render(@NotNull PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 15, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);

        //Drawing tooltips to the screen
        Optional<GuiEventListener> hoveredElement = getChildAt(mouseX, mouseY);
        if (hoveredElement.isPresent() && hoveredElement.get() instanceof TooltipAccessor) {
            this.renderTooltip(matrices, ((TooltipAccessor) hoveredElement.get()).getTooltip().orElse(null), mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        AutoToolsConfig.save();
        this.minecraft.setScreen(this.lastScreen);
    }
}
