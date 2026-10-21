package be.mathiasvandepol.tvvolume;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/** Observes activity metadata only; never retrieves screen content or filters remote keys. */
public final class VolumeOverlayService extends AccessibilityService {
    private AvrVolumeOverlay overlay;
    private ForegroundPolicy foreground;
    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        if (overlay != null) overlay.close();
        foreground = new ForegroundPolicy(TvInputApps.find(getPackageManager()));
        overlay = new AvrVolumeOverlay(this, foreground);
        overlay.start();
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (foreground == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        String pkg = event.getPackageName() == null ? null : event.getPackageName().toString();
        String name = event.getClassName() == null ? null : event.getClassName().toString();
        foreground.observe(pkg, TvInputApps.isActivity(getPackageManager(), pkg, name));
        overlay.foregroundChanged();
    }
    @Override public void onInterrupt() { }
    @Override public void onDestroy() {
        if (overlay != null) overlay.close();
        super.onDestroy();
    }
}
