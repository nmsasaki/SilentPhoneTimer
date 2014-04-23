package ca.nmsasaki.silenttouch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShortcutReceiver extends BroadcastReceiver {

	private static final String TAG = "SilentTouch";
	
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i(TAG, "ShortcutReceiver::onReceive - enter");

		final String curIntentAction = intent.getAction();
		Log.i(TAG, "onReceive Intent=" + curIntentAction);

		// ----------------------------------------------------
		// Update the widgets via the service
		// ----------------------------------------------------
		Log.i(TAG, "ShortcutReceiver::Start Service");
		Intent serviceIntent = new Intent(context, WidgetService.class);
		serviceIntent.setAction(intent.getAction());
		context.startService(serviceIntent);
		// ----------------------------------------------------

		
		Log.i(TAG, "ShortcutReceiver::onReceive - exit");
		
	}

	
	
}
