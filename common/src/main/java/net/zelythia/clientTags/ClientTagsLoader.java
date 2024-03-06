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
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ClientTagsLoader {
    static final Logger LOGGER = LogManager.getLogger("client-tags");

    /**
     * Load a given tag from the available mods into a set of {@code Identifier}s.
     * Parsing based on
     */
    public static <T> LoadedTag loadTag(Tag.Named<T> tag, Registry<T> registry) {
        HashSet<Tag.BuilderEntry> tags = new HashSet<>();

        HashSet<Path> tagFiles = getTagFiles(registry, tag.getName());

        for (Path tagPath : tagFiles) {
            try (BufferedReader tagReader = Files.newBufferedReader(tagPath)) {
                JsonParser parser = new JsonParser();
                JsonElement jsonElement = parser.parse(tagReader);


                if (jsonElement.isJsonObject()) {
                    //TODO add own json parsing so be don't need BuilderEntries and can use Entries instead
                    tags.addAll(Tag.Builder.tag().addFromJson(jsonElement.getAsJsonObject(), tagPath.getFileName().toString().replace(".json", "")).getEntries().collect(Collectors.toList()));
                }

            } catch (IOException e) {
                LOGGER.error("Error loading tag: " + tag, e);
            }
        }

        HashSet<ResourceLocation> completeIds = new HashSet<>();
        HashSet<ResourceLocation> immediateChildIds = new HashSet<>();
        HashSet<Tag<T>> immediateChildTags = new HashSet<>();

        for (Tag.BuilderEntry tagEntry : tags) {
            tagEntry.getEntry().build(
                    resourceLocation -> {
                        Wrapper<T> namedTag = new Wrapper<>(resourceLocation);
                        immediateChildTags.add(namedTag);
                        return namedTag;
                    },
                    resourceLocation -> {
                        immediateChildIds.add(resourceLocation);
                        return registry.getOptional(resourceLocation).orElse(null);
                    },
                    registryEntry -> {
                        completeIds.add(registry.getKey(registryEntry));
                    }
            );
        }

        // Ensure that the tag does not refer to itself
        immediateChildTags.remove(tag);

        return new LoadedTag(Collections.unmodifiableSet(completeIds), Collections.unmodifiableSet(immediateChildTags),
                Collections.unmodifiableSet(immediateChildIds));
    }

    public static final class Wrapper<T> implements Tag.Named<T> {
        private final ResourceLocation name;

        public Wrapper(ResourceLocation resourceLocation) {
            this.name = resourceLocation;
        }

        public ResourceLocation getName() {
            return this.name;
        }

        private Tag<T> resolve() {
            return null;
        }

        public boolean contains(T object) {
            return false;
        }

        public List<T> getValues() {
            return Collections.emptyList();
        }
    }

    public static final class LoadedTag {
        private final Set<ResourceLocation> completeIds;
        private final Set<Tag<?>> immediateChildTags;
        private final Set<ResourceLocation> immediateChildIds;

        public LoadedTag(Set<ResourceLocation> completeIds, Set<Tag<?>> immediateChildTags, Set<ResourceLocation> immediateChildIds) {
            this.completeIds = completeIds;
            this.immediateChildTags = immediateChildTags;
            this.immediateChildIds = immediateChildIds;
        }

        public Set<ResourceLocation> completeIds() {
            return completeIds;
        }

        public Set<Tag<?>> immediateChildTags() {
            return immediateChildTags;
        }

        public Set<ResourceLocation> immediateChildIds() {
            return immediateChildIds;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            LoadedTag that = (LoadedTag) obj;
            return Objects.equals(this.completeIds, that.completeIds) &&
                    Objects.equals(this.immediateChildTags, that.immediateChildTags) &&
                    Objects.equals(this.immediateChildIds, that.immediateChildIds);
        }

        @Override
        public int hashCode() {
            return Objects.hash(completeIds, immediateChildTags, immediateChildIds);
        }

        @Override
        public String toString() {
            return "LoadedTag[" +
                    "completeIds=" + completeIds + ", " +
                    "immediateChildTags=" + immediateChildTags + ", " +
                    "immediateChildIds=" + immediateChildIds + ']';
        }

    }

    /**
     * @param registry the Registry of the entries of the tag
     * @param identifier  the Identifier of the tag
     * @return the paths to all tag json files within the available mods
     */
    private static HashSet<Path> getTagFiles(Registry<?> registry, ResourceLocation identifier) {
        return getTagFiles("tags/" + registry.key().location().getPath(), identifier);
    }

    /**
     * @return the paths to all tag json files within the available mods
     */
    private static HashSet<Path> getTagFiles(String tagType, ResourceLocation identifier) {
        String tagFile = String.format("data/%s/%s/%s.json", identifier.getNamespace(), tagType, identifier.getPath());
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
