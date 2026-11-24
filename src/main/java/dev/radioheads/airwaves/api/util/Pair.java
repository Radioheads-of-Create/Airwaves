package dev.radioheads.airwaves.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Pair<A, B>(
        A left,
        B right
) {
    @Contract("_, _ -> new")
    public static <A, B> @NotNull Pair<A, B> of(A left, B right) {
        return new Pair<>(left, right);
    }
}
