package net.zelythia.forge;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;
import net.zelythia.TooltipHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@Mod(AutoTools.MOD_ID)
public class AutoToolsForge {
    private boolean keyPressed = false;

    public static final KeyMapping KEY_CHANGE_TOOL = new KeyMapping("key.autotools.get_tool", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category");
    public static final KeyMapping KEY_SILKTOUCH = new KeyMapping("key.autotools.silktouch", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.autotools.category");

    public AutoToolsForge() {
        //Registering the clientSetup method
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        // Registering mod for game events
        MinecraftForge.EVENT_BUS.register(this);

        //Registering the config
        ModLoadingContext.get().registerExtensionPoint(
                ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory(((minecraft, screen) -> AutoConfig.getConfigScreen(AutoToolsConfigImpl.class, screen).get()))
        );
    }

    //Called once when the client is set up
    public void clientSetup(final FMLCommonSetupEvent event) {
        AutoConfig.register(AutoToolsConfigImpl.class, GsonConfigSerializer::new);  //TODO switch to Jankson after Lists are fixed
        AutoConfig.getConfigHolder(AutoToolsConfigImpl.class).registerSaveListener((configHolder, autoToolsConfig) -> {
            AutoTools.reloadConfig();
            return InteractionResult.SUCCESS;
        });

        ClientRegistry.registerKeyBinding(KEY_CHANGE_TOOL);
        ClientRegistry.registerKeyBinding(KEY_SILKTOUCH);
        AutoTools.init();
    }


    @SubscribeEvent
    public void ClientTickEvent(@NotNull TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft client = Minecraft.getInstance();

            if (AutoToolsConfig.TOGGLE) {
                //Handling key presses
                if (KEY_CHANGE_TOOL.consumeClick()) {
                    if (!keyPressed) {
                        AutoTools.toggle = !AutoTools.toggle;
                        client.player.sendMessage(new TextComponent(AutoTools.toggle ? new TranslatableComponent("chat.enabled_autotools").getString() : new TranslatableComponent("chat.disabled_autotools").getString()), client.player.getUUID());
                        keyPressed = true;
                    }
                } else {
                    keyPressed = false;
                }
            } else {
                if (KEY_CHANGE_TOOL.consumeClick()) {
                    AutoTools.startedMining = false;
                    AutoTools.getCorrectTool(client.hitResult, client);
                }
            }
        } else if (event.phase == TickEvent.Phase.END) {
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

            if(KEY_SILKTOUCH.consumeClick()) {
                AutoToolsConfig.PreferSilkTouch[] values = AutoToolsConfig.PreferSilkTouch.values();
                AutoToolsConfig.PREFER_SILK_TOUCH = values[(AutoToolsConfig.PREFER_SILK_TOUCH.ordinal() + 1) % values.length];

                client.player.displayClientMessage(new TranslatableComponent("chat.cycle_silktouch").append(new TranslatableComponent("autotools.configuration.preferSilkTouch." + AutoToolsConfig.PREFER_SILK_TOUCH)), false);

                AutoToolsConfig.save();
            }
        }
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent event) {
        TooltipHelper.applyTooltip(event.getItemStack(), event.getToolTip());
    }

    @SubscribeEvent
    public void onJoin(ClientPlayerNetworkEvent.LoggedInEvent event) {
        AutoTools.swaps.clear();
    }
}
