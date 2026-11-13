package dev.radioheads.airwaves.api.util;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AirwaveUtil {
    public static <T> void callNotNull(@Nullable T object, Consumer<T> consumer) {
        if (object != null) {
            consumer.accept(object);
        }
    }

    public static <T> void callNotNull(@Nullable T object, Runnable runnable) {
        if (object != null) {
            runnable.run();
        }
    }
}
