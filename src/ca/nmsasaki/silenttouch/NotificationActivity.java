package ca.nmsasaki.silenttouch;

import ca.nmsasaki.silentphonetimer.R;
import android.app.Activity;
import android.os.Bundle;

public class NotificationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notification_on_layout);
	}
}