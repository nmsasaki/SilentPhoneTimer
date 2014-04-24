/**
 *  To use this class, you must implement the RingerModeListenerHandler interface
 *  to perform the call back action
 *  
 */
package ca.nmsasaki.silenttouch;

import android.annotation.SuppressLint;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/* 
 * @SupressLint("NewApi") forces android to use the newer onChange method rather than
 * the older one. This means this code will NOT work on older devices
*/
@SuppressLint("NewApi")
public class RingerModeListener extends ContentObserver {
	
	private static final String TAG = "SilentTouch";

	public interface RingerModeListenerHandler {
		public void onChange(boolean SelfChange, Uri uri);
	}
	
	private RingerModeListenerHandler mRingerModeListenerHandler = null;

	public RingerModeListener(Handler handler, RingerModeListenerHandler ringerModeListenerHandler ) {
		super(handler);
		
		Log.d(TAG, "RingerModeListener::Constructor");
		mRingerModeListenerHandler = ringerModeListenerHandler;
	}

	/* older method deprecated and not used because of @SuppressLint above */
	@Override
	public void onChange(boolean SelfChange) {
		this.onChange(SelfChange, null);
		Log.w(TAG, String.format("RingerModeListener::onChange(selfChange), %b", SelfChange));
	}
	
	@Override
	public void onChange(boolean SelfChange, Uri uri) {
		
		Log.d(TAG, "RingerModeListener::onChange(selfChange, URI)");
		mRingerModeListenerHandler.onChange(SelfChange, uri);
		
	}
	
}
