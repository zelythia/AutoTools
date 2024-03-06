package net.zelythia.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;
import net.zelythia.fabric.events.ClientBlockBreakEvent;
import org.lwjgl.glfw.GLFW;

public class AutoToolsFabric implements ClientModInitializer {
    public static boolean switchItem = true;
    private boolean keyPressed = false;
    public static boolean blockBroken = false;

    @Override
    public void onInitializeClient() {
        AutoTools.SILK_TOUCH = TagRegistry.block(new ResourceLocation(AutoTools.MOD_ID, "silk_touch"));
        AutoTools.SILK_TOUCH_SETTING_ALWAYS = TagRegistry.block(new ResourceLocation(AutoTools.MOD_ID, "silk_touch_setting_always"));
        AutoTools.SILK_TOUCH_SETTING_ALWAYS_ORES = TagRegistry.block(new ResourceLocation(AutoTools.MOD_ID, "silk_touch_setting_always_ores"));
        AutoTools.SILK_TOUCH_SETTING_ALWAYS_EXC_ORES = TagRegistry.block(new ResourceLocation(AutoTools.MOD_ID, "silk_touch_setting_always_exc_ores"));
        AutoTools.FORTUNE = TagRegistry.block(new ResourceLocation(AutoTools.MOD_ID, "fortune"));
        AutoTools.FORTUNE_SETTING = TagRegistry.block(new ResourceLocation(AutoTools.MOD_ID, "fortune_setting"));
        AutoTools.DO_NOT_SWAP_UNLESS_ENCH = TagRegistry.block(new ResourceLocation(AutoTools.MOD_ID, "do_not_swap_unless_ench"));

        AutoTools.init();

        KeyMapping key_changeTool = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.autotools.get_tool", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category"));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (AutoToolsConfig.TOGGLE) {
                //Changing the toggle setting:
                //When toggling the keybinding should only be reacted to once per press
                if (key_changeTool.consumeClick()) {
                    if (!keyPressed) {
                        switchItem = !switchItem;
                        client.player.sendMessage(new TextComponent(switchItem ? new TranslatableComponent("chat.enabled_autotools").getString() : new TranslatableComponent("chat.disabled_autotools").getString()), client.player.getUUID());
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
                    AutoTools.getCorrectTool(client.hitResult, client);
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!Minecraft.getInstance().options.keyAttack.isDown()) {
                if (AutoToolsConfig.SWITCH_BACK && (AutoToolsConfig.TOGGLE || blockBroken)) {
                    AutoTools.switchBack();
                    blockBroken = false;
                }
            }
        });

        ClientBlockBreakEvent.EVENT.register((levelAccessor, blockPos, blockState) -> {
            if (AutoToolsConfig.SWITCH_BACK && !AutoToolsConfig.TOGGLE) {
                blockBroken = true;
            }
        });

    }
}
