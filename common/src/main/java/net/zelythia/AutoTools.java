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
import net.minecraft.tags.ItemTags;
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
import net.zelythia.config.AutoToolsConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class AutoTools {
    public static final String MOD_ID = "autotools";
    public static final Logger LOGGER = LogManager.getLogger("AutoTools");

    public static final Set<ResourceLocation> SILK_TOUCH = new HashSet<>();
    public static final Set<ResourceLocation> SILK_TOUCH_SETTINGS_ALWAYS = new HashSet<>();
    public static final Set<ResourceLocation> SILK_TOUCH_SETTING_ALWAYS_ORES = new HashSet<>();
    public static final Set<ResourceLocation> SILK_TOUCH_SETTING_ALWAYS_EXC_ORES = new HashSet<>();
    public static final Set<ResourceLocation> FORTUNE = new HashSet<>();
    public static final Set<ResourceLocation> FORTUNE_SETTING = new HashSet<>();
    public static final Set<ResourceLocation> SHEARS = new HashSet<>();
    public static final Set<ResourceLocation> DO_NOT_SWAP_UNLESS_ENCH = new HashSet<>();

    public static final HashMap<ResourceLocation, List<ResourceLocation>> CUSTOM_TOOLS = new HashMap<>();

    public static final Stack<Integer> swaps = new Stack<>();
    public static boolean toggle = true;

    //Used for SWITCH_BACK when toggle is disabled
    public static boolean startedMining = false;

    // Used for the experimental swap delay
    public static boolean swapped = false;


    /**
     * To be called by forge/fabric client-initialized methods
     */
    public static void init() {
        reloadConfig();
    }

    public static void reloadConfig() {
        AutoToolsConfig.BlockLists lists = AutoToolsConfig.blockLists();

        createLists(lists.silktouch, TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "shears")), SILK_TOUCH);
        createLists(lists.silktouch_setting_always, TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "silk_touch_setting_always")), SILK_TOUCH_SETTINGS_ALWAYS);
        createLists(lists.silktouch_setting_always_ores, TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "silk_touch_setting_always_ores")), SILK_TOUCH_SETTING_ALWAYS_ORES);
        createLists(lists.silktouch_setting_exc_ores, TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "silk_touch_setting_always_exc_ores")), SILK_TOUCH_SETTING_ALWAYS_EXC_ORES);

        createLists(lists.fortune, TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "fortune")), FORTUNE);
        createLists(lists.fortune_setting, TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "fortune_setting")), FORTUNE_SETTING);

        createLists(lists.shears, TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "shears")), SHEARS);
        createLists(lists.do_not_swap_unless_ench, TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MOD_ID, "do_not_swap_unless_ench")), DO_NOT_SWAP_UNLESS_ENCH);


        loadCustomItems();
        //Not the best way of adding custom tools. Fine as long as it won't get any more
        CUSTOM_TOOLS.computeIfAbsent(ResourceLocation.fromNamespaceAndPath("minecraft", "bamboo"), k -> new ArrayList<>()).addAll(ClientTags.getOrCreateLocalTag(ItemTags.SWORDS));
    }

    private static void createLists(List<String> input, TagKey<Block> tag, Set<ResourceLocation> output) {
        output.clear();

        for (String identifier : input) {
            //Tags
            if(identifier.startsWith("#")){
                ResourceLocation resourceLocation = ResourceLocation.tryParse(identifier);
                if(resourceLocation != null){
                    output.addAll(ClientTags.getOrCreateLocalTag(TagKey.create(Registries.BLOCK, resourceLocation)));
                }
                continue;
            }

            ResourceLocation resourceLocation = ResourceLocation.tryParse(identifier);
            if(resourceLocation != null){
                output.add(resourceLocation);
            }
        }

        if(AutoToolsConfig.blockLists().enable_datapacks){
            output.addAll(ClientTags.getOrCreateLocalTag(tag));
        }
    }

    private static void loadCustomItems() {
        CUSTOM_TOOLS.clear();

        try {
            JsonElement jsonElement = JsonParser.parseString("{" + AutoToolsConfig.blockLists().customTools.stream().reduce((s, s2) -> s + ", " + s2).orElse("") + "}");
            if (!jsonElement.isJsonObject()) return;
            JsonObject jsonObject = (JsonObject) jsonElement;

            for (String key : jsonObject.keySet()) {

                ArrayList<ResourceLocation> tools = new ArrayList<>();
                if (jsonObject.get(key).isJsonArray()) {
                    JsonArray toolsArray = jsonObject.getAsJsonArray(key);

                    for (int i = 0; i < toolsArray.size(); i++) {
                        String tool = toolsArray.get(i).getAsString();

                        //Tag
                        if(tool.startsWith("#")){
                            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, ResourceLocation.parse(tool.substring(1)));
                            Set<ResourceLocation> tag = ClientTags.getOrCreateLocalTag(tagKey);

                            tools.addAll(tag);
                            continue;
                        }

                        tools.add(ResourceLocation.parse(tool));
                    }
                } else {
                    String tool = jsonObject.get(key).getAsString();

                    //Tag
                    if(tool.startsWith("#")){
                        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, ResourceLocation.parse(tool.substring(1)));
                        Set<ResourceLocation> tag = ClientTags.getOrCreateLocalTag(tagKey);

                        tools.addAll(tag);
                    }
                    else tools.add(ResourceLocation.parse(tool));
                }

                if(key.startsWith("#")){
                    TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK, ResourceLocation.parse(key.substring(1)));
                    Set<ResourceLocation> tag = ClientTags.getOrCreateLocalTag(blockTagKey);
                    tag.forEach(resourceLocation -> CUSTOM_TOOLS.computeIfAbsent(resourceLocation, k -> new ArrayList<>()).addAll(tools));
                }
                else CUSTOM_TOOLS.computeIfAbsent(ResourceLocation.parse(key), k -> new ArrayList<>()).addAll(tools);
            }

            LOGGER.info("Loaded custom block configs: " + CUSTOM_TOOLS.keySet());
        } catch (Exception e) {
            LOGGER.error("Error while parsing custom blocks", e);
        }
    }

    public static void onBlockBreaking(Minecraft client, HitResult hitResult) {
        if (AutoToolsConfig.get().toggle && AutoTools.toggle) {
            if (client.player.isCreative()) {
                if (!AutoToolsConfig.get().disableCreative) {
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
        if(sourceSlot == inventory.getSelectedSlot()) return;

        if (swaps.empty()) {
            swaps.push(inventory.getSelectedSlot());
        }

        if (sourceSlot <= 8 && !AutoToolsConfig.get().keepSlot) {
            if (swaps.getLast() != inventory.getSelectedSlot()) {
                if (swaps.peek() != sourceSlot) swaps.push(inventory.getSelectedSlot());
            }
            inventory.setSelectedSlot(sourceSlot);

            return;
        }

        if(sourceSlot <= 8) sourceSlot += 36;   // Needs to be done because the hotbar slots are shifted by 36 in slot index

        int destSlot = AutoToolsConfig.get().keepSlot ? inventory.getSelectedSlot() : getSuitableHotbarSlot(inventory);
        if (!AutoToolsConfig.get().targetSlots.contains(destSlot + 1)) destSlot = AutoToolsConfig.get().targetSlots.getFirst() - 1;

        if (swaps.peek() != sourceSlot) swaps.push(sourceSlot);
        if (swaps.peek() != destSlot) swaps.push(destSlot);

        swapped = true;
        client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, sourceSlot, destSlot, ClickType.SWAP, client.player);

        inventory.setSelectedSlot(destSlot);
        inventory.setChanged();

        if(!AutoToolsConfig.get().switchBack) swaps.clear(); //Easy way to safe some memory because swaps are only needed for switchBack
    }

    /**
     * Mirroring Inventory.getSuitableHotbarSlot() with regards for TARGET_SLOTS
     */
    public static int getSuitableHotbarSlot(Inventory inventory) {
        int i;
        int j;
        for (i = 0; i < 9; ++i) {
            j = (inventory.getSelectedSlot() + i) % 9;
            if (AutoToolsConfig.get().targetSlots.contains(j + 1) && inventory.getItem(j).isEmpty()) {
                return j;
            }
        }

        for (i = 0; i < 9; ++i) {
            j = (inventory.getSelectedSlot() + i) % 9;
            if (AutoToolsConfig.get().targetSlots.contains(j + 1) && !inventory.getItem(j).isEnchanted()) {
                return j;
            }
        }

        return inventory.getSelectedSlot();
    }

    /**
     * Used for AutoToolsConfig.get().SWITCH_BACK to switch to the last tool the player was holding before using AutoTools
     */
    public static void switchBack() {
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
                if (AutoToolsConfig.get().keepSlot && i != inventory.getSelectedSlot()) {
                    client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, inventory.getSelectedSlot(), i, ClickType.SWAP, client.player);
                    return;
                }

                inventory.setSelectedSlot(i);
                return;
            }

            client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, i, inventory.getSelectedSlot(), ClickType.SWAP, client.player);
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
        if (player.hasEffect(MobEffects.MINING_FATIGUE)) {
            float g;
            switch (player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
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


        if(level.registryAccess().lookup(Registries.ENCHANTMENT).isPresent()){
            HolderLookup.RegistryLookup<Enchantment> EnchantmentsLookup = level.registryAccess().lookup(Registries.ENCHANTMENT).get();

            Optional<Holder.Reference<Enchantment>> Enchantment_SilkTouch = EnchantmentsLookup.get(Enchantments.SILK_TOUCH);
            Optional<Holder.Reference<Enchantment>> Enchantment_Fortune = EnchantmentsLookup.get(Enchantments.FORTUNE);

            if (stack.isEnchanted()) {
                //SilkTouch
                if (Enchantment_SilkTouch.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(Enchantment_SilkTouch.get(), stack) == 1) {

                    if (SILK_TOUCH.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))
                            || AutoToolsConfig.get().preferSilkTouch == AutoToolsConfig.PreferSilkTouch.always && SILK_TOUCH_SETTINGS_ALWAYS.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))
                            || AutoToolsConfig.get().preferSilkTouch == AutoToolsConfig.PreferSilkTouch.except_ores && SILK_TOUCH_SETTING_ALWAYS_EXC_ORES.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))
                            || AutoToolsConfig.get().preferSilkTouch == AutoToolsConfig.PreferSilkTouch.ores && SILK_TOUCH_SETTING_ALWAYS_ORES.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))) {
                        priority = 6;
                    }
                }
                //Fortune
                else if (Enchantment_Fortune.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(Enchantment_Fortune.get(), stack) >= 1) {
                    if (FORTUNE.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))
                            || AutoToolsConfig.get().alwaysPreferFortune && FORTUNE_SETTING.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))) {
                        priority += EnchantmentHelper.getItemEnchantmentLevel(Enchantment_Fortune.get(), stack);
                    }
                    if(AutoToolsConfig.get().alwaysPreferFortune && FORTUNE_SETTING.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))){
                        priority += 6;
                    }
                }

                //Prefer fortune hoes over other fortune tools when farming
                if (FORTUNE.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())) && DO_NOT_SWAP_UNLESS_ENCH.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())) && ClientTags.isInWithLocalFallback(ItemTags.HOES, stack.getItem())) {
                    priority += 1;
                }
            }

            //Prioritize non-enchanted items based on settings
            if (blockState.getDestroySpeed(level, pos) != 0 && miningSpeed > 1) {
                if (Enchantment_SilkTouch.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(Enchantment_SilkTouch.get(), stack) == 0 && !SILK_TOUCH.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))) {
                    if ((SILK_TOUCH_SETTING_ALWAYS_EXC_ORES.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())) && AutoToolsConfig.get().preferSilkTouch != AutoToolsConfig.PreferSilkTouch.except_ores)
                            || (SILK_TOUCH_SETTING_ALWAYS_ORES.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())) && AutoToolsConfig.get().preferSilkTouch != AutoToolsConfig.PreferSilkTouch.ores)
                            || (SILK_TOUCH_SETTINGS_ALWAYS.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())) && AutoToolsConfig.get().preferSilkTouch != AutoToolsConfig.PreferSilkTouch.always)) {
                        priority += 1;
                    }
                }

                if (Enchantment_Fortune.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(Enchantment_Fortune.get(), stack) == 0 && !FORTUNE.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))
                        && FORTUNE_SETTING.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())) && !AutoToolsConfig.get().alwaysPreferFortune) {
                    priority += 1;
                }
            }
        }

        if (stack.is(Items.SHEARS) && SHEARS.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))) {
            priority += 6;
        }

        return new ItemMiningSpeed(miningSpeed * modifier, priority);
    }

    /**
     * Custom {@link Inventory#findSlotMatchingItem(ItemStack)} method that ignored ItemTags
     */
    public static int findSlotMatchingItem(Inventory inventory, ItemStack itemStack) {

        for (int i = 0; i < inventory.getContainerSize() - Inventory.EQUIPMENT_SLOT_MAPPING.size(); ++i) {
            if (ItemStack.isSameItem(itemStack, inventory.getItem(i))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @return If the ItemStack should be considered a valid tool based on the MIN_DURABILITY config option
     */
    public static boolean checkDurability(ItemStack stack){
        if (AutoToolsConfig.get().minDurability < 1) {
            double durability = (double) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
            if (durability < AutoToolsConfig.get().minDurability)
                return false;
        } else if (stack.getMaxDamage() - stack.getDamageValue() <= AutoToolsConfig.get().minDurability)
            return false;

        return true;
    }

    public static void getCorrectTool(HitResult hit, Minecraft client) {
        Inventory inventory = client.player.getInventory();

        if (AutoToolsConfig.get().ignoredSlots.contains(inventory.getSelectedSlot() + 1)) return;

        ItemStack stack = inventory.getSelectedItem();
        if(AutoToolsConfig.get().enabled == AutoToolsConfig.Enabled.tool && !stack.getComponents().has(DataComponents.TOOL)) return;
        else if(AutoToolsConfig.get().enabled == AutoToolsConfig.Enabled.no_tool && stack.getComponents().has(DataComponents.TOOL)) return;

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
            if (!AutoToolsConfig.get().toggle && blockState.getBlock() == Blocks.END_PORTAL_FRAME) {
                toolSlot = AutoTools.findSlotMatchingItem(inventory, new ItemStack(Items.ENDER_EYE));

                if (toolSlot == -1) {
                } else if (toolSlot <= 8) {
                    inventory.setSelectedSlot(toolSlot);
                    return;
                } else {
                    selectItem(client, inventory, toolSlot);
                    return;
                }
            }

            //Disabling tool switching on instant mine-able blocks unless it drops more with fortune
            //Calling with blockGetter == null because none of the parameters are being (Might clash with mixins)
            if (blockState.getDestroySpeed(null, blockHitResult.getBlockPos()) == 0 && !DO_NOT_SWAP_UNLESS_ENCH.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))) {
                return;
            }

            if (AutoToolsConfig.get().onlySwitchIfNecessary) {
                if (inventory.getItem(inventory.getSelectedSlot()).getItem().isCorrectToolForDrops(inventory.getItem(inventory.getSelectedSlot()), blockState)
                        || !blockState.requiresCorrectToolForDrops()) return;
            }

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                Item item = inventory.getItem(i).getItem();

                if (item != Items.AIR) {
                    ItemMiningSpeed newMiningSpeed = new ItemMiningSpeed(1f, 0);

                    if (item.isCorrectToolForDrops(inventory.getItem(i), blockState) || !blockState.requiresCorrectToolForDrops()) {
                        if(!checkDurability(inventory.getItem(i))) continue;

                        newMiningSpeed = getMiningSpeed(inventory.getItem(i), blockState, blockHitResult.getBlockPos(), inventory.player, client.level);
                    }

                    if (newMiningSpeed.equals(miningSpeed)) {
                        if (toolSlot != -1) {
                            if (AutoToolsConfig.get().preferHotbarTool) {
                                if (i <= 8 && (toolSlot > 8 || i == inventory.getSelectedSlot() ||
                                        ((AutoToolsConfig.get().preferLowDurability && inventory.getItem(i).getDamageValue() > inventory.getItem(toolSlot).getDamageValue())
                                                || (!AutoToolsConfig.get().preferLowDurability && inventory.getItem(i).getDamageValue() < inventory.getItem(toolSlot).getDamageValue())))
                                ) {
                                    toolSlot = i;
                                    miningSpeed = newMiningSpeed;
                                }
                            } else if ((AutoToolsConfig.get().preferLowDurability && inventory.getItem(i).getDamageValue() > inventory.getItem(toolSlot).getDamageValue())
                                    || (!AutoToolsConfig.get().preferLowDurability && inventory.getItem(i).getDamageValue() < inventory.getItem(toolSlot).getDamageValue())) {
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

            if (toolSlot == -1 || DO_NOT_SWAP_UNLESS_ENCH.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())) && miningSpeed.priority == 0) {
            } else {
                selectItem(client, inventory, toolSlot);
            }
        } else if (AutoToolsConfig.get().changeForEntities && hit.getType() == HitResult.Type.ENTITY) {
            if (AutoToolsConfig.get().switchBack) return; //SwitchBack doesn't really make sense for mobs

            Entity entity = ((EntityHitResult) hit).getEntity();

            int toolSlot = -1;
            float attackDamage = 0;

            if (AutoToolsConfig.get().keepAxe && ClientTags.isInWithLocalFallback(ItemTags.AXES, inventory.getSelectedItem().getItem())) {
                return;
            }

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                Item item = inventory.getItem(i).getItem();

                if (item != Items.AIR) {
                    double newAttackDamage = 1.0;

                    if (entity instanceof Boat || entity instanceof AbstractMinecart || entity instanceof LivingEntity) {

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

                        if(!checkDurability(inventory.getItem(i))) continue;

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
                if (!AutoToolsConfig.get().toggle && client.player.isCreative()) {
                    inventory.setItem(inventory.getSuitableHotbarSlot(), new ItemStack(Items.NETHERITE_SWORD));
                }
            } else {
                selectItem(client, inventory, toolSlot);
            }

        }
    }
}
