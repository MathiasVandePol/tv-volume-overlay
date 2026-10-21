package be.mathiasvandepol.tvvolume;
import org.junit.Test;
import static org.junit.Assert.*;
public class OverlayOptionsTest {
    @Test public void unknownSavedChoiceUsesDefault() {
        assertEquals(OverlayOptions.Position.TOP_RIGHT, OverlayOptions.parse(OverlayOptions.Position.class, "future", OverlayOptions.Position.TOP_RIGHT));
        assertEquals(OverlayOptions.Theme.DARK, OverlayOptions.parse(OverlayOptions.Theme.class, null, OverlayOptions.Theme.DARK));
        assertEquals(OverlayOptions.Layout.VERTICAL, OverlayOptions.parse(OverlayOptions.Layout.class, "VERTICAL", OverlayOptions.Layout.HORIZONTAL));
    }
    @Test public void transparencyHasCorrectEndpointsAndClampsSavedValues() {
        assertEquals(255, OverlayOptions.backgroundAlpha(0));
        assertEquals(128, OverlayOptions.backgroundAlpha(50));
        assertEquals(0, OverlayOptions.backgroundAlpha(100));
        assertEquals(255, OverlayOptions.backgroundAlpha(-20));
        assertEquals(0, OverlayOptions.backgroundAlpha(200));
        assertEquals(235, OverlayOptions.backgroundAlpha(8));
    }
    @Test public void labelFallsBackAndBoundsUntrustedInput() {
        assertEquals("Volume", OverlayOptions.label("  ", "Volume"));
        assertEquals("Living room", OverlayOptions.label(" Living room ", "Volume"));
        assertEquals("TV  Sound", OverlayOptions.label("TV\n\tSound", "Volume"));
        assertEquals(32, OverlayOptions.label("x".repeat(200), "Volume").length());
    }
    @Test public void unknownSizeAndDurationKeepExistingAppearanceAndTimeout() {
        assertEquals(1f, OverlayOptions.parse(OverlayOptions.Size.class, "huge", OverlayOptions.Size.NORMAL).factor, 0f);
        assertEquals(1900, OverlayOptions.parse(OverlayOptions.Duration.class, null, OverlayOptions.Duration.NORMAL).millis);
        assertEquals(0.75f, OverlayOptions.Size.SMALL.factor, 0f);
        assertEquals(1.25f, OverlayOptions.Size.LARGE.factor, 0f);
        assertEquals(1000, OverlayOptions.Duration.SHORT.millis);
        assertEquals(5000, OverlayOptions.Duration.EXTRA_LONG.millis);
    }
    @Test public void numberChoicesAlsoApplyWhenMuted() {
        assertEquals("21 / 100", OverlayOptions.number(OverlayOptions.Numbers.FULL, 21, 100, false));
        assertEquals("21", OverlayOptions.number(OverlayOptions.Numbers.VALUE, 21, 100, false));
        assertEquals("", OverlayOptions.number(OverlayOptions.Numbers.HIDDEN, 21, 100, false));
        assertEquals("", OverlayOptions.number(OverlayOptions.Numbers.HIDDEN, 21, 100, true));
        assertEquals("—", OverlayOptions.number(OverlayOptions.Numbers.VALUE, 21, 100, true));
    }
}
