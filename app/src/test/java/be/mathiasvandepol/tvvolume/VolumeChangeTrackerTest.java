package be.mathiasvandepol.tvvolume;

import org.junit.Test;
import static org.junit.Assert.*;

public class VolumeChangeTrackerTest {
    @Test public void initialStateAndUnchangedStatusStaySilent() {
        VolumeChangeTracker tracker = new VolumeChangeTracker();
        assertFalse(tracker.observe(1, 28, 100, false));
        for (int i = 0; i < 100; i++) assertFalse(tracker.observe(1, 28, 100, false));
    }

    @Test public void actualVolumeAndMuteChangesAreShownOnce() {
        VolumeChangeTracker tracker = new VolumeChangeTracker();
        tracker.observe(1, 28, 100, false);
        assertTrue(tracker.observe(1, 29, 100, false));
        assertFalse(tracker.observe(1, 29, 100, false));
        assertTrue(tracker.observe(1, 28, 100, false));
        assertTrue(tracker.observe(1, 28, 100, true));
        assertFalse(tracker.observe(1, 28, 100, true));
        assertTrue(tracker.observe(1, 28, 100, false));
    }

    @Test public void heldVolumeChangesAndEndpointsAreNotThrottled() {
        VolumeChangeTracker tracker = new VolumeChangeTracker();
        tracker.observe(1, 4, 100, false);
        for (int value = 3; value >= 0; value--) assertTrue(tracker.observe(1, value, 100, false));
        assertFalse(tracker.observe(1, 0, 100, false));
        for (int value = 1; value <= 100; value++) assertTrue(tracker.observe(1, value, 100, false));
        assertFalse(tracker.observe(1, 100, 100, false));
    }

    @Test public void disconnectAndReconnectEstablishANewBaseline() {
        VolumeChangeTracker tracker = new VolumeChangeTracker();
        tracker.observe(1, 28, 100, false);
        assertFalse(tracker.observe(0, 7, 15, false));
        assertFalse(tracker.observe(1, 31, 100, false));
        assertTrue(tracker.observe(1, 30, 100, false));
    }

    @Test public void switchingArcDeviceOrRangeDoesNotShowSynchronization() {
        VolumeChangeTracker tracker = new VolumeChangeTracker();
        tracker.observe(1, 28, 100, false);
        assertFalse(tracker.observe(2, 15, 100, true));
        assertFalse(tracker.observe(2, 5, 30, false));
        assertTrue(tracker.observe(2, 6, 30, false));
    }

    @Test public void invalidStatusDoesNotProduceAChangeOrPoisonTheNextBaseline() {
        VolumeChangeTracker tracker = new VolumeChangeTracker();
        tracker.observe(1, 28, 100, false);
        assertFalse(tracker.observe(1, -1, 100, false));
        assertFalse(tracker.observe(1, 33, 100, false));
        assertFalse(tracker.observe(1, 101, 100, false));
        assertFalse(tracker.observe(1, 33, 100, false));
        assertFalse(tracker.observe(1, 0, 0, false));
        assertFalse(tracker.observe(1, 33, 100, false));
        assertTrue(tracker.observe(1, 32, 100, false));
    }

    @Test public void reinitializationDoesNotReplayTheLastVolume() {
        VolumeChangeTracker tracker = new VolumeChangeTracker();
        tracker.observe(1, 28, 100, false);
        tracker.reset();
        assertFalse(tracker.observe(1, 35, 100, true));
        assertTrue(tracker.observe(1, 35, 100, false));
    }
}
