package net.zelythia.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.zelythia.AutoTools;
import net.zelythia.config.AutoToolsConfig;
import net.zelythia.TooltipHelper;
import net.zelythia.config.autoconfig.BlockList;
import net.zelythia.config.autoconfig.BlockListAnnotationProvider;
import net.zelythia.config.autoconfig.CustomToolsTransformer;
import org.lwjgl.glfw.GLFW;

public class AutoToolsFabric implements ClientModInitializer {
    private boolean keyPressed = false;

    private static final KeyMapping KEY_AUTOTOOLS = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.autotools.get_tool", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category"));
    private static final KeyMapping KEY_SILKTOUCH = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.autotools.silktouch", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.autotools.category"));

    @Override
    public void onInitializeClient() {
        AutoConfig.register(net.zelythia.config.AutoToolsConfig.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));

        AutoConfig.getConfigHolder(net.zelythia.config.AutoToolsConfig.class).registerSaveListener((configHolder, autoToolsConfig) -> {
            AutoTools.reloadConfig();
            return InteractionResult.SUCCESS;
        });

        GuiRegistry registry = AutoConfig.getGuiRegistry(net.zelythia.config.AutoToolsConfig.class);
        registry.registerAnnotationProvider(new BlockListAnnotationProvider(), BlockList.class);
        registry.registerPredicateTransformer(new CustomToolsTransformer(), field -> field.getName().equals("customTools"));

        AutoTools.init();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (AutoToolsConfig.get().toggle) {
                //Changing the toggle setting:
                //When toggling the keybinding should only be reacted to once per press
                if (KEY_AUTOTOOLS.consumeClick()) {
                    if (!keyPressed) {
                        AutoTools.toggle = !AutoTools.toggle;
                        client.player.sendSystemMessage(AutoTools.toggle ? Component.translatable("chat.enabled_autotools") : Component.translatable("chat.disabled_autotools"));
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
                if (Minecraft.getInstance().options.keyAttack.isDown()) {
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
                AutoToolsConfig.get().preferSilkTouch = values[(AutoToolsConfig.get().preferSilkTouch.ordinal() + 1) % values.length];

                client.player.displayClientMessage(Component.translatable("chat.cycle_silktouch").append(Component.translatable("text.autoconfig.autotools.option.general.preferSilkTouch." + AutoToolsConfig.get().preferSilkTouch)), false);

                AutoToolsConfig.save();
            }
        });


        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            TooltipHelper.applyTooltip(stack, lines);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            AutoTools.swaps.clear();
        });
    }


}
