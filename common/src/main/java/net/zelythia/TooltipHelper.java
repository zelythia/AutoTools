package net.zelythia;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

import java.util.List;

public class TooltipHelper {

    public static void applyTooltip(ItemStack stack, List<Component> tooltip) {
        if (AutoToolsConfig.SHOWDPS) {
            Item item = stack.getItem();

            if (item != Items.AIR) {
                float baseAttackDamage = 0;
                float attackDamage = 0;
                float attackSpeed = 0;
                ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);

                if (modifiers == null) return;

                for (ItemAttributeModifiers.Entry modifier : modifiers.modifiers()) {
                    if (modifier.modifier().id().equals(ResourceLocation.parse("minecraft:base_attack_damage"))) {
                        baseAttackDamage = (float) modifier.modifier().amount();
                    } else if (modifier.modifier().id().equals(ResourceLocation.parse("minecraft:base_attack_speed"))) {
                        attackSpeed = (float) modifier.modifier().amount();
                    }
                }

                if (baseAttackDamage > 0) {
                    attackDamage = (1 + baseAttackDamage) * (4F + attackSpeed);
                }

                //Check for enchantments
                float optionalAttackDamage = baseAttackDamage;
                if (stack.isEnchanted()) {
                    ItemEnchantments itemEnchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                        Enchantment enchantment = entry.getKey().value();
                        List<ConditionalEffect<EnchantmentValueEffect>> effects = enchantment.getEffects(EnchantmentEffectComponents.DAMAGE);

                        for (ConditionalEffect<EnchantmentValueEffect> effect : effects) {
                            optionalAttackDamage = effect.effect().process(entry.getIntValue(), RandomSource.create(), optionalAttackDamage);
                        }
                    }

                    optionalAttackDamage = (1 + optionalAttackDamage) * (4F + attackSpeed);
                }


                if (attackDamage > 1) {
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

                    tooltip.add(index, Component.literal(" " + damage + " Dps").withStyle(ChatFormatting.DARK_GREEN));
                }
            }
        }
    }
}
