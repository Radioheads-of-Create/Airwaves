package dev.radioheads.airwaves.api.antenna;

// a is the electrical half-length in radians
final class Wire {
    private static final int STEPS = 180;

    private Wire() {}

    // Far-field pattern
    static double field(double a, double theta) {
        double sine = Math.sin(theta);
        if (Math.abs(sine) < 1.0E-9) {
            return 0.0;
        }
        return (Math.cos(a * Math.cos(theta)) - Math.cos(a)) / sine;
    }

    // Radiated power integral by Simpson's rule
    static double power(double a) {
        double h = Math.PI / STEPS;
        double sum = 0.0;
        for (int i = 0; i <= STEPS; i++) {
            double theta = i * h;
            double f = field(a, theta);
            double weight = i == 0 || i == STEPS ? 1.0 : i % 2 == 1 ? 4.0 : 2.0;
            sum += weight * f * f * Math.sin(theta);
        }
        return sum * h / 3.0;
    }
}
