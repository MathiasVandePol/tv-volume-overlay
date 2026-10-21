package be.mathiasvandepol.tvvolume;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

/** Read-only AVR feedback, above apps, owned by the already-enabled accessibility service. */
final class AvrVolumeOverlay {
    private static final String TAG = "AvrVolumeOverlay";
    private static final String VOLUME_CHANGED = "android.media.VOLUME_CHANGED_ACTION";
    private static final String MUTE_CHANGED = "android.media.STREAM_MUTE_CHANGED_ACTION";
    private static final String STREAM = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    private final AccessibilityService service;
    private final AudioManager audio;
    private final WindowManager windows;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AudioAttributes media = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();
    private VolumePanel panel;
    private final OverlayPreferences options;
    private final ForegroundPolicy foreground;
    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceListener = (prefs, key) -> {
        hide();
        panel = null;
    };
    private boolean samplePanel;
    private boolean attached;
    private boolean registered;
    private boolean closed;
    private final Runnable hide = this::hide;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (OverlayTest.ACTION.equals(intent.getAction())) {
                setResultCode(showPanel(21, 100, false, true) ? android.app.Activity.RESULT_OK : android.app.Activity.RESULT_CANCELED);
                return;
            }
            if (intent.getIntExtra(STREAM, -1) != AudioManager.STREAM_MUSIC) return;
            // Read trusted AudioManager state instead of trusting broadcast extras as a volume value.
            showCurrent();
        }
    };

    AvrVolumeOverlay(AccessibilityService service, ForegroundPolicy foreground) {
        this.foreground = foreground;
        options = new OverlayPreferences(service);
        this.service = service;
        audio = service.getSystemService(AudioManager.class);
        windows = service.getSystemService(WindowManager.class);
    }

    void start() {
        if (registered) return;
        IntentFilter filter = new IntentFilter(VOLUME_CHANGED);
        filter.addAction(MUTE_CHANGED);
        filter.addAction(OverlayTest.ACTION);
        // System volume notifications and an explicit same-UID test request only.
        service.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        registered = true;
        options.store.registerOnSharedPreferenceChangeListener(preferenceListener);
    }

    private boolean usesArc() {
        if (audio == null) return false;
        for (AudioDeviceInfo device : audio.getAudioDevicesForAttributes(media)) {
            int type = device.getType();
            if (type == AudioDeviceInfo.TYPE_HDMI_ARC || type == AudioDeviceInfo.TYPE_HDMI_EARC) return true;
        }
        return false;
    }

    private void showCurrent() {
        if (closed || audio == null || windows == null) return;
        if (foreground.suppress(options.hideInputs())) { hide(); return; }
        try {
            int value = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maximum = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (!AvrVolumeState.shouldShow(AudioManager.STREAM_MUSIC, usesArc(), value, maximum)) {
                hide();
                return;
            }
            showPanel(value, maximum, audio.isStreamMute(AudioManager.STREAM_MUSIC), false);
        } catch (RuntimeException error) {
            Log.w(TAG, "Unable to read AVR state", error);
            hide();
        }
    }

    private boolean showPanel(int value, int maximum, boolean muted, boolean sample) {
        if (closed || windows == null) return false;
        try {
            if (panel == null || samplePanel != sample) {
                hide();
                panel = new VolumePanel(service, options);
                samplePanel = sample;
                if (sample) panel.markAsTest();
            }
            panel.update(value, maximum, muted);
            if (!attached) {
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        panel.panelWidth(), WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
                position(params);
                params.setTitle(sample ? "Volume test" : "Volume");
                windows.addView(panel, params);
                attached = true;
            }
            handler.removeCallbacks(hide);
            handler.postDelayed(hide, options.duration().millis);
            return true;
        } catch (RuntimeException error) {
            // Feedback must never interrupt audio or the foreground app.
            Log.w(TAG, "Unable to show volume feedback", error);
            hide();
            return false;
        }
    }

    @SuppressLint("RtlHardcoded") // Positions name the physical screen edge chosen by the user.
    private void position(WindowManager.LayoutParams params) {
        OverlayOptions.Position position = options.position();
        boolean top = position.name().startsWith("TOP");
        int side = position.name().endsWith("LEFT") ? Gravity.LEFT
                : position.name().endsWith("RIGHT") ? Gravity.RIGHT : Gravity.CENTER_HORIZONTAL;
        params.gravity = (top ? Gravity.TOP : Gravity.BOTTOM) | side;
        params.x = side == Gravity.CENTER_HORIZONTAL ? 0 : dp(34);
        params.y = dp(top ? 70 : 34);
    }

    void foregroundChanged() {
        if (foreground.suppress(options.hideInputs())) hide();
    }

    private int dp(int value) {
        return Math.round(value * service.getResources().getDisplayMetrics().density);
    }

    private void hide() {
        handler.removeCallbacks(hide);
        if (!attached) return;
        try { windows.removeViewImmediate(panel); }
        catch (RuntimeException error) { Log.w(TAG, "AVR feedback window already removed"); }
        attached = false;
    }

    void close() {
        closed = true;
        options.store.unregisterOnSharedPreferenceChangeListener(preferenceListener);
        handler.removeCallbacksAndMessages(null);
        if (registered) {
            service.unregisterReceiver(receiver);
            registered = false;
        }
        hide();
    }
}
