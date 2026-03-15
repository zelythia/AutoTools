package net.zelythia.autotools.forge;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zelythia.autotools.AutoTools;
import net.zelythia.autotools.PlatformHelper;
import net.zelythia.autotools.TooltipHelper;
import net.zelythia.autotools.config.AutoToolsConfig;
import net.zelythia.autotools.config.autoconfig.BlockList;
import net.zelythia.autotools.config.autoconfig.BlockListAnnotationProvider;
import net.zelythia.autotools.config.autoconfig.CustomToolsTransformer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@Mod(AutoTools.MOD_ID)
public class AutoToolsForge {
    private boolean keyPressed = false;

    public static final KeyMapping KEY_CHANGE_TOOL = new KeyMapping("key.autotools.get_tool", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category");
    public static final KeyMapping KEY_SILKTOUCH = new KeyMapping("key.autotools.silktouch", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.autotools.category");

    public AutoToolsForge(FMLJavaModLoadingContext  context) {
        //Registering the config
        AutoConfig.register(AutoToolsConfig.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));

        AutoConfig.getConfigHolder(AutoToolsConfig.class).registerSaveListener((configHolder, autoToolsConfig) -> {
            AutoTools.reloadConfig();
            return InteractionResult.SUCCESS;
        });

        GuiRegistry registry = AutoConfig.getGuiRegistry(AutoToolsConfig.class);
        registry.registerAnnotationProvider(new BlockListAnnotationProvider(), BlockList.class);
        registry.registerPredicateTransformer(new CustomToolsTransformer(), field -> field.getName().equals("customTools"));

        AutoTools.init();


        //Registering modEventBut events
        context.getModEventBus().addListener(this::registerKeyBinding);

        // Registering mod for game events
        MinecraftForge.EVENT_BUS.register(this);

        //Registering the config
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(((minecraft, screen) -> AutoConfig.getConfigScreen(AutoToolsConfig.class, screen).get()))
        );
    }

    public void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(KEY_CHANGE_TOOL);
        event.register(KEY_SILKTOUCH);
    }


    @SubscribeEvent
    public void ClientTickStart(@NotNull TickEvent.ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();

        if (AutoToolsConfig.get().toggle) {
            //Handling key presses
            if (KEY_CHANGE_TOOL.consumeClick()) {
                if (!keyPressed) {
                    AutoTools.toggle = !AutoTools.toggle;
                    client.player.sendSystemMessage(AutoTools.toggle ? Component.translatable("chat.enabled_autotools") : Component.translatable("chat.disabled_autotools"));
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
    }

    @SubscribeEvent
    public void ClientTickEnd(@NotNull TickEvent.ClientTickEvent.Post event){
        Minecraft client = Minecraft.getInstance();

        if (AutoToolsConfig.get().switchBack) {
            if (client.options.keyAttack.isDown() || PlatformHelper.controllableAttackDown()) {
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
