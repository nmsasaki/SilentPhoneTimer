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
//	private static final String PINTENT_EXTRAS_TIMEREXPIRED = "TIMEREXPIRED";
//	private static final String PINTENT_EXTRAS_USERCANCELED = "USERCANCELED";
	
	private static final String INTENT_EXTRAS_CANCEL_REASON_LABEL = "CANCEL_REASON";
	private static final int INTENT_EXTRAS_CANCEL_REASON_NA = 0;
	private static final int INTENT_EXTRAS_CANCEL_REASON_TIMEREXPIRED = 2;
	private static final int INTENT_EXTRAS_CANCEL_REASON_USERCANCELED = 4;
	
//	private static final int PINTENT_TIMER_ID = 1;
//	private static final int PINTENT_CANCEL_ID = 2;

	private static PendingIntent mAlarmIntent = null;
	private static boolean mOnInitialize = true;

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
			
			if (mOnInitialize) {
				Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_APPWIDGET_UPDATE (initialize)");
				mOnInitialize = false;
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
				intentAlarmReceiver.putExtra(INTENT_EXTRAS_CANCEL_REASON_LABEL, INTENT_EXTRAS_CANCEL_REASON_TIMEREXPIRED);
				mAlarmIntent = PendingIntent.getBroadcast(context, 0, intentAlarmReceiver, 0);

				//final long alarmExpire = SystemClock.elapsedRealtime() + SILENT_DURATION_MILLISECONDS; // this uptime is ms since Jan 1, 1970.
				final long alarmExpire = System.currentTimeMillis() + SILENT_DURATION_MILLISECONDS; // this based on system time
				alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmExpire, mAlarmIntent);
				
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
				
				// notification strings
				final String notiTitle = context.getString(R.string.notification_title);
				final String notiCancel = context.getString(R.string.notification_ON_cancel);
				//String notiTickerText = context.getString(R.string.notification_ON_ticker);
				//notiTickerText = String.format(notiTickerText, dateString);
				String notiContentText = context.getString(R.string.notification_ON_content);
				notiContentText = String.format(notiContentText, dateString);

				// Intent 1: When main part of notification is clicked, just return to home screen
				// Intent 2: When cancel is clicked, remove notification and enable regular notifications
				
				// Pending intent to be fired when notification is clicked
				Intent notiIntent = new Intent(context, MyWidgetProvider.class);
				notiIntent.setAction(Intent.ACTION_DELETE);
				notiIntent.putExtra(INTENT_EXTRAS_CANCEL_REASON_LABEL, INTENT_EXTRAS_CANCEL_REASON_USERCANCELED);
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
			Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_DELETE: START");
			
			final int reasonCode = intent.getIntExtra(INTENT_EXTRAS_CANCEL_REASON_LABEL, INTENT_EXTRAS_CANCEL_REASON_NA);
			
			Log.i(TAG, String.format("reasonCode=%d", reasonCode));

			if (reasonCode == INTENT_EXTRAS_CANCEL_REASON_USERCANCELED) 
			{
				Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_DELETE: USER CANCELED");
				// cancel timer and clear notification
				AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
				if (alarmMgr!= null) {
				    alarmMgr.cancel(mAlarmIntent);
				}
				
			}
			
			else if (reasonCode == INTENT_EXTRAS_CANCEL_REASON_TIMEREXPIRED) 
			{	
				Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_DELETE: TIMER EXPIRED");
				
				// If the timer expired Build notification ------------------------------------------------
				final long alarmExpired = System.currentTimeMillis();
				DateFormat dateFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);
				String dateString = dateFormatter.format(alarmExpired);
				
				final String notiTitle = context.getString(R.string.notification_title);
//				final String notiCancel = context.getString(R.string.notification_OFF_cancel);
//				String notiTickerText = context.getString(R.string.notification_OFF_ticker);
//				notiTickerText = String.format(notiTickerText, dateString);
				String notiContentText = context.getString(R.string.notification_OFF_timer);
				notiContentText = String.format(notiContentText, dateString);
				
				// Define the Notification's expanded message and Intent:
				Notification.Builder notificationBuilder = new Notification.Builder(context)
						.setSmallIcon(android.R.drawable.ic_lock_silent_mode)
						.setContentTitle(notiTitle)
						.setContentText(notiContentText);

				// Pass the Notification to the NotificationManager:
				NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());

				Log.i(TAG, "MyWidgetProvider::onReceive - ACTION_DELETE: FINISHED");
			}
			else
			{
				Log.e(TAG, "MyWidgetProvider::onReceive - ACTION_DELETE: UNKNOWN REASON");
			}
			
		}
		else
		{
			Log.e(TAG, "MyWidgetProvider::onReceive - unknown trigger");
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
