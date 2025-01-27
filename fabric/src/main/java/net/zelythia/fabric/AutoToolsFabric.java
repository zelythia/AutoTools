package net.zelythia.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
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
import net.zelythia.AutoToolsConfig;
import net.zelythia.TooltipHelper;
import org.lwjgl.glfw.GLFW;

public class AutoToolsFabric implements ClientModInitializer {
    private boolean keyPressed = false;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(AutoToolsConfigImpl.class, GsonConfigSerializer::new);  //TODO switch to Jankson after Lists are fixed

        AutoConfig.getConfigHolder(AutoToolsConfigImpl.class).registerSaveListener((configHolder, autoToolsConfig) -> {
            AutoToolsConfig.load();
            return InteractionResult.SUCCESS;
        });

        AutoTools.init();

        KeyMapping key_changeTool = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.autotools.get_tool", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category"));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (AutoToolsConfig.TOGGLE) {
                //Changing the toggle setting:
                //When toggling the keybinding should only be reacted to once per press
                if (key_changeTool.consumeClick()) {
                    if (!keyPressed) {
                        AutoTools.toggle = !AutoTools.toggle;
                        client.player.sendSystemMessage(AutoTools.toggle ? Component.translatable("chat.enabled_autotools") : Component.translatable("chat.disabled_autotools"));
                        keyPressed = true;
                    }
                    //resetting the keyPressed-count
                    while (key_changeTool.consumeClick()) {
                        keyPressed = true;
                    }
                } else {
                    keyPressed = false;
                }
            } else {
                if (key_changeTool.consumeClick()) {
                    AutoTools.startedMining = false;
                    AutoTools.getCorrectTool(client.hitResult, client);
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (AutoToolsConfig.SWITCH_BACK) {
                if (Minecraft.getInstance().options.keyAttack.isDown()) {
                    AutoTools.startedMining = true;
                } else {
                    //Detecting switchBack for entities when using toggle, switching back otherwise if the key is released
                    if ((AutoToolsConfig.TOGGLE && AutoTools.lastBlock == null) || (!AutoToolsConfig.TOGGLE && AutoTools.startedMining)) {
                        AutoTools.switchBack();
                    }
                }
            }
        });


        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            TooltipHelper.applyTooltip(stack, lines);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            AutoTools.swaps.clear();
            AutoTools.lastBlock = null;
        });
    }


}
