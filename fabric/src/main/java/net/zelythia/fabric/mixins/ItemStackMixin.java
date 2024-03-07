package net.zelythia.fabric.mixins;


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
import net.zelythia.AutoToolsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract ItemStack copy();

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean isEnchanted();

    @ModifyVariable(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasTag()Z", ordinal = 1), name = "list", ordinal = 0)
    public List<Component> addDPSTooltip(List<Component> list) {
        if (AutoToolsConfig.SHOWDPS) {
            ItemStack stack = this.copy();
            Item item = this.getItem();

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
                if (this.isEnchanted()) {
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
                    String damage = (optionalAttackDamage > attackDamage) ?
                            (double) Math.round(attackDamage * 10d) / 10d + " (" + (double) Math.round(optionalAttackDamage * 10d) / 10d + ")" :
                            String.valueOf((double) Math.round(attackDamage * 10d) / 10d);
                    list.add(new TextComponent(" " + damage + " Dps").withStyle(ChatFormatting.DARK_GREEN));
                }
            }
        }

        return list;
    }

}
