package net.zelythia.autotools.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.impl.builders.IntListBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.zelythia.autotools.AutoTools;
import net.zelythia.autotools.PlatformHelper;
import net.zelythia.autotools.TooltipHelper;
import net.zelythia.autotools.config.AutoToolsConfig;
import net.zelythia.autotools.config.autoconfig.BlockList;
import net.zelythia.autotools.config.autoconfig.BlockListAnnotationProvider;
import net.zelythia.autotools.config.autoconfig.CustomToolsTransformer;
import net.zelythia.autotools.config.autoconfig.HotbarSlots;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.Optional;

public class AutoToolsFabric implements ClientModInitializer {
    private boolean keyPressed = false;

    private static final KeyMapping KEY_AUTOTOOLS = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.autotools.get_tool", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category"));
    private static final KeyMapping KEY_SILKTOUCH = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.autotools.silktouch", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.autotools.category"));


    @Override
    public void onInitializeClient() {
        AutoConfig.register(net.zelythia.autotools.config.AutoToolsConfig.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));

        AutoConfig.getConfigHolder(net.zelythia.autotools.config.AutoToolsConfig.class).registerSaveListener((configHolder, autoToolsConfig) -> {
            AutoTools.reloadConfig();
            return InteractionResult.SUCCESS;
        });

        GuiRegistry registry = AutoConfig.getGuiRegistry(net.zelythia.autotools.config.AutoToolsConfig.class);
        registry.registerAnnotationProvider(new BlockListAnnotationProvider(), BlockList.class);
        registry.registerPredicateTransformer(new CustomToolsTransformer(), field -> field.getName().equals("customTools"));
        registry.registerAnnotationProvider((i18n, field, config, defaults, registry1) -> {
            return Collections.singletonList(new IntListBuilder(Component.translatable("text.cloth-config.reset_value"), Component.translatable(i18n), Utils.getUnsafely(field, config))
                    .setDefaultValue(() -> Utils.getUnsafely(field, defaults))
                    .setSaveConsumer((newValue) -> Utils.setUnsafely(field, config, newValue))
                    .setErrorSupplier(integers -> {
                        if(!field.getAnnotation(HotbarSlots.class).allowEmpty() && integers.isEmpty()) return Optional.of(Component.translatable("text.autoconfig.autotools.error.isEmpty"));
                        return Optional.empty();
                    })
                    .setMin(1)
                    .setMax(9)
                    .build());
        }, HotbarSlots.class);

        AutoTools.init();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (AutoToolsConfig.get().toggle) {
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
            if (AutoToolsConfig.get().switchBack) {
                if (Minecraft.getInstance().options.keyAttack.isDown() || PlatformHelper.controllableAttackDown()) {
                    AutoTools.startedMining = true;
                } else {
                    //Detecting switchBack for entities when using toggle, switching back otherwise if the key is released
                    if (AutoToolsConfig.get().toggle || AutoTools.startedMining) {
                        AutoTools.switchBack();
                    }
                }
            }

            if(KEY_SILKTOUCH.consumeClick()) {
                AutoToolsConfig.PreferSilkTouch[] values = AutoToolsConfig.PreferSilkTouch.values();
                AutoToolsConfig.get().preferSilkTouch = values[(AutoToolsConfig.get().preferSilkTouch .ordinal() + 1) % values.length];

                client.player.displayClientMessage(Component.translatable("chat.cycle_silktouch").append(Component.translatable("text.autoconfig.autotools.option.general.preferSilkTouch." + AutoToolsConfig.get().preferSilkTouch )), false);

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


}
