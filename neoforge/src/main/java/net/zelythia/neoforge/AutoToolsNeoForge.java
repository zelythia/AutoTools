package net.zelythia.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;
import net.zelythia.AutoToolsConfigScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@Mod(AutoTools.MOD_ID)
public class AutoToolsNeoForge {

    private boolean switchItem = true;
    private boolean keyPressed = false;
    public static boolean blockBroken = false;

    public static final Lazy<KeyMapping> KEY_CHANGE_TOOL = Lazy.of(() -> new KeyMapping("key.autotools.get_tool", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category"));

    public AutoToolsNeoForge(IEventBus modEventBus) {
        //Registering the clientSetup method
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeyBinding);

        // Registering mod for game events
        NeoForge.EVENT_BUS.register(this);

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
        event.register(KEY_CHANGE_TOOL.get());
    }


    @SubscribeEvent
    public void BlockBreakEvent(BlockEvent.BreakEvent event) {
        if (AutoToolsConfig.SWITCH_BACK && !AutoToolsConfig.TOGGLE) {
            blockBroken = true;
        }
    }

    @SubscribeEvent
    public void ClientTickEvent(@NotNull TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft client = Minecraft.getInstance();

            if (AutoToolsConfig.TOGGLE) {
                //Handling key presses
                if (KEY_CHANGE_TOOL.get().consumeClick()) {
                    if (!keyPressed) {
                        switchItem = !switchItem;
                        client.player.sendSystemMessage(switchItem ? Component.translatable("chat.enabled_autotools") : Component.translatable("chat.disabled_autotools"));
                        keyPressed = true;
                    }
                } else {
                    keyPressed = false;
                }
            } else {
                if (KEY_CHANGE_TOOL.get().consumeClick()) {
                    AutoTools.getCorrectTool(client.hitResult, client);
                }
            }
        } else if (event.phase == TickEvent.Phase.END) {
            if (!Minecraft.getInstance().options.keyAttack.isDown()) {
                if (AutoToolsConfig.SWITCH_BACK && (AutoToolsConfig.TOGGLE || blockBroken)) {
                    AutoTools.switchBack();
                    blockBroken = false;
                }
            }
        }
    }

    @SubscribeEvent
    public void ClickInputEvent(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack()) {
            if (AutoToolsConfig.TOGGLE && switchItem) {
                Minecraft instance = Minecraft.getInstance();

                if (instance.player.isCreative()) {
                    if (!AutoToolsConfig.DISABLECREATIVE) {
                        AutoTools.getCorrectTool(instance.hitResult, instance);
                    }
                } else {
                    AutoTools.getCorrectTool(instance.hitResult, instance);
                }
            }
        }
    }

    @SubscribeEvent
    public void onToolTip(ItemTooltipEvent event) {
        if (AutoToolsConfig.SHOWDPS) {
            ItemStack stack = event.getItemStack();
            Item item = stack.getItem();

            if (item != Items.AIR) {
                double attackDamage = 1.0;

                if (stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).toArray().length > 0) {
                    //Every item with an attackDamage has an ATTACK_DAMAGE modifier
                    if (stack.getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE)) {
                        //Calculating DPS
                        if (stack.getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_SPEED)) {
                            //Attack damage
                            attackDamage = (1 + ((AttributeModifier) stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).toArray()[0]).getAmount())
                                    * //Attack speed
                                    (4F + ((AttributeModifier) stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED).toArray()[0]).getAmount());
                        } else {
                            attackDamage = 1 + ((AttributeModifier) stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).toArray()[0]).getAmount();
                        }
                    }
                }

                double optionalAttackDamage = attackDamage;
                //Check for enchantments
                if (stack.isEnchanted()) {
                    attackDamage += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);

                    if (optionalAttackDamage + EnchantmentHelper.getDamageBonus(stack, MobType.UNDEAD) > attackDamage) {
                        optionalAttackDamage += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEAD);
                    } else if (optionalAttackDamage + EnchantmentHelper.getDamageBonus(stack, MobType.ARTHROPOD) > attackDamage) {
                        optionalAttackDamage += EnchantmentHelper.getDamageBonus(stack, MobType.ARTHROPOD);
                    } else if (optionalAttackDamage + EnchantmentHelper.getDamageBonus(stack, MobType.WATER) > attackDamage) {
                        optionalAttackDamage += EnchantmentHelper.getDamageBonus(stack, MobType.WATER);
                    }
                }

                if (attackDamage > 1) {

                    //Needs to be an array because variables used in for-each loops have to be final
                    final int[] index = {0};

                    event.getToolTip().forEach((toolTip) -> {
                        if (toolTip.getString().equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                            index[0] = event.getToolTip().indexOf(toolTip);
                        }
                    });


                    String damage = (optionalAttackDamage > attackDamage) ?
                            (double) Math.round(attackDamage * 10d) / 10d + " (" + (double) Math.round(optionalAttackDamage * 10d) / 10d + ")" :
                            String.valueOf((double) Math.round(attackDamage * 10d) / 10d);

                    if (index[0] > 0 && index[0] < event.getToolTip().size()) {
                        event.getToolTip().add(index[0] - 1, Component.literal(" " + damage + " Dps").withStyle(ChatFormatting.DARK_GREEN));
                    } else {
                        event.getToolTip().add(Component.literal(" " + damage + " Dps").withStyle(ChatFormatting.DARK_GREEN));
                    }
                }
            }
        }
    }
}
