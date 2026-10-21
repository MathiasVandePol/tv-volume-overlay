package be.mathiasvandepol.tvvolume;

/** Persisted choices are parsed defensively so upgrades never break the service. */
final class OverlayOptions {
    enum Position { TOP_LEFT, TOP_CENTER, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT }
    enum Layout { HORIZONTAL, VERTICAL }
    enum Theme { DARK, LIGHT, BLACK }
    enum Numbers { FULL, VALUE, HIDDEN }
    static <T extends Enum<T>> T parse(Class<T> type, String value, T fallback) {
        try { return Enum.valueOf(type, value); }
        catch (IllegalArgumentException | NullPointerException e) { return fallback; }
    }
    static String label(String custom, String fallback) {
        String clean = custom == null ? "" : custom.replaceAll("[\\p{Cntrl}]", " ").trim();
        return clean.isEmpty() ? fallback : clean.substring(0, Math.min(32, clean.length()));
    }
    static String number(Numbers mode, int value, int maximum, boolean muted) {
        if (mode == Numbers.HIDDEN) return "";
        if (muted) return "—";
        return mode == Numbers.VALUE ? Integer.toString(value) : value + " / " + maximum;
    }
    private OverlayOptions() { }
}
