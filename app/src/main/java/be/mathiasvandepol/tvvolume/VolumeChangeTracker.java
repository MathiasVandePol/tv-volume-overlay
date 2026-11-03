package be.mathiasvandepol.tvvolume;

/** Repeated audio status and route/scale synchronization must not reopen the overlay. */
final class VolumeChangeTracker {
    private boolean initialized;
    private long route;
    private int value;
    private int maximum;
    private boolean muted;

    boolean observe(long route, int value, int maximum, boolean muted) {
        if (!AvrVolumeState.shouldShow(3, route != 0, value, maximum)) {
            reset();
            return false;
        }
        boolean changed = initialized && this.route == route && this.maximum == maximum
                && (this.value != value || this.muted != muted);
        initialized = true;
        this.route = route;
        this.value = value;
        this.maximum = maximum;
        this.muted = muted;
        return changed;
    }

    void reset() {
        initialized = false;
    }
}
