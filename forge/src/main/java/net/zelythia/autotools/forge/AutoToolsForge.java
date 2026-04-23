package net.zelythia.autotools.forge;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.impl.builders.IntListBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
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
import net.zelythia.autotools.AutoTools;
import net.zelythia.autotools.PlatformHelper;
import net.zelythia.autotools.TooltipHelper;
import net.zelythia.autotools.config.AutoToolsConfig;
import net.zelythia.autotools.config.autoconfig.BlockList;
import net.zelythia.autotools.config.autoconfig.BlockListAnnotationProvider;
import net.zelythia.autotools.config.autoconfig.CustomToolsTransformer;
import net.zelythia.autotools.config.autoconfig.HotbarSlots;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.Optional;

@Mod(AutoTools.MOD_ID)
public class AutoToolsForge {
    private boolean keyPressed = false;

    public static final KeyMapping KEY_CHANGE_TOOL = new KeyMapping("key.autotools.get_tool", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category");
    public static final KeyMapping KEY_SILKTOUCH = new KeyMapping("key.autotools.silktouch", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.autotools.category");

    public AutoToolsForge() {
        // Registering config
        AutoConfig.register(AutoToolsConfig.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));

        AutoConfig.getConfigHolder(AutoToolsConfig.class).registerSaveListener((configHolder, autoToolsConfig) -> {
            AutoTools.reloadConfig();
            return InteractionResult.SUCCESS;
        });

        GuiRegistry registry = AutoConfig.getGuiRegistry(AutoToolsConfig.class);
        registry.registerAnnotationProvider(new BlockListAnnotationProvider(), BlockList.class);
        registry.registerPredicateTransformer(new CustomToolsTransformer(), field -> field.getName().equals("customTools"));
        registry.registerAnnotationProvider((i18n, field, config, defaults, registry1) -> {
            return Collections.singletonList(new IntListBuilder(new TranslatableComponent("text.cloth-config.reset_value"), new TranslatableComponent(i18n), Utils.getUnsafely(field, config))
                    .setDefaultValue(() -> Utils.getUnsafely(field, defaults))
                    .setSaveConsumer((newValue) -> Utils.setUnsafely(field, config, newValue))
                    .setErrorSupplier(integers -> {
                        if(!field.getAnnotation(HotbarSlots.class).allowEmpty() && integers.isEmpty()) return Optional.of(new TranslatableComponent("text.autoconfig.autotools.error.isEmpty"));
                        return Optional.empty();
                    })
                    .setMin(1)
                    .setMax(9)
                    .build());
        }, HotbarSlots.class);

        AutoTools.init();


        // Registering the clientSetup method
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        // Registering mod for game events
        MinecraftForge.EVENT_BUS.register(this);

        //Registering the config
        ModLoadingContext.get().registerExtensionPoint(
                ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory(((minecraft, screen) -> AutoConfig.getConfigScreen(AutoToolsConfig.class, screen).get()))
        );
    }

    //Called once when the client is set up
    public void clientSetup(final FMLCommonSetupEvent event) {
        ClientRegistry.registerKeyBinding(KEY_CHANGE_TOOL);
        ClientRegistry.registerKeyBinding(KEY_SILKTOUCH);
    }


    @SubscribeEvent
    public void ClientTickEvent(@NotNull TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft client = Minecraft.getInstance();

            if (AutoToolsConfig.get().toggle) {
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

                client.player.displayClientMessage(new TranslatableComponent("chat.cycle_silktouch").append(new TranslatableComponent("text.autoconfig.autotools.option.general.preferSilkTouch." + AutoToolsConfig.get().preferSilkTouch)), false);

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
