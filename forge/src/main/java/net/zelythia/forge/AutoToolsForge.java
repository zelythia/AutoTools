package net.zelythia.forge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;
import net.zelythia.AutoToolsConfigScreen;
import net.zelythia.TooltipHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@Mod(AutoTools.MOD_ID)
public class AutoToolsForge {
    private boolean keyPressed = false;

    public static final KeyMapping key_changeTool = new KeyMapping("key.autotools.get_tool", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category");

    public AutoToolsForge() {
        //Registering the clientSetup method
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeyBinding);

        // Registering mod for game events
        MinecraftForge.EVENT_BUS.register(this);

        //Registering the config
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AutoToolsConfigImpl.SPEC, "autotools.toml");
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(((minecraft, screen) -> new AutoToolsConfigScreen(screen)))
        );
    }

    //Called once when the client is set up
    public void clientSetup(final FMLCommonSetupEvent event) {
        AutoTools.init();
    }

    public void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(key_changeTool);
    }


    @SubscribeEvent
    public void ClientTickStart(@NotNull TickEvent.ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();

        if (AutoToolsConfig.TOGGLE) {
            //Handling key presses
            if (key_changeTool.consumeClick()) {
                if (!keyPressed) {
                    AutoTools.toggle = !AutoTools.toggle;
                    client.gui.setOverlayMessage(AutoTools.toggle ? Component.translatable("chat.enabled_autotools") : Component.translatable("chat.disabled_autotools"), true);
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
    }

    @SubscribeEvent
    public void ClientTickEnd(@NotNull TickEvent.ClientTickEvent.Post event){
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
