package be.mathiasvandepol.tvvolume;

import android.Manifest;
import android.app.Activity;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.os.ParcelFileDescriptor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

/** Changes accessibility settings only on the test device, restoring its original values. */
@RunWith(AndroidJUnit4.class)
public class OverlayWindowDeviceTest {
    private final android.app.Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final Context context = instrumentation.getTargetContext();

    @Test public void testButtonShowsRealWindowWithConfiguredTimeoutWithoutChangingAudio() {
        UiAutomation automation = instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        String originalServices = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        String originalEnabled = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        OverlayPreferences options = new OverlayPreferences(context);
        AudioManager audio = context.getSystemService(AudioManager.class);
        int originalVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        boolean originalMute = audio.isStreamMute(AudioManager.STREAM_MUSIC);
        Activity activity = null;
        automation.adoptShellPermissionIdentity(Manifest.permission.WRITE_SECURE_SETTINGS);
        try {
            String component = context.getPackageName() + "/" + VolumeOverlayService.class.getName();
            Settings.Secure.putString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    originalServices == null || originalServices.isEmpty() ? component : originalServices + ":" + component);
            Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
            options.store.edit().putString("duration", "EXTRA_LONG").putString("size", "LARGE")
                    .putBoolean("show_label", false).putString("numbers", "HIDDEN").commit();
            activity = instrumentation.startActivitySync(new Intent(context, AppearanceActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            instrumentation.waitForIdleSync();
            final Activity screen = activity;
            // Retry only while waiting for Android to bind the newly enabled service.
            long deadline = SystemClock.uptimeMillis() + 8000;
            do {
                instrumentation.runOnMainSync(() -> findText(screen.getWindow().getDecorView(), context.getString(R.string.test_overlay)).performClick());
                if (await(() -> hasTestWindow(automation), 400)) break;
            } while (SystemClock.uptimeMillis() < deadline);
            assertTrue("Test button must create a real accessibility overlay", hasTestWindow(automation));
            SystemClock.sleep(1250);
            assertTrue("Five-second setting must outlast the one-second setting", hasTestWindow(automation));
            options.store.edit().putString("duration", "SHORT").commit();
            assertTrue("Changing settings removes the old sample", await(() -> !hasTestWindow(automation), 1500));
            instrumentation.runOnMainSync(() -> findText(screen.getWindow().getDecorView(), context.getString(R.string.test_overlay)).performClick());
            assertTrue(await(() -> hasTestWindow(automation), 600));
            assertTrue("One-second test overlay must disappear", await(() -> !hasTestWindow(automation), 2000));
            assertEquals(originalVolume, audio.getStreamVolume(AudioManager.STREAM_MUSIC));
            assertEquals(originalMute, audio.isStreamMute(AudioManager.STREAM_MUSIC));
        } finally {
            Settings.Secure.putString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, originalServices);
            Settings.Secure.putString(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, originalEnabled);
            automation.dropShellPermissionIdentity();
            if (activity != null) {
                final Activity screen = activity;
                instrumentation.runOnMainSync(screen::finish);
            }
            options.store.edit().clear().commit();
        }
    }
    private static boolean hasTestWindow(UiAutomation automation) {
        // Non-touchable feedback windows may be omitted from accessibility's interactive-window list.
        // Inspect WindowManager directly through the test harness, never from the shipped app.
        try (ParcelFileDescriptor.AutoCloseInputStream input = new ParcelFileDescriptor.AutoCloseInputStream(
                automation.executeShellCommand("dumpsys window windows"))) {
            String windows = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            int title = windows.indexOf(" u0 Volume test}:");
            if (title < 0) return false;
            int end = windows.indexOf("  Window #", title);
            String window = windows.substring(title, end < 0 ? windows.length() : end);
            return window.contains("ty=ACCESSIBILITY_OVERLAY") || window.contains("ty=2032");
        } catch (IOException e) { throw new AssertionError(e); }
    }
    private static boolean await(BooleanSupplier condition, long timeout) {
        long deadline = SystemClock.uptimeMillis() + timeout;
        do {
            if (condition.getAsBoolean()) return true;
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() < deadline);
        return condition.getAsBoolean();
    }
    private static View findText(View view, String text) {
        if (view instanceof TextView && text.contentEquals(((TextView) view).getText())) return view;
        if (view instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view;
            for (int i = 0; i < parent.getChildCount(); i++) {
                View found = findText(parent.getChildAt(i), text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
