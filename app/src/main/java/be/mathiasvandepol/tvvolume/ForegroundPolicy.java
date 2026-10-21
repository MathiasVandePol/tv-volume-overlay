package be.mathiasvandepol.tvvolume;

import java.util.HashSet;
import java.util.Set;

/** Keeps only the current activity package in memory; widget/popup events cannot clear it. */
final class ForegroundPolicy {
    private final Set<String> inputPackages;
    private String foreground = "";
    ForegroundPolicy(Set<String> inputPackages) { this.inputPackages = new HashSet<>(inputPackages); }
    void observe(String packageName, boolean isActivity) {
        if (isActivity && packageName != null && !packageName.isEmpty()) foreground = packageName;
    }
    boolean suppress(boolean enabled) { return enabled && inputPackages.contains(foreground); }
}
