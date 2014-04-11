package ca.nmsasaki.silenttouch;

import ca.nmsasaki.silentphonetimer.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ShortcutActivity extends Activity {

	private static final String TAG = "SilentTouch";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i(TAG, "ShortcutActivity::onCreate - enter");

		Intent i= new Intent();
	    Intent shortcutActivity = new Intent(this, FullscreenActivity.class);

	    i.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutActivity);
	    i.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_diplay_name);
	    i.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launchf));
	    
	    setResult(RESULT_OK, i);
	    
		Log.i(TAG, "ShortcutActivity::onCreate - exit1");

	    finish();

		Log.i(TAG, "ShortcutActivity::onCreate - exit2");

	}

}
