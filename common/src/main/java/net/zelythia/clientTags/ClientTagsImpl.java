/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.zelythia.clientTags;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientTagsImpl {
	private static final Map<Tag<?>, ClientTagsLoader.LoadedTag> LOCAL_TAG_HIERARCHY = new ConcurrentHashMap<>();

	public static <T> boolean isInWithLocalFallback(Tag.Named<T> tagKey, T registryEntry) {
		return isInWithLocalFallback(tagKey, registryEntry, new HashSet<>());
	}

	@SuppressWarnings("unchecked")
	private static <T> boolean isInWithLocalFallback(Tag.Named<T> tag, T registryEntry, Set<Tag<T>> checked) {
		if (checked.contains(tag)) {
			return false;
		}

		checked.add(tag);

		Optional<Tag<T>> syncedTag = getSyncedTag(tag, registryEntry);
		if(syncedTag.isPresent()){
			return syncedTag.get().contains(registryEntry);
		}

		Optional<Registry<T>> registry = getRegistry(registryEntry);
		if(!registry.isPresent() || registry.get().getKey(registryEntry) == null){
			return false;
		}


		// Recursively search the entries contained with the tag
		ClientTagsLoader.LoadedTag wt = ClientTagsImpl.getOrCreatePartiallySyncedTag(tag, registry.get());

		if (wt.immediateChildIds().contains(registry.get().getKey(registryEntry))) {
			return true;
		}

		for (Tag<?> key : wt.immediateChildTags()) {
			if (isInWithLocalFallback((Tag.Named<T>) key, registryEntry, checked)) {
				return true;
			}

			checked.add((Tag<T>) key);
		}

		return false;
	}


	@SuppressWarnings("unchecked")
	public static <T> Optional<Tag<T>> getSyncedTag(Tag.Named<T> tag, T registryEntry){
		TagCollection<?> tags = TagCollection.empty();

		if(registryEntry instanceof Block) tags = BlockTags.getAllTags();
		else if(registryEntry instanceof Item) tags = ItemTags.getAllTags();
		else if(registryEntry instanceof EntityType) tags = EntityTypeTags.getAllTags();
		else if(registryEntry instanceof Fluid){
			for (Tag.Named<Fluid> wrapper : FluidTags.getWrappers()) {
				if(wrapper.getName().equals(tag.getName())){
					return Optional.of((Tag<T>) wrapper);
				}
			}
		}

		return Optional.ofNullable((Tag<T>) tags.getTag(tag.getName()));
	}

	@SuppressWarnings("unchecked")
	public static <T> Optional<Registry<T>> getRegistry(T registryEntry){
		if(registryEntry instanceof Block) return Optional.of((Registry<T>) Registry.BLOCK);
		else if(registryEntry instanceof Item) return Optional.of((Registry<T>) Registry.ITEM);
		else if(registryEntry instanceof EntityType) return Optional.of((Registry<T>) Registry.ENTITY_TYPE);
		else if(registryEntry instanceof Fluid) return Optional.of((Registry<T>) Registry.FLUID);

		return Optional.empty();
	}


	public static <T> ClientTagsLoader.LoadedTag getOrCreatePartiallySyncedTag(Tag.Named<T> tagKey, Registry<T> registry) {
		ClientTagsLoader.LoadedTag loadedTag = LOCAL_TAG_HIERARCHY.get(tagKey);

		if (loadedTag == null) {
			loadedTag = ClientTagsLoader.loadTag(tagKey, registry);
			LOCAL_TAG_HIERARCHY.put(tagKey, loadedTag);
		}

		return loadedTag;
	}
}
