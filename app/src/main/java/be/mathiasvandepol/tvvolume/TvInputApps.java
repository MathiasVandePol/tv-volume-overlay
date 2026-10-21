package be.mathiasvandepol.tvvolume;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.tv.TvContract;
import java.util.HashSet;
import java.util.Set;

final class TvInputApps {
    static Set<String> find(PackageManager manager) {
        Intent view = new Intent(Intent.ACTION_VIEW).setDataAndType(
                TvContract.buildChannelUriForPassthroughInput("tv-volume-overlay/Discovery"),
                "vnd.android.cursor.item/channel");
        Set<String> result = new HashSet<>();
        for (ResolveInfo info : manager.queryIntentActivities(view, PackageManager.MATCH_DEFAULT_ONLY)) {
            result.add(info.activityInfo.packageName);
        }
        return result;
    }
    static boolean isActivity(PackageManager manager, String pkg, String name) {
        if (pkg == null || name == null || name.startsWith("android.widget.") || name.startsWith("android.view.")) return false;
        try {
            manager.getActivityInfo(new ComponentName(pkg, name), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            // Some TV firmware limits package visibility even for foreground activities.
            return name.endsWith("Activity");
        }
    }
    private TvInputApps() { }
}
