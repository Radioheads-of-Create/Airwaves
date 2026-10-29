package dev.radioheads.airwaves;

import dev.radioheads.airwaves.api.antenna.CompassPoint;
import net.createmod.catnip.lang.Lang;
import org.jetbrains.annotations.ApiStatus;
import oshi.util.tuples.Pair;

import java.util.*;

// title case and snake to title case are from
// https://github.com/kube-mods/kubejs/blob/2601/src/main/java/dev/latvian/mods/kubejs/plugin/builtin/wrapper/StringUtilsWrapper.java
// with some modifications
public final class AirwaveLang extends Lang {
    private AirwaveLang() {}
    private static final Set<String> ALWAYS_LOWER_CASE = new HashSet<>(Arrays.asList("a", "an", "the", "of", "on", "in", "and", "or", "but", "for"));

    public static String snakeCaseToTitleCase(String string) {
        StringJoiner joiner = new StringJoiner("-");
        String[] split = string.split("_");

        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            String titleCase = toTitleCase(s, i == 0);
            joiner.add(titleCase);
        }

        return joiner.toString();
    }


    /**
     * Capitalizes the first letter of the string unless it is "a", "an", "the", "of", "on", "in", "and", "or", "but" or "for"
     */
    public static String toTitleCase(String s) {
        return toTitleCase(s, false);
    }

    /**
     * Capitalizes the first letter of the string. If ignoreSpecial is true, it will also capitalize articles and prepositions
     */
    public static String toTitleCase(String s, boolean ignoreSpecial) {
        if (s.isEmpty()) {
            return "";
        } else if (!ignoreSpecial && ALWAYS_LOWER_CASE.contains(s)) {
            return s;
        } else if (s.length() == 1) {
            return s.toUpperCase(Locale.ROOT);
        }

        char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }


    // Everything below is for generation of lang
    private static List<Pair<String, String>> lang;

    @ApiStatus.Internal
    public static void init() {
        lang = new ArrayList<>();
        collect();
        lang.forEach(pair -> Airwaves.REGISTRATE.addRawLang(pair.getA(), pair.getB()));
        lang = null;
    }

    private static void collect() {
        for (CompassPoint point : CompassPoint.values()) {
            add("compass." + point.name().toLowerCase(Locale.ROOT), snakeCaseToTitleCase(point.name().toLowerCase(Locale.ROOT)));
        }

        add("direction.display", "%s° %s");
        add("direction.target", "Target");

        add("container.tuning_table", "Tuning Table");

        addBasic("creative_tab.airwave_main", "Airwaves");

        List<String> tiers = List.of("low_power", "mid_power", "high_power", "max_power", "creative");

        tiers.forEach(tier -> add("tier." + tier, snakeCaseToTitleCase(tier)));

        addAdvancement("root", "Airwaves", "It's Airwave time!");
        addAdvancement("speaker_cone", "Good Morning Vietnam!", "Obtain a radio");
        addAdvancement("resistor", "Join the resistance", "Ohmmmmmm...");
    }

    private static void add(String key, String value) {
        addBasic(Airwaves.MOD_ID + "." + key, value);
    }

    private static void addBasic(String key, String value) {
        lang.add(new Pair<>(key, value));
    }

    private static void addAdvancement(String key, String title, String description) {
        addBasic("advancement.airwaves." + key, title);
        addBasic("advancement.airwaves." + key + ".description", description);
    }
}
