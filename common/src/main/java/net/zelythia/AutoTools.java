package net.zelythia;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.zelythia.clientTags.ClientTags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class AutoTools {
    public static final String MOD_ID = "autotools";
    public static final Logger LOGGER = LogManager.getLogger("AutoTools");

    public static final TagKey<Block> SHEARS = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "shears"));
    public static final TagKey<Block> SILK_TOUCH = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "silk_touch"));
    public static final TagKey<Block> SILK_TOUCH_SETTING_ALWAYS = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "silk_touch_setting_always"));
    public static final TagKey<Block> SILK_TOUCH_SETTING_ALWAYS_ORES = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "silk_touch_setting_always_ores"));
    public static final TagKey<Block> SILK_TOUCH_SETTING_ALWAYS_EXC_ORES = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "silk_touch_setting_always_exc_ores"));
    public static final TagKey<Block> FORTUNE = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "fortune"));
    public static final TagKey<Block> FORTUNE_SETTING = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "fortune_setting"));
    public static final TagKey<Block> DO_NOT_SWAP_UNLESS_ENCH = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "do_not_swap_unless_ench"));

    public static final HashMap<ResourceLocation, List<ResourceLocation>> CUSTOM_TOOLS = new HashMap<>();
    private static final HashMap<String, ResourceLocation[]> TOOL_LISTS = new HashMap<>() {{
        put("autotools:pickaxe", new ResourceLocation[]{new ResourceLocation("minecraft:netherite_pickaxe"), new ResourceLocation("minecraft:diamond_pickaxe"), new ResourceLocation("minecraft:iron_pickaxe"), new ResourceLocation("minecraft:golden_pickaxe"), new ResourceLocation("minecraft:stone_pickaxe"), new ResourceLocation("minecraft:wooden_pickaxe")});
        put("autotools:shovel", new ResourceLocation[]{new ResourceLocation("minecraft:netherite_shovel"), new ResourceLocation("minecraft:diamond_shovel"), new ResourceLocation("minecraft:iron_shovel"), new ResourceLocation("minecraft:golden_shovel"), new ResourceLocation("minecraft:stone_shovel"), new ResourceLocation("minecraft:wooden_shovel")});
        put("autotools:hoe", new ResourceLocation[]{new ResourceLocation("minecraft:netherite_hoe"), new ResourceLocation("minecraft:diamond_hoe"), new ResourceLocation("minecraft:iron_hoe"), new ResourceLocation("minecraft:golden_hoe"), new ResourceLocation("minecraft:stone_hoe"), new ResourceLocation("minecraft:wooden_hoe")});
        put("autotools:sword", new ResourceLocation[]{new ResourceLocation("minecraft:netherite_sword"), new ResourceLocation("minecraft:diamond_sword"), new ResourceLocation("minecraft:iron_sword"), new ResourceLocation("minecraft:golden_sword"), new ResourceLocation("minecraft:stone_sword"), new ResourceLocation("minecraft:wooden_sword")});
        put("autotools:axe", new ResourceLocation[]{new ResourceLocation("minecraft:netherite_axe"), new ResourceLocation("minecraft:diamond_axe"), new ResourceLocation("minecraft:iron_axe"), new ResourceLocation("minecraft:golden_axe"), new ResourceLocation("minecraft:stone_axe"), new ResourceLocation("minecraft:wooden_axe")});
    }};

    public static final Stack<Integer> swaps = new Stack<>();
    public static boolean toggle = true;
    public static BlockState lastBlock = null;

    //Used for SWITCH_BACK when toggle is disable
    public static boolean startedMining = false;

    // Used for the experimental swap delay
    public static boolean swapped = false;

    //To be called by forge/fabric client-initialized methods
    public static void init() {
        reloadConfig();
    }

    public static void reloadConfig() {
        AutoToolsConfig.load();

        //Not the best way of adding custom tools. Fine as long as it won't get any more
        CUSTOM_TOOLS.put(new ResourceLocation("minecraft", "bamboo"), new ArrayList<>(Arrays.asList(TOOL_LISTS.get("autotools:sword"))));
        loadCustomItems();

        AutoToolsConfig.IGNORED_SLOTS = AutoToolsConfig.IGNORED_SLOTS.stream().map(i -> i - 1).toList();
        AutoToolsConfig.TARGET_SLOTS = AutoToolsConfig.TARGET_SLOTS.stream().map(i -> i - 1).toList();
    }

    private static void loadCustomItems() {
        try {
            JsonElement jsonElement = JsonParser.parseString(AutoToolsConfig.CUSTOM_TOOLS);
            if (!jsonElement.isJsonObject()) return;
            JsonObject jsonObject = (JsonObject) jsonElement;

            for (String key : jsonObject.keySet()) {

                ArrayList<ResourceLocation> tools = new ArrayList<>();
                if (jsonObject.get(key).isJsonArray()) {
                    JsonArray toolsArray = jsonObject.getAsJsonArray(key);

                    for (int i = 0; i < toolsArray.size(); i++) {
                        if (TOOL_LISTS.containsKey(toolsArray.get(i).getAsString())) {
                            tools.addAll(Arrays.asList(TOOL_LISTS.get(toolsArray.get(i).getAsString())));
                            continue;
                        }

                        tools.add(new ResourceLocation(toolsArray.get(i).getAsString()));
                    }
                } else {
                    if (TOOL_LISTS.containsKey(jsonObject.get(key).getAsString())) {
                        tools.addAll(List.of(TOOL_LISTS.get(jsonObject.get(key).getAsString())));
                    } else tools.add(new ResourceLocation(jsonObject.get(key).getAsString()));
                }

                CUSTOM_TOOLS.computeIfAbsent(new ResourceLocation(key), k -> new ArrayList<>()).addAll(tools);
            }

            LOGGER.info("Loaded custom block configs: " + CUSTOM_TOOLS.keySet());
        } catch (Exception e) {
            LOGGER.error("Error while parsing custom blocks");
        }
    }

    public static void onBlockBreaking(Minecraft client, HitResult hitResult) {
        if (AutoToolsConfig.TOGGLE && AutoTools.toggle) {
            if (client.player.isCreative()) {
                if (!AutoToolsConfig.DISABLECREATIVE) {
                    AutoTools.getCorrectTool(hitResult, client);
                }
            } else {
                AutoTools.getCorrectTool(hitResult, client);
            }
        }
    }

    /**
     * Brings the item from sourceSlot into the players main hand
     *
     * @param sourceSlot The slot with the item you want to select
     */
    public static void selectItem(Minecraft client, Inventory inventory, int sourceSlot) {
        if(sourceSlot == inventory.selected) return;

        if (swaps.empty()) {
            swaps.push(inventory.selected);
        }

        if (sourceSlot <= 8 && !AutoToolsConfig.KEEPSLOT) {
            if (swaps.getLast() != inventory.selected) {
                if (swaps.peek() != sourceSlot) swaps.push(inventory.selected);
            }
            inventory.selected = sourceSlot;

            return;
        }

        if (sourceSlot <= 8)
            sourceSlot += 36;   // Needs to be done because the hotbar slots are shifted by 36 in slot index

        int destSlot = AutoToolsConfig.KEEPSLOT ? inventory.selected : getSuitableHotbarSlot(inventory);
        if (!AutoToolsConfig.TARGET_SLOTS.contains(destSlot)) destSlot = AutoToolsConfig.TARGET_SLOTS.getFirst();

        if (swaps.peek() != sourceSlot) swaps.push(sourceSlot);
        if (swaps.peek() != destSlot) swaps.push(destSlot);

        swapped = true;
        client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, sourceSlot, destSlot, ClickType.SWAP, client.player);

        inventory.selected = destSlot;
        inventory.setChanged();

        if(!AutoToolsConfig.SWITCH_BACK) swaps.clear(); //Easy way to safe some memory because swaps are only needed for switchBack
    }


    /**
     * Mirroring Inventory.getSuitableHotbarSlot() with regards for TARGET_SLOTS
     */
    public static int getSuitableHotbarSlot(Inventory inventory) {
        int i;
        int j;
        for (i = 0; i < 9; ++i) {
            j = (inventory.selected + i) % 9;
            if (AutoToolsConfig.TARGET_SLOTS.contains(j) && inventory.items.get(j).isEmpty()) {
                return j;
            }
        }

        for (i = 0; i < 9; ++i) {
            j = (inventory.selected + i) % 9;
            if (AutoToolsConfig.TARGET_SLOTS.contains(j) && !inventory.items.get(j).isEnchanted()) {
                return j;
            }
        }

        return inventory.selected;
    }


    /**
     * Used for AutoToolsConfig.SWITCH_BACK to switch to the last tool the player was holding before using AutoTools
     */
    public static void switchBack() {
        if (!AutoToolsConfig.SWITCH_BACK) return;    //Shouldn't be necessary, but just in case
        //Don't switch if the player wants to mine another block || swaps.empty()
        if (Minecraft.getInstance().options.keyAttack.isDown() || swaps.empty()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null) return;

        Inventory inventory = client.player.getInventory();

        //Minimizing swaps by removing reverted/duplicate . Flattening/Squashing the Stack. E.g. [0, 30, 0, 30, 0] -> [0]
        boolean changed = true;
        while(changed && swaps.size() > 1){
            changed = false;

            loop:
            for (int intervalSize = swaps.size()/2; intervalSize >= 2; intervalSize--) {
                for (int topOffset = 0; topOffset <= swaps.size()/ intervalSize; topOffset++) {
                    boolean foundDuplicate = true;

                    if(swaps.size() - 1 - topOffset >= 2* intervalSize){
                        for (int j = topOffset; j < intervalSize + topOffset; j++) {
                            if(swaps.elementAt(swaps.size() - j - 1) != swaps.elementAt(swaps.size() - 1 - intervalSize - j)){
                                foundDuplicate = false;
                                break;
                            }
                        }
                    }
                    else{
                        foundDuplicate = false;
                    }
                    if(foundDuplicate) {
                        int initialSize = swaps.size();
                        for (int i = 0; i < intervalSize *2; i++) {
                            swaps.remove(initialSize - topOffset - intervalSize *2);
                        }

                        changed = true;
                        break loop;
                    }
                }
            }
        }

        while (!swaps.empty()) {
            int i = swaps.pop();

            if (i <= 8) {
                if (AutoToolsConfig.KEEPSLOT && i != inventory.selected) {
                    client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, inventory.selected, i, ClickType.SWAP, client.player);
                    return;
                }

                inventory.selected = i;
                return;
            }

            client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, i, inventory.selected, ClickType.SWAP, client.player);
        }

        inventory.setChanged();
    }

    /**
     * Returns the miningSpeed and priority of an item [default = (1,0)]
     */
    public static ItemMiningSpeed getMiningSpeed(ItemStack stack, BlockState blockState, BlockPos pos) {
        float modifier = 1F;
        int priority = 0;
        float miningSpeed = stack.getDestroySpeed(blockState);

        if (stack.isEnchanted()) {
            //Efficiency
            if (blockState.getDestroySpeed(null, pos) != 0) {
                if (stack.isCorrectToolForDrops(blockState))
                    modifier += (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.EFFICIENCY, stack) * 20F) / 100F;
            }

            //SilkTouch
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 1) {
                if (ClientTags.isInWithLocalFallback(SILK_TOUCH, blockState.getBlock())
                        || AutoToolsConfig.PREFER_SILK_TOUCH == AutoToolsConfig.PreferSilkTouch.always && ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS, blockState.getBlock())
                        || AutoToolsConfig.PREFER_SILK_TOUCH == AutoToolsConfig.PreferSilkTouch.except_ores && ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_EXC_ORES, blockState.getBlock())
                        || AutoToolsConfig.PREFER_SILK_TOUCH == AutoToolsConfig.PreferSilkTouch.ores && ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_ORES, blockState.getBlock())) {
                    priority = 6;
                }
            }
            //Fortune
            else if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FORTUNE, stack) >= 1) {
                if (ClientTags.isInWithLocalFallback(FORTUNE, blockState.getBlock())
                        || AutoToolsConfig.ALWAYS_PREFER_FORTUNE && ClientTags.isInWithLocalFallback(FORTUNE_SETTING, blockState.getBlock())) {
                    priority += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FORTUNE, stack);
                }
            }

            //Hoe check to make sure we prefer fortune hoes over other fortune tools when farming
            if (ClientTags.isInWithLocalFallback(FORTUNE, blockState.getBlock()) && ClientTags.isInWithLocalFallback(DO_NOT_SWAP_UNLESS_ENCH, blockState.getBlock()) && stack.getItem() instanceof HoeItem) {
                priority += 1;
            }
        }

        if (blockState.getDestroySpeed(null, pos) != 0 && miningSpeed > 1) {
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0 && !ClientTags.isInWithLocalFallback(SILK_TOUCH, blockState.getBlock())) {
                if ((ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_EXC_ORES, blockState.getBlock()) && AutoToolsConfig.PREFER_SILK_TOUCH != AutoToolsConfig.PreferSilkTouch.except_ores)
                        || (ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_ORES, blockState.getBlock()) && AutoToolsConfig.PREFER_SILK_TOUCH != AutoToolsConfig.PreferSilkTouch.ores)
                        || (ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS, blockState.getBlock()) && AutoToolsConfig.PREFER_SILK_TOUCH != AutoToolsConfig.PreferSilkTouch.always)) {
                    priority += 1;
                }
            }

            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FORTUNE, stack) == 0 && !ClientTags.isInWithLocalFallback(FORTUNE, blockState.getBlock())
                    && ClientTags.isInWithLocalFallback(FORTUNE_SETTING, blockState.getBlock()) && !AutoToolsConfig.ALWAYS_PREFER_FORTUNE) {
                priority += 1;
            }
        }

        if (stack.is(Items.SHEARS) && ClientTags.isInWithLocalFallback(SHEARS, blockState.getBlock())) {
            priority += 6;
        }

        return new ItemMiningSpeed(miningSpeed * modifier, priority);
    }

    /**
     * Custom {@link Inventory#findSlotMatchingItem(ItemStack)} method that ignored ItemTags
     */
    public static int findSlotMatchingItem(Inventory inventory, ItemStack itemStack) {
        for (int i = 0; i < inventory.items.size(); ++i) {
            if (ItemStack.isSameItem(itemStack, inventory.items.get(i))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @return If the ItemStack should be considered a valid tool based on the MIN_DURABILITY config option
     */
    public static boolean checkDurability(ItemStack stack){
        if (AutoToolsConfig.MIN_DURABILITY < 1) {
            double durability = (double) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
            if (durability < AutoToolsConfig.MIN_DURABILITY)
                return false;
        } else if (stack.getMaxDamage() - stack.getDamageValue() <= AutoToolsConfig.MIN_DURABILITY)
            return false;

        return true;
    }


    public static void getCorrectTool(HitResult hit, Minecraft client) {
        Inventory inventory = client.player.getInventory();

        if (AutoToolsConfig.IGNORED_SLOTS.contains(inventory.selected)) return;

        ItemStack stack = inventory.getSelected();
        if(AutoToolsConfig.ENABLED == AutoToolsConfig.Enabled.tool && !stack.getComponents().has(DataComponents.TOOL)) return;
        else if(AutoToolsConfig.ENABLED == AutoToolsConfig.Enabled.no_tool && stack.getComponents().has(DataComponents.TOOL)) return;

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hit;
            BlockState blockState = client.level.getBlockState(blockHitResult.getBlockPos());

            int toolSlot = -1;
            ItemMiningSpeed miningSpeed = new ItemMiningSpeed(1f, 0);

            //Detection for custom tools
            if (CUSTOM_TOOLS.containsKey(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))) {
                List<ResourceLocation> tools = CUSTOM_TOOLS.get(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()));

                for (ResourceLocation resourceLocation : tools) {
                    if (Objects.equals(resourceLocation, new ResourceLocation("autotools", "disabled"))) return;

                    toolSlot = AutoTools.findSlotMatchingItem(inventory, new ItemStack(BuiltInRegistries.ITEM.get(resourceLocation)));
                    if (toolSlot != -1) break;
                }

                if (toolSlot == -1) {
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
                    return;
                }
            }

            //Disabling tool switching on instant mine-able blocks unless it drops more with fortune
            //Calling with blockGetter == null because none of the parameters are being (Might clash with mixins)
            if (blockState.getDestroySpeed(null, blockHitResult.getBlockPos()) == 0 && !ClientTags.isInWithLocalFallback(DO_NOT_SWAP_UNLESS_ENCH, blockState.getBlock())) {
                return;
            }

            if (AutoToolsConfig.ONLY_SWITCH_IF_NECESSARY) {
                if (inventory.getItem(inventory.selected).getItem().isCorrectToolForDrops(inventory.getItem(inventory.selected), blockState)
                        || !blockState.requiresCorrectToolForDrops()) return;
            }

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                Item item = inventory.getItem(i).getItem();

                if (item != Items.AIR) {
                    ItemMiningSpeed newMiningSpeed = new ItemMiningSpeed(1f, 0);

                    if (item.isCorrectToolForDrops(inventory.getItem(i), blockState) || !blockState.requiresCorrectToolForDrops()) {
                        if(!checkDurability(inventory.getItem(i))) continue;

                        newMiningSpeed = getMiningSpeed(inventory.getItem(i), blockState, blockHitResult.getBlockPos());
                    }

                    if (newMiningSpeed.equals(miningSpeed)) {
                        if (toolSlot != -1) {
                            if (AutoToolsConfig.PREFER_HOTBAR_TOOL) {
                                if (i <= 8 && (toolSlot > 8 || i == inventory.selected ||
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
                    } else if (newMiningSpeed.priority > miningSpeed.priority || (newMiningSpeed.miningSpeed > miningSpeed.miningSpeed && newMiningSpeed.priority >= miningSpeed.priority)) {
                        toolSlot = i;
                        miningSpeed = newMiningSpeed;
                    }
                }
            }

            if (toolSlot == -1 || ClientTags.isInWithLocalFallback(DO_NOT_SWAP_UNLESS_ENCH, blockState.getBlock()) && miningSpeed.priority == 0) {
            } else {
                selectItem(client, inventory, toolSlot);
            }
        } else if (AutoToolsConfig.CHANGE_FOR_ENTITIES && hit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hit).getEntity();

            int toolSlot = -1;
            float attackDamage = 0;

            if (AutoToolsConfig.KEEP_AXE && Arrays.asList(TOOL_LISTS.get("autotools:axe")).contains(BuiltInRegistries.ITEM.getKey(inventory.getSelected().getItem()))) {
                return;
            }

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                Item item = inventory.getItem(i).getItem();

                if (item != Items.AIR) {
                    double newAttackDamage = 1.0;


                    if (entity instanceof Boat || entity instanceof AbstractMinecart || entity instanceof LivingEntity) {
                        if (entity instanceof LivingEntity livingEntity) {
                            if (!item.hurtEnemy(inventory.getItem(i), livingEntity, inventory.player)) {
                                continue;
                            }
                        }

                        //Custom tool detection
                        if (CUSTOM_TOOLS.containsKey(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()))) {
                            List<ResourceLocation> tools = CUSTOM_TOOLS.get(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));

                            for (ResourceLocation resourceLocation : tools) {
                                if (Objects.equals(resourceLocation, new ResourceLocation("autotools", "disabled")))
                                    return;

                                toolSlot = AutoTools.findSlotMatchingItem(inventory, new ItemStack(BuiltInRegistries.ITEM.get(resourceLocation)));
                                if (toolSlot != -1) break;
                            }

                            if (toolSlot == -1) {
                            } else {
                                selectItem(client, inventory, toolSlot);
                                return;
                            }
                        }

                        if(!checkDurability(inventory.getItem(i))) continue;

                        float baseAttackDamage = 0;
                        float baseAttackSpeed = 0;
                        if (inventory.getItem(i).has(DataComponents.ATTRIBUTE_MODIFIERS)) {
                            for (ItemAttributeModifiers.Entry modifier : inventory.getItem(i).get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers()) {
                                if (modifier.attribute().is(Attributes.ATTACK_DAMAGE)) {
                                    baseAttackDamage = (float) modifier.modifier().amount();
                                    continue;
                                }
                                if (modifier.attribute().is(Attributes.ATTACK_SPEED)) {
                                    baseAttackSpeed = (float) modifier.modifier().amount();
                                }
                            }
                        }

                        if (baseAttackDamage > 0) {
                            if (inventory.getItem(i).isEnchanted()) {
                                if (inventory.getItem(i).isEnchanted()) {
                                    if (((EntityHitResult) hit).getEntity() instanceof LivingEntity livingEntity) {
                                        baseAttackDamage += EnchantmentHelper.getDamageBonus(inventory.getItem(i), livingEntity.getType());
                                    }
                                }
                            }

                            //Calculating DPS
                            if (baseAttackSpeed > 0) {
                                newAttackDamage = (1 + baseAttackDamage) * (4F + baseAttackSpeed);
                            } else {
                                newAttackDamage = 1 + baseAttackDamage;
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
            } else {
                selectItem(client, inventory, toolSlot);
            }

        }
    }
}
