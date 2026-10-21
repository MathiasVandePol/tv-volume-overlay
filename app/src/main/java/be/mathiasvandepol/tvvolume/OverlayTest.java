package be.mathiasvandepol.tvvolume;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/** The receiver is non-exported; only this app can request an explicitly labelled sample. */
final class OverlayTest {
    static final String ACTION = "be.mathiasvandepol.tvvolume.action.TEST_OVERLAY";
    static void show(Context context) {
        context.sendOrderedBroadcast(new Intent(ACTION).setPackage(context.getPackageName()), null,
                new BroadcastReceiver() {
                    @Override public void onReceive(Context receiverContext, Intent intent) {
                        if (getResultCode() != Activity.RESULT_OK) {
                            Toast.makeText(receiverContext, R.string.test_unavailable, Toast.LENGTH_LONG).show();
                        }
                    }
                }, null, Activity.RESULT_CANCELED, null, null);
    }
    private OverlayTest() { }
}
