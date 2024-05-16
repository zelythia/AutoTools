package net.zelythia.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;
import net.zelythia.AutoToolsConfigScreen;
import net.zelythia.TooltipHelper;
import org.lwjgl.glfw.GLFW;

@Mod(AutoTools.MOD_ID)
public class AutoToolsNeoForge {
    private boolean keyPressed = false;

    public static final Lazy<KeyMapping> KEY_CHANGE_TOOL = Lazy.of(() -> new KeyMapping("key.autotools.get_tool", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category"));

    public AutoToolsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        //Registering the clientSetup method
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeyBinding);

        // Registering mod for game events
        NeoForge.EVENT_BUS.register(this);

        //Registering the config
        modContainer.registerConfig(ModConfig.Type.CLIENT, AutoToolsConfigImpl.SPEC, "autotools.toml");
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (client, screen) -> new AutoToolsConfigScreen(screen));
    }

    //Called once when the client is set up
    public void clientSetup(final FMLCommonSetupEvent event) {
        AutoTools.init();
    }

    public void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(KEY_CHANGE_TOOL.get());
    }

    @SubscribeEvent
    public void ClientTickStart(ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();

        if (AutoToolsConfig.TOGGLE) {
            //Handling key presses
            if (KEY_CHANGE_TOOL.get().consumeClick()) {
                if (!keyPressed) {
                    AutoTools.toggle = !AutoTools.toggle;
                    client.player.sendSystemMessage(AutoTools.toggle ? Component.translatable("chat.enabled_autotools") : Component.translatable("chat.disabled_autotools"));
                    keyPressed = true;
                }
            } else {
                keyPressed = false;
            }
        } else {
            if (KEY_CHANGE_TOOL.get().consumeClick()) {
                AutoTools.startedMining = false;
                AutoTools.getCorrectTool(client.hitResult, client);
            }
        }
    }

    @SubscribeEvent
    public void ClientTickEnd(ClientTickEvent.Post event) {
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
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent event) {
        TooltipHelper.applyTooltip(event.getItemStack(), event.getToolTip());
    }

    @SubscribeEvent
    public void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        AutoTools.swaps.clear();
        AutoTools.lastBlock = null;
    }
}
