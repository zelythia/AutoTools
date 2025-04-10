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
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;
import net.zelythia.TooltipHelper;
import org.lwjgl.glfw.GLFW;

@Mod(AutoTools.MOD_ID)
public class AutoToolsNeoForge {
    private boolean keyPressed = false;

    public static final Lazy<KeyMapping> KEY_CHANGE_TOOL = Lazy.of(() -> new KeyMapping("key.autotools.get_tool", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category"));
    public static final Lazy<KeyMapping> KEY_SILKTOUCH = Lazy.of(() -> new KeyMapping("key.autotools.silktouch", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.autotools.category"));

    public AutoToolsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        //Registering the clientSetup method
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeyBinding);

        // Registering mod for game events
        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::ConfigLoaded);

        //Registering the config
        modContainer.registerConfig(ModConfig.Type.CLIENT, AutoToolsConfigImpl.SPEC, "autotools.toml");
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    //Called once when the client is set up
    public void clientSetup(final FMLCommonSetupEvent event) {
        AutoTools.init();
    }

    public void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(KEY_CHANGE_TOOL.get());
        event.register(KEY_SILKTOUCH.get());
    }

    public void ConfigLoaded(ModConfigEvent.Reloading event) {
        AutoToolsConfig.load();
    }

    @SubscribeEvent
    public void ClientTickStart(ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();

        if (AutoToolsConfig.TOGGLE) {
            //Handling key presses
            if (KEY_CHANGE_TOOL.get().consumeClick()) {
                if (!keyPressed) {
                    AutoTools.toggle = !AutoTools.toggle;
                    client.player.displayClientMessage(AutoTools.toggle ? Component.translatable("chat.enabled_autotools") : Component.translatable("chat.disabled_autotools"), false);
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
        Minecraft client = Minecraft.getInstance();

        if (AutoToolsConfig.SWITCH_BACK) {
            if (client.options.keyAttack.isDown()) {
                AutoTools.startedMining = true;
            } else {
                //Detecting switchBack for entities when using toggle, switching back otherwise if the key is released
                if (AutoToolsConfig.TOGGLE || AutoTools.startedMining) {
                    AutoTools.switchBack();
                }
            }
        }

        if(KEY_SILKTOUCH.get().consumeClick()) {
            AutoToolsConfig.PreferSilkTouch[] values = AutoToolsConfig.PreferSilkTouch.values();
            AutoToolsConfig.PREFER_SILK_TOUCH = values[(AutoToolsConfig.PREFER_SILK_TOUCH.ordinal() + 1) % values.length];

            client.player.displayClientMessage(Component.translatable("chat.cycle_silktouch").append(Component.translatable("autotools.configuration.preferSilkTouch." + AutoToolsConfig.PREFER_SILK_TOUCH)), false);

            AutoToolsConfig.save();
        }
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent event) {
        TooltipHelper.applyTooltip(event.getItemStack(), event.getToolTip());
    }

    @SubscribeEvent
    public void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        AutoTools.swaps.clear();
    }
}
