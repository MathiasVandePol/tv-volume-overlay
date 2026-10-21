package be.mathiasvandepol.tvvolume;

/** Display decisions only: the overlay must never invent or change the receiver's volume. */
final class AvrVolumeState {
    static boolean shouldShow(int stream, boolean arcRoute, int value, int maximum) {
        return stream == 3 && arcRoute && maximum > 0 && value >= 0 && value <= maximum;
    }

    static int percent(int value, int maximum) {
        if (maximum <= 0) return 0;
        return Math.max(0, Math.min(100, Math.round(100f * value / maximum)));
    }
}
