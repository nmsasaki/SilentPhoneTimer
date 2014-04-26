/**
 *    Copyright 2014 Neil Sasaki

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   Class: WidgetService
   Description: This application sets the phone to mute 30 minute intervals.
     This class does all the main processing for the application.
     On initial action: ACTION_SHORTCUT_CLICK, sets phone to quiet mode for 30 minutes
     Other possible actions: 
     * ACTION_TIMER_EXPIRE
         * mute mode expired - need to restore regular notifications
     * ACTION_NOTIFICATION_CANCEL_CLICK
     *   * user manually cancels mute mode from notification drawer
     * ACTION_RINGERMODE_CHANGE
         * user manually changes mode from mute to something else (normal or vibrate)

 */

//3456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
//       1         2         3         4         5         6         7         8         9        10        11        12

package ca.nmsasaki.android.mute30;

import java.text.DateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
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
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

// SuppressLint needed for Settings.Global.MODE_RINGER and force using other newer API calls
// This means that the app will not run on older versions
// Fortunately, a huge amount of people are using API 16 (4.1.x) already 
// http://developer.android.com/about/dashboards/index.html?utm_source=ausdroid.net
 
@SuppressLint("NewApi")
public class WidgetService extends Service implements RingerModeListener.RingerModeListenerHandler {

	private static final String TAG = "Mute30";
	private static final int NOTIFICATION_ID = 1;
	private static final Uri URI_SETTINGS_GLOBAL_RINGERMODE = Settings.Global.getUriFor(Settings.Global.MODE_RINGER);
	
	// when testing minimum is 2 minutes because I round seconds down to 00 to
	// timer expires on minute change
	//public static final long MUTE_DURATION_MILLISECONDS = 2 * 60 * 1000;
	public static final long MUTE_DURATION_MILLISECONDS = 30 * 60 * 1000;

	private static final String PREF_NAME = "mute30prefs";
	private static final String PREF_NAME_ALARM_EXPIRE = "alarm_expire";
	private static final String PREF_NAME_ORIGINAL_RINGER_MODE = "orig_ringer_mode";

	private static final String PACKAGE_NAME = "ca.nmsasaki.android.mute30";
	public static final String ACTION_SHORTCUT_CLICK = PACKAGE_NAME + "ACTION_SHORTCUT_CLICK";
	private static final String ACTION_NOTIFICATION_CANCEL_CLICK = PACKAGE_NAME + "ACTION_NOTIFICATION_CANCEL_CLICK";
	private static final String ACTION_TIMER_EXPIRE = PACKAGE_NAME + "ACTION_TIMER_EXPIRE";
	private static final String ACTION_RINGERMODE_CHANGE = PACKAGE_NAME + "ACTION_RINGERMODE_CHANGE";

	// TODO: FUTURE static variables - are these ok?
	private static Toast mToast = null;
	private static RingerModeListener mRingerModeListener = null;

	private int mOriginalRingerMode = AudioManager.RINGER_MODE_NORMAL;
	private long mAlarmExpireTime = 0;
	private boolean mPrefsUpdated = false;

	
	
	/**
	 * Entry point for service
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "WidgetService::onStart - enter");

		handleCommand(intent);

		return START_STICKY;
	}

	/**
	 * Does all the real work based on the intent.
	 * Threading note: Everything runs on main thread
	 * 	 There should be no long running actions
	 *   There is no UI that will lag
	 *   
	 * @param intent
	 */
	private void handleCommand(Intent intent) {

		final String curIntentAction = intent.getAction();
		final Context context = getApplicationContext();
		Log.i(TAG, "WidgetService::handleCommand - " + curIntentAction);

		SharedPreferences prefs = prefsRead(context);

		if (curIntentAction.equals(ACTION_SHORTCUT_CLICK)) {
			actionShortcutClick(context);
		} else if (curIntentAction.equals(ACTION_TIMER_EXPIRE)) {
			actionTimerExpire(context);
		} else if (curIntentAction.equals(ACTION_NOTIFICATION_CANCEL_CLICK)) {
			actionNotificationCancelClick(context);
		} else if (curIntentAction.equals(ACTION_RINGERMODE_CHANGE)) {
			actionRingerModeChange(context);
		}
		
		prefsWrite(prefs);

		Log.d(TAG, "WidgetService::handleCommand - exit");
		stopSelf();
	}

	/**
	 * Needed for Service class
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "WigetService::onBind()");
		return null;
	}

	/**
	 * 
	 */
	@Override
	public void onDestroy() {
		Log.d(TAG, "WidgetService::onDestroy - exit");
	}

	/**
	 * Used to respond to manual changes to RingerMode.
	 * Actual work is done through handleCommand to ensure 
	 * state is read and written properly etc
	 */
	@Override
	public void onChange(boolean SelfChange, Uri uri) {
		Log.i(TAG, "WidgetService::RingerModeListener::onChange");

		// --------------------------------------------------
		// ensure URI is the expected global setting
		if (!uri.equals(URI_SETTINGS_GLOBAL_RINGERMODE)) {
			Log.w(TAG, String.format("Unexpected Settings.Global URI=%s", uri.toString()));
		}

		// --------------------------------------------------
		// we only care about this event if the mode is NOT silent
		Context context = getApplicationContext();
		AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		final int curAudioMode = audioMgr.getRingerMode();
		final String curModeString = ringerMode_intToString(curAudioMode);

		Log.i(TAG, "AudioMode=" + curModeString);

		// --------------------------------------------------
		// handle work if phone is not in silent mode now
		if (curAudioMode != AudioManager.RINGER_MODE_SILENT) {
			Intent intent = new Intent(context, WidgetService.class);
			intent.setAction(ACTION_RINGERMODE_CHANGE);
			handleCommand(intent);
		}

		Log.d(TAG, "RingerModeListener::onChange - exit");
	}
	
	//------------------------------------------------------
	// main action handling functions
	//------------------------------------------------------
	/**
	 * User clicked on the application short cut
	 * This should mute the phone (silent ringer mode) for 30 minutes or 
	 * add 30 more minutes to the existing timer
	 * 
	 * Steps:
	 * 1. Set New Timer (or Update Timer) 
	 * 2. Set RingerMode 
	 * 3. Create Toast 
	 * 4. Build Notification 
	 * 5. Start RingerMode Observer
	 * 
	 * @param context
	 */
	private void actionShortcutClick(Context context) {
		// User clicked on widget
		// Perform actions before notifying user
		// this will expose performance delays and show bugs

		Log.d(TAG, "ACTION_SHORTCUT_CLICK - enter");

		// ----------------------------------------------------
		// check current ringermode
		AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		final int curAudioMode = audioMgr.getRingerMode();
		final String curModeString = ringerMode_intToString(curAudioMode);
		Log.i(TAG, "AudioMode=" + curModeString);

		// --------------------------------------------------
		// set timer to re-enable original ringer mode
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		if (getAlarmExpireTime() == 0) {
			// record current state
			// set time for silence to expire
			setAlarmExpireTime(System.currentTimeMillis() + MUTE_DURATION_MILLISECONDS);

			// set ringer mode to restore here
			// if the ringer mode is silent already, then set restore mode to
			// normal
			// (otherwise, why did they click on widget... assume they want it
			// to expire)
			if (curAudioMode == AudioManager.RINGER_MODE_SILENT) {
				setOriginalRingerMode(AudioManager.RINGER_MODE_NORMAL);
			} else {
				setOriginalRingerMode(curAudioMode);
			}

		} else {
			// set time for silence to expire based on previous value
			setAlarmExpireTime(getAlarmExpireTime() + MUTE_DURATION_MILLISECONDS);
		}

		PendingIntent alarmIntent = alarmIntentCreate(context);
		setAlarmExpireTime(truncateSeconds(getAlarmExpireTime()));
		alarmMgr.set(AlarmManager.RTC_WAKEUP, getAlarmExpireTime(), alarmIntent);

		final String dateStringLog = DateFormat.getTimeInstance(DateFormat.LONG).format(getAlarmExpireTime());
		Log.i(TAG, "Alarm expireTime=" + dateStringLog);

		// ----------------------------------------------------
		// set ringer mode after alarm is set
		// to ensure mode will become re-enabled
		if (curAudioMode != AudioManager.RINGER_MODE_SILENT) {
			audioMgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			Log.i(TAG, "AudioMode=0-RINGER_MODE_SILENT");
		}

		// ----------------------------------------------------
		// Create toast to alert user
		String toastText = context.getString(R.string.toast_ON);
		final String dateStringUser = DateFormat.getTimeInstance(DateFormat.SHORT).format(
				getAlarmExpireTime());
		toastText = String.format(toastText, dateStringUser);

		toastShow(context, toastText);

		// ------------------------------------------------
		// Build notification

		// notification strings
		final String notiTitle = context.getString(R.string.notification_title);
		final String notiCancel = context.getString(R.string.notification_ON_cancel);
		String notiContentText = context.getString(R.string.notification_ON_content);
		notiContentText = String.format(notiContentText, dateStringUser);

		// Pending intent to be fired when notification is clicked
		Intent notiIntent = new Intent(context, ShortcutReceiver.class);
		notiIntent.setAction(ACTION_NOTIFICATION_CANCEL_CLICK);
		PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, 0, notiIntent, 0);

		// TODO: FUTURE - find actual vibrate icon - cannot find official
		// vibrate icon
		// use regular notification icon
		int iconId = android.R.drawable.ic_lock_silent_mode_off;
		// if (mRingerMode == AudioManager.RINGER_MODE_VIBRATE) {
		// iconId = android.R.drawable.;
		// }

		// Define the Notification's expanded message and Intent:
		Notification.Builder notiBuilder = new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_notify)
				.setContentTitle(notiTitle)
				.setContentText(notiContentText)
				.addAction(iconId, notiCancel, cancelPendingIntent);

		// Pass the Notification to the NotificationManager:
		NotificationManager notiMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notiMgr.notify(NOTIFICATION_ID, notiBuilder.build());

		// ----------------------------------------------------
		// Start Content Observer if one does not exist
		// TODO: CLEANUP - change to singleton?
		if (mRingerModeListener == null) {
			mRingerModeListener = new RingerModeListener(new Handler(), this);
			getContentResolver().registerContentObserver(URI_SETTINGS_GLOBAL_RINGERMODE, false, mRingerModeListener);
			Log.i(TAG, "RingerModeListener::Registered");
		}
		// ----------------------------------------------------

		Log.d(TAG, "ACTION_SHORTCUT_CLICK - exit");

	}

	
	/**
	 * User clicked to cancel mute mode from the Android notification drawer
	 * 
	 * Work required:
	 * 1. Stop ringerMode observer 
	 * 2. Restore Ringer Mode 
	 * 3. Cancel Timer 
	 * 4. Clear Notification
	 * 
	 * @param context
	 */
	private void actionNotificationCancelClick(Context context) {
		Log.d(TAG, "ACTION_NOTIFICATION_CANCEL_CLICK - enter");

		// User canceled the mode manually from notifications

		ringerModeObserverStop();

		// -----------------------------------------------
		// Restore previous RingerMode
		AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		final int curAudioMode = audioMgr.getRingerMode();

		final String curModeString = ringerMode_intToString(curAudioMode);
		Log.i(TAG, "AudioMode=" + curModeString);

		if (curAudioMode == AudioManager.RINGER_MODE_SILENT) {
			audioMgr.setRingerMode(getOriginalRingerMode());
			Log.i(TAG, String.format("AudioMode=%d", getOriginalRingerMode()));
		}

		alarmCancel(context);

		// -------------------------------------------
		// clear notification
		notificationCancel(context);

		Log.d(TAG, "ACTION_NOTIFICATION_CANCEL_CLICK - exit");

	}
	
	/**
	 * User manually changed the Ringer mode from silent to something else
	 * Steps:
	 * 1. Cancel timer so there is not another action triggered
	 * 2. Cancel the existing silent mode notification
	 * 3. Show toast to respond to user action 
	 * 4. Stop observing ringer changes
	 * 
	 * @param context
	 */
	private void actionRingerModeChange(Context context) {
		Log.d(TAG, "ACTION_RINGERMODE_CHANGE - enter");

		// -------------------------------------------
		// cancel timer 
		alarmCancel(context);

		// -------------------------------------------
		// Build notification to say timer expired
		// final int NotiContentID =
		// R.string.notification_OFF_user_change_ringer;
		// notificationUserCancel(context, NotiContentID);

		// -------------------------------------------
		// Cancel notification
		notificationCancel(context);

		// Show toast to user
		final String toastText = (String) context.getString(R.string.toast_OFF_user_change_ringer);
		toastShow(context, toastText);

		// ----------------------------------------------------
		// Stop Content Observer
		ringerModeObserverStop();
		
		Log.d(TAG, "ACTION_RINGERMODE_CHANGE - exit");
		
	}

	
	/**
	 * Mute timer has expired. Need to set phone back to original ringer mode
	 * Steps:
	 * 1. Stop RingerMode Observer 
	 * 2. Restore Ringer Mode 
	 * 3. Show Notification (to show user when timer expired)
	 * 
	 * @param context
	 */
	private void actionTimerExpire(Context context) {

		Log.d(TAG, "ACTION_TIMER_EXPIRE - enter");

		// Timer expired - restore previous notification type

		// --------------------------------------------------
		// enable previous ringerMode
		AudioManager audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		final int curAudioMode = audioMgr.getRingerMode();
		final String curModeString = ringerMode_intToString(curAudioMode);

		Log.i(TAG, "AudioMode=" + curModeString);

		ringerModeObserverStop();

		if (curAudioMode == AudioManager.RINGER_MODE_SILENT) {
			Log.i(TAG, String.format("before AudioMode=%d", getOriginalRingerMode()));
			audioMgr.setRingerMode(getOriginalRingerMode());
			Log.i(TAG, String.format("after AudioMode=%d", getOriginalRingerMode()));
		}

		// -------------------------------------------
		// Build notification to say timer expired
		final String notiContent = (String) getString(R.string.notification_OFF_timer);
		notificationShowCancelReason(context, notiContent);

		// set AlarmExpire to 0 to show that there is no more timer
		setAlarmExpireTime(0);

		Log.d(TAG, "ACTION_TIMER_EXPIRE - exit");

	}

	// --------------------------------------------------
	// helper functions
	// --------------------------------------------------

	/**
	 * Display a notification explaining why mute mode ended
	 * 
	 * @param context, contentText
	 */
	private void notificationShowCancelReason(Context context, final String contentText) {
		final String notiTitle = context.getString(R.string.notification_title);

		// Define the Notification's expanded message and Intent:
		Notification.Builder notiBuilder = new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_notify).setContentTitle(notiTitle)
				.setContentText(contentText);

		// Pass the Notification to the NotificationManager:
		NotificationManager notiMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notiMgr.notify(NOTIFICATION_ID, notiBuilder.build());
	}


	/**
	 * Make notification disappear from android notification drawer
	 * @param context
	 */
	private void notificationCancel(Context context) {
		
		NotificationManager notiMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notiMgr.cancel(NOTIFICATION_ID);
	}
	
	/**
	 * Show toast with given text
	 * Cancel existing toast if it is on screen to handle clicking multiple times 
	 * 
	 * @param context
	 * @param toastText
	 */
	private void toastShow(Context context, String toastText) {
		if (mToast != null) {
			mToast.cancel();
		}
		mToast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
		mToast.show();
	}

	/**
	 * @param context
	 * @return PendingIntent for AlarmManager
	 */
	private PendingIntent alarmIntentCreate (Context context) {
		Intent intentAlarmReceiver = new Intent(context, ShortcutReceiver.class);
		intentAlarmReceiver.setAction(ACTION_TIMER_EXPIRE);

		return PendingIntent.getBroadcast(context, 0, intentAlarmReceiver, 0);
	}
	
	/**
	 * Cancel timer now and set alarmExpireTime = 0 
	 * 
	 * @param context
	 */
	private void alarmCancel(Context context) {
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = alarmIntentCreate(context);
		alarmMgr.cancel(alarmIntent);

		// set AlarmExpire to 0 to show that there is no more timer
		setAlarmExpireTime(0);
	}

	/**
	 * Unregister listener on user RingerMode changes
	 */
	private void ringerModeObserverStop() {
		
		if (mRingerModeListener != null) {
			getContentResolver().unregisterContentObserver(mRingerModeListener);
			mRingerModeListener = null;
			Log.i(TAG, "RingerModeListener - Cancelled");
		}
	}

	
	// ---------------------------------------------------------------
	// Static functions
	// TODO: CLEANUP - where should these go? Don't need to be in this class
	// ---------------------------------------------------------------
	
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
	 * @param curAudioMode
	 *            from AudioManager.RINGER_MODE_XXXX
	 * @return String version of curAudioMode if the ringer mode is unknown,
	 *         include int value for reference debugging
	 */
	public static String ringerMode_intToString(int curAudioMode) {

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

	
	// ---------------------------------------------------------------
	// Preferences stuff
	// TODO: CLEANUP - move preferences into separate class?
	// ---------------------------------------------------------------
	
	/**
	 * @param prefs
	 * 
	 * if preferences were updated, then write to storage
	 */
	private void prefsWrite(SharedPreferences prefs) {
		
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
	private SharedPreferences prefsRead(final Context context) {
		
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
		setOriginalRingerMode(prefs.getInt(PREF_NAME_ORIGINAL_RINGER_MODE, AudioManager.RINGER_MODE_NORMAL));
		setAlarmExpireTime(prefs.getLong(PREF_NAME_ALARM_EXPIRE, 0));
		return prefs;
	}

	
	// ---------------------------------------------------------------
	// Getters and setters
	// ---------------------------------------------------------------

	
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
