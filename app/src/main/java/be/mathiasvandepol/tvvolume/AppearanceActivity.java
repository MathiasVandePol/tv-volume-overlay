package be.mathiasvandepol.tvvolume;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/** All settings remain local; the sample never changes audio or requires a running service. */
public final class AppearanceActivity extends Activity {
    private OverlayPreferences options;
    private FrameLayout preview;
    private boolean sampleMuted;
    private boolean updatingLauncher;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        options = new OverlayPreferences(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(36), dp(20), dp(36), dp(20));
        root.setBackgroundColor(0xFF0B0E14);
        root.addView(text(R.string.customize, 26));
        LinearLayout columns = new LinearLayout(this);
        root.addView(columns, new LinearLayout.LayoutParams(-1, 0, 1));
        ScrollView scroll = new ScrollView(this);
        columns.addView(scroll, new LinearLayout.LayoutParams(0, -1, 1));
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(0, 0, dp(24), dp(12));
        scroll.addView(controls);
        LinearLayout sample = new LinearLayout(this);
        sample.setPadding(dp(12), dp(12), 0, 0);
        sample.setOrientation(LinearLayout.VERTICAL);
        columns.addView(sample, new LinearLayout.LayoutParams(dp(300), -1));
        sample.addView(text(R.string.preview_title, 18));
        sample.addView(text(R.string.preview_hint, 13));
        preview = new FrameLayout(this);
        preview.setBackgroundColor(0xFF1C2638);
        LinearLayout.LayoutParams canvas = new LinearLayout.LayoutParams(-1, 0, 1);
        canvas.topMargin = dp(12);
        sample.addView(preview, canvas);
        Button mute = new Button(this);
        mute.setAllCaps(false);
        mute.setText(R.string.preview_mute);
        mute.setOnClickListener(view -> { sampleMuted = !sampleMuted; refreshPreview(); });
        sample.addView(mute, new LinearLayout.LayoutParams(-1, dp(48)));
        choice(controls, R.string.position, R.array.positions, "position", OverlayOptions.Position.values(), options.position());
        choice(controls, R.string.layout, R.array.layouts, "layout", OverlayOptions.Layout.values(), options.layout());
        choice(controls, R.string.size, R.array.sizes, "size", OverlayOptions.Size.values(), options.size());
        choice(controls, R.string.duration, R.array.durations, "duration", OverlayOptions.Duration.values(), options.duration());
        Button test = new Button(this);
        test.setText(R.string.test_overlay);
        test.setAllCaps(false);
        test.setOnClickListener(view -> {
            // Hide the keyboard before testing the actual screen position.
            android.view.inputmethod.InputMethodManager keyboard = getSystemService(android.view.inputmethod.InputMethodManager.class);
            if (keyboard != null) keyboard.hideSoftInputFromWindow(test.getWindowToken(), 0);
            OverlayTest.show(this);
        });
        controls.addView(test, new LinearLayout.LayoutParams(-1, dp(48)));
        controls.addView(text(R.string.test_hint, 13));
        choice(controls, R.string.theme, R.array.themes, "theme", OverlayOptions.Theme.values(), options.theme());
        toggle(controls, R.string.show_label, options.showLabel(), checked -> save("show_label", checked));
        controls.addView(text(R.string.custom_label, 15));
        EditText label = new EditText(this);
        label.setTextColor(Color.WHITE);
        label.setHintTextColor(0xFFAAB5C5);
        label.setHint(R.string.avr_volume_title);
        label.setSingleLine(true);
        label.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        label.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        label.setText(options.customLabel());
        label.setContentDescription(getString(R.string.custom_label));
        controls.addView(label, new LinearLayout.LayoutParams(-1, dp(52)));
        label.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                options.store.edit().putString("label", s.toString()).apply();
                refreshPreview();
            }
            public void afterTextChanged(Editable s) { }
        });
        controls.addView(text(R.string.label_hint, 13));
        choice(controls, R.string.numbers, R.array.number_formats, "numbers", OverlayOptions.Numbers.values(), options.numbers());
        toggle(controls, R.string.hide_inputs, options.hideInputs(), checked -> save("hide_inputs", checked));
        controls.addView(text(R.string.inputs_hint, 13));
        TextView detection = text(R.string.inputs_found, 13);
        detection.setText(getString(R.string.inputs_found, TvInputApps.find(getPackageManager()).size()));
        controls.addView(detection);
        CheckBox hideIcon = toggle(controls, R.string.hide_icon, launcherHidden(), checked -> { });
        hideIcon.setOnCheckedChangeListener((button, checked) -> {
            if (updatingLauncher) return;
            if (checked) {
                new AlertDialog.Builder(this).setTitle(R.string.hide_icon)
                        .setMessage(R.string.hide_icon_hint)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> setLauncherHidden(true, hideIcon))
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> resetLauncherSwitch(hideIcon))
                        .setOnCancelListener(dialog -> resetLauncherSwitch(hideIcon)).show();
            } else setLauncherHidden(false, hideIcon);
        });
        controls.addView(text(R.string.hide_icon_hint, 13));
        setContentView(root);
        refreshPreview();
    }
    private <T extends Enum<T>> void choice(LinearLayout parent, int title, int array, String key, T[] values, T selected) {
        TextView label = text(title, 15);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, -2);
        labelParams.topMargin = dp(12);
        parent.addView(label, labelParams);
        Spinner spinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setContentDescription(getString(title));
        spinner.setSelection(selected.ordinal());
        parent.addView(spinner, new LinearLayout.LayoutParams(-1, dp(48)));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> view, View child, int index, long id) {
                if (!values[index].name().equals(options.store.getString(key, ""))) {
                    options.store.edit().putString(key, values[index].name()).apply();
                    refreshPreview();
                }
            }
            public void onNothingSelected(AdapterView<?> view) { }
        });
    }
    private interface ToggleAction { void changed(boolean checked); }
    private CheckBox toggle(LinearLayout parent, int title, boolean selected, ToggleAction action) {
        CheckBox checkbox = new CheckBox(this);
        checkbox.setText(title);
        checkbox.setTextColor(Color.WHITE);
        checkbox.setTextSize(16);
        checkbox.setChecked(selected);
        checkbox.setMinHeight(dp(52));
        parent.addView(checkbox, new LinearLayout.LayoutParams(-1, -2));
        checkbox.setOnCheckedChangeListener((button, checked) -> action.changed(checked));
        return checkbox;
    }
    private void save(String key, boolean value) { options.store.edit().putBoolean(key, value).apply(); refreshPreview(); }
    private void refreshPreview() {
        if (preview == null) return;
        preview.removeAllViews();
        VolumePanel panel = new VolumePanel(this, options, true);
        panel.update(21, 100, sampleMuted);
        String position = options.position().name();
        int side = position.endsWith("LEFT") ? Gravity.START : position.endsWith("RIGHT") ? Gravity.END : Gravity.CENTER_HORIZONTAL;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(panel.panelWidth(), -2,
                (position.startsWith("TOP") ? Gravity.TOP : Gravity.BOTTOM) | side);
        params.setMargins(dp(8), dp(8), dp(8), dp(8));
        preview.addView(panel, params);
    }
    private ComponentName launcher() { return new ComponentName(this, getPackageName() + ".LauncherEntry"); }
    private boolean launcherHidden() {
        return getPackageManager().getComponentEnabledSetting(launcher()) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    }
    private void resetLauncherSwitch(CheckBox checkbox) {
        updatingLauncher = true;
        checkbox.setChecked(launcherHidden());
        updatingLauncher = false;
    }
    private void setLauncherHidden(boolean hide, CheckBox checkbox) {
        try {
            getPackageManager().setComponentEnabledSetting(launcher(), hide
                    ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } catch (RuntimeException e) {
            Toast.makeText(this, R.string.launcher_failed, Toast.LENGTH_LONG).show();
        }
        resetLauncherSwitch(checkbox);
    }
    private TextView text(int id, int size) {
        TextView result = new TextView(this);
        result.setText(id);
        result.setTextColor(size <= 13 ? 0xFFAAB5C5 : Color.WHITE);
        result.setTextSize(size);
        return result;
    }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
}
