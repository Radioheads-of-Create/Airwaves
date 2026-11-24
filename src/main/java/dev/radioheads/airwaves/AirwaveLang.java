package dev.radioheads.airwaves;

import dev.radioheads.airwaves.api.antenna.CompassPoint;
import dev.radioheads.airwaves.api.util.Pair;
import dev.radioheads.airwaves.integration.ponder.AirwavePonderTags;
import dev.radioheads.airwaves.registry.AirwaveRecipeTypes;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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

    public static String snakeCaseToSpacedTitleCase(String string) {
        StringJoiner joiner = new StringJoiner(" ");
        String[] split = string.split("_");

        for (int i = 0; i < split.length; i++) {
            joiner.add(toTitleCase(split[i], i == 0));
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

    public static LangBuilder builder() {
        return new LangBuilder(Airwaves.MOD_ID);
    }

    public static LangBuilder translate(String langKey, Object... args) {
        return builder().translate(langKey, args);
    }

    // Everything below is for generation of lang
    private static List<Pair<String, String>> lang;

    @ApiStatus.Internal
    public static void init() {
        lang = new ArrayList<>();
        collect();
        lang.forEach(pair -> Airwaves.registrate().addRawLang(pair.left(), pair.right()));
        lang = null;
    }

    private static void collect() {
        for (CompassPoint point : CompassPoint.values()) {
            add("compass." + point.name().toLowerCase(Locale.ROOT), snakeCaseToTitleCase(point.name().toLowerCase(Locale.ROOT)));
        }

        for (AirwaveRecipeTypes type : AirwaveRecipeTypes.values()) {
            addRecipe(type.name().toLowerCase(Locale.ROOT));
        }

        add("direction.display", "%s° %s");
        add("direction.target", "Target");

        add("container.tuning_table", "Tuning Table");

        add("radio.signal", "Signal: %s%%");
        add("radio.no_signal", "No signal");
        add("radio.no_power", "No power");

        add("transmitter.power", "Transmit Power");
        add("microphone.output", "Output: %s%%");

        add("antenna.none", "No antenna");
        add("antenna.whip", "Whip antenna, %s blocks");
        add("antenna.dipole", "Dipole antenna, %s blocks");
        add("antenna.yagi", "Yagi antenna, %s elements");
        add("antenna.top_loaded", "Top-loaded vertical, %s blocks");

        addHf("power", "Radio POWER");
        addHf("power.on", "Radio ON");
        addHf("power.off", "Radio OFF");
        addHf("f_lock", "Frequency LOCK");
        addHf("f_lock.on", "Frequency LOCKED");
        addHf("f_lock.off", "Frequency UNLOCKED");
        addHf("mhz", "MHz STEP");
        addHf("mhz.on", "MHz STEP ON");
        addHf("mhz.off", "MHz STEP OFF");
        addHf("down", "Band DOWN");
        addHf("up", "Band UP");
        addHf("band", "%sm BAND, %s");
        addHf("frequency", "%s MHz");
        addHf("a_b", "VFO A/B");
        addHf("a_b.a", "VFO A, %s");
        addHf("a_b.b", "VFO B, %s");
        addHf("ant", "Antenna CHECK");
        addHf("ant.readout", "%s, %s dB loss");
        addHf("pwr_down", "TX Power DOWN");
        addHf("pwr_up", "TX Power UP");
        addHf("watts", "TX Power %s W");
        addHf("vfo", "Tuning DIAL");
        addHf("vfo.fast", "Tuning FAST");
        addHf("vfo.fine", "Tuning FINE");
        addHf("sql", "Squelch");
        addHf("sql.level", "Squelch %s%%");
        addHf("sql.open", "Squelch OPEN");
        addHf("af", "AF Volume");
        addHf("af.level", "AF Volume %s%%");

        add("section.hardware", "Hardware");
        add("section.materials", "Materials");
        add("section.circuitry", "Circuitry");
        add("section.creative", "Creative");

        add("cable.select", "Click on a valid device to connect.");
        add("cable.length", "Length: %s blocks");
        add("cable.connected", "Connected!");
        add("cable.cut", "Cable cut!");
        add("cable.cleared", "Selection cleared.");
        add("cable.too_far", "Too far away! Cables reach up to %s blocks.");
        add("cable.not_enough", "You don't have enough cable to reach this far!");
        add("cable.already_linked", "These are already connected!");
        add("cable.no_socket", "That block has no room for another cable!");
        add("cable.wrong_line", "Radio and telephone lines can't be joined!");

        add("insulator.in", "This insulator is in a...");
        add("line.radio_network", "Radio Network");
        add("line.telephone_network", "Telephone Network");
        add("line.tx", "TX");
        add("line.rx", "RX");

        add("telephone.dial", "Dial %s");
        add("telephone.confirm", "Confirm number");
        add("telephone.missing", "This number does not exist!");
        add("telephone.unavailable", "This number is unavailable!");
        add("telephone.length", "Numbers are 6 digits long!");
        add("telephone_earpiece.bound", "Bound to %s");
        add("telephone_earpiece.unnumbered", "Bound to a telephone with no number yet");
        addBasic("airwaves.subtitle.telephone_ring", "Telephone rings");

        add("tooltip.cable.loss_per_block", "%s dB loss per block");
        add("tooltip.old_content_no_functionallity", "This has no functionality currently.");
        add("tooltip.amount.remaining", "Remaining Amount: %s");

        addBasic("creative_tab.airwave_main", "Airwaves");

        addAdvancement("root", "Airwaves", "It's Airwave time!");
        addAdvancement("cheater", "You feel a bitter taste in your mouth...", "You're free. Free at last");
        addAdvancement("turtle", "s Cuteeee", "*turtle noises*");
        addAdvancement("speaker_cone", "Good Morning Vietnam!", "Obtain a radio");
        addAdvancement("resistor", "Join the resistance", "Ohmmmmmm...");
        addAdvancement("pressed_shape", "The shape goes into a shape press that presses the shape into a pressed shape", "Press a shape into a pressed shape");

        PonderLang.create(AirwavePonderTags.RADIO)
                .header("Radio")
                .text("insert text here")
                .tagTitle("Radios")
                .describeTag("All about the radio system")
                .generateLang();
    }

    private static void add(String key, String value) {
        addBasic(Airwaves.MOD_ID + "." + key, value);
    }

    private static void addBasic(String key, String value) {
        lang.add(Pair.of(key, value));
    }

    private static void addBasic(Pair<String, String> pair) {
        lang.add(pair);
    }

    private static void addAdvancement(String key, String title, String description) {
        addBasic("advancement.airwaves." + key, title);
        addBasic("advancement.airwaves." + key + ".description", description);
    }

    private static void addRecipe(String key) {
        add("recipe." + key, snakeCaseToSpacedTitleCase(key));
    }
    
    private static void addHf(String key, String value) {
        add("hf_transceiver." + key, value);
    }

    private static void ponder(String key, String value) {
        add("ponder." + key, value);
    }

    private static class PonderLang {
        private final String type;
        private final List<Pair<String, String>> toGenerate = new ArrayList<>();
        private final List<Pair<String, String>> ponderTexts = new ArrayList<>();

        private PonderLang(@NotNull ResourceLocation type) {
            this.type = type.getPath();
        }

        @Contract("_ -> new")
        public static @NotNull PonderLang create(ResourceLocation type) {
            return new PonderLang(type);
        }

        public PonderLang header(String header) {
            lang(key("header"), header);
            return this;
        }

        public PonderLang text(String text) {
            ponderLang(key("text_" + (ponderTexts.size() + 1)), text);
            return this;
        }

        public PonderLang tagTitle(String title) {
            lang(baseTag(), title);
            return this;
        }

        public PonderLang describeTag(String tag) {
            lang(keyTag("description"), tag);
            return this;
        }

        private void lang(String key, String value) {
            toGenerate.add(Pair.of(key, value));
        }

        private void ponderLang(String key, String value) {
            ponderTexts.add(Pair.of(key, value));
        }

        private String key(String key) {
          return "airwaves.ponder." + type + "." + key;
        }

        private String keyTag(String key) {
            return baseTag() + "." + key;
        }

        private String baseTag() {
            return "airwaves.ponder.tag." + type;
        }

        public void generateLang() {
            for (var pair : toGenerate) {
                addBasic(pair);
            }

            for (var pair : ponderTexts) {
                addBasic(pair);
            }
        }
    }
}
