package dev.radioheads.airwaves.content.block;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class HfTransceiverHints {
    public record Panel(boolean standby, boolean locked, boolean megahertz, boolean fast, boolean slotB, long hertz, int watts,
                        float volume, float squelch, int tuningSteps, int pressed, long pressedAt, Component antenna, double lossDb) {}

    private HfTransceiverHints() {}

    public static Component label(HfTransceiverControl control) {
        return text(control.id());
    }

    @Nullable
    public static Component change(HfTransceiverControl control, Panel before, Panel after) {
        if (control.kind == HfTransceiverControl.Kind.KEY) {
            boolean pressed = after.pressedAt() != before.pressedAt() && after.pressed() == control.ordinal();
            return pressed ? pressed(control, after) : null;
        }
        return switch (control) {
            case VFO -> after.fast() != before.fast() ? text(after.fast() ? "vfo.fast" : "vfo.fine")
                    : after.tuningSteps() != before.tuningSteps() ? tuned(after) : null;
            case SQL -> after.squelch() == before.squelch() ? null
                    : after.squelch() <= 0.0F ? text("sql.open") : text("sql.level", percent(after.squelch()));
            case AF -> after.volume() == before.volume() ? null : text("af.level", percent(after.volume()));
            default -> null;
        };
    }

    private static Component pressed(HfTransceiverControl control, Panel panel) {
        if (control == HfTransceiverControl.POWER || panel.standby()) {
            return text(panel.standby() ? "power.off" : "power.on");
        }
        return switch (control) {
            case F_LOCK -> text(panel.locked() ? "f_lock.on" : "f_lock.off");
            case MHZ -> panel.locked() ? text("f_lock.on") : text(panel.megahertz() ? "mhz.on" : "mhz.off");
            case DOWN, UP -> panel.locked() ? text("f_lock.on") : band(panel);
            case A_B -> panel.locked() ? text("f_lock.on") : text(panel.slotB() ? "a_b.b" : "a_b.a", frequency(panel.hertz()));
            case ANT -> text("ant.readout", panel.antenna(), String.format(Locale.ROOT, "%.1f", panel.lossDb()));
            case PWR_DOWN, PWR_UP -> text("watts", panel.watts());
            default -> label(control);
        };
    }

    private static Component tuned(Panel panel) {
        if (panel.standby()) {
            return text("power.off");
        }
        return panel.locked() ? text("f_lock.on") : frequency(panel.hertz());
    }

    private static Component band(Panel panel) {
        int band = HfTransceiverPanel.bandOf(panel.hertz());
        if (panel.megahertz() || band < 0) {
            return frequency(panel.hertz());
        }
        return text("band", HfTransceiverPanel.metres(band), frequency(panel.hertz()));
    }

    private static Component frequency(long hertz) {
        return text("frequency", String.format(Locale.ROOT, "%.3f", hertz / 1.0e6));
    }

    private static int percent(float value) {
        return Math.round(value * 100.0F);
    }

    private static Component text(String key, Object... args) {
        return Component.translatable("airwaves.hf_transceiver." + key, args);
    }
}
