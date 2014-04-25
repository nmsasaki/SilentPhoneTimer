/**    Copyright 2014 Neil Sasaki

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   Class: FullScreenActivity
   Description: Entry point to trigger functionality.
    Starts service then exits. There is no actual UI.
    
    A launch-able activity was better than a widget because:
    1. I could put it in the application doc. 
    2. Widget has no state to show
    3. I can add the shortcut to the home screen on install 
 */


package ca.nmsasaki.android.silent30;

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

	private static final String TAG = "Silent30";
//	private static final int NOTIFICATION_ID = 1;

//	private Button mTestButton = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "FullscreenActivity::onCreate");

		Log.d(TAG, "StartService with ACTION_SHORTCUT_CLICK");
		Context context = getApplicationContext();
		Intent serviceIntent = new Intent(context, WidgetService.class);
		serviceIntent.setAction(WidgetService.ACTION_SHORTCUT_CLICK);
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
//				mNotificationManager.notify(NOTIFICATION_ID,
//						notificationBuilder.build());
//			}
//		});
	}

}
