package be.mathiasvandepol.tvvolume;

import android.content.Context;
import android.content.SharedPreferences;

final class OverlayPreferences {
    final SharedPreferences store;
    OverlayPreferences(Context context) { store = context.getSharedPreferences("appearance", Context.MODE_PRIVATE); }
    OverlayOptions.Position position() { return OverlayOptions.parse(OverlayOptions.Position.class, store.getString("position", ""), OverlayOptions.Position.TOP_RIGHT); }
    OverlayOptions.Layout layout() { return OverlayOptions.parse(OverlayOptions.Layout.class, store.getString("layout", ""), OverlayOptions.Layout.HORIZONTAL); }
    OverlayOptions.Theme theme() { return OverlayOptions.parse(OverlayOptions.Theme.class, store.getString("theme", ""), OverlayOptions.Theme.DARK); }
    OverlayOptions.Numbers numbers() { return OverlayOptions.parse(OverlayOptions.Numbers.class, store.getString("numbers", ""), OverlayOptions.Numbers.FULL); }
    OverlayOptions.Size size() { return OverlayOptions.parse(OverlayOptions.Size.class, store.getString("size", ""), OverlayOptions.Size.NORMAL); }
    OverlayOptions.Duration duration() { return OverlayOptions.parse(OverlayOptions.Duration.class, store.getString("duration", ""), OverlayOptions.Duration.NORMAL); }
    int transparency() { return OverlayOptions.transparency(store.getInt("transparency", 8)); }
    boolean showLabel() { return store.getBoolean("show_label", true); }
    boolean hideInputs() { return store.getBoolean("hide_inputs", true); }
    String customLabel() { return store.getString("label", ""); }
    String label(Context context) { return OverlayOptions.label(customLabel(), context.getString(R.string.avr_volume_title)); }
}
