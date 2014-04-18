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
import java.util.GregorianCalendar;
import java.text.DateFormat;

public class MyWidgetProvider extends AppWidgetProvider {

	// when testing minimum is 2 minutes because I round seconds down to 00 to timer expires on minute change
	//private static final long SILENT_DURATION_MILLISECONDS = 30 * 60 * 1000;
	private static final long SILENT_DURATION_MILLISECONDS = 2 * 60 * 1000;
	private static final String TAG = "SilentTouch";
	private static final int MY_NOTIFICATION_ID = 1;

	private static PendingIntent mAlarmIntent = null;
	private static Toast mToast = null;

	private static final String INTENT_ACTION_WIDGET_CLICK = "ca.nmsasaki.silenttouch.INTENT_ACTION_WIDGET_CLICK";
	private static final String INTENT_ACTION_NOTIFICATION_CANCEL_CLICK = "ca.nmsasaki.silenttouch.INTENT_ACTION_NOTIFICATION_CANCEL_CLICK";
	private static final String INTENT_ACTION_TIMER_EXPIRED = "ca.nmsasaki.silenttouch.INTENT_ACTION_TIMER_EXPIRED";

	private static long mAlarmExpire = 0;

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

		if (curIntentAction == INTENT_ACTION_WIDGET_CLICK) {
			// User clicked on widget
			// Perform actions before notifying user
			// this will expose performance delays and show bugs
			
			Log.i(TAG, "INTENT_ACTION_WIDGET_CLICK - enter");
			
			// ----------------------------------------------------
			// check current ringermode
			AudioManager audioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			final int curAudioMode = audioManager.getRingerMode();
			
			// TODO: Extract RINGERMODE string to function
			String curModeString = "UNKNOWN";

			switch (curAudioMode) {
			case AudioManager.RINGER_MODE_NORMAL:
				curModeString = "NORMAL";
				break;
			case AudioManager.RINGER_MODE_SILENT:
				curModeString = "SILENT";
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				curModeString = "VIBRATE";
				break;
			default:
				break;
			}

			Log.i(TAG, "AudioMode=" + curModeString);

			// --------------------------------------------------
			// set timer to re-enable original ringer mode
			AlarmManager alarmMgr = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);

			// TODO: move calendar getInstance and refactor code to extract truncate seconds 
			// into separate function that takes start time as a function 
			Calendar calendar = Calendar.getInstance();
			
			if (mAlarmIntent == null) {
				// Create a NEW timer for Canceling Silent Mode
				// -----------------------------------------
				Intent intentAlarmReceiver = new Intent(context,
						MyWidgetProvider.class);
				intentAlarmReceiver.setAction(INTENT_ACTION_TIMER_EXPIRED);

				mAlarmIntent = PendingIntent.getBroadcast(context, 0,
						intentAlarmReceiver, 0);

				mAlarmExpire = System.currentTimeMillis()
						+ SILENT_DURATION_MILLISECONDS;
				// truncate seconds
				calendar.setTimeInMillis(mAlarmExpire);
				calendar.set(Calendar.SECOND,0);
				mAlarmExpire = calendar.getTimeInMillis();
				
				alarmMgr.set(AlarmManager.RTC_WAKEUP, mAlarmExpire,
						mAlarmIntent);
				
				Log.i(TAG, "AudioMode=" + curModeString);

			} else {
				// Update existing timer
				// ------------------------------------------

				mAlarmExpire = mAlarmExpire + SILENT_DURATION_MILLISECONDS;
				// truncate seconds
				calendar.setTimeInMillis(mAlarmExpire);
				calendar.set(Calendar.SECOND,0);
				mAlarmExpire = calendar.getTimeInMillis();
				
				alarmMgr.set(AlarmManager.RTC_WAKEUP, mAlarmExpire,
						mAlarmIntent);
			}
			
			
			// TODO: Extract Dateformatting to function
			final DateFormat dateFormatterUser = DateFormat
					.getTimeInstance(DateFormat.SHORT);
			final DateFormat dateFormatterLog = DateFormat
					.getTimeInstance(DateFormat.LONG);

			final String dateStringUser = dateFormatterUser
					.format(mAlarmExpire);
			final String dateStringLog = dateFormatterLog.format(mAlarmExpire);

			Log.i(TAG, "onReceive Set expireTime=" + dateStringLog);

			// ----------------------------------------------------
			// set ringer mode after alarm is set 
			// to ensure mode will become re-enabled
			if (curAudioMode != AudioManager.RINGER_MODE_SILENT) {
				audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				Log.i(TAG, "AudioMode=RINGER_MODE_SILENT");
			}

			// ----------------------------------------------------
			// Create toast to alert user

			String toastText = context.getString(R.string.toast_ON);
			toastText = String.format(toastText, dateStringUser);

			if (mToast != null) {
				mToast.cancel();
			}
			mToast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
			mToast.show();

			// ------------------------------------------------
			// Build notification

			// notification strings
			final String notiTitle = context
					.getString(R.string.notification_title);
			final String notiCancel = context
					.getString(R.string.notification_ON_cancel);

			String notiContentText = context
					.getString(R.string.notification_ON_content);
			notiContentText = String.format(notiContentText, dateStringUser);

			// Pending intent to be fired when notification is clicked
			Intent notiIntent = new Intent(context, MyWidgetProvider.class);
			notiIntent.setAction(INTENT_ACTION_NOTIFICATION_CANCEL_CLICK);
			PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(
					context, 0, notiIntent, 0);

			// Define the Notification's expanded message and Intent:
			Notification.Builder notificationBuilder = new Notification.Builder(
					context)
					.setSmallIcon(R.drawable.ic_notify)
					.setContentTitle(notiTitle)
					.setContentText(notiContentText)
					.addAction(android.R.drawable.ic_lock_silent_mode_off,
							notiCancel, cancelPendingIntent);

			// Pass the Notification to the NotificationManager:
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(MY_NOTIFICATION_ID,
					notificationBuilder.build());

			Log.i(TAG, "INTENT_ACTION_WIDGET_CLICK - exit");

		} else if (curIntentAction == INTENT_ACTION_TIMER_EXPIRED) {
			Log.i(TAG, "INTENT_ACTION_TIMER_EXPIRED - enter");

			// Timer expired - restore previous notification type

			// --------------------------------------------------
			// enable previous ringerMode
			AudioManager audioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			final int curAudioMode = audioManager.getRingerMode();

			// TODO: Extract RINGERMODE string to function
			String curModeString = "UNKNOWN";
			switch (curAudioMode) {
			case AudioManager.RINGER_MODE_NORMAL:
				curModeString = "NORMAL";
				break;
			case AudioManager.RINGER_MODE_SILENT:
				curModeString = "SILENT";
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				curModeString = "VIBRATE";
				break;
			default:
				break;
			}
			Log.i(TAG, "AudioMode=" + curModeString);


			if (curAudioMode == AudioManager.RINGER_MODE_SILENT) {
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				Log.i(TAG, "AudioMode=RINGER_MODE_NORMAL");
			}

			// -------------------------------------------
			// Build notification to say timer expired
			final long alarmExpired = System.currentTimeMillis();
			
			// TODO: Extract Dateformatting to function
			final DateFormat dateFormatterUser = DateFormat
					.getTimeInstance(DateFormat.SHORT);
			final DateFormat dateFormatterLog = DateFormat
					.getTimeInstance(DateFormat.LONG);
			String dateStringUser = dateFormatterUser.format(alarmExpired);
			String dateStringLog = dateFormatterLog.format(alarmExpired);
			Log.i(TAG, "onReceive Actual expireTime:" + dateStringLog);

			final String notiTitle = context
					.getString(R.string.notification_title);

			String notiContentText = context
					.getString(R.string.notification_OFF_timer);
			notiContentText = String.format(notiContentText, dateStringUser);

			// Define the Notification's expanded message and Intent:
			Notification.Builder notificationBuilder = new Notification.Builder(
					context)
					.setSmallIcon(R.drawable.ic_notify)
					.setContentTitle(notiTitle).setContentText(notiContentText);

			// Pass the Notification to the NotificationManager:
			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(MY_NOTIFICATION_ID,
					notificationBuilder.build());

			mAlarmIntent = null;
			Log.i(TAG, "INTENT_ACTION_TIMER_EXPIRED - exit");

		} else if (intent.getAction() == INTENT_ACTION_NOTIFICATION_CANCEL_CLICK) {
			Log.i(TAG, "INTENT_ACTION_NOTIFICATION_CANCEL_CLICK - enter");

			// User canceled the mode manually from notifications

			// -----------------------------------------------
			// Restore previous RingerMode
			AudioManager audioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			final int curAudioMode = audioManager.getRingerMode();
			String curModeString = "UNKNOWN";

			switch (curAudioMode) {
			case AudioManager.RINGER_MODE_NORMAL:
				curModeString = "NORMAL";
				break;
			case AudioManager.RINGER_MODE_SILENT:
				curModeString = "SILENT";
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				curModeString = "VIBRATE";
				break;
			default:
				break;
			}
			Log.i(TAG, "AudioMode=" + curModeString);

			if (curAudioMode == AudioManager.RINGER_MODE_SILENT) {
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				Log.i(TAG, "AudioMode=RINGER_MODE_NORMAL");
			}

			// -------------------------------------------
			// cancel timer
			AlarmManager alarmMgr = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			if (alarmMgr != null && mAlarmIntent != null) {
				alarmMgr.cancel(mAlarmIntent);
			}

			// -------------------------------------------
			// clear notification
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(MY_NOTIFICATION_ID);

			mAlarmIntent = null;
			
			Log.i(TAG, "INTENT_ACTION_NOTIFICATION_CANCEL_CLICK - exit");
		}

		super.onReceive(context, intent);
		Log.i(TAG, "MyWidgetProvider::onReceive - exit");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.i(TAG, "MyWidgetProvider::onUpdate - enter");

		ComponentName thisWidget = new ComponentName(context,
				MyWidgetProvider.class);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);

		// Register an onClickListener for widget update
		// ----------------------------------------------------
		Intent updateIntent = new Intent(context, MyWidgetProvider.class);
		updateIntent.setAction(INTENT_ACTION_WIDGET_CLICK);
		// updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		// updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
		// appWidgetIds);
		PendingIntent updatePIntent = PendingIntent.getBroadcast(context, 0,
				updateIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.widget_image, updatePIntent);
		appWidgetManager.updateAppWidget(thisWidget, remoteViews);
		
		Log.i(TAG, "MyWidgetProvider::onUpdate - Register Widget Click Intent");
		// ----------------------------------------------------

		// // Update the widgets via the service
		// ----------------------------------------------------
		// context.startService(intent);
		// ----------------------------------------------------

		Log.i(TAG, "MyWidgetProvider::onUpdate - exit");

	}

}
