package net.zelythia.autotools;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.zelythia.autotools.clientTags.ClientTags;
import net.zelythia.autotools.config.AutoToolsConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class AutoTools {
    public static final String MOD_ID = "autotools";
    public static final Logger LOGGER = LogManager.getLogger("AutoTools");

    public static final Set<ResourceLocation> SILK_TOUCH = new HashSet<>();
    public static final Set<ResourceLocation> SILK_TOUCH_SETTING_ALWAYS = new HashSet<>();
    public static final Set<ResourceLocation> SILK_TOUCH_SETTING_ALWAYS_ORES = new HashSet<>();
    public static final Set<ResourceLocation> SILK_TOUCH_SETTING_ALWAYS_EXC_ORES = new HashSet<>();
    public static final Set<ResourceLocation> FORTUNE = new HashSet<>();
    public static final Set<ResourceLocation> FORTUNE_SETTING = new HashSet<>();
    public static final Set<ResourceLocation> SHEARS = new HashSet<>();
    public static final Set<ResourceLocation> DO_NOT_SWAP_UNLESS_ENCH = new HashSet<>();


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

    //Used for SWITCH_BACK when toggle is disable
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

        createLists(lists.silktouch, TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "shears")), SILK_TOUCH);
        createLists(lists.silktouch_setting_always, TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "silk_touch_setting_always")), SILK_TOUCH_SETTING_ALWAYS);
        createLists(lists.silktouch_setting_always_ores, TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "silk_touch_setting_always_ores")), SILK_TOUCH_SETTING_ALWAYS_ORES);
        createLists(lists.silktouch_setting_exc_ores, TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "silk_touch_setting_always_exc_ores")), SILK_TOUCH_SETTING_ALWAYS_EXC_ORES);

        createLists(lists.fortune, TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "fortune")), FORTUNE);
        createLists(lists.fortune_setting, TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "fortune_setting")), FORTUNE_SETTING);

        createLists(lists.shears, TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "shears")), SHEARS);
        createLists(lists.do_not_swap_unless_ench, TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "do_not_swap_unless_ench")), DO_NOT_SWAP_UNLESS_ENCH);


        loadCustomItems();
        //Not the best way of adding custom tools. Fine as long as it won't get any more
        CUSTOM_TOOLS.computeIfAbsent(new ResourceLocation("minecraft", "bamboo"), k -> new ArrayList<>()).addAll(new ArrayList<>(Arrays.asList(TOOL_LISTS.get("autotools:sword"))));
    }

    private static void createLists(List<String> input, TagKey<Block> tag, Set<ResourceLocation> output) {
        output.clear();

        for (String identifier : input) {
            //Tags
            if(identifier.startsWith("#")){
                ResourceLocation resourceLocation = ResourceLocation.tryParse(identifier.substring(1));
                if(resourceLocation != null){
                    output.addAll(ClientTags.getOrCreateLocalTag(TagKey.create(Registry.BLOCK_REGISTRY, resourceLocation)));
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
                            TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(tool.substring(1)));
                            Set<ResourceLocation> tag = ClientTags.getOrCreateLocalTag(tagKey);

                            tools.addAll(tag);
                            continue;
                        }

                        tools.add(new ResourceLocation(tool));
                    }
                } else {
                    String tool = jsonObject.get(key).getAsString();

                    //Tag
                    if(tool.startsWith("#")){
                        TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(tool.substring(1)));
                        Set<ResourceLocation> tag = ClientTags.getOrCreateLocalTag(tagKey);

                        tools.addAll(tag);
                    }
                    else tools.add(new ResourceLocation(tool));
                }

                if(key.startsWith("#")){
                    TagKey<Block> blockTagKey = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(key.substring(1)));
                    Set<ResourceLocation> tag = ClientTags.getOrCreateLocalTag(blockTagKey);
                    tag.forEach(resourceLocation -> CUSTOM_TOOLS.computeIfAbsent(resourceLocation, k -> new ArrayList<>()).addAll(tools));
                }
                else CUSTOM_TOOLS.computeIfAbsent(new ResourceLocation(key), k -> new ArrayList<>()).addAll(tools);
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
        if(sourceSlot == inventory.selected) return;

        if (swaps.empty()) {
            swaps.push(inventory.selected);
        }

        if (sourceSlot <= 8 && !AutoToolsConfig.get().keepSlot) {
            if (swaps.get(swaps.size() - 1) != inventory.selected) {
                if(swaps.peek() != sourceSlot) swaps.push(inventory.selected);
            }
            inventory.selected = sourceSlot;

            if(!AutoToolsConfig.get().switchBack) swaps.clear();
            return;
        }

        sourceSlot = toGuiSlotIndex(sourceSlot);

        int destSlot = AutoToolsConfig.get().keepSlot ? inventory.selected : getSuitableHotbarSlot(inventory);
        if (!AutoToolsConfig.get().targetSlots.contains(destSlot + 1)) destSlot = AutoToolsConfig.get().targetSlots.get(0) - 1;


        if (swaps.peek() != sourceSlot) swaps.push(sourceSlot);
        if (swaps.peek() != destSlot) swaps.push(destSlot);

        swapped = true;
        client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, sourceSlot, destSlot, ClickType.SWAP, client.player);

        inventory.selected = destSlot;
        inventory.setChanged();

        if(!AutoToolsConfig.get().switchBack) swaps.clear(); //Easy way to safe some memory because swaps are only needed for switchBack
    }

    public static int toGuiSlotIndex(int inventoryIndex){
        if(inventoryIndex <= 8) return inventoryIndex + 36;                             // Hotbar
        if(inventoryIndex >= 36 && inventoryIndex <= 39) return 44 - inventoryIndex;    // Armor
        if(inventoryIndex == 40) return 45;                                             // Offhand
        return inventoryIndex;                                                          // Inventory
    }

    /**
     * Mirroring Inventory.getSuitableHotbarSlot() with regards for TARGET_SLOTS
     */
    public static int getSuitableHotbarSlot(Inventory inventory) {
        int i;
        int j;
        for (i = 0; i < 9; ++i) {
            j = (inventory.selected + i) % 9;
            if (AutoToolsConfig.get().targetSlots.contains(j + 1) && inventory.items.get(j).isEmpty()) {
                return j;
            }
        }

        for (i = 0; i < 9; ++i) {
            j = (inventory.selected + i) % 9;
            if (AutoToolsConfig.get().targetSlots.contains(j + 1) && !inventory.items.get(j).isEnchanted()) {
                return j;
            }
        }

        return inventory.selected;
    }

    /**
     * Used for AutoToolsConfig.get().SWITCH_BACK to switch to the last tool the player was holding before using AutoTools
     */
    public static void switchBack() {
        if (!AutoToolsConfig.get().switchBack) return;    //Shouldn't be necessary, but just in case

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
                if (AutoToolsConfig.get().keepSlot && i != inventory.selected) {
                    client.gameMode.handleInventoryMouseClick(client.player.inventoryMenu.containerId, inventory.selected + 36, i, ClickType.SWAP, client.player);
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
            if (blockState.getDestroySpeed(null, pos) != 0 && stack.isCorrectToolForDrops(blockState)) {
                modifier += (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack) * 20F) / 100F;
            }

            //SilkTouch
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 1) {
                if (SILK_TOUCH.contains(Registry.BLOCK.getKey(blockState.getBlock()))
                        || AutoToolsConfig.get().preferSilkTouch == AutoToolsConfig.PreferSilkTouch.always && SILK_TOUCH_SETTING_ALWAYS.contains(Registry.BLOCK.getKey(blockState.getBlock()))
                        || AutoToolsConfig.get().preferSilkTouch == AutoToolsConfig.PreferSilkTouch.except_ores && SILK_TOUCH_SETTING_ALWAYS_EXC_ORES.contains(Registry.BLOCK.getKey(blockState.getBlock()))
                        || AutoToolsConfig.get().preferSilkTouch == AutoToolsConfig.PreferSilkTouch.ores && SILK_TOUCH_SETTING_ALWAYS_ORES.contains(Registry.BLOCK.getKey(blockState.getBlock()))) {
                    priority = 6;
                }
            }
            //Fortune
            else if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack) >= 1) {
                if (FORTUNE.contains(Registry.BLOCK.getKey(blockState.getBlock()))
                        || AutoToolsConfig.get().alwaysPreferFortune && FORTUNE_SETTING.contains(Registry.BLOCK.getKey(blockState.getBlock()))) {
                    priority += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack);
                }
                if(AutoToolsConfig.get().alwaysPreferFortune && FORTUNE_SETTING.contains(Registry.BLOCK.getKey(blockState.getBlock()))){
                    priority += 6;
                }
            }

            //Hoe check to make sure we prefer fortune hoes over other fortune tools when farming
            if (FORTUNE.contains(Registry.BLOCK.getKey(blockState.getBlock())) && DO_NOT_SWAP_UNLESS_ENCH.contains(Registry.BLOCK.getKey(blockState.getBlock())) && stack.getItem() instanceof HoeItem) {
                priority += 1;
            }
        }

        if (blockState.getDestroySpeed(null, pos) != 0 && miningSpeed > 1) {
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0 && !SILK_TOUCH.contains(Registry.BLOCK.getKey(blockState.getBlock()))) {
                if ((SILK_TOUCH_SETTING_ALWAYS_EXC_ORES.contains(Registry.BLOCK.getKey(blockState.getBlock())) && AutoToolsConfig.get().preferSilkTouch != AutoToolsConfig.PreferSilkTouch.except_ores)
                        || (SILK_TOUCH_SETTING_ALWAYS_ORES.contains(Registry.BLOCK.getKey(blockState.getBlock())) && AutoToolsConfig.get().preferSilkTouch != AutoToolsConfig.PreferSilkTouch.ores)
                        || (SILK_TOUCH_SETTING_ALWAYS.contains(Registry.BLOCK.getKey(blockState.getBlock())) && AutoToolsConfig.get().preferSilkTouch != AutoToolsConfig.PreferSilkTouch.always)) {
                    priority += 1;
                }
            }

            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack) == 0 && !FORTUNE.contains(Registry.BLOCK.getKey(blockState.getBlock()))
                    && FORTUNE_SETTING.contains(Registry.BLOCK.getKey(blockState.getBlock())) && !AutoToolsConfig.get().alwaysPreferFortune) {
                priority += 1;
            }
        }

        if (stack.sameItem(new ItemStack(Items.SHEARS)) && SHEARS.contains(Registry.BLOCK.getKey(blockState.getBlock()))) {
            priority += 6;
        }

        return new ItemMiningSpeed(miningSpeed * modifier, priority);
    }

    /**
     * Custom {@link Inventory#findSlotMatchingItem(ItemStack)} method that ignored ItemTags
     */
    public static int findSlotMatchingItem(Inventory inventory, ItemStack itemStack) {
        for (int i = 0; i < inventory.items.size(); ++i) {
            if (ItemStack.isSameIgnoreDurability(itemStack, inventory.items.get(i))) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @return If the ItemStack should be considered a valid tool based on the minDurability config option
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

        if (AutoToolsConfig.get().ignoredSlots.contains(inventory.selected + 1)) return;

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hit;
            BlockState blockState = client.level.getBlockState(blockHitResult.getBlockPos());

            if(AutoToolsConfig.get().enabled == AutoToolsConfig.Enabled.tool && !(getMiningSpeed(inventory.getSelected(), blockState, blockHitResult.getBlockPos()).miningSpeed > 1 || inventory.getSelected().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE))) return;
            else if(AutoToolsConfig.get().enabled == AutoToolsConfig.Enabled.no_tool && (getMiningSpeed(inventory.getSelected(), blockState, blockHitResult.getBlockPos()).miningSpeed > 1 || inventory.getSelected().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE))) return;

            int toolSlot = -1;
            ItemMiningSpeed miningSpeed = new ItemMiningSpeed(getMiningSpeed(ItemStack.EMPTY, blockState, blockHitResult.getBlockPos()).miningSpeed, 0);

            //Detection for custom tools
            if (CUSTOM_TOOLS.containsKey(Registry.BLOCK.getKey(blockState.getBlock()))) {
                List<ResourceLocation> tools = CUSTOM_TOOLS.get(Registry.BLOCK.getKey(blockState.getBlock()));

                for (ResourceLocation resourceLocation : tools) {
                    if (Objects.equals(resourceLocation, new ResourceLocation("autotools", "disabled"))) return;

                    toolSlot = AutoTools.findSlotMatchingItem(inventory, new ItemStack(Registry.ITEM.get(resourceLocation)));
                    if (toolSlot != -1 && !AutoToolsConfig.get().ignoredInventorySlots.contains(toolSlot)) break;
                }

                if (toolSlot == -1 || AutoToolsConfig.get().ignoredInventorySlots.contains(toolSlot)) {
                } else {
                    selectItem(client, inventory, toolSlot);
                    return;
                }
            }

            //End portal-Frame detection
            if (!AutoToolsConfig.get().toggle && blockState.getBlock() == Blocks.END_PORTAL_FRAME) {
                toolSlot = AutoTools.findSlotMatchingItem(inventory, new ItemStack(Items.ENDER_EYE));

                if (toolSlot == -1 || AutoToolsConfig.get().ignoredInventorySlots.contains(toolSlot)) {
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
            if (blockState.getDestroySpeed(null, blockHitResult.getBlockPos()) == 0 && !DO_NOT_SWAP_UNLESS_ENCH.contains(Registry.BLOCK.getKey(blockState.getBlock()))) {
                return;
            }

            if (AutoToolsConfig.get().onlySwitchIfNecessary) {
                if (inventory.getItem(inventory.selected).getItem().isCorrectToolForDrops(blockState)
                        || !blockState.requiresCorrectToolForDrops()) return;
            }

            int containerLimit = AutoToolsConfig.get().hotbarOnly? 9: inventory.getContainerSize();
            for (int i = 0; i < containerLimit; i++) {
                if(AutoToolsConfig.get().ignoredInventorySlots.contains(i)) continue;
                Item item = inventory.getItem(i).getItem();

                if (item != Items.AIR) {
                    ItemMiningSpeed newMiningSpeed = new ItemMiningSpeed(getMiningSpeed(ItemStack.EMPTY, blockState, blockHitResult.getBlockPos()).miningSpeed, 0);

                    if (item.isCorrectToolForDrops(blockState) || !blockState.requiresCorrectToolForDrops()) {
                        if(!checkDurability(inventory.getItem(i))) continue;

                        newMiningSpeed = getMiningSpeed(inventory.getItem(i), blockState, blockHitResult.getBlockPos());
                    }

                    if (newMiningSpeed.equals(miningSpeed)) {
                        if (toolSlot != -1) {
                            if (AutoToolsConfig.get().preferHotbarTool) {
                                if (i <= 8 && (toolSlot > 8 || i == inventory.selected ||
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

            if(DO_NOT_SWAP_UNLESS_ENCH.contains(Registry.BLOCK.getKey(blockState.getBlock())) && miningSpeed.priority == 0){
                return;
            }
            else if(toolSlot == -1) {
                // If we haven't found a valid tool, select an item with no durability
                if(AutoToolsConfig.get().preserveDurability) {
                    if(inventory.getSelected().getMaxDamage() == 0) return;


                    if(AutoToolsConfig.get().switchBack && !swaps.empty()){
                        int firstItem = swaps.get(0);
                        if(inventory.getItem(firstItem).getMaxDamage() == 0){
                            selectItem(client, inventory, firstItem);
                            return;
                        }
                    }

                    // Prefer empty hand over other items in hotbar
                    for (int i = 0; i < 9; i++) {
                        if(inventory.getItem(i).isEmpty()){
                            selectItem(client, inventory, i);
                            return;
                        }
                    }

                    int containerLimit1 = AutoToolsConfig.get().hotbarOnly? 9: inventory.getContainerSize();
                    for (int i = 0; i < containerLimit1; i++) {
                        if(inventory.getItem(i).getMaxDamage() == 0){
                            selectItem(client, inventory, i);
                            return;
                        }
                    }

                }
            } else {
                selectItem(client, inventory, toolSlot);
            }
        } else if (AutoToolsConfig.get().changeForEntities && hit.getType() == HitResult.Type.ENTITY) {
            if (AutoToolsConfig.get().switchBack) return;

            if(AutoToolsConfig.get().enabled == AutoToolsConfig.Enabled.tool && !(inventory.getSelected().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE))) return;
            else if(AutoToolsConfig.get().enabled == AutoToolsConfig.Enabled.no_tool && (inventory.getSelected().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE))) return;


            if (AutoToolsConfig.get().keepAxe && Arrays.asList(TOOL_LISTS.get("autotools:axe")).contains(Registry.ITEM.getKey(inventory.getSelected().getItem()))) {
                return;
            }

            Entity entity = ((EntityHitResult) hit).getEntity();

            if (entity instanceof Boat || entity instanceof AbstractMinecart || entity instanceof LivingEntity) {
                int toolSlot = -1;
                float attackDamage = 0;

                //Custom tool detection
                if (CUSTOM_TOOLS.containsKey(Registry.ENTITY_TYPE.getKey(entity.getType()))) {
                    List<ResourceLocation> tools = CUSTOM_TOOLS.get(Registry.ENTITY_TYPE.getKey(entity.getType()));

                    for (ResourceLocation resourceLocation : tools) {
                        if (Objects.equals(resourceLocation, new ResourceLocation("autotools", "disabled")))
                            return;

                        toolSlot = AutoTools.findSlotMatchingItem(inventory, new ItemStack(Registry.ITEM.get(resourceLocation)));
                        if (toolSlot != -1 && !AutoToolsConfig.get().ignoredInventorySlots.contains(toolSlot)) break;
                    }

                    if (toolSlot == -1 || AutoToolsConfig.get().ignoredInventorySlots.contains(toolSlot)) {
                    } else {
                        selectItem(client, inventory, toolSlot);
                        return;
                    }
                }


                int containerLimit = AutoToolsConfig.get().hotbarOnly? 9: inventory.getContainerSize();
                for (int i = 0; i < containerLimit; i++) {
                    if(AutoToolsConfig.get().ignoredInventorySlots.contains(i)) continue;
                    Item item = inventory.getItem(i).getItem();

                    if (item != Items.AIR) {
                        if(!checkDurability(inventory.getItem(i))) continue;
                        //Only switch if the player can hurt the entity with the current item
                        if (entity instanceof LivingEntity livingEntity) {
                            if (!item.hurtEnemy(inventory.getItem(i), livingEntity, inventory.player)) {
                                continue;
                            }
                        }
                        double newAttackDamage = 1.0;

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
                            if (((EntityHitResult) hit).getEntity() instanceof LivingEntity livingEntity) {
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
}
