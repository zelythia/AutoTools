package net.zelythia;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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

    public static final TagKey<Block> SHEARS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "shears"));
    public static final TagKey<Block> SILK_TOUCH = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "silk_touch"));
    public static final TagKey<Block> SILK_TOUCH_SETTING_ALWAYS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "silk_touch_setting_always"));
    public static final TagKey<Block> SILK_TOUCH_SETTING_ALWAYS_ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "silk_touch_setting_always_ores"));
    public static final TagKey<Block> SILK_TOUCH_SETTING_ALWAYS_EXC_ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "silk_touch_setting_always_exc_ores"));
    public static final TagKey<Block> FORTUNE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "fortune"));
    public static final TagKey<Block> FORTUNE_SETTING = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "fortune_setting"));
    public static final TagKey<Block> DO_NOT_SWAP_UNLESS_ENCH = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "do_not_swap_unless_ench"));

    public static final HashMap<ResourceLocation, List<ResourceLocation>> CUSTOM_TOOLS = new HashMap<>();
    private static final HashMap<String, ResourceLocation[]> TOOL_LISTS = new HashMap<>() {{
        put("autotools:pickaxe", new ResourceLocation[]{ResourceLocation.parse("minecraft:netherite_pickaxe"), ResourceLocation.parse("minecraft:diamond_pickaxe"), ResourceLocation.parse("minecraft:iron_pickaxe"), ResourceLocation.parse("minecraft:golden_pickaxe"), ResourceLocation.parse("minecraft:stone_pickaxe"), ResourceLocation.parse("minecraft:wooden_pickaxe")});
        put("autotools:shovel", new ResourceLocation[]{ResourceLocation.parse("minecraft:netherite_shovel"), ResourceLocation.parse("minecraft:diamond_shovel"), ResourceLocation.parse("minecraft:iron_shovel"), ResourceLocation.parse("minecraft:golden_shovel"), ResourceLocation.parse("minecraft:stone_shovel"), ResourceLocation.parse("minecraft:wooden_shovel")});
        put("autotools:hoe", new ResourceLocation[]{ResourceLocation.parse("minecraft:netherite_hoe"), ResourceLocation.parse("minecraft:diamond_hoe"), ResourceLocation.parse("minecraft:iron_hoe"), ResourceLocation.parse("minecraft:golden_hoe"), ResourceLocation.parse("minecraft:stone_hoe"), ResourceLocation.parse("minecraft:wooden_hoe")});
        put("autotools:sword", new ResourceLocation[]{ResourceLocation.parse("minecraft:netherite_sword"), ResourceLocation.parse("minecraft:diamond_sword"), ResourceLocation.parse("minecraft:iron_sword"), ResourceLocation.parse("minecraft:golden_sword"), ResourceLocation.parse("minecraft:stone_sword"), ResourceLocation.parse("minecraft:wooden_sword")});
        put("autotools:axe", new ResourceLocation[]{ResourceLocation.parse("minecraft:netherite_axe"), ResourceLocation.parse("minecraft:diamond_axe"), ResourceLocation.parse("minecraft:iron_axe"), ResourceLocation.parse("minecraft:golden_axe"), ResourceLocation.parse("minecraft:stone_axe"), ResourceLocation.parse("minecraft:wooden_axe")});
    }};

    public static final List<Integer> IGNORED_SLOTS = new ArrayList<>();
    public static final List<Integer> TARGET_SLOTS = new ArrayList<>();

    public static final Stack<Integer> swaps = new Stack<>();
    public static boolean toggle = true;
    public static BlockState lastBlock = null;
    /**
     * Used for SWITCH_BACK when toggle is disabled
     */
    public static boolean startedMining = false;


    /**
     * To be called by forge/fabric client-initialized methods
     */
    public static void init() {
        reloadConfig();
    }

    public static void reloadConfig() {
        AutoToolsConfig.load();

        //Not the best way of adding custom tools. Fine as long as it won't get any more
        CUSTOM_TOOLS.put(ResourceLocation.fromNamespaceAndPath("minecraft", "bamboo"), new ArrayList<>(Arrays.asList(TOOL_LISTS.get("autotools:sword"))));
        loadCustomItems();


        AutoTools.IGNORED_SLOTS.clear();
        for (String s : AutoToolsConfig.IGNORED_SLOTS.replaceAll("[\\[\\]]", "").split(",")) {
            if (s.isEmpty()) continue;
            try {
                int i = Integer.parseInt(s) - 1;
                if (i < 9) AutoTools.IGNORED_SLOTS.add(i);
                else LOGGER.error("Incorrect config entry for ignoredSlots: " + i + " must be between 1-9");
            } catch (NumberFormatException e) {
                LOGGER.error("Incorrect config entry for ignoredSlots: unknown number: " + s);
            }
        }

        AutoTools.TARGET_SLOTS.clear();
        for (String s : AutoToolsConfig.TARGET_SLOTS.replaceAll("[\\[\\]]", "").split(",")) {
            if (s.isEmpty()) continue;
            try {
                int i = Integer.parseInt(s) - 1;
                if (i < 9) AutoTools.TARGET_SLOTS.add(i);
                else LOGGER.error("Incorrect config entry for targetSlots: " + i + " must be between 1-9");
            } catch (NumberFormatException e) {
                LOGGER.error("Incorrect config entry for targetSlots: unknown number: " + s);
            }
        }
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

                        tools.add(ResourceLocation.parse(toolsArray.get(i).getAsString()));
                    }
                } else {
                    if (TOOL_LISTS.containsKey(jsonObject.get(key).getAsString())) {
                        tools.addAll(List.of(TOOL_LISTS.get(jsonObject.get(key).getAsString())));
                    } else tools.add(ResourceLocation.parse(jsonObject.get(key).getAsString()));
                }

                CUSTOM_TOOLS.computeIfAbsent(ResourceLocation.parse(key), k -> new ArrayList<>()).addAll(tools);
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
        if (swaps.empty()) {
            swaps.push(inventory.selected);
        }

        if (sourceSlot <= 8 && !AutoToolsConfig.KEEPSLOT) {
            if (swaps.get(swaps.size() - 1) != inventory.selected) {
                if (swaps.peek() != sourceSlot) swaps.push(inventory.selected);
            }
            inventory.selected = sourceSlot;

            return;
        }

        if(sourceSlot <= 8) sourceSlot += 36;   // Needs to be done because the hotbar slots are shifted by 36 in slot index

        int destSlot = AutoToolsConfig.KEEPSLOT ? inventory.selected : getSuitableHotbarSlot(inventory);
        if (!TARGET_SLOTS.contains(destSlot)) destSlot = TARGET_SLOTS.get(0);

        if (swaps.peek() != sourceSlot) swaps.push(sourceSlot);
        if (swaps.peek() != destSlot) swaps.push(destSlot);

        client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, sourceSlot, destSlot, ClickType.SWAP, client.player);

        inventory.selected = destSlot;
        inventory.setChanged();
    }

    /**
     * Mirroring Inventory.getSuitableHotbarSlot() with regards for TARGET_SLOTS
     */
    public static int getSuitableHotbarSlot(Inventory inventory) {
        int i;
        int j;
        for (i = 0; i < 9; ++i) {
            j = (inventory.selected + i) % 9;
            if (TARGET_SLOTS.contains(j) && inventory.items.get(j).isEmpty()) {
                return j;
            }
        }

        for (i = 0; i < 9; ++i) {
            j = (inventory.selected + i) % 9;
            if (TARGET_SLOTS.contains(j) && !inventory.items.get(j).isEnchanted()) {
                return j;
            }
        }

        return inventory.selected;
    }

    /**
     * Used for AutoToolsConfig.SWITCH_BACK to switch to the last tool the player was holding before using AutoTools
     */
    public static void switchBack() {
        //Don't switch if the player wants to mine another block || swaps.empty()
        if (Minecraft.getInstance().options.keyAttack.isDown() || swaps.empty()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameMode == null) return;

        Inventory inventory = client.player.getInventory();

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
    public static ItemMiningSpeed getMiningSpeed(ItemStack stack, BlockState blockState, BlockPos pos, Player player, Level level) {
        float modifier = 1F;
        int priority = 0;
        float miningSpeed = stack.getDestroySpeed(blockState);

        //Vanilla check for mining speed
        if (miningSpeed > 1.0F) {
            miningSpeed += (float) player.getAttributeValue(Attributes.MINING_EFFICIENCY);
        }
        if (MobEffectUtil.hasDigSpeed(player)) {
            miningSpeed *= 1.0F + (float) (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
        }
        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            float g;
            switch (player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0:
                    g = 0.3F;
                    break;
                case 1:
                    g = 0.09F;
                    break;
                case 2:
                    g = 0.0027F;
                    break;
                case 3:
                default:
                    g = 8.1E-4F;
            }

            miningSpeed *= g;
        }
        miningSpeed *= (float) player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
        if (player.isEyeInFluid(FluidTags.WATER)) {
            miningSpeed *= (float) player.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
        }


        HolderLookup.RegistryLookup<Enchantment> EnchantmentsLookup = level.registryAccess().lookup(Registries.ENCHANTMENT).get();
        if (stack.isEnchanted()) {

            //SilkTouch
            if (EnchantmentHelper.getItemEnchantmentLevel(EnchantmentsLookup.get(Enchantments.SILK_TOUCH).get(), stack) == 1) {
                if (ClientTags.isInWithLocalFallback(SILK_TOUCH, blockState.getBlock())
                        || AutoToolsConfig.PREFER_SILK_TOUCH.equals("always") && ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS, blockState.getBlock())
                        || AutoToolsConfig.PREFER_SILK_TOUCH.equals("except_ores") && ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_EXC_ORES, blockState.getBlock())
                        || AutoToolsConfig.PREFER_SILK_TOUCH.equals("always_ores") && ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_ORES, blockState.getBlock())) {
                    priority = 6;
                }
            }
            //Fortune
            else if (EnchantmentHelper.getItemEnchantmentLevel(EnchantmentsLookup.get(Enchantments.FORTUNE).get(), stack) >= 1) {
                if (ClientTags.isInWithLocalFallback(FORTUNE, blockState.getBlock())
                        || AutoToolsConfig.ALWAYS_PREFER_FORTUNE && ClientTags.isInWithLocalFallback(FORTUNE_SETTING, blockState.getBlock())) {
                    priority += EnchantmentHelper.getItemEnchantmentLevel(EnchantmentsLookup.get(Enchantments.FORTUNE).get(), stack);
                }
            }

            //Hoe check to make sure we prefer fortune hoes over other fortune tools when farming
            if (ClientTags.isInWithLocalFallback(FORTUNE, blockState.getBlock()) && ClientTags.isInWithLocalFallback(DO_NOT_SWAP_UNLESS_ENCH, blockState.getBlock()) && stack.getItem() instanceof HoeItem) {
                priority += 1;
            }
        }

        if (blockState.getDestroySpeed(null, pos) != 0 && miningSpeed > 1) {
            if (EnchantmentHelper.getItemEnchantmentLevel(EnchantmentsLookup.get(Enchantments.SILK_TOUCH).get(), stack) == 0 && !ClientTags.isInWithLocalFallback(SILK_TOUCH, blockState.getBlock())) {
                if ((ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_EXC_ORES, blockState.getBlock()) && !AutoToolsConfig.PREFER_SILK_TOUCH.equals("except_ores"))
                        || (ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS_ORES, blockState.getBlock()) && !AutoToolsConfig.PREFER_SILK_TOUCH.equals("always_ores"))
                        || (ClientTags.isInWithLocalFallback(SILK_TOUCH_SETTING_ALWAYS, blockState.getBlock()) && !AutoToolsConfig.PREFER_SILK_TOUCH.equals("always"))) {
                    priority += 1;
                }
            }

            if (EnchantmentHelper.getItemEnchantmentLevel(EnchantmentsLookup.get(Enchantments.FORTUNE).get(), stack) == 0 && !ClientTags.isInWithLocalFallback(FORTUNE, blockState.getBlock())
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


    public static void getCorrectTool(HitResult hit, Minecraft client) {
        Inventory inventory = client.player.getInventory();

        if (IGNORED_SLOTS.contains(inventory.selected)) return;

        ItemStack stack = inventory.getSelected();
        if(AutoToolsConfig.ENABLED.equals("tool") && !stack.getComponents().has(DataComponents.TOOL)) return;
        else if(AutoToolsConfig.ENABLED.equals("no_tool") && stack.getComponents().has(DataComponents.TOOL)) return;

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hit;
            BlockState blockState = client.level.getBlockState(blockHitResult.getBlockPos());

            int toolSlot = -1;
            ItemMiningSpeed miningSpeed = new ItemMiningSpeed(1f, 0);

            //Detection for custom tools
            if (CUSTOM_TOOLS.containsKey(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))) {
                List<ResourceLocation> tools = CUSTOM_TOOLS.get(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()));

                for (ResourceLocation resourceLocation : tools) {
                    if (Objects.equals(resourceLocation, ResourceLocation.fromNamespaceAndPath("autotools", "disabled")))
                        return;

                    Optional<Holder.Reference<Item>> itemReference = BuiltInRegistries.ITEM.get(resourceLocation);
                    if(itemReference.isPresent()) {
                        toolSlot = AutoTools.findSlotMatchingItem(inventory, new ItemStack(itemReference.get()));
                        if (toolSlot != -1) break;
                    }
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
                        if (AutoToolsConfig.MIN_DURABILITY < 1) {
                            double durability = (double) (inventory.getItem(i).getMaxDamage() - inventory.getItem(i).getDamageValue()) / inventory.getItem(i).getMaxDamage();
                            if (durability < AutoToolsConfig.MIN_DURABILITY)
                                continue;
                        } else if (inventory.getItem(i).getMaxDamage() - inventory.getItem(i).getDamageValue() <= AutoToolsConfig.MIN_DURABILITY)
                            continue;

                        newMiningSpeed = getMiningSpeed(inventory.getItem(i), blockState, blockHitResult.getBlockPos(), inventory.player, client.level);
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
                                if (Objects.equals(resourceLocation, ResourceLocation.fromNamespaceAndPath("autotools", "disabled")))
                                    return;

                                Optional<Holder.Reference<Item>> itemReference = BuiltInRegistries.ITEM.get(resourceLocation);
                                if(itemReference.isPresent()) {
                                    toolSlot = AutoTools.findSlotMatchingItem(inventory, new ItemStack(itemReference.get()));
                                    if (toolSlot != -1) break;
                                }
                            }

                            if (toolSlot == -1) {
                            } else {
                                selectItem(client, inventory, toolSlot);
                                return;
                            }
                        }

                        if (AutoToolsConfig.MIN_DURABILITY < 1) {
                            double durability = (double) (inventory.getItem(i).getMaxDamage() - inventory.getItem(i).getDamageValue()) / inventory.getItem(i).getMaxDamage();
                            if (durability < AutoToolsConfig.MIN_DURABILITY)
                                continue;
                        } else if (inventory.getItem(i).getMaxDamage() - inventory.getItem(i).getDamageValue() <= AutoToolsConfig.MIN_DURABILITY)
                            continue;

                        float baseAttackDamage = 0;
                        float baseAttackSpeed = 0;
                        if (inventory.getItem(i).has(DataComponents.ATTRIBUTE_MODIFIERS)) {
                            for (ItemAttributeModifiers.Entry modifier : inventory.getItem(i).get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers()) {
                                if (modifier.modifier().id().equals(ResourceLocation.parse("minecraft:base_attack_damage"))) {
                                    baseAttackDamage = (float) modifier.modifier().amount();
                                    continue;
                                }
                                if (modifier.modifier().id().equals(ResourceLocation.parse("minecraft:base_attack_speed"))) {
                                    baseAttackSpeed = (float) modifier.modifier().amount();
                                }
                            }
                        }

                        if (baseAttackDamage > 0) {
                            if (inventory.getItem(i).isEnchanted()) {
                                //We want to call this, but it requires a ServerLevel:
                                //EnchantmentHelper.modifyDamage(client.level, inventory.getItem(i), ((EntityHitResult) hit).getEntity(), client.level.damageSources().generic(), (float) baseAttackDamage);

                                DamageSource damageSource = client.level.damageSources().playerAttack(client.player);
                                LootParams lootParams = (new LootParams.Builder(null)).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ENCHANTMENT_LEVEL, i).withParameter(LootContextParams.ORIGIN, entity.position()).withParameter(LootContextParams.DAMAGE_SOURCE, damageSource).withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity()).withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity()).create(LootContextParamSets.ENCHANTED_DAMAGE);
                                LootContext lootContext = new LootContext(lootParams, RandomSource.create(), client.level.registryAccess());

                                ItemEnchantments itemEnchantments = inventory.getItem(i).getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                                for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                                    Enchantment enchantment = entry.getKey().value();
                                    List<ConditionalEffect<EnchantmentValueEffect>> effects = enchantment.getEffects(EnchantmentEffectComponents.DAMAGE);

                                    for (ConditionalEffect<EnchantmentValueEffect> effect : effects) {
                                        if (effect.matches(lootContext)) {
                                            baseAttackDamage = effect.effect().process(entry.getIntValue(), entity.getRandom(), baseAttackDamage);
                                        }
                                    }
                                }
                            }

                            //Calculating DPS
                            newAttackDamage = (1 + baseAttackDamage) * (4F + baseAttackSpeed);
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
