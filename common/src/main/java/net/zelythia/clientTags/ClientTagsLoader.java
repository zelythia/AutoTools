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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientTagsLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger("client-tags");
	/**
	 * Load a given tag from the available mods into a set of {@code Identifier}s.
	 * Parsing based on {@link net.minecraft.tags.TagLoader#load(net.minecraft.server.packs.resources.ResourceManager)}
	 */
	public static <T> LoadedTag loadTag(TagKey<?> tagKey) {
		var tags = new HashSet<Tag.BuilderEntry>();
		HashSet<Path> tagFiles = getTagFiles(tagKey.registry(), tagKey.location());

		for (Path tagPath : tagFiles) {
			try (BufferedReader tagReader = Files.newBufferedReader(tagPath)) {
				JsonParser parser = new JsonParser();
				JsonElement jsonElement = parser.parse(tagReader);

				if (jsonElement.isJsonObject()) {
					//TODO add own json parsing so be don't need BuilderEntries and can use Entries instead
					tags.addAll(Tag.Builder.tag().addFromJson(jsonElement.getAsJsonObject(), tagPath.getFileName().toString().replace(".json", "")).getEntries().collect(Collectors.toList()));
				}
			} catch (IOException e) {
				LOGGER.error("Error loading tag: " + tagKey, e);
			}
		}

		HashSet<ResourceLocation> completeIds = new HashSet<>();
		HashSet<ResourceLocation> immediateChildIds = new HashSet<>();
		HashSet<TagKey<?>> immediateChildTags = new HashSet<>();

		for (Tag.BuilderEntry tagEntry : tags) {
			tagEntry.entry().build(
					resourceLocation -> {
						Tag<T> tag = new Tag<>(Collections.EMPTY_LIST);
						TagKey<T> tagKey1 = (TagKey<T>) new TagKey<>(tagKey.registry(), resourceLocation);
						immediateChildTags.add(tagKey1);
						return tag;
					},
					resourceLocation -> {
						immediateChildIds.add(resourceLocation);
						return (T) Registry.REGISTRY.get(tagKey.registry().location()).getOptional(resourceLocation).orElse(null);
					},
					registryEntry -> {
						Registry<T> registry = (Registry<T>) Registry.REGISTRY.get(tagKey.registry().location());
						completeIds.add(registry.getKey(registryEntry));
					}
			);
		}

		// Ensure that the tag does not refer to itself
		immediateChildTags.remove(tagKey);

		return new LoadedTag(Collections.unmodifiableSet(completeIds), Collections.unmodifiableSet(immediateChildTags),
				Collections.unmodifiableSet(immediateChildIds));
	}

	public record LoadedTag(Set<ResourceLocation> completeIds, Set<TagKey<?>> immediateChildTags, Set<ResourceLocation> immediateChildIds) {
	}

	/**
	 * @param registryKey the RegistryKey of the TagKey
	 * @param identifier  the Identifier of the tag
	 * @return the paths to all tag json files within the available mods
	 */
	private static HashSet<Path> getTagFiles(ResourceKey<? extends Registry<?>> registryKey, ResourceLocation identifier) {
		return getTagFiles(TagManager.getTagDir(registryKey), identifier);
	}

	/**
	 * @return the paths to all tag json files within the available mods
	 */
	private static HashSet<Path> getTagFiles(String tagType, ResourceLocation identifier) {
		String tagFile = "data/%s/%s/%s.json".formatted(identifier.getNamespace(), tagType, identifier.getPath());
		return getResourcePaths(tagFile);
	}

	/**
	 * @return all paths from the available mods that match the given internal path
	 */
	@ExpectPlatform
	private static HashSet<Path> getResourcePaths(String path) {
		throw new AssertionError();
	}
}
