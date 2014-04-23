package ca.nmsasaki.silenttouch;

import java.text.DateFormat;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class WidgetService extends Service {

	// when testing minimum is 2 minutes because I round seconds down to 00 to
	// timer expires on minute change
	//private static final long SILENT_DURATION_MILLISECONDS = 2 * 60 * 1000;
	public static final long SILENT_DURATION_MILLISECONDS = 30 * 60 * 1000;
	private static final String TAG = "SilentTouch";
	private static final int MY_NOTIFICATION_ID = 1;

	// private static final String INTENT_ACTION_WIDGET_CLICK = "ca.nmsasaki.silenttouch.INTENT_ACTION_WIDGET_CLICK";
	private static final String PREF_NAME = "ca.nmsasaki.silenttouch.prefs";
	private static final String PREF_NAME_ALARM_EXPIRE = "ca.nmsasaki.silenttouch.prefs.alarm_expire";
	private static final String PREF_NAME_ORIGINAL_RINGER_MODE = "ca.nmsasaki.silenttouch.prefs.orig_ringer_mode";
	public static final String INTENT_USER_CLICK = "ca.nmsasaki.silenttouch.INTENT_USER_CLICK";
	private static final String INTENT_ACTION_NOTIFICATION_CANCEL_CLICK = "ca.nmsasaki.silenttouch.INTENT_ACTION_NOTIFICATION_CANCEL_CLICK";
	private static final String INTENT_ACTION_TIMER_EXPIRED = "ca.nmsasaki.silenttouch.INTENT_ACTION_TIMER_EXPIRED";

	// TODO: remove static mToast
	// mToast is used to cancel an existing toast if user clicks on widget in succession
	// seems to work find and should be ok if this state gets cleaned up by android
	private static Toast mToast = null;

	
	private int mOriginalRingerMode = AudioManager.RINGER_MODE_NORMAL;
	private long mAlarmExpireTime = 0;
	private boolean mPrefsUpdated = false;


	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "WidgetService::onStart - enter");

		final String curIntentAction = intent.getAction();
		final Context context = getApplicationContext();
		
		SharedPreferences prefs = readPrefs(context);

		if (curIntentAction.equals(INTENT_USER_CLICK)) {
			UserClickedWidget(context);
		} else if (curIntentAction.equals(INTENT_ACTION_TIMER_EXPIRED)) {
			TimerExpired(context);
		} else if (curIntentAction.equals(INTENT_ACTION_NOTIFICATION_CANCEL_CLICK)) {
			UserClickedCancel(context);
		}
		
		writePrefs(prefs);
		
		Log.i(TAG, "WidgetService::onStart - exit");
		stopSelf();

	}

	/**
	 * @param prefs
	 * 
	 * if preferences were updated, then write to storage 
	 */
	private void writePrefs(SharedPreferences prefs) {
		if (isPrefsUpdated()) {
			Editor editor = prefs.edit();
			editor.putInt(PREF_NAME_ORIGINAL_RINGER_MODE, getOriginalRingerMode());
			editor.putLong(PREF_NAME_ALARM_EXPIRE, getAlarmExpireTime());
			editor.commit();
		}
	}

	/**
	 * @param context
	 * @return SharedPreferences populated with app state
	 */
	private SharedPreferences readPrefs(final Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		setOriginalRingerMode(prefs.getInt(PREF_NAME_ORIGINAL_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL));
		setAlarmExpireTime(prefs.getLong(PREF_NAME_ALARM_EXPIRE, 0));
		return prefs;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "WigetService::onBind()");
		return null;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "WidgetService::onDestroy - exit");
	}

	/**
	 * @param context
	 * @return
	 * 
	 * 1. Restore Ringer Mode 
	 * 2. Cancel Timer 
	 * 3. Clear Notification
	 */
	private void UserClickedCancel(Context context) {
		Log.i(TAG, "INTENT_ACTION_NOTIFICATION_CANCEL_CLICK - enter");

		// User canceled the mode manually from notifications

		// -----------------------------------------------
		// Restore previous RingerMode
		AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		final int curAudioMode = audioMgr.getRingerMode();

		final String curModeString = RingerMode_intToString(curAudioMode);
		Log.i(TAG, "AudioMode=" + curModeString);

		if (curAudioMode == AudioManager.RINGER_MODE_SILENT) {
			audioMgr.setRingerMode(getOriginalRingerMode());
			Log.i(TAG, String.format("AudioMode=%d", getOriginalRingerMode()));
		}

		// -------------------------------------------
		// cancel timer
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = createAlarmIntent(context);
		alarmMgr.cancel(alarmIntent);

		// -------------------------------------------
		// clear notification
		NotificationManager notiMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notiMgr.cancel(MY_NOTIFICATION_ID);

		// set AlarmExpire to 0 to show that there is no more timer
		setAlarmExpireTime(0);
		
		Log.i(TAG, "INTENT_ACTION_NOTIFICATION_CANCEL_CLICK - exit");

	}

	/**
	 * @param context
	 * @return
	 * 1. Restore Ringer Mode 
	 * 2. Build Notification
	 */
	private void TimerExpired(Context context) {

		Log.i(TAG, "INTENT_ACTION_TIMER_EXPIRED - enter");

		// Timer expired - restore previous notification type

		// --------------------------------------------------
		// enable previous ringerMode
		AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		final int curAudioMode = audioMgr.getRingerMode();
		final String curModeString = RingerMode_intToString(curAudioMode);

		Log.i(TAG, "AudioMode=" + curModeString);

		if (curAudioMode == AudioManager.RINGER_MODE_SILENT) {
			Log.i(TAG, String.format("before AudioMode=%d", getOriginalRingerMode()));
			audioMgr.setRingerMode(getOriginalRingerMode());
			Log.i(TAG, String.format("after AudioMode=%d", getOriginalRingerMode()));
		}

		// -------------------------------------------
		// Build notification to say timer expired
		final long alarmExpired = System.currentTimeMillis();
		final String dateStringLog = DateFormat.getTimeInstance(DateFormat.LONG).format(alarmExpired);
		Log.i(TAG, "onReceive Actual expireTime:" + dateStringLog);

		final String notiTitle = context.getString(R.string.notification_title);
		String notiContentText = context.getString(R.string.notification_OFF_timer);
		final String dateStringUser = DateFormat.getTimeInstance(DateFormat.SHORT).format(alarmExpired);
		notiContentText = String.format(notiContentText, dateStringUser);

		// Define the Notification's expanded message and Intent:
		Notification.Builder notiBuilder = new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_notify).setContentTitle(notiTitle)
				.setContentText(notiContentText);

		// Pass the Notification to the NotificationManager:
		NotificationManager notiMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notiMgr.notify(MY_NOTIFICATION_ID, notiBuilder.build());
		
		// set AlarmExpire to 0 to show that there is no more timer
		setAlarmExpireTime(0);
		
		Log.i(TAG, "INTENT_ACTION_TIMER_EXPIRED - exit");

	}

	/**
	 * @param context
	 * @return
	 * 
	 * 1. Set New Timer or Update Timer 
	 * 2. Set RingerMode 
	 * 3. Create Toast 
	 * 4. Build Notification
	 */
	private void UserClickedWidget(Context context) {
		// User clicked on widget
		// Perform actions before notifying user
		// this will expose performance delays and show bugs

		Log.i(TAG, "INTENT_ACTION_WIDGET_CLICK - enter");

		// ----------------------------------------------------
		// check current ringermode
		AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		final int curAudioMode = audioMgr.getRingerMode();
		final String curModeString = RingerMode_intToString(curAudioMode);
		Log.i(TAG, "AudioMode=" + curModeString);

		// --------------------------------------------------
		// set timer to re-enable original ringer mode
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		if (getAlarmExpireTime() == 0) {
			// record current state
			// set time for silence to expire
			setAlarmExpireTime(System.currentTimeMillis() + SILENT_DURATION_MILLISECONDS);

			// set ringer mode to restore here
			// if the ringer mode is silent already, then set restore mode to normal
			//     (otherwise, why did they click on widget... assume they want it to expire)
			if (curAudioMode == AudioManager.RINGER_MODE_SILENT) {
				setOriginalRingerMode(AudioManager.RINGER_MODE_NORMAL);
			} else {
				setOriginalRingerMode(curAudioMode);
			}

		} else {
			// set time for silence to expire based on previous value
			setAlarmExpireTime(getAlarmExpireTime() + SILENT_DURATION_MILLISECONDS);
		}
		
		PendingIntent alarmIntent = createAlarmIntent(context);
		setAlarmExpireTime(truncateSeconds(getAlarmExpireTime()));
		alarmMgr.set(AlarmManager.RTC_WAKEUP, getAlarmExpireTime(), alarmIntent);

		final String dateStringLog = DateFormat.getTimeInstance(DateFormat.LONG).format(getAlarmExpireTime());
		Log.i(TAG, "onReceive Set expireTime=" + dateStringLog);

		// ----------------------------------------------------
		// set ringer mode after alarm is set
		// to ensure mode will become re-enabled
		if (curAudioMode != AudioManager.RINGER_MODE_SILENT) {
			audioMgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			Log.i(TAG, "AudioMode=RINGER_MODE_SILENT");
		}

		// ----------------------------------------------------
		// Create toast to alert user
		String toastText = context.getString(R.string.toast_ON);
		final String dateStringUser = DateFormat.getTimeInstance(DateFormat.SHORT).format(
				getAlarmExpireTime());
		toastText = String.format(toastText, dateStringUser);

		if (mToast != null) {
			mToast.cancel();
		}
		mToast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
		mToast.show();

		// ------------------------------------------------
		// Build notification

		// notification strings
		final String notiTitle = context.getString(R.string.notification_title);
		final String notiCancel = context.getString(R.string.notification_ON_cancel);

		String notiContentText = context.getString(R.string.notification_ON_content);
		notiContentText = String.format(notiContentText, dateStringUser);

		// Pending intent to be fired when notification is clicked
		Intent notiIntent = new Intent(context, ShortcutReceiver.class);
		notiIntent.setAction(INTENT_ACTION_NOTIFICATION_CANCEL_CLICK);
		PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, 0, notiIntent, 0);

		// TODO: find actual vibrate icon - cannot find official vibrate icon
		// use regular notification icon
		int iconId = android.R.drawable.ic_lock_silent_mode_off;
		// if (mRingerMode == AudioManager.RINGER_MODE_VIBRATE) {
		// iconId = android.R.drawable.;
		// }

		// Define the Notification's expanded message and Intent:
		Notification.Builder notiBuilder = new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_notify).setContentTitle(notiTitle)
				.setContentText(notiContentText).addAction(iconId, notiCancel, cancelPendingIntent);

		// Pass the Notification to the NotificationManager:
		NotificationManager notMgr = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notMgr.notify(MY_NOTIFICATION_ID, notiBuilder.build());

		Log.i(TAG, "INTENT_ACTION_WIDGET_CLICK - exit");

	}

	/**
	 * @param context
	 * @return PendingIntent for AlarmManager
	 */
	private PendingIntent createAlarmIntent(Context context) {
		Intent intentAlarmReceiver = new Intent(context, ShortcutReceiver.class);
		intentAlarmReceiver.setAction(INTENT_ACTION_TIMER_EXPIRED);

		return PendingIntent.getBroadcast(context, 0, intentAlarmReceiver, 0);
	}

	/**
	 * @param timeInMilliseconds
	 * @return equivalent time in Milliseconds rounded down to nearest minute
	 */
	public static long truncateSeconds(long timeInMilliseconds) {
		long returnValue = 0;

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMilliseconds);
		calendar.set(Calendar.SECOND, 0);
		returnValue = calendar.getTimeInMillis();

		return returnValue;
	}

	/**
	 * @param curAudioMode from AudioManager.RINGER_MODE_XXXX
	 * @return String version of curAudioMode 
	 * 			if the ringer mode is unknown, include int value for reference debugging
	 */
	public static String RingerMode_intToString(int curAudioMode) {

		String returnString = "UNKNOWN_MODE";

		switch (curAudioMode) {
		case AudioManager.RINGER_MODE_SILENT:
			returnString = "0-RINGER_MODE_SILENT";
			break;
		case AudioManager.RINGER_MODE_VIBRATE:
			returnString = "1-RINGER_MODE_VIBRATE";
			break;
		case AudioManager.RINGER_MODE_NORMAL:
			returnString = "2-RINGER_MODE_NORMAL";
			break;
		default:
			// Integer intObj = curAudioMode;
			// returnString = intObj.toString() + "-UNKNOWN_MODE";
			returnString = curAudioMode + "-UNKNOWN_MODE";
			// returnString = (String) returnString.toCharArray();
			break;
		}
		return returnString;
	}

	private int getOriginalRingerMode() {
		return mOriginalRingerMode;
	}

	private void setOriginalRingerMode(int mOriginalRingerMode) {
		this.mOriginalRingerMode = mOriginalRingerMode;
		this.setPrefsUpdated(true);
	}

	private long getAlarmExpireTime() {
		return mAlarmExpireTime;
	}

	private void setAlarmExpireTime(long mAlarmExpireTime) {
		this.mAlarmExpireTime = mAlarmExpireTime;
		this.setPrefsUpdated(true);
	}

	private boolean isPrefsUpdated() {
		return mPrefsUpdated;
	}

	private void setPrefsUpdated(boolean mPrefsUpdated) {
		this.mPrefsUpdated = mPrefsUpdated;
	}

}
