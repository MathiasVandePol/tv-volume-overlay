package be.mathiasvandepol.tvvolume;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
public class ForegroundPolicyTest {
    @Test public void hidesInputViewerAndRestoresOnApps() {
        ForegroundPolicy policy = new ForegroundPolicy(Set.of("tv.input"));
        assertFalse(policy.suppress(true));
        policy.observe("tv.input", true);
        assertTrue(policy.suppress(true));
        assertFalse(policy.suppress(false));
        policy.observe("netflix", true);
        assertFalse(policy.suppress(true));
    }
    @Test public void overlayAndSystemPopupsCannotClearInputState() {
        ForegroundPolicy policy = new ForegroundPolicy(Set.of("tv.input"));
        policy.observe("tv.input", true);
        policy.observe("be.mathiasvandepol.tvvolume", false);
        policy.observe("android", false);
        policy.observe(null, true);
        policy.observe("", true);
        assertTrue(policy.suppress(true));
    }
    @Test public void noDetectedViewerDoesNotHideEverything() {
        ForegroundPolicy policy = new ForegroundPolicy(Set.of());
        policy.observe("netflix", true);
        assertFalse(policy.suppress(true));
    }
}
