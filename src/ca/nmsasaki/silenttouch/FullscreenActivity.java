package ca.nmsasaki.silenttouch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Intent;
//import android.content.res.Resources;
//import android.widget.Button;


public class FullscreenActivity extends Activity {

	private static final String TAG = "SilentTouch";
//	private static final int MY_NOTIFICATION_ID = 1;

//	private Button mTestButton = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "FullscreenActivity::onCreate");

		Log.d(TAG, "StartService with INTENT_USER_CLICK");
		Context context = getApplicationContext();
		Intent serviceIntent = new Intent(context, WidgetService.class);
		serviceIntent.setAction(WidgetService.INTENT_USER_CLICK);
		context.startService(serviceIntent);
		
		Log.d(TAG, "FullscreenActivity::killActivity");
		finish();

//		setContentView(R.layout.activity_fullscreen);
//
//		final Button mTestButton = (Button) findViewById(R.id.test_button);
//		
//		Resources res = getResources();
//		final String notiTitle = res.getString(R.string.notification_title);
//		final String notiContentText = res.getString(R.string.notification_ON_content);
//		final String notiCancel = res.getString(R.string.notification_ON_cancel);
//		
//		mTestButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//
//				Log.i(TAG, "FullscreenActivity::onButtonClick");
//
//				// Pending intent to be fired when notification is clicked
//				Intent intent = new Intent(v.getContext(), FullscreenActivity.class);
//				PendingIntent cancelPendingIntent = PendingIntent.getActivity(v.getContext(), 01,
//						intent, Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//				
//				// Define the Notification's expanded message and Intent:
//				Notification.Builder notificationBuilder = new Notification.Builder(
//						getApplicationContext())
//						.setSmallIcon(android.R.drawable.ic_lock_silent_mode)
//						.setContentTitle(notiTitle)
//						.setContentText(notiContentText)
//						.addAction(android.R.drawable.ic_lock_silent_mode_off, notiCancel, cancelPendingIntent);
//
//				// Pass the Notification to the NotificationManager:
//				NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//				mNotificationManager.notify(MY_NOTIFICATION_ID,
//						notificationBuilder.build());
//			}
//		});
	}

}
