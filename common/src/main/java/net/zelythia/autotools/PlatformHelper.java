package net.zelythia.autotools;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;
import java.util.HashSet;

public class PlatformHelper {

    @ExpectPlatform
    public static HashSet<Path> getResourcePaths(String path) {
        throw new AssertionError();
    }

}
