package be.mathiasvandepol.tvvolume;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SettingsDeviceTest {
    private final android.app.Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final Context context = instrumentation.getTargetContext();
    private Activity activity;
    private ComponentName launcher() { return new ComponentName(context, context.getPackageName() + ".LauncherEntry"); }
    @After public void cleanup() {
        instrumentation.runOnMainSync(() -> {
            if (activity != null) activity.finish();
            new OverlayPreferences(context).store.edit().clear().commit();
            context.getPackageManager().setComponentEnabledSetting(launcher(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        });
    }
    private Activity open(Class<? extends Activity> type) {
        activity = instrumentation.startActivitySync(new Intent(context, type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        instrumentation.waitForIdleSync();
        return activity;
    }
    @Test public void allAppearanceCombinationsRenderIncludingHiddenTextAndMute() {
        open(AppearanceActivity.class);
        instrumentation.runOnMainSync(() -> {
            OverlayPreferences options = new OverlayPreferences(context);
            for (OverlayOptions.Size size : OverlayOptions.Size.values())
            for (OverlayOptions.Layout layout : OverlayOptions.Layout.values())
                for (OverlayOptions.Theme theme : OverlayOptions.Theme.values())
                    for (OverlayOptions.Numbers numbers : OverlayOptions.Numbers.values())
                        for (boolean show : new boolean[]{false, true}) {
                            options.store.edit().putString("size", size.name()).putString("layout", layout.name()).putString("theme", theme.name())
                                    .putString("numbers", numbers.name()).putBoolean("show_label", show)
                                    .putString("label", "Living room receiver volume test").commit();
                            VolumePanel panel = new VolumePanel(activity, options);
                            for (boolean muted : new boolean[]{false, true}) {
                                panel.update(21, 100, muted);
                                panel.measure(View.MeasureSpec.makeMeasureSpec(panel.panelWidth(), View.MeasureSpec.EXACTLY),
                                        View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.AT_MOST));
                                panel.layout(0, 0, panel.getMeasuredWidth(), panel.getMeasuredHeight());
                                assertTrue(panel.getHeight() > 0);
                                assertTrue(panel.getHeight() < 800);
                                Bitmap bitmap = Bitmap.createBitmap(panel.getWidth(), panel.getHeight(), Bitmap.Config.ARGB_8888);
                                panel.draw(new Canvas(bitmap));
                                bitmap.recycle();
                                ViewGroup row = (ViewGroup) panel.getChildAt(0);
                                assertEquals(show ? View.VISIBLE : View.GONE, row.getChildAt(0).getVisibility());
                                assertEquals(numbers == OverlayOptions.Numbers.HIDDEN ? View.GONE : View.VISIBLE, row.getChildAt(1).getVisibility());
                                assertEquals(OverlayOptions.number(numbers, 21, 100, muted), ((TextView) row.getChildAt(1)).getText().toString());
                            }
                        }
        });
    }
    @Test public void samplePreviewFitsItsColumnWhileTestPanelKeepsActualSize() {
        open(AppearanceActivity.class);
        instrumentation.runOnMainSync(() -> {
            OverlayPreferences options = new OverlayPreferences(context);
            options.store.edit().putString("size", "LARGE").putString("layout", "HORIZONTAL").commit();
            VolumePanel actual = new VolumePanel(activity, options);
            VolumePanel sample = new VolumePanel(activity, options, true);
            assertTrue(actual.panelWidth() > sample.panelWidth());
            float density = context.getResources().getDisplayMetrics().density;
            assertEquals(Math.round(325 * density), actual.panelWidth());
            assertTrue(sample.panelWidth() <= Math.round(270 * density));
        });
    }
    @Test public void hidingLauncherDoesNotDisableSettingsAndCanBeUndoneInUi() {
        PackageManager manager = context.getPackageManager();
        manager.setComponentEnabledSetting(launcher(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        Intent launch = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER).setPackage(context.getPackageName());
        assertTrue(manager.queryIntentActivities(launch, 0).isEmpty());
        Activity main = open(MainActivity.class);
        assertNotNull(main);
        main.finish();
        Activity settings = open(AppearanceActivity.class);
        instrumentation.runOnMainSync(() -> {
            CheckBox hide = (CheckBox) findText(settings.getWindow().getDecorView(), context.getString(R.string.hide_icon));
            assertNotNull(hide);
            assertTrue(hide.isChecked());
            hide.performClick();
        });
        assertFalse(manager.queryIntentActivities(launch, 0).isEmpty());
    }
    @Test public void accessibilityMetadataKeepsSettingsAvailableAndNoKeyOrScreenCapability() {
        AccessibilityManager manager = context.getSystemService(AccessibilityManager.class);
        AccessibilityServiceInfo service = manager.getInstalledAccessibilityServiceList().stream()
                .filter(info -> context.getPackageName().equals(info.getResolveInfo().serviceInfo.packageName))
                .findFirst().orElseThrow();
        assertEquals(0, service.getCapabilities());
        assertEquals(0, service.flags & AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS);
        assertEquals(MainActivity.class.getName(), service.getSettingsActivityName());
        assertNull(service.packageNames);
    }
    private static View findText(View view, String text) {
        if (view instanceof TextView && text.contentEquals(((TextView) view).getText())) return view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View result = findText(group.getChildAt(i), text);
                if (result != null) return result;
            }
        }
        return null;
    }
}
