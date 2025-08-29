package net.zelythia;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class ControllableCompat {

    @ExpectPlatform
    public static boolean attackDown() {
        throw new AssertionError();
    }
}
