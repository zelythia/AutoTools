package net.zelythia.autotools.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.zelythia.autotools.AutoTools;
import net.zelythia.autotools.PlatformHelper;
import net.zelythia.autotools.TooltipHelper;
import net.zelythia.autotools.config.AutoToolsConfig;
import net.zelythia.autotools.config.autoconfig.BlockList;
import net.zelythia.autotools.config.autoconfig.BlockListAnnotationProvider;
import net.zelythia.autotools.config.autoconfig.CustomToolsTransformer;
import org.lwjgl.glfw.GLFW;

@Mod(AutoTools.MOD_ID)
public class AutoToolsNeoForge {
    private boolean keyPressed = false;

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(AutoTools.MOD_ID, "keys"));
    private static final KeyMapping KEY_AUTOTOOLS = new KeyMapping("key.autotools.get_tool", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
    private static final KeyMapping KEY_SILKTOUCH = new KeyMapping("key.autotools.silktouch", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);

    public AutoToolsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        // Registering config
        AutoConfig.register(AutoToolsConfig.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));

        AutoConfig.getConfigHolder(AutoToolsConfig.class).registerSaveListener((configHolder, autoToolsConfig) -> {
            AutoTools.reloadConfig();
            return InteractionResult.SUCCESS;
        });

        GuiRegistry registry = AutoConfigClient.getGuiRegistry(AutoToolsConfig.class);
        registry.registerAnnotationProvider(new BlockListAnnotationProvider(), BlockList.class);
        registry.registerPredicateTransformer(new CustomToolsTransformer(), field -> field.getName().equals("customTools"));

        AutoTools.init();


        modEventBus.addListener(this::registerKeyBinding);

        // Registering mod for game events
        NeoForge.EVENT_BUS.register(this);

        //Registering the config
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (modContainer1, parent) -> {
            return AutoConfigClient.getConfigScreen(AutoToolsConfig.class, parent).get();
        });
    }


    public void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(KEY_AUTOTOOLS);
        event.register(KEY_SILKTOUCH);
    }

    @SubscribeEvent
    public void ClientTickStart(ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();

        if (AutoToolsConfig.get().toggle) {
            //Handling key presses
            if (KEY_AUTOTOOLS.consumeClick()) {
                if (!keyPressed) {
                    AutoTools.toggle = !AutoTools.toggle;
                    client.player.sendSystemMessage(AutoTools.toggle ? Component.translatable("chat.enabled_autotools") : Component.translatable("chat.disabled_autotools"));
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
    }

    @SubscribeEvent
    public void ClientTickEnd(ClientTickEvent.Post event) {
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

            client.player.sendSystemMessage(Component.translatable("chat.cycle_silktouch").append(Component.translatable("text.autoconfig.autotools.option.general.preferSilkTouch." + AutoToolsConfig.get().preferSilkTouch)));

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
