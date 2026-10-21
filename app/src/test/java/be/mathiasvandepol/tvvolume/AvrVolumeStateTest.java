package be.mathiasvandepol.tvvolume;
import org.junit.Test;
import static org.junit.Assert.*;
public class AvrVolumeStateTest {
    @Test public void onlyMediaOnActiveArcRouteIsShown() {
        assertTrue(AvrVolumeState.shouldShow(3, true, 21, 100));
        assertFalse(AvrVolumeState.shouldShow(1, true, 21, 100));
        assertFalse(AvrVolumeState.shouldShow(3, false, 21, 100));
    }
    @Test public void rejectsUnknownOrImpossibleReceiverState() {
        assertFalse(AvrVolumeState.shouldShow(3, true, -1, 100));
        assertFalse(AvrVolumeState.shouldShow(3, true, 101, 100));
        assertFalse(AvrVolumeState.shouldShow(3, true, 0, 0));
        assertTrue(AvrVolumeState.shouldShow(3, true, 0, 100));
        assertTrue(AvrVolumeState.shouldShow(3, true, 100, 100));
    }
    @Test public void mapsReceiverRangeWithoutOverflowOrDivisionByZero() {
        assertEquals(21, AvrVolumeState.percent(21, 100));
        assertEquals(50, AvrVolumeState.percent(15, 30));
        assertEquals(100, AvrVolumeState.percent(100, 100));
        assertEquals(0, AvrVolumeState.percent(0, 0));
        assertEquals(0, AvrVolumeState.percent(-1, 100));
        assertEquals(100, AvrVolumeState.percent(101, 100));
    }
}
