/**
 * 
 */
package ca.nmsasaki.silenttouch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * @author Neil Dev
 *
 */
//TODO: SuppressLint investigate this

@SuppressLint("NewApi")
public class RingerModeListener extends ContentObserver {
	
	private static final String TAG = "SilentTouch";

	public RingerModeListener(Handler handler) {
		super(handler);
	}

	@Override
	public void onChange(boolean SelfChange) {
		this.onChange(SelfChange, null);
	}
	
	@Override
	public void onChange(boolean SelfChange, Uri uri) {
		//TODO: Handle Settings Change
		
		Log.i(TAG, "RingerModeListener::onReceive - enter");

		if (SelfChange) {
			Log.i(TAG, "Ignore Self Change");
		} else {
			Log.i(TAG, "Cancel Timer");
		}
		// ----------------------------------------------------
		// Update the widgets via the service
		// ----------------------------------------------------
//		Log.i(TAG, "RingerModeListener::Start Service");
//		Context context = getApplicationContext();
//		Intent serviceIntent = new Intent(context, WidgetService.class);
//		serviceIntent.setAction(intent.getAction());
//		context.startService(serviceIntent);
		// ----------------------------------------------------

		
		Log.i(TAG, "RingerModeListener::onReceive - exit");
		
	}
	
}
