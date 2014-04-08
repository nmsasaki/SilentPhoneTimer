package ca.nmsasaki.silentphonetimer;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import java.text.DateFormat;

public class MyWidgetProvider extends AppWidgetProvider {

	private static final long SILENT_DURATION_MILLISECONDS = 1 * 60 * 1000;
	private static final String TAG = "SilentPhoneTimer";
	private static final int MY_NOTIFICATION_ID = 1;

	private static PendingIntent mAlarmIntent = null;
	private static Toast mToast = null;
	private static boolean mOnInitialize = true;
	private static long mAlarmExpire = 0;

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		Log.d(TAG, "MyWidgetProvider::onEnabled");

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		//super.onReceive(context, intent);

		Log.d(TAG, "MyWidgetProvider::onReceive - enter");

		if (intent.getAction() == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
			
			if (mOnInitialize) {
				Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_APPWIDGET_UPDATE (initialize)");
				mOnInitialize = false;
			}
			else
			{
				Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_APPWIDGET_UPDATE (user click)");
				// Perform actions before notifying user - this will expose performance delays and show bugs ----------------------
				// ----------------------------------------------------
				
				//TODO: Change to Silent Notifications
				
				AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
				
				if (mAlarmIntent == null) {
					// Create timer for Canceling Silent Mode -----------------------------------------
					Intent intentAlarmReceiver = new Intent(context, MyWidgetProvider.class);
					intentAlarmReceiver.setAction(Intent.ACTION_DELETE);
					mAlarmIntent = PendingIntent.getBroadcast(context, 0, intentAlarmReceiver, 0);

					//final long alarmExpire = SystemClock.elapsedRealtime() + SILENT_DURATION_MILLISECONDS; // this uptime is ms since Jan 1, 1970.
					mAlarmExpire = System.currentTimeMillis() + SILENT_DURATION_MILLISECONDS; // this based on system time
					alarmMgr.set(AlarmManager.RTC_WAKEUP, mAlarmExpire, mAlarmIntent);
				}
				else
				{
					mAlarmExpire = mAlarmExpire + SILENT_DURATION_MILLISECONDS; // this based on system time
					alarmMgr.set(AlarmManager.RTC_WAKEUP, mAlarmExpire, mAlarmIntent);
				}
				
				
				// ----------------------------------------------------

				final DateFormat dateFormatterUser = DateFormat.getTimeInstance(DateFormat.SHORT);
				final DateFormat dateFormatterLog = DateFormat.getTimeInstance(DateFormat.LONG);
				// TimeZone tz = dateFormatter.getTimeZone(); - for testing timezone
				// String dateStringTest = dateFormatter.format(SystemClock.elapsedRealtime());
				final String dateStringUser = dateFormatterUser.format(mAlarmExpire);
				final String dateStringLog = dateFormatterLog.format(mAlarmExpire);

				Log.i(TAG, "MyWidgetProvider::onReceive expireTime=" + dateStringLog);
				// toast
				String toastText = context.getString(R.string.toast_ON);
				toastText = String.format(toastText, dateStringUser);
				
				if (mToast!=null){
					mToast.cancel();
				}
				mToast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
				mToast.show();

				
				// Build notification ------------------------------------------------
				
				// notification strings
				final String notiTitle = context.getString(R.string.notification_title);
				final String notiCancel = context.getString(R.string.notification_ON_cancel);
				//String notiTickerText = context.getString(R.string.notification_ON_ticker);
				//notiTickerText = String.format(notiTickerText, dateString);
				String notiContentText = context.getString(R.string.notification_ON_content);
				notiContentText = String.format(notiContentText, dateStringUser);

				// Intent 1: When main part of notification is clicked, just return to home screen
				// Intent 2: When cancel is clicked, remove notification and enable regular notifications
				
				// Pending intent to be fired when notification is clicked
				Intent notiIntent = new Intent(context, MyWidgetProvider.class);
				notiIntent.setAction(Intent.ACTION_EDIT);
				PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, 0, notiIntent, 0);

				
				// Define the Notification's expanded message and Intent:
				Notification.Builder notificationBuilder = new Notification.Builder(context)
						//.setTicker(notiTickerText)
						.setSmallIcon(android.R.drawable.ic_lock_silent_mode)
						.setContentTitle(notiTitle)
						.setContentText(notiContentText)
						.addAction(android.R.drawable.ic_lock_silent_mode_off, notiCancel, cancelPendingIntent);

				// Pass the Notification to the NotificationManager:
				NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());
			}
			
		}
		else if (intent.getAction() == Intent.ACTION_DELETE) {
			Log.d(TAG, "MyWidgetProvider::onReceive - ACTION_DELETE: START");
			
				Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_DELETE: TIMER EXPIRED");
				
				//TODO: Change to Normal Notifications
				// If the timer expired Build notification ------------------------------------------------
				final long alarmExpired = System.currentTimeMillis();
				final DateFormat dateFormatterUser = DateFormat.getTimeInstance(DateFormat.SHORT);
				final DateFormat dateFormatterLog = DateFormat.getTimeInstance(DateFormat.LONG);
				String dateStringUser = dateFormatterUser.format(alarmExpired);
				String dateStringLog = dateFormatterLog.format(alarmExpired);
				Log.i(TAG, "Actual Expiry Time:" + dateStringLog);
				
				final String notiTitle = context.getString(R.string.notification_title);
//				final String notiCancel = context.getString(R.string.notification_OFF_cancel);
//				String notiTickerText = context.getString(R.string.notification_OFF_ticker);
//				notiTickerText = String.format(notiTickerText, dateString);
				String notiContentText = context.getString(R.string.notification_OFF_timer);
				notiContentText = String.format(notiContentText, dateStringUser);
				
				Log.d(TAG, "MyWidgetProvider::onReceive - ACTION_DELETE: UPDATE NOTIFICATION");
				// Define the Notification's expanded message and Intent:
				Notification.Builder notificationBuilder = new Notification.Builder(context)
						.setSmallIcon(android.R.drawable.ic_lock_silent_mode)
						.setContentTitle(notiTitle)
						.setContentText(notiContentText);

				// Pass the Notification to the NotificationManager:
				NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());

			    mAlarmIntent = null;

				Log.d(TAG, "MyWidgetProvider::onReceive - ACTION_DELETE: FINISH");
			
		}
		else if (intent.getAction() == Intent.ACTION_EDIT) {
			Log.d(TAG, "MyWidgetProvider::onReceive - ACTION_EDIT: START");

			//TODO: Change to Normal Notifications

			// cancel timer and clear notification
			Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_EDIT: CANCEL ALARM");
			AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			if (alarmMgr!= null && mAlarmIntent != null) {
			    alarmMgr.cancel(mAlarmIntent);
			}
			
			Log.d(TAG, "MyWidgetProvider::onReceive - ACTION_EDIT: CANCEL NOTIFICATION");
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(MY_NOTIFICATION_ID);

		    mAlarmIntent = null;
			Log.d(TAG, "MyWidgetProvider::onReceive - ACTION_EDIT: FINISH");
		}
		else
		{
			Log.e(TAG, "MyWidgetProvider::onReceive - unknown trigger");
		}
		// AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		// Toast.makeText(context, "Touched view", Toast.LENGTH_SHORT).show();

		super.onReceive(context, intent);
		Log.d(TAG, "MyWidgetProvider::onReceive - exit");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.d(TAG, "MyWidgetProvider::onUpdate - enter");

		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		
		// Register an onClickListener for widget update ----------------------------------------------------
		Intent updateIntent = new Intent(context, MyWidgetProvider.class);
		updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		// updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		PendingIntent updatePIntent = PendingIntent.getBroadcast(context, 0, updateIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.widget_image, updatePIntent);
		appWidgetManager.updateAppWidget(thisWidget, remoteViews);
		// ----------------------------------------------------

		// // Update the widgets via the service ----------------------------------------------------
		// context.startService(intent);
		// ----------------------------------------------------

		Log.d(TAG, "MyWidgetProvider::onUpdate - exit");

	}

}
