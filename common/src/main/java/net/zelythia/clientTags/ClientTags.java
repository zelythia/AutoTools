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

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Allows the use of tags by directly loading them from the installed mods.
 *
 * <p>Tags are loaded by the server, either the internal server in singleplayer or the connected server and
 * synced to the client. This can be a pain point for interoperability, as a tag that does not exist on the server
 * because it is part of a mod only present on the client will no longer be available to the client that may wish to
 * query it.
 *
 * <p>Client Tags resolve that issue by lazily reading the tag json files within the mods on the side of the caller,
 * directly, allowing for mods to query tags such as ConventionalBlockTags
 * even when connected to a vanilla server.
 */
public final class ClientTags {
	private ClientTags() {
	}

	/**
	 * Loads a tag into the cache, recursively loading any contained tags along with it.
	 *
	 * @param tag the {@code Tag} to load
	 * @param registry The registry for T
	 * @return a set of {@code Identifier}s this tag contains
	 */
	public static <T> Set<ResourceLocation> getOrCreateLocalTag(Tag.Named<T> tag, Registry<T> registry) {
		return ClientTagsImpl.getOrCreatePartiallySyncedTag(tag, registry).completeIds();
	}

	/**
	 * Loads a tag into the cache, recursively loading any contained tags along with it.
	 *
	 * @param tag the {@code Tag} to load
	 * @param registryEntry An entry of the tag
	 * @return a set of {@code Identifier}s this tag contains
	 */
	public static <T> Set<ResourceLocation> getOrCreateLocalTag(Tag.Named<T> tag, T registryEntry) {
		return ClientTagsImpl.getOrCreatePartiallySyncedTag(tag, ClientTagsImpl.getRegistry(registryEntry).get()).completeIds();
	}

	/**
	 * Checks if an entry is in a tag, for use with entries from a dynamic registry,
	 * such as {@link net.minecraft.world.level.biome.Biome}s.
	 *
	 * <p>If the synced tag does exist, it is queried. If it does not exist,
	 * the tag populated from the available mods is checked, recursively checking the
	 * synced tags and entries contained within.
	 *
	 * @param tagKey        the {@code Tag} to be checked
	 * @param registryEntry the entry to check
	 * @return if the entry is in the given tag
	 */
	public static <T> boolean isInWithLocalFallback(Tag<T> tagKey, T registryEntry) {
		Objects.requireNonNull(tagKey);
		Objects.requireNonNull(registryEntry);
		if(!(tagKey instanceof Tag.Named)){
 			ClientTagsLoader.LOGGER.error("ClientTags only support Named Tags");
			 return false;
		}
		return ClientTagsImpl.isInWithLocalFallback((Tag.Named<T>) tagKey, registryEntry);
	}

	/**
	 * Checks if an entry is in a tag provided by the available mods.
	 *
	 * @param tagKey      the {@code Tag} to being checked
	 * @param registryEntry the entry to check
	 * @return if the entry is in the given tag
	 */
	public static <T> boolean isInLocal(Tag.Named<T> tagKey, T registryEntry) {
		Objects.requireNonNull(tagKey);
		Objects.requireNonNull(registryEntry);

		Optional<Registry<T>> registry = ClientTagsImpl.getRegistry(registryEntry);
		if(registry.isPresent()){
			Set<ResourceLocation> ids = getOrCreateLocalTag(tagKey, registry.get());
			return ids.contains(registry.get().getKey(registryEntry));
		}

		return false;
	}
}
