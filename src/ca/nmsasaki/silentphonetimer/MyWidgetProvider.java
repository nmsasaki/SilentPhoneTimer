package ca.nmsasaki.silentphonetimer;

import java.text.SimpleDateFormat;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.TimeZone;

public class MyWidgetProvider extends AppWidgetProvider {

	private static final long SILENT_DURATION_MILLISECONDS = 1 * 60 * 1000;
	private static final String TAG = "SilentPhoneTimer";
	private static final int MY_NOTIFICATION_ID = 1;
	private static boolean onInitialize = true;

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		Log.i(TAG, "MyWidgetProvider::onEnabled");

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		//super.onReceive(context, intent);

		Log.i(TAG, "MyWidgetProvider::onReceive - enter");

		if (intent.getAction() == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
			
			if (onInitialize) {
				Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_APPWIDGET_UPDATE (initialize)");
				onInitialize = false;
			}
			else
			{
				Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_APPWIDGET_UPDATE (user click)");
				// Perform actions before notifying user - this will expose performance delays and show bugs ----------------------
				// ----------------------------------------------------

				// Create timer for Canceling Silent Mode -----------------------------------------
				AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
				Intent intentAlarmReceiver = new Intent(context, MyWidgetProvider.class);
				intentAlarmReceiver.setAction(Intent.ACTION_DELETE);
				PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intentAlarmReceiver, 0);

				//final long alarmExpire = SystemClock.elapsedRealtime() + SILENT_DURATION_MILLISECONDS; // this uptime is ms since Jan 1, 1970.
				final long alarmExpire = System.currentTimeMillis() + SILENT_DURATION_MILLISECONDS; // this based on system time
				alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmExpire, alarmIntent);
				
				// ----------------------------------------------------

				DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);
				// TimeZone tz = dateFormatter.getTimeZone(); - for testing timezone
				// String dateStringTest = dateFormatter.format(SystemClock.elapsedRealtime());
				String dateString = dateFormatter.format(alarmExpire);

				// Log.i(TAG, "MyWidgetProvider::onReceive time=" + dateStringTest + ", " + dateString + ", TZ=" + tz.getDisplayName());
				// toast
				String toastText = context.getString(R.string.toast_ON);
				toastText = String.format(toastText, dateString);
				Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
				
				// Build notification ------------------------------------------------

				final String notiTitle = context.getString(R.string.notification_title);
				final String notiCancel = context.getString(R.string.notification_ON_cancel);
				//String notiTickerText = context.getString(R.string.notification_ON_ticker);
				//notiTickerText = String.format(notiTickerText, dateString);
				String notiContentText = context.getString(R.string.notification_ON_content);
				notiContentText = String.format(notiContentText, dateString);
				
				// Pending intent to be fired when notification is clicked
				Intent notiIntent = new Intent(context, MyWidgetProvider.class);
				PendingIntent cancelPendingIntent = PendingIntent.getActivity(context, 01, notiIntent, Intent.FLAG_ACTIVITY_CLEAR_TASK);

				
				// Define the Notification's expanded message and Intent:
				Notification.Builder notificationBuilder = new Notification.Builder(context)
						//.setTicker(notiTickerText)
						.setSmallIcon(android.R.drawable.ic_lock_silent_mode)
						.setContentTitle(notiTitle)
						.setContentText(notiContentText)
						.addAction(android.R.drawable.ic_lock_silent_mode_off, notiCancel, cancelPendingIntent);

				// Pass the Notification to the NotificationManager:
				NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());
			}
			
		}
		else if (intent.getAction() == Intent.ACTION_DELETE) {
			Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_DELETE");
			
//			// toast
//			final String toastText = context.getString(R.string.toast_OFF);
//			Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
//			
			// Build notification ------------------------------------------------

			final long alarmExpired = System.currentTimeMillis();
			DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);
			String dateString = dateFormatter.format(alarmExpired);
			
			final String notiTitle = context.getString(R.string.notification_title);
			final String notiCancel = context.getString(R.string.notification_OFF_cancel);
			String notiTickerText = context.getString(R.string.notification_OFF_ticker);
			notiTickerText = String.format(notiTickerText, dateString);
			String notiContentText = context.getString(R.string.notification_OFF_content);
			notiContentText = String.format(notiContentText, dateString);
			
			// Pending intent to be fired when notification is clicked
			Intent notiIntent = new Intent(context, MyWidgetProvider.class);
			notiIntent.setAction(Intent.ACTION_DELETE);
			PendingIntent cancelPendingIntent = PendingIntent.getActivity(context, 01, notiIntent, Intent.FLAG_ACTIVITY_CLEAR_TASK);

			
			// Define the Notification's expanded message and Intent:
			Notification.Builder notificationBuilder = new Notification.Builder(context)
					.setTicker(notiTickerText)
					.setSmallIcon(android.R.drawable.ic_lock_silent_mode)
					.setContentTitle(notiTitle)
					.setContentText(notiContentText)
					.addAction(android.R.drawable.ic_delete, notiCancel, cancelPendingIntent);

			// Pass the Notification to the NotificationManager:
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());
			
		}
		else
		{
			Log.i(TAG, "MyWidgetProvider::onReceive - unknown trigger");
		}
		// AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		// Toast.makeText(context, "Touched view", Toast.LENGTH_SHORT).show();

		super.onReceive(context, intent);
		Log.i(TAG, "MyWidgetProvider::onReceive - exit");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.i(TAG, "MyWidgetProvider::onUpdate - enter");

		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		
		// Register an onClickListener for widget update ----------------------------------------------------
		Intent updateIntent = new Intent(context, MyWidgetProvider.class);
		updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		PendingIntent updatePIntent = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_image, updatePIntent);
		appWidgetManager.updateAppWidget(thisWidget, remoteViews);
		// ----------------------------------------------------

		// // Update the widgets via the service ----------------------------------------------------
		// context.startService(intent);
		// ----------------------------------------------------

		Log.i(TAG, "MyWidgetProvider::onUpdate - exit");

	}

}
