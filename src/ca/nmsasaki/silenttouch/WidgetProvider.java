package ca.nmsasaki.silenttouch;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Calendar;
import java.text.DateFormat;

public class WidgetProvider extends AppWidgetProvider {

//	// when testing minimum is 2 minutes because I round seconds down to 00 to timer expires on minute change
//	//private static final long SILENT_DURATION_MILLISECONDS = 30 * 60 * 1000;
//	private static final long SILENT_DURATION_MILLISECONDS = 2 * 60 * 1000;
	private static final String TAG = "SilentTouch";
//	private static final int MY_NOTIFICATION_ID = 1;
//
	public static final String INTENT_ACTION_WIDGET_CLICK = "ca.nmsasaki.silenttouch.INTENT_ACTION_WIDGET_CLICK";
//	private static final String INTENT_ACTION_NOTIFICATION_CANCEL_CLICK = "ca.nmsasaki.silenttouch.INTENT_ACTION_NOTIFICATION_CANCEL_CLICK";
//	private static final String INTENT_ACTION_TIMER_EXPIRED = "ca.nmsasaki.silenttouch.INTENT_ACTION_TIMER_EXPIRED";

	// mToast is used to cancel an existing toast if user clicks on widget in succession
	// should be ok if this state gets cleaned up by android
//	private static Toast mToast = null;
//	
//	// TODO: remove dependence on this variable used for storing original alarm state
//	private static int mRingerMode = AudioManager.RINGER_MODE_NORMAL;
//	// TODO: remove dependence on this variable used for updating an existing alarm
//	private static long mAlarmExpire = 0;
//	// TODO: remove dependence on this variable used for updating exiting timer
//	private static PendingIntent mAlarmIntent = null;

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		Log.d(TAG, "MyWidgetProvider::onEnabled");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// super.onReceive(context, intent);

		Log.i(TAG, "MyWidgetProvider::onReceive - enter");

		final String curIntentAction = intent.getAction();
		Log.i(TAG, "onReceive Intent=" + curIntentAction);

//		if (curIntentAction == INTENT_ACTION_WIDGET_CLICK) {
//			UserClickedWidget(context);
//		} else if (curIntentAction == INTENT_ACTION_TIMER_EXPIRED) {
//			TimerExpired(context);
//		} else if (intent.getAction() == INTENT_ACTION_NOTIFICATION_CANCEL_CLICK) {
//			UserClickedCancel(context);
//		}

		// ----------------------------------------------------

		// Update the widgets via the service
		// ----------------------------------------------------
		Log.i(TAG, "MyWidgetProvider::Start Service");
		Intent serviceIntent = new Intent(context, WidgetService.class);
		serviceIntent.setAction(intent.getAction());
		context.startService(serviceIntent);
		// ----------------------------------------------------

		
		super.onReceive(context, intent);
		Log.i(TAG, "MyWidgetProvider::onReceive - exit");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.i(TAG, "MyWidgetProvider::onUpdate - enter");

		// Register an onClickListener for widget update
		// ----------------------------------------------------

		ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

		Intent updateIntent = new Intent(context, WidgetProvider.class);
		updateIntent.setAction(WidgetProvider.INTENT_ACTION_WIDGET_CLICK);
		PendingIntent updatePIntent = PendingIntent.getBroadcast(context, 0, updateIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.widget_image, updatePIntent);
		appWidgetManager.updateAppWidget(thisWidget, remoteViews);
		Log.i(TAG, "MyWidgetProvider::onUpdate - Register Widget Click Intent");

		Log.i(TAG, "MyWidgetProvider::onUpdate - exit");

	}
	
	
//	/* ***********************************************
//		1. Restore Ringer Mode
//		2. Cancel Timer
//		3. Clear Notification
//	 ************************************************* */
//	private void UserClickedCancel(Context context) {
//		Log.i(TAG, "INTENT_ACTION_NOTIFICATION_CANCEL_CLICK - enter");
//
//		// User canceled the mode manually from notifications
//
//		// -----------------------------------------------
//		// Restore previous RingerMode
//		AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//		final int curAudioMode = audioMgr.getRingerMode();
//
//		final String curModeString = RingerMode_intToString(curAudioMode);
//		Log.i(TAG, "AudioMode=" + curModeString);
//
//		if (curAudioMode == AudioManager.RINGER_MODE_SILENT) {
//			audioMgr.setRingerMode(mRingerMode);
//			Log.i(TAG, String.format("AudioMode=%d",mRingerMode));
//		}
//
//		// -------------------------------------------
//		// cancel timer
//		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//		
//		// TODO: Should be able to handle null intent here by building one
//		if (mAlarmIntent != null) {
//			alarmMgr.cancel(mAlarmIntent);
//		}
//
//		// -------------------------------------------
//		// clear notification
//		NotificationManager notiMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//		notiMgr.cancel(MY_NOTIFICATION_ID);
//		mAlarmIntent = null;
//		
//		Log.i(TAG, "INTENT_ACTION_NOTIFICATION_CANCEL_CLICK - exit");
//
//		
//	}
//
//	/* ***********************************************
//		1. Restore Ringer Mode
//		2. Build Notification
//	 ************************************************* */
//	private void TimerExpired(Context context) {
//
//		Log.i(TAG, "INTENT_ACTION_TIMER_EXPIRED - enter");
//
//		// Timer expired - restore previous notification type
//
//		// --------------------------------------------------
//		// enable previous ringerMode
//		AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//		final int curAudioMode = audioMgr.getRingerMode();
//		final String curModeString = RingerMode_intToString(curAudioMode);
//		
//		Log.i(TAG, "AudioMode=" + curModeString);
//
//		if (curAudioMode == AudioManager.RINGER_MODE_SILENT) {
//			Log.i(TAG, String.format("before AudioMode=%d", mRingerMode));
//			audioMgr.setRingerMode(mRingerMode);
//			Log.i(TAG, String.format("after AudioMode=%d", mRingerMode));
//		}
//
//		// -------------------------------------------
//		// Build notification to say timer expired
//		final long alarmExpired = System.currentTimeMillis();
//		final String dateStringLog = DateFormat.getTimeInstance(DateFormat.LONG).format(alarmExpired);
//		Log.i(TAG, "onReceive Actual expireTime:" + dateStringLog);
//
//		final String notiTitle = context.getString(R.string.notification_title);
//		String notiContentText = context.getString(R.string.notification_OFF_timer);
//		final String dateStringUser = DateFormat.getTimeInstance(DateFormat.SHORT).format(alarmExpired);
//		notiContentText = String.format(notiContentText, dateStringUser);
//
//		// Define the Notification's expanded message and Intent:
//		Notification.Builder notiBuilder = new Notification.Builder(context)
//				.setSmallIcon(R.drawable.ic_notify)
//				.setContentTitle(notiTitle).setContentText(notiContentText);
//
//		// Pass the Notification to the NotificationManager:
//		NotificationManager notiMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//		notiMgr.notify(MY_NOTIFICATION_ID, notiBuilder.build());
//
//		mAlarmIntent = null;
//		Log.i(TAG, "INTENT_ACTION_TIMER_EXPIRED - exit");
//		
//	}
//
//	/* ***********************************************
//		1. Set New Timer or Update Timer
//		2. Set RingerMode
//		3. Create Toast
//		4. Build Notification
//	 ************************************************* */
//	private void UserClickedWidget(Context context) {
//		// User clicked on widget
//		// Perform actions before notifying user
//		// this will expose performance delays and show bugs
//
//		Log.i(TAG, "INTENT_ACTION_WIDGET_CLICK - enter");
//		
//		// ----------------------------------------------------
//		// check current ringermode
//		AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//		final int curAudioMode = audioMgr.getRingerMode();
//		final String curModeString = RingerMode_intToString(curAudioMode);
//
//		Log.i(TAG, "AudioMode=" + curModeString);
//
//		// --------------------------------------------------
//		// set timer to re-enable original ringer mode
//		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//		if (mAlarmIntent == null) {
//			// Create a NEW timer for Canceling Silent Mode
//			// -----------------------------------------
//			Intent intentAlarmReceiver = new Intent(context, WidgetProvider.class);
//			intentAlarmReceiver.setAction(INTENT_ACTION_TIMER_EXPIRED);
//			mAlarmIntent = PendingIntent.getBroadcast(context, 0, intentAlarmReceiver, 0);
//			
//			mAlarmExpire = System.currentTimeMillis() + SILENT_DURATION_MILLISECONDS;
//			mAlarmExpire = truncateSeconds(mAlarmExpire);
//			
//			alarmMgr.set(AlarmManager.RTC_WAKEUP, mAlarmExpire, mAlarmIntent);
//			
//			// set ringer mode to restore here
//			// do NOT set it when timer is being updated (it would already be silent)
//			// if the ringer mode is silent already, then set restore mode to normal
//			mRingerMode = curAudioMode;
//			if (curAudioMode == AudioManager.RINGER_MODE_SILENT){
//				mRingerMode = AudioManager.RINGER_MODE_NORMAL;
//			}
//			
//		} else {
//			// Update existing timer
//			// ------------------------------------------
//
//			mAlarmExpire = mAlarmExpire + SILENT_DURATION_MILLISECONDS;
//			mAlarmExpire = truncateSeconds(mAlarmExpire);
//			alarmMgr.set(AlarmManager.RTC_WAKEUP, mAlarmExpire, mAlarmIntent);
//		}
//		
//		
//		final String dateStringLog = DateFormat.getTimeInstance(DateFormat.LONG).format(mAlarmExpire);
//		Log.i(TAG, "onReceive Set expireTime=" + dateStringLog);
//
//		// ----------------------------------------------------
//		// set ringer mode after alarm is set 
//		// to ensure mode will become re-enabled
//		if (curAudioMode != AudioManager.RINGER_MODE_SILENT) {
//			audioMgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
//			Log.i(TAG, "AudioMode=RINGER_MODE_SILENT");
//		}
//
//		// ----------------------------------------------------
//		// Create toast to alert user
//		String toastText = context.getString(R.string.toast_ON);
//		final String dateStringUser = DateFormat.getTimeInstance(DateFormat.SHORT).format(mAlarmExpire);
//		toastText = String.format(toastText, dateStringUser);
//
//		if (mToast != null) {
//			mToast.cancel();
//		}
//		mToast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
//		mToast.show();
//
//		// ------------------------------------------------
//		// Build notification
//
//		// notification strings
//		final String notiTitle = context.getString(R.string.notification_title);
//		final String notiCancel = context.getString(R.string.notification_ON_cancel);
//
//		String notiContentText = context.getString(R.string.notification_ON_content);
//		notiContentText = String.format(notiContentText, dateStringUser);
//
//		// Pending intent to be fired when notification is clicked
//		Intent notiIntent = new Intent(context, WidgetProvider.class);
//		notiIntent.setAction(INTENT_ACTION_NOTIFICATION_CANCEL_CLICK);
//		PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, 0, notiIntent, 0);
//
//		// TODO: find actual vibrate icon - cannot find official vibrate icon
//		// use regular notification icon 
//		int iconId = android.R.drawable.ic_lock_silent_mode_off;
////		if (mRingerMode == AudioManager.RINGER_MODE_VIBRATE) {
////			iconId = android.R.drawable.;
////		}
//
//		// Define the Notification's expanded message and Intent:
//		Notification.Builder notiBuilder = new Notification.Builder(
//				context)
//				.setSmallIcon(R.drawable.ic_notify)
//				.setContentTitle(notiTitle)
//				.setContentText(notiContentText)
//				.addAction(iconId, notiCancel, cancelPendingIntent);
//
//		// Pass the Notification to the NotificationManager:
//		NotificationManager notMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//		notMgr.notify(MY_NOTIFICATION_ID, notiBuilder.build());
//
//		Log.i(TAG, "INTENT_ACTION_WIDGET_CLICK - exit");
//
//	}
//
//	// ********************************
//	// Sets seconds portion of input time to 0
//	// e.g. 12:30:17 -> 12:30:00
//	public static long truncateSeconds(long timeInMilliseconds){
//		long returnValue = 0;
//
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTimeInMillis(timeInMilliseconds);
//		calendar.set(Calendar.SECOND,0);
//		returnValue = calendar.getTimeInMillis();
//
//		return returnValue;
//		}
//	
//	// ********************************
//	// convert integer of RingMode into a string
//	// 
//	public static String RingerMode_intToString(int curAudioMode) {
//
//		String returnString = "UNKNOWN_MODE";
//		
//		switch (curAudioMode) {
//		case AudioManager.RINGER_MODE_SILENT:
//			returnString = "0-RINGER_MODE_SILENT";
//			break;
//		case AudioManager.RINGER_MODE_VIBRATE:
//			returnString = "1-RINGER_MODE_VIBRATE";
//			break;
//		case AudioManager.RINGER_MODE_NORMAL:
//			returnString = "2-RINGER_MODE_NORMAL";
//			break;
//		default:
////			Integer intObj = curAudioMode;
////			returnString = intObj.toString() + "-UNKNOWN_MODE";
//			returnString = curAudioMode + "-UNKNOWN_MODE";
////			returnString = (String) returnString.toCharArray();
//			break;
//		}
//		return returnString;
//	}


}
