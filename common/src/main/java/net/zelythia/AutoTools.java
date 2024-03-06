package net.zelythia;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.zelythia.clientTags.ClientTags;
import net.zelythia.clientTags.ClientTagsImpl;
import net.zelythia.clientTags.ClientTagsLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class AutoTools {
    public static final String MOD_ID = "autotools";
    public static final Logger LOGGER = LogManager.getLogger("AutoTools");

    public static Tag<Block> SILK_TOUCH;
    public static Tag<Block> SILK_TOUCH_SETTING_ALWAYS;
    public static Tag<Block> SILK_TOUCH_SETTING_ALWAYS_ORES;
    public static Tag<Block> SILK_TOUCH_SETTING_ALWAYS_EXC_ORES;
    public static Tag<Block> FORTUNE;
    public static Tag<Block> FORTUNE_SETTING;
    public static Tag<Block> DO_NOT_SWAP_UNLESS_ENCH;

    public static final HashMap<ResourceLocation, ResourceLocation[]> CUSTOM_TOOLS = new HashMap<>();
    private static final HashMap<String, ResourceLocation[]> TOOL_LISTS = new HashMap<String, ResourceLocation[]>() {{
        put("autotools:pickaxe", new ResourceLocation[]{new ResourceLocation("minecraft:netherite_pickaxe"), new ResourceLocation("minecraft:diamond_pickaxe"), new ResourceLocation("minecraft:iron_pickaxe"), new ResourceLocation("minecraft:golden_pickaxe"), new ResourceLocation("minecraft:stone_pickaxe"), new ResourceLocation("minecraft:wooden_pickaxe")});
        put("autotools:shovel", new ResourceLocation[]{new ResourceLocation("minecraft:netherite_shovel"), new ResourceLocation("minecraft:diamond_shovel"), new ResourceLocation("minecraft:iron_shovel"), new ResourceLocation("minecraft:golden_shovel"), new ResourceLocation("minecraft:stone_shovel"), new ResourceLocation("minecraft:wooden_shovel")});
        put("autotools:hoe", new ResourceLocation[]{new ResourceLocation("minecraft:netherite_hoe"), new ResourceLocation("minecraft:diamond_hoe"), new ResourceLocation("minecraft:iron_hoe"), new ResourceLocation("minecraft:golden_hoe"), new ResourceLocation("minecraft:stone_hoe"), new ResourceLocation("minecraft:wooden_hoe")});
        put("autotools:sword", new ResourceLocation[]{new ResourceLocation("minecraft:netherite_sword"), new ResourceLocation("minecraft:diamond_sword"), new ResourceLocation("minecraft:iron_sword"), new ResourceLocation("minecraft:golden_sword"), new ResourceLocation("minecraft:stone_sword"), new ResourceLocation("minecraft:wooden_sword")});
        put("autotools:axe", new ResourceLocation[]{new ResourceLocation("minecraft:netherite_axe"), new ResourceLocation("minecraft:diamond_axe"), new ResourceLocation("minecraft:iron_axe"), new ResourceLocation("minecraft:golden_axe"), new ResourceLocation("minecraft:stone_axe"), new ResourceLocation("minecraft:wooden_axe")});
    }};

    private static final Stack<Integer> swaps = new Stack<>();


    /**
     * To be called by forge/fabric client-initialized methods
     */
    public static void init() {
        AutoToolsConfig.load();

        loadCustomItems();
    }

    public static void loadCustomItems() {

        JsonParser jsonParser = new JsonParser();

        try {
            JsonElement jsonElement = jsonParser.parse(AutoToolsConfig.CUSTOM_TOOLS);
            if (!jsonElement.isJsonObject()) return;
            JsonObject jsonObject = (JsonObject) jsonElement;

            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();

            for (Map.Entry<String, JsonElement> entry : entrySet) {

                ArrayList<ResourceLocation> tools = new ArrayList<>();

                if (entry.getValue().isJsonArray()) {
                    JsonArray toolsArray = entry.getValue().getAsJsonArray();

                    for (int i = 0; i < toolsArray.size(); i++) {
                        if (TOOL_LISTS.containsKey(toolsArray.get(i).getAsString())) {
                            tools.addAll(Arrays.asList(TOOL_LISTS.get(toolsArray.get(i).getAsString())));
                            continue;
                        }

                        tools.add(new ResourceLocation(toolsArray.get(i).getAsString()));
                    }
                } else {
                    if (TOOL_LISTS.containsKey(entry.getValue().getAsString())) {
                        tools.addAll(Arrays.asList(TOOL_LISTS.get(entry.getValue().getAsString())));
                    } else tools.add(new ResourceLocation(entry.getValue().getAsString()));
                }

                CUSTOM_TOOLS.put(new ResourceLocation(entry.getKey()), Arrays.copyOf(tools.toArray(), tools.size(), ResourceLocation[].class));
            }

            LOGGER.info("Loaded custom block configs: " + CUSTOM_TOOLS.keySet());
        } catch (Exception e) {
            LOGGER.error("Error while parsing custom blocks");
        }
    }

    /**
     * Brings the item from sourceSlot into the players main hand
     *
     * @param sourceSlot The slot with the item you want to select
     */
    public static void selectItem(Minecraft client, Inventory inventory, int sourceSlot) {
        int destSlot = AutoToolsConfig.KEEPSLOT ? inventory.selected : inventory.getSuitableHotbarSlot();
        swaps.push(sourceSlot);

        if (Screen.hasShiftDown()) {
            //Simulating a click on the toolSlot and the swappableSlot with the ClickType = SWAP so it updates with the server
            client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, destSlot + 18, sourceSlot, ClickType.SWAP, client.player);
            client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, destSlot + 27, sourceSlot, ClickType.SWAP, client.player);
            client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, destSlot + 36, sourceSlot, ClickType.SWAP, client.player);
        } else {
            client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, destSlot + 36, sourceSlot, ClickType.SWAP, client.player);
        }

        inventory.selected = destSlot;
    }

    /**
     * Used for AutoToolsConfig.SWITCH_BACK to switch to the last tool the player was holding before using AutoTools
     */
    public static void switchBack() {
        if (swaps.empty()) return;
        Minecraft client = Minecraft.getInstance();
        if(client.player == null || client.gameMode == null) return;

        Inventory inventory = client.player.inventory;

        while (!swaps.empty()) {
            int i = swaps.pop();

            if (i <= 8) {
                inventory.selected = i;
                return;
            }

            client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, i, inventory.selected, ClickType.SWAP, client.player);
        }
    }

    /**
     * Return an enchantment modifier for when the stack is used a specific block
     * @return -1 , 0 or positive Float
     */
    public static float getEnchantmentModifier(ItemStack stack, BlockState blockState, BlockPos pos) {
        float modifier = 1F;
        if (stack.isEnchanted()) {
            //Efficiency
            if (blockState.getDestroySpeed(null, pos) != 0) {
                modifier *= (1F + (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack) * 20F) / 100F);
            }

            //SilkTouch
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 1) {
                if (ClientTags.isInWithLocalFallback(SILK_TOUCH, blockState.getBlock())
                        || AutoToolsConfig.PREFER_SILK_TOUCH.equals("always") && ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS, blockState.getBlock())
                        || AutoToolsConfig.PREFER_SILK_TOUCH.equals("except_ores") && ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_EXC_ORES, blockState.getBlock())
                        || AutoToolsConfig.PREFER_SILK_TOUCH.equals("always_ores") && ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_ORES, blockState.getBlock())) {
                    modifier *= 1000F;
                } else if (AutoToolsConfig.PREFER_SILK_TOUCH.equals("never")
                        || (AutoToolsConfig.PREFER_SILK_TOUCH.equals("except_ores") && !ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_EXC_ORES, blockState.getBlock()))
                        || (AutoToolsConfig.PREFER_SILK_TOUCH.equals("always_ores") && !ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_ORES, blockState.getBlock()))) {
                    return -1;
                }
            }
            //Fortune
            else if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack) >= 1) {
                if (ClientTags.isInWithLocalFallback(FORTUNE, blockState.getBlock())
                        && !(AutoToolsConfig.PREFER_SILK_TOUCH.equals("always") && ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS, blockState.getBlock()))
                        && !(AutoToolsConfig.PREFER_SILK_TOUCH.equals("always_ores") && ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_ORES, blockState.getBlock()))
                        || AutoToolsConfig.ALWAYS_PREFER_FORTUNE && ClientTags.isInWithLocalFallback(FORTUNE_SETTING, blockState.getBlock())) {
                    modifier *= 1000F * EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack);
                }
            }

            //Hoe check
            if (ClientTags.isInWithLocalFallback(FORTUNE, blockState.getBlock()) && ClientTags.isInWithLocalFallback(DO_NOT_SWAP_UNLESS_ENCH, blockState.getBlock()) && stack.getItem() instanceof HoeItem) {
                modifier += 1;
            }
        }
        return modifier - 1;
    }

    /**
     * Custom {@link Inventory#findSlotMatchingItem(ItemStack)} method that ignored ItemTags
     */
    public static int findSlotMatchingItem(Inventory inventory, ItemStack itemStack) {
        for(int i = 0; i < inventory.items.size(); ++i) {
            if (ItemStack.isSame(itemStack, inventory.items.get(i))) {
                return i;
            }
        }

        return -1;
    }


    public static void getCorrectTool(HitResult hit, Minecraft client) {
        Inventory inventory = client.player.inventory;

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hit;
            BlockState blockState = client.level.getBlockState(blockHitResult.getBlockPos());

            int toolSlot = -1;
            float miningSpeed = 1;

            //Detection for custom tools
            if (CUSTOM_TOOLS.containsKey(Registry.BLOCK.getKey(blockState.getBlock()))) {
                ResourceLocation[] tools = CUSTOM_TOOLS.get(Registry.BLOCK.getKey(blockState.getBlock()));

                for (ResourceLocation resourceLocation : tools) {
                    if(Objects.equals(resourceLocation, new ResourceLocation("autotools", "disabled"))) return;

                    toolSlot = AutoTools.findSlotMatchingItem(inventory, new ItemStack(Registry.ITEM.get(resourceLocation)));
                    if (toolSlot != -1) break;
                }

                if (toolSlot == -1) {
                } else if (toolSlot <= 8) {
                    if (swaps.empty() || swaps.get(swaps.size() - 1) != inventory.selected) {
                        swaps.push(inventory.selected);
                    }
                    inventory.selected = toolSlot;
                    return;
                } else {
                    selectItem(client, inventory, toolSlot);
                    return;
                }
            }

            //End portal-Frame detection
            if (!AutoToolsConfig.TOGGLE && blockState.getBlock() == Blocks.END_PORTAL_FRAME) {
                toolSlot = AutoTools.findSlotMatchingItem(inventory, new ItemStack(Items.ENDER_EYE));

                if (toolSlot == -1) {
                } else if (toolSlot <= 8) {
                    inventory.selected = toolSlot;
                    return;
                } else {
                    selectItem(client, inventory, toolSlot);
                }
            }

            //Disabling tool switching on instant mine-able blocks unless it drops more with fortune
            //Calling with blockGetter == null because none of the parameters are being (Might clash with mixins)
            if (blockState.getDestroySpeed(null, blockHitResult.getBlockPos()) == 0 && !ClientTags.isInWithLocalFallback(DO_NOT_SWAP_UNLESS_ENCH, blockState.getBlock())) {
                return;
            }

            if (AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY) {
                if (inventory.getItem(inventory.selected).getItem().isCorrectToolForDrops(blockState)
                        || !blockState.requiresCorrectToolForDrops()) return;
            }


            boolean foundEnchantment = false;
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                Item item = inventory.getItem(i).getItem();

                if (item != Items.AIR) {
                    float newMiningSpeed = 1;

                    if (item.isCorrectToolForDrops(blockState) || !blockState.requiresCorrectToolForDrops()) {
                        newMiningSpeed = item.getDestroySpeed(inventory.getItem(i), blockState);

                        newMiningSpeed += getEnchantmentModifier(inventory.getItem(i), blockState, blockHitResult.getBlockPos());
                        if (!foundEnchantment)
                            foundEnchantment = getEnchantmentModifier(inventory.getItem(i), blockState, blockHitResult.getBlockPos()) != 0;
                    }

                    if (newMiningSpeed == miningSpeed) {
                        if (toolSlot != -1) {
                            if (AutoToolsConfig.PREFER_HOTBAR_TOOL) {
                                if (i <= 8 && (toolSlot > 8 ||
                                        ((AutoToolsConfig.PREFER_LOW_DURABILITY && inventory.getItem(i).getDamageValue() > inventory.getItem(toolSlot).getDamageValue())
                                                || (!AutoToolsConfig.PREFER_LOW_DURABILITY && inventory.getItem(i).getDamageValue() < inventory.getItem(toolSlot).getDamageValue())))
                                ) {
                                    toolSlot = i;
                                    miningSpeed = newMiningSpeed;
                                }
                            } else if ((AutoToolsConfig.PREFER_LOW_DURABILITY && inventory.getItem(i).getDamageValue() > inventory.getItem(toolSlot).getDamageValue())
                                    || (!AutoToolsConfig.PREFER_LOW_DURABILITY && inventory.getItem(i).getDamageValue() < inventory.getItem(toolSlot).getDamageValue())) {
                                toolSlot = i;
                                miningSpeed = newMiningSpeed;
                            }
                        }
                    } else if (newMiningSpeed > miningSpeed) {
                        toolSlot = i;
                        miningSpeed = newMiningSpeed;
                    }
                }
            }

            if (toolSlot == -1 || ClientTags.isInWithLocalFallback(DO_NOT_SWAP_UNLESS_ENCH, blockState.getBlock()) && !foundEnchantment) {
            } else if (toolSlot <= 8) {
                if (swaps.empty() || swaps.get(swaps.size() - 1) != inventory.selected) {
                    swaps.push(inventory.selected);
                }
                inventory.selected = toolSlot;
            } else {
                selectItem(client, inventory, toolSlot);
            }
        }
        else if (AutoToolsConfig.CHANGE_FOR_ENTITIES && hit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hit).getEntity();

            int toolSlot = -1;
            float attackDamage = 0;

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                Item item = inventory.getItem(i).getItem();

                if (item != Items.AIR) {
                    double newAttackDamage = 1.0;


                    if (entity instanceof Boat || entity instanceof AbstractMinecart || entity instanceof LivingEntity) {
                        if (entity instanceof LivingEntity) {
                            LivingEntity livingEntity = (LivingEntity) entity;
                            if (!item.hurtEnemy(inventory.getItem(i), livingEntity, inventory.player)) {
                                continue;
                            }
                        }

                        //Custom tool detection
                        if (CUSTOM_TOOLS.containsKey(Registry.ENTITY_TYPE.getKey(entity.getType()))) {
                            ResourceLocation[] tools = CUSTOM_TOOLS.get(Registry.ENTITY_TYPE.getKey(entity.getType()));

                            for (ResourceLocation resourceLocation : tools) {
                                if(Objects.equals(resourceLocation, new ResourceLocation("autotools", "disabled"))) return;

                                toolSlot = AutoTools.findSlotMatchingItem(inventory, new ItemStack(Registry.ITEM.get(resourceLocation)));
                                if (toolSlot != -1) break;
                            }

                            if (toolSlot == -1) {
                            } else if (toolSlot <= 8) {
                                if (swaps.empty() || swaps.get(swaps.size() - 1) != inventory.selected) {
                                    swaps.push(inventory.selected);
                                }
                                inventory.selected = toolSlot;
                                return;
                            } else {
                                selectItem(client, inventory, toolSlot);
                                return;
                            }
                        }

                        //Every item with an attackDamage larger than 1 has an ATTACK_DAMAGE attribute/modifier
                        if (inventory.getItem(i).getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE)) {
                            //Calculating DPS
                            if (inventory.getItem(i).getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_SPEED)) {
                                //Damage
                                newAttackDamage = (1 + ((AttributeModifier) inventory.getItem(i).getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).toArray()[0]).getAmount())
                                        //Attack speed
                                        * (4F + ((AttributeModifier) inventory.getItem(i).getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED).toArray()[0]).getAmount());
                            } else {
                                newAttackDamage = 1 + ((AttributeModifier) inventory.getItem(i).getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).toArray()[0]).getAmount();
                            }
                        }

                        //Enchantments
                        if (inventory.getItem(i).isEnchanted()) {
                            if (((EntityHitResult) hit).getEntity() instanceof LivingEntity) {
                                LivingEntity livingEntity = (LivingEntity) ((EntityHitResult) hit).getEntity();
                                newAttackDamage += EnchantmentHelper.getDamageBonus(inventory.getItem(i), livingEntity.getMobType());
                            }
                        }

                        if (newAttackDamage > attackDamage || (newAttackDamage == attackDamage && toolSlot != -1 && inventory.getItem(i).getDamageValue() < inventory.getItem(toolSlot).getDamageValue())) {
                            attackDamage = (float) newAttackDamage;
                            toolSlot = i;
                        } else if (newAttackDamage == attackDamage) {
                            if (inventory.getItem(i).getDamageValue() > inventory.getItem(toolSlot).getDamageValue()) {
                                toolSlot = i;
                            }
                        }
                    }
                }
            }

            if (toolSlot == -1) {
                if (!AutoToolsConfig.TOGGLE && client.player.isCreative()) {
                    inventory.setItem(inventory.getSuitableHotbarSlot(), new ItemStack(Items.NETHERITE_SWORD));
                }
            } else if (toolSlot <= 8) {
                inventory.selected = toolSlot;
            } else {
                selectItem(client, inventory, toolSlot);
            }

        }
    }
}
