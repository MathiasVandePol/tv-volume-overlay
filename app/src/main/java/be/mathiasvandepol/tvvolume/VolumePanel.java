package be.mathiasvandepol.tvvolume;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared renderer for the real overlay and the explicitly labelled settings preview. */
final class VolumePanel extends LinearLayout {
    private final TextView title;
    private final TextView value;
    private final Meter meter;
    private final OverlayPreferences options;
    private final float scale;
    final boolean vertical;
    public VolumePanel(Context context) { this(context, new OverlayPreferences(context)); }
    VolumePanel(Context context, OverlayPreferences options) { this(context, options, false); }
    VolumePanel(Context context, OverlayPreferences options, boolean fitPreview) {
        super(context);
        this.options = options;
        vertical = options.layout() == OverlayOptions.Layout.VERTICAL;
        // The small settings preview fits its column. The test overlay uses the true size.
        scale = fitPreview ? Math.min(options.size().factor, 270f / (vertical ? 132 : 260)) : options.size().factor;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setPadding(dp(18), dp(14), dp(18), dp(14));
        setFocusable(false);
        int background = options.theme() == OverlayOptions.Theme.LIGHT ? 0xFFF2F4F8
                : options.theme() == OverlayOptions.Theme.BLACK ? 0xFF000000 : 0xEB1B1D21;
        int ink = options.theme() == OverlayOptions.Theme.LIGHT ? 0xFF172033 : 0xFFF5F7FC;
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(background);
        shape.setCornerRadius(dp(21));
        shape.setStroke(dp(1), options.theme() == OverlayOptions.Theme.LIGHT ? 0xFFCCD2DB : 0xFF4B505B);
        setBackground(shape);
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(vertical ? VERTICAL : HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        title = text(15, ink);
        title.setMaxLines(2);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setVisibility(options.showLabel() ? VISIBLE : GONE);
        value = text(vertical ? 16 : 21, ink);
        value.setVisibility(options.numbers() == OverlayOptions.Numbers.HIDDEN ? GONE : VISIBLE);
        if (vertical) {
            title.setGravity(Gravity.CENTER);
            value.setGravity(Gravity.CENTER);
            row.addView(title, new LayoutParams(-1, -2));
            row.addView(value, new LayoutParams(-1, -2));
        } else {
            row.addView(title, new LayoutParams(0, -2, 1));
            row.addView(value, new LayoutParams(options.showLabel() ? -2 : -1, -2));
            value.setGravity(options.showLabel() ? Gravity.END : Gravity.CENTER);
        }
        boolean hasText = options.showLabel() || options.numbers() != OverlayOptions.Numbers.HIDDEN;
        row.setVisibility(hasText ? VISIBLE : GONE);
        addView(row, new LayoutParams(-1, -2));
        meter = new Meter(context, vertical, ink, options.theme() == OverlayOptions.Theme.LIGHT ? 0xFFD2D8E2 : 0xFF46494F);
        LayoutParams bar = new LayoutParams(vertical ? dp(12) : -1, dp(vertical ? 140 : 6));
        bar.topMargin = hasText ? dp(10) : 0;
        addView(meter, bar);
    }
    void update(int level, int max, boolean muted) {
        title.setText(muted ? getContext().getString(R.string.avr_volume_muted) : options.label(getContext()));
        value.setText(OverlayOptions.number(options.numbers(), level, max, muted));
        meter.percent = muted ? 0 : AvrVolumeState.percent(level, max);
        meter.muted = muted;
        meter.invalidate();
    }
    void markAsTest() {
        TextView badge = text(12, options.theme() == OverlayOptions.Theme.LIGHT ? 0xFF172033 : 0xFFF5F7FC);
        badge.setText(R.string.overlay_test_badge);
        badge.setGravity(Gravity.CENTER);
        LayoutParams params = new LayoutParams(-1, -2);
        params.bottomMargin = dp(8);
        addView(badge, 0, params);
    }
    int panelWidth() { return dp(vertical ? 132 : 260); }
    private TextView text(int size, int color) {
        TextView result = new TextView(getContext());
        result.setTextSize(size * scale);
        result.setTextColor(color);
        result.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        return result;
    }
    private int dp(int n) { return Math.round(n * scale * getResources().getDisplayMetrics().density); }
    private static final class Meter extends View {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final boolean vertical;
        final int ink, track;
        int percent;
        boolean muted;
        Meter(Context context, boolean vertical, int ink, int track) {
            super(context);
            this.vertical = vertical;
            this.ink = ink;
            this.track = track;
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        @Override protected void onDraw(Canvas canvas) {
            float width = getWidth(), height = getHeight(), radius = Math.min(width, height) / 2;
            paint.setColor(muted ? 0xFFE46B6B : track);
            canvas.drawRoundRect(0, 0, width, height, radius, radius, paint);
            if (!muted && percent > 0) {
                paint.setColor(ink);
                canvas.drawRoundRect(0, vertical ? height * (1 - percent / 100f) : 0,
                        vertical ? width : width * percent / 100f, height, radius, radius, paint);
            }
        }
    }
}
