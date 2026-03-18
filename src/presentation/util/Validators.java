package presentation.util;

import java.util.function.Consumer;

public final class Validators {
    private Validators() {
    }

    public static String requireText(String rawValue, String label, Consumer<String> onError) {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isEmpty()) {
            report(onError, label + " is required.");
            return null;
        }
        return value;
    }

    public static Integer requireInt(String rawValue, String label, Consumer<String> onError) {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isEmpty()) {
            report(onError, label + " is required.");
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            report(onError, label + " must be a number.");
            return null;
        }
    }

    public static Float requireFloat(String rawValue, String label, Consumer<String> onError) {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isEmpty()) {
            report(onError, label + " is required.");
            return null;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            report(onError, label + " must be a number.");
            return null;
        }
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("^(010|011|012|015)\\d{8}$");
    }

    private static void report(Consumer<String> onError, String message) {
        if (onError != null) {
            onError.accept(message);
        }
    }
}

