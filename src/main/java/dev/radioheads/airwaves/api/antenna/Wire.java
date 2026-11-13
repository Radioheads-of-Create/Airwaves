package dev.radioheads.airwaves.api.antenna;

// Electrical length in radians a wire b top hat
final class Wire {
    private static final int STEPS = 180;

    private Wire() {}

    // Far-field pattern
    static double field(double a, double theta) {
        return field(a, 0.0, theta);
    }

    // Far-field pattern of a top-loaded wire
    static double field(double a, double b, double theta) {
        double sine = Math.sin(theta);
        if (Math.abs(sine) < 1.0E-9) {
            return 0.0;
        }
        double cosine = Math.cos(theta);
        return (Math.cos(b) * Math.cos(a * cosine) - cosine * Math.sin(b) * Math.sin(a * cosine) - Math.cos(a + b)) / sine;
    }

    // Radiated power integral by Simpson's rule
    static double power(double a) {
        return power(a, 0.0);
    }

    static double power(double a, double b) {
        double h = Math.PI / STEPS;
        double sum = 0.0;
        for (int i = 0; i <= STEPS; i++) {
            double theta = i * h;
            double f = field(a, b, theta);
            double weight = i == 0 || i == STEPS ? 1.0 : i % 2 == 1 ? 4.0 : 2.0;
            sum += weight * f * f * Math.sin(theta);
        }
        return sum * h / 3.0;
    }
}
