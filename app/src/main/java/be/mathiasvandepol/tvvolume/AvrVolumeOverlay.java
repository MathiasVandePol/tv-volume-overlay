package be.mathiasvandepol.tvvolume;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/** Read-only AVR feedback, above apps, owned by the already-enabled accessibility service. */
final class AvrVolumeOverlay {
    private static final String TAG = "AvrVolumeOverlay";
    private static final String VOLUME_CHANGED = "android.media.VOLUME_CHANGED_ACTION";
    private static final String MUTE_CHANGED = "android.media.STREAM_MUTE_CHANGED_ACTION";
    private static final String STREAM = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    private static final long VISIBLE_MS = 1900;
    private final AccessibilityService service;
    private final AudioManager audio;
    private final WindowManager windows;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AudioAttributes media = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();
    private LinearLayout panel;
    private TextView valueText;
    private TextView titleText;
    private ProgressBar progress;
    private boolean attached;
    private boolean registered;
    private boolean closed;
    private final Runnable hide = this::hide;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra(STREAM, -1) != AudioManager.STREAM_MUSIC) return;
            // Read trusted AudioManager state instead of trusting broadcast extras as a volume value.
            showCurrent();
        }
    };

    AvrVolumeOverlay(AccessibilityService service) {
        this.service = service;
        audio = service.getSystemService(AudioManager.class);
        windows = service.getSystemService(WindowManager.class);
    }

    void start() {
        if (registered) return;
        IntentFilter filter = new IntentFilter(VOLUME_CHANGED);
        filter.addAction(MUTE_CHANGED);
        // Both broadcasts come from system_server. Other apps cannot drive this overlay.
        service.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        registered = true;
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
        try {
            int value = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maximum = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (!AvrVolumeState.shouldShow(AudioManager.STREAM_MUSIC, usesArc(), value, maximum)) {
                hide();
                return;
            }
            boolean muted = audio.isStreamMute(AudioManager.STREAM_MUSIC);
            if (panel == null) createPanel();
            titleText.setText(muted ? R.string.avr_volume_muted : R.string.avr_volume_title);
            valueText.setText(muted ? "—" : value + " / " + maximum);
            progress.setProgress(muted ? 0 : AvrVolumeState.percent(value, maximum));
            panel.setContentDescription(service.getString(muted ? R.string.avr_volume_muted : R.string.avr_volume_title)
                    + ", " + (muted ? "" : value + " / " + maximum));
            if (!attached) {
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        dp(238), WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.TOP | Gravity.END;
                params.x = dp(34);
                params.y = dp(115);
                params.setTitle("AVR volume");
                windows.addView(panel, params);
                attached = true;
            }
            handler.removeCallbacks(hide);
            handler.postDelayed(hide, VISIBLE_MS);
            Log.d(TAG, "shown volume=" + value + " max=" + maximum + " muted=" + muted);
        } catch (RuntimeException error) {
            // An overlay is optional feedback: a routing/window error must not kill Home or audio.
            Log.w(TAG, "Unable to show AVR feedback", error);
            hide();
        }
    }

    private void createPanel() {
        panel = new LinearLayout(service);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(14), dp(18), dp(14));
        panel.setFocusable(false);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.argb(235, 27, 29, 33));
        background.setCornerRadius(dp(21));
        background.setStroke(dp(1), Color.argb(72, 255, 255, 255));
        panel.setBackground(background);
        LinearLayout row = new LinearLayout(service);
        row.setGravity(Gravity.CENTER_VERTICAL);
        titleText = new TextView(service);
        titleText.setTextColor(Color.rgb(202, 206, 214));
        titleText.setTextSize(15);
        titleText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        row.addView(titleText, new LinearLayout.LayoutParams(0, dp(30), 1));
        valueText = new TextView(service);
        valueText.setTextColor(Color.WHITE);
        valueText.setTextSize(21);
        valueText.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        row.addView(valueText, new LinearLayout.LayoutParams(-2, dp(30)));
        panel.addView(row);
        progress = new ProgressBar(service, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(ColorStateList.valueOf(Color.WHITE));
        progress.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(70, 73, 79)));
        LinearLayout.LayoutParams line = new LinearLayout.LayoutParams(-1, dp(5));
        line.topMargin = dp(8);
        panel.addView(progress, line);
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
        handler.removeCallbacksAndMessages(null);
        if (registered) {
            service.unregisterReceiver(receiver);
            registered = false;
        }
        hide();
    }
}
