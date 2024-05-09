package net.zelythia.forge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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
    private boolean keyPressed = false;

    KeyMapping key_changeTool = new KeyMapping("key.autotools.get_tool", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "key.autotools.category");

    public AutoToolsForge() {
        AutoTools.SHEARS = BlockTags.bind(AutoTools.MOD_ID+":shears");
        AutoTools.SILK_TOUCH = BlockTags.bind(AutoTools.MOD_ID+":silk_touch");
        AutoTools.SILK_TOUCH_SETTING_ALWAYS = BlockTags.bind(AutoTools.MOD_ID+":silk_touch_setting_always");
        AutoTools.SILK_TOUCH_SETTING_ALWAYS_ORES = BlockTags.bind(AutoTools.MOD_ID+":silk_touch_setting_always_ores");
        AutoTools.SILK_TOUCH_SETTING_ALWAYS_EXC_ORES = BlockTags.bind(AutoTools.MOD_ID+":silk_touch_setting_always_exc_ores");
        AutoTools.FORTUNE = BlockTags.bind(AutoTools.MOD_ID+":fortune");
        AutoTools.FORTUNE_SETTING = BlockTags.bind(AutoTools.MOD_ID+":fortune_setting");
        AutoTools.DO_NOT_SWAP_UNLESS_ENCH = BlockTags.bind(AutoTools.MOD_ID+":do_not_swap_unless_ench");

        //Removing Tags from List, so they won't be expected on a server
        BlockTags.getWrappers().remove(AutoTools.SHEARS);
        BlockTags.getWrappers().remove(AutoTools.SILK_TOUCH);
        BlockTags.getWrappers().remove(AutoTools.SILK_TOUCH_SETTING_ALWAYS);
        BlockTags.getWrappers().remove(AutoTools.SILK_TOUCH_SETTING_ALWAYS_ORES);
        BlockTags.getWrappers().remove(AutoTools.SILK_TOUCH_SETTING_ALWAYS_EXC_ORES);
        BlockTags.getWrappers().remove(AutoTools.FORTUNE);
        BlockTags.getWrappers().remove(AutoTools.FORTUNE_SETTING);
        BlockTags.getWrappers().remove(AutoTools.DO_NOT_SWAP_UNLESS_ENCH);


        //Registering the clientSetup method
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        // Registering mod for game events
        MinecraftForge.EVENT_BUS.register(this);

        //Registering the config
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AutoToolsConfigImpl.SPEC, "autotools.toml");
        ModLoadingContext.get().registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY,
                () -> (minecraft, screen) -> new AutoToolsConfigScreen(screen)
        );
    }

    //Called once when the client is set up
    public void clientSetup(final FMLCommonSetupEvent event) {
        ClientRegistry.registerKeyBinding(key_changeTool);
        AutoTools.init();
    }

    @SubscribeEvent
    public void BlockBreakEvent(BlockEvent.BreakEvent event) {
        if (AutoToolsConfig.SWITCH_BACK && !AutoToolsConfig.TOGGLE) {
            AutoTools.blockBroken = true;
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
                        AutoTools.switchItem = !AutoTools.switchItem;
                        client.player.sendMessage(new TextComponent(AutoTools.switchItem ? new TranslatableComponent("chat.enabled_autotools").getString() : new TranslatableComponent("chat.disabled_autotools").getString()), client.player.getUUID());
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
                if (AutoToolsConfig.SWITCH_BACK && (AutoToolsConfig.TOGGLE || AutoTools.blockBroken)) {
                    AutoTools.switchBack();
                    AutoTools.blockBroken = false;
                }
            }
        }
    }

    @SubscribeEvent
    public void ClickInputEvent(InputEvent.ClickInputEvent event) {
        if(event.isAttack()){
            Minecraft client = Minecraft.getInstance();
            AutoTools.onBlockBreaking(client, client.hitResult);
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

                    final int[] index = {0};

                    event.getToolTip().forEach((toolTip) -> {
                        if (toolTip instanceof TextComponent) {
                            TextComponent textComponent = (TextComponent) toolTip;
                            if (textComponent.getText().equals(Registry.ITEM.getKey(stack.getItem()).toString())) {
                                index[0] = event.getToolTip().indexOf(toolTip);
                            }
                        }
                    });


                    String damage = (optionalAttackDamage > attackDamage) ?
                            (double) Math.round(attackDamage * 10d) / 10d + " (" + (double) Math.round(optionalAttackDamage * 10d) / 10d + ")" :
                            String.valueOf((double) Math.round(attackDamage * 10d) / 10d);

                    if (index[0] > 0 && index[0] < event.getToolTip().size()) {
                        event.getToolTip().add(index[0] - 1, new TextComponent(" " + damage + " Dps").withStyle(ChatFormatting.DARK_GREEN));
                    } else {
                        event.getToolTip().add(new TextComponent(" " + damage + " Dps").withStyle(ChatFormatting.DARK_GREEN));
                    }
                }
            }
        }
    }
}
