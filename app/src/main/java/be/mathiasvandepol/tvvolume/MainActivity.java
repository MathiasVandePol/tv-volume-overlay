package be.mathiasvandepol.tvvolume;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private TextView status;
    private final AccessibilityManager.AccessibilityServicesStateChangeListener serviceListener = this::updateStatus;
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(60), dp(28), dp(60), dp(28));
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setBackgroundColor(Color.rgb(11, 14, 20));
        root.addView(text(R.string.app_name, 15, 0xFFB9C6DA));
        TextView title = text(R.string.title, 37, Color.WHITE);
        title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        root.addView(title);
        root.addView(text(R.string.subtitle, 17, 0xFFAAB5C5));
        status = text(R.string.status_off, 14, 0xFFFFD28A);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-2, -2);
        statusParams.topMargin = dp(16);
        root.addView(status, statusParams);
        TextView instructions = text(R.string.instructions, 18, 0xFFEBEFF6);
        instructions.setLineSpacing(dp(5), 1);
        LinearLayout.LayoutParams instructionsParams = new LinearLayout.LayoutParams(-1, -2);
        instructionsParams.topMargin = dp(14);
        root.addView(instructions, instructionsParams);
        LinearLayout.LayoutParams requirementsParams = new LinearLayout.LayoutParams(-1, -2);
        requirementsParams.topMargin = dp(14);
        root.addView(text(R.string.requirements, 14, 0xFFB1BDD0), requirementsParams);
        root.addView(text(R.string.privacy, 13, 0xFF8D9BB0));
        Button settings = actionButton(R.string.open_settings);
        settings.setOnClickListener(view -> {
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch (ActivityNotFoundException e) { Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_LONG).show(); }
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-2, dp(48));
        buttonParams.topMargin = dp(20);
        root.addView(settings, buttonParams);
        Button customize = actionButton(R.string.customize);
        customize.setOnClickListener(view -> startActivity(new Intent(this, AppearanceActivity.class)));
        LinearLayout.LayoutParams customizeParams = new LinearLayout.LayoutParams(-2, dp(48));
        customizeParams.topMargin = dp(8);
        root.addView(customize, customizeParams);
        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(root);
        setContentView(scroll);
        settings.requestFocus();
    }
    private Button actionButton(int label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setPadding(dp(24), 0, dp(24), 0);
        button.setMinHeight(dp(48));
        button.setMinWidth(dp(280));
        button.setOnFocusChangeListener((view, focused) -> styleButton(button, focused));
        styleButton(button, false);
        return button;
    }
    private void styleButton(Button button, boolean focused) {
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dp(15));
        background.setColor(focused ? 0xFFD6E5FF : 0xFF223048);
        button.setTextColor(focused ? 0xFF111C2C : Color.WHITE);
        button.setBackground(background);
    }
    private TextView text(int resource, int sp, int color) {
        TextView view = new TextView(this);
        view.setText(resource);
        view.setTextSize(sp);
        view.setTextColor(color);
        return view;
    }
    @Override protected void onStart() {
        super.onStart();
        AccessibilityManager manager = getSystemService(AccessibilityManager.class);
        if (manager != null) manager.addAccessibilityServicesStateChangeListener(getMainExecutor(), serviceListener);
    }
    @Override protected void onStop() {
        AccessibilityManager manager = getSystemService(AccessibilityManager.class);
        if (manager != null) manager.removeAccessibilityServicesStateChangeListener(serviceListener);
        super.onStop();
    }
    @Override protected void onResume() {
        super.onResume();
        updateStatus(getSystemService(AccessibilityManager.class));
    }
    private void updateStatus(AccessibilityManager manager) {
        boolean enabled = false;
        if (manager != null) {
            for (AccessibilityServiceInfo info : manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
                if (getPackageName().equals(info.getResolveInfo().serviceInfo.packageName)) enabled = true;
            }
        }
        status.setText(enabled ? R.string.status_on : R.string.status_off);
        status.setTextColor(enabled ? 0xFF9CE3B8 : 0xFFFFD28A);
    }
}
