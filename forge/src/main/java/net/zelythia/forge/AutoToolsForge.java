package net.zelythia.forge;

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
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;
import net.zelythia.AutoToolsConfigScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@Mod(AutoTools.MOD_ID)
public class AutoToolsForge {

    private boolean switchItem = true;
    private boolean keyPressed = false;
    public static boolean blockBroken = false;

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
                if (key_changeTool.consumeClick()) {
                    if (!keyPressed) {
                        switchItem = !switchItem;
                        client.player.sendSystemMessage(switchItem ? Component.translatable("chat.enabled_autotools") : Component.translatable("chat.disabled_autotools"));
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
        if (AutoToolsConfig.TOGGLE && switchItem) {
            if (event.isAttack()) {
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
