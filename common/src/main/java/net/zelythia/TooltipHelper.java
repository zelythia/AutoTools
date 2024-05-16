package net.zelythia;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;

public class TooltipHelper {

    public static void applyTooltip(ItemStack stack, List<Component> tooltip) {
        if (AutoToolsConfig.SHOWDPS) {
            Item item = stack.getItem();

            if (item != Items.AIR) {
                double attackDamage = 0;
                double attackSpeed = 0;

                attackDamage = stack.getDamageValue();

                for (ItemAttributeModifiers.Entry modifier : stack.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers()) {
                    if(modifier.attribute().is(Attributes.ATTACK_DAMAGE)){
                        attackDamage = modifier.modifier().amount();
                    }
                    else if(modifier.attribute().is(Attributes.ATTACK_SPEED)){
                        attackSpeed = modifier.modifier().amount();
                    }
                }

                if(attackDamage > 0){
                    if(attackSpeed > 0){
                        attackDamage = (1 + attackDamage) * (4F + attackSpeed);
                    }
                    else{
                        attackDamage = attackDamage + 1;
                    }
                }
//
                double optionalAttackDamage = attackDamage;
                //Check for enchantments
                if (stack.isEnchanted()) {
                    attackDamage += EnchantmentHelper.getDamageBonus(stack, null);

                    if (optionalAttackDamage + EnchantmentHelper.getDamageBonus(stack, EntityType.ZOMBIE) > attackDamage) {
                        optionalAttackDamage += EnchantmentHelper.getDamageBonus(stack, EntityType.ZOMBIE);
                    } else if (optionalAttackDamage + EnchantmentHelper.getDamageBonus(stack, EntityType.SPIDER) > attackDamage) {
                        optionalAttackDamage += EnchantmentHelper.getDamageBonus(stack, EntityType.SPIDER);
                    } else if (optionalAttackDamage + EnchantmentHelper.getDamageBonus(stack, EntityType.GUARDIAN) > attackDamage) {
                        optionalAttackDamage += EnchantmentHelper.getDamageBonus(stack, EntityType.GUARDIAN);
                    }
                }

                if (attackDamage > 1) {

                    int index = 0;
                    for (int i = tooltip.size() - 1; i >= 0; i--) {
                        if(tooltip.get(i).getStyle().getColor() != null){
                            if(tooltip.get(i).getStyle().getColor().getValue() == 43520){
                                index = i;
                                break;
                            }
                        }
                    }
                    if (index < tooltip.size()) index++;

                    String damage = (optionalAttackDamage > attackDamage) ?
                            (double) Math.round(attackDamage * 10d) / 10d + " (" + (double) Math.round(optionalAttackDamage * 10d) / 10d + ")" :
                            String.valueOf((double) Math.round(attackDamage * 10d) / 10d);

                    tooltip.add(index, Component.literal(" " + damage + " Dps").withStyle(ChatFormatting.DARK_GREEN));
                }
            }
        }
    }
}
