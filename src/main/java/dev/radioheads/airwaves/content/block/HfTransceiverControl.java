package dev.radioheads.airwaves.content.block;

import org.jetbrains.annotations.Nullable;

public enum HfTransceiverControl {
    POWER(Kind.KEY, 'z', 1.0F, 7.0F, 3.0F, 8.0F, 13.0F, 1.0F, 1.8F, 15.0F, 2.0F, 2.8F, 14.0F, 1.5F, 2.3F),
    F_LOCK(Kind.KEY, 'z', 12.0F, 1.0F, 13.0F, 2.0F, 3.0F, 7.0F, 1.8F, 4.0F, 8.0F, 2.8F, 3.5F, 7.5F, 2.3F),
    MHZ(Kind.KEY, 'z', 14.0F, 1.0F, 15.0F, 2.0F, 1.0F, 7.0F, 1.8F, 2.0F, 8.0F, 2.8F, 1.5F, 7.5F, 2.3F),
    DOWN(Kind.KEY, 'z', 12.0F, 3.0F, 13.0F, 4.0F, 3.0F, 5.0F, 1.8F, 4.0F, 6.0F, 2.8F, 3.5F, 5.5F, 2.3F),
    UP(Kind.KEY, 'z', 14.0F, 3.0F, 15.0F, 4.0F, 1.0F, 5.0F, 1.8F, 2.0F, 6.0F, 2.8F, 1.5F, 5.5F, 2.3F),
    A_B(Kind.KEY, 'z', 8.0F, 5.0F, 9.0F, 6.0F, 7.0F, 3.0F, 1.8F, 8.0F, 4.0F, 2.8F, 7.5F, 3.5F, 2.3F),
    ANT(Kind.KEY, 'z', 10.0F, 5.0F, 11.0F, 6.0F, 5.0F, 3.0F, 1.8F, 6.0F, 4.0F, 2.8F, 5.5F, 3.5F, 2.3F),
    PWR_DOWN(Kind.KEY, 'z', 8.0F, 7.0F, 9.0F, 8.0F, 7.0F, 1.0F, 1.8F, 8.0F, 2.0F, 2.8F, 7.5F, 1.5F, 2.3F),
    PWR_UP(Kind.KEY, 'z', 10.0F, 7.0F, 11.0F, 8.0F, 5.0F, 1.0F, 1.8F, 6.0F, 2.0F, 2.8F, 5.5F, 1.5F, 2.3F),
    VFO(Kind.KNOB, 'z', 12.0F, 5.0F, 15.0F, 8.0F, 1.0F, 1.0F, 0.5F, 4.0F, 4.0F, 2.5F, 2.5F, 2.5F, 2.0F),
    SQL(Kind.KNOB, 'z', 4.0F, 5.0F, 7.0F, 8.0F, 9.0F, 1.0F, 1.0F, 12.0F, 4.0F, 3.0F, 10.5F, 2.5F, 2.0F),
    AF(Kind.WHEEL, 'x', 3.0F, 1.0F, 4.0F, 4.0F, 12.0F, 5.0F, 1.5F, 13.0F, 8.0F, 4.5F, 12.5F, 6.5F, 3.0F);

    public static final float PANEL_HEIGHT = 9.0F;
    public static final float FRONT_Z = 2.0F;
    public static final float[] BODY = {0.0F, 0.0F, 2.0F, 16.0F, 9.0F, 16.0F};
    public static final float[] SCREEN = {5.0F, 5.0F, 2.2F, 12.0F, 8.0F, 3.0F};
    public static final float[] ANTENNA = {5.0F, 5.0F, 16.0F};
    public static final String[] LEDS = {"on_air", "on_air_lit", "rx", "rx_lit"};

    public enum Kind {
        KEY,
        KNOB,
        WHEEL
    }

    public final Kind kind;
    public final char axis;
    public final float panelX0;
    public final float panelY0;
    public final float panelX1;
    public final float panelY1;
    public final float[] box;
    public final float[] pivot;

    HfTransceiverControl(Kind kind, char axis, float panelX0, float panelY0, float panelX1, float panelY1,
                         float x0, float y0, float z0, float x1, float y1, float z1, float pivotX, float pivotY, float pivotZ) {
        this.kind = kind;
        this.axis = axis;
        this.panelX0 = panelX0;
        this.panelY0 = panelY0;
        this.panelX1 = panelX1;
        this.panelY1 = panelY1;
        this.box = new float[] {x0, y0, z0, x1, y1, z1};
        this.pivot = new float[] {pivotX, pivotY, pivotZ};
    }

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public boolean contains(double panelX, double panelY, double margin) {
        return panelX >= this.panelX0 - margin && panelX <= this.panelX1 + margin && panelY >= this.panelY0 - margin && panelY <= this.panelY1 + margin;
    }

    @Nullable
    public static HfTransceiverControl at(double panelX, double panelY) {
        HfTransceiverControl best = null;
        for (HfTransceiverControl control : values()) {
            if (control.contains(panelX, panelY, 0.0)) {
                return control;
            }
            if (best == null && control.contains(panelX, panelY, 0.25)) {
                best = control;
            }
        }
        return best;
    }
}
