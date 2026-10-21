package be.mathiasvandepol.tvvolume;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
/** Hosts read-only feedback; never filters keys, reads windows or opens another app. */
public final class VolumeOverlayService extends AccessibilityService {
    private AvrVolumeOverlay overlay;
    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        if (overlay != null) overlay.close();
        overlay = new AvrVolumeOverlay(this);
        overlay.start();
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }
    @Override public void onDestroy() {
        if (overlay != null) overlay.close();
        super.onDestroy();
    }
}
