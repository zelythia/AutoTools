package net.zelythia.autotools.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.zelythia.autotools.AutoTools;
import net.zelythia.autotools.TooltipHelper;
import net.zelythia.autotools.config.AutoToolsConfig;
import net.zelythia.autotools.config.autoconfig.BlockList;
import net.zelythia.autotools.config.autoconfig.BlockListAnnotationProvider;
import net.zelythia.autotools.config.autoconfig.CustomToolsTransformer;
import org.lwjgl.glfw.GLFW;

@Mod(AutoTools.MOD_ID)
public class AutoToolsNeoForge {
    private boolean keyPressed = false;

    public static final Lazy<KeyMapping> KEY_CHANGE_TOOL = Lazy.of(() -> new KeyMapping("key.autotools.get_tool", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category"));
    public static final Lazy<KeyMapping> KEY_SILKTOUCH = Lazy.of(() -> new KeyMapping("key.autotools.silktouch", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.autotools.category"));

    public AutoToolsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Registering config
        AutoConfig.register(AutoToolsConfig.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));

        AutoConfig.getConfigHolder(AutoToolsConfig.class).registerSaveListener((configHolder, autoToolsConfig) -> {
            AutoTools.reloadConfig();
            return InteractionResult.SUCCESS;
        });

        GuiRegistry registry = AutoConfig.getGuiRegistry(AutoToolsConfig.class);
        registry.registerAnnotationProvider(new BlockListAnnotationProvider(), BlockList.class);
        registry.registerPredicateTransformer(new CustomToolsTransformer(), field -> field.getName().equals("customTools"));

        AutoTools.init();


        modEventBus.addListener(this::registerKeyBinding);

        // Registering mod for game events
        NeoForge.EVENT_BUS.register(this);

        //Registering the config
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (client, parent) -> AutoConfig.getConfigScreen(AutoToolsConfig.class, parent).get());
    }

    public void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(KEY_CHANGE_TOOL.get());
        event.register(KEY_SILKTOUCH.get());
    }

    @SubscribeEvent
    public void ClientTickStart(ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();

        if (AutoToolsConfig.get().toggle) {
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
        Minecraft client = Minecraft.getInstance();

        if (AutoToolsConfig.get().switchBack) {
            if (client.options.keyAttack.isDown()) {
                AutoTools.startedMining = true;
            } else {
                //Detecting switchBack for entities when using toggle, switching back otherwise if the key is released
                if (AutoToolsConfig.get().toggle || AutoTools.startedMining) {
                    AutoTools.switchBack();
                }
            }
        }

        if(KEY_SILKTOUCH.get().consumeClick()) {
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
