package net.zelythia.autotools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.zelythia.autotools.config.AutoToolsConfig;

import java.util.List;

public class TooltipHelper {

    public static void applyTooltip(ItemStack stack, List<Component> tooltip) {
        if (AutoToolsConfig.get().showDPS) {
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
                    //Searching for the index of the last stat(green) tooltip
                    int index = 0;
                    for (int i = tooltip.size() - 1; i >= 0; i--) {
                        if (tooltip.get(i).getStyle().getColor() != null) {
                            if (tooltip.get(i).getStyle().getColor().getValue() == 43520) {
                                index = i;
                                break;
                            }
                        }
                    }
                    if (index < tooltip.size()) index++;

                    String damage = (optionalAttackDamage > attackDamage) ?
                            (double) Math.round(attackDamage * 10d) / 10d + " (" + (double) Math.round(optionalAttackDamage * 10d) / 10d + ")" :
                            String.valueOf((double) Math.round(attackDamage * 10d) / 10d);

                    tooltip.add(index, new TextComponent(" " + damage + " Dps").withStyle(ChatFormatting.DARK_GREEN));
                }
            }
        }
    }
}
