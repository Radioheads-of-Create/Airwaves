package dev.radioheads.airwaves.content.telephone;

// Six digit numbers from 1 to 8
public final class PhoneNumbers {
    public static final int LENGTH = 6;
    public static final int NETWORK_DIGITS = 2;
    public static final int DIGITS = 8;

    private PhoneNumbers() {}

    public static String format(String digits) {
        return digits.length() <= NETWORK_DIGITS ? digits : digits.substring(0, NETWORK_DIGITS) + "-" + digits.substring(NETWORK_DIGITS);
    }

    public static boolean valid(String number) {
        return number.length() == LENGTH && number.chars().allMatch(c -> c >= '1' && c <= '0' + DIGITS);
    }
}
