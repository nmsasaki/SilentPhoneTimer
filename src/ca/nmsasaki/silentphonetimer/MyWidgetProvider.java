package ca.nmsasaki.silentphonetimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
//import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//import android.content.res.Resources;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

//import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	private static final String TAG = "SilentPhoneTimer";
	private static final int MY_NOTIFICATION_ID = 1;

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		Log.i(TAG, "MyWidgetProvider::onEnabled");

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		Log.i(TAG, "MyWidgetProvider::onReceive");

		// AppWidgetManager mgr = AppWidgetManager.getInstance(context);
		// Toast.makeText(context, "Touched view", Toast.LENGTH_SHORT).show();

		// super.onReceive(context, intent);
		//
		// Log.i(TAG, "MyWidgetProvider::onReceive - exit");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.i(TAG, "MyWidgetProvider::onUpdate - enter");

		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

		// Create toast ----------------------------------------------------
		Log.i(TAG, "MyWidgetProvider::onUpdate - make toast");
		Toast.makeText(context, "Touched view", Toast.LENGTH_SHORT).show();
		// ----------------------------------------------------

		// Build notification ------------------------------------------------

		final String notiTickerText = context.getString(R.string.notification_ticker);
		final String notiTitle = context.getString(R.string.notification_title);
		final String notiContentText = context.getString(R.string.notification_content);
		final String notiCancel = context.getString(R.string.notification_cancel);
		
		// Pending intent to be fired when notification is clicked
		Intent notiIntent = new Intent(context, FullscreenActivity.class);
		PendingIntent cancelPendingIntent = PendingIntent.getActivity(context, 01, notiIntent, Intent.FLAG_ACTIVITY_CLEAR_TASK);

		
		// Define the Notification's expanded message and Intent:
		Notification.Builder notificationBuilder = new Notification.Builder(context)
				.setTicker(notiTickerText)
				.setSmallIcon(android.R.drawable.ic_lock_silent_mode)
				.setContentTitle(notiTitle)
				.setContentText(notiContentText)
				.addAction(android.R.drawable.ic_lock_silent_mode_off, notiCancel, cancelPendingIntent);

		// Pass the Notification to the NotificationManager:
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());
		
		
		// Register an onClickListener for widget update ----------------------------------------------------
		Intent intent = new Intent(context, MyWidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_image, pendingIntent);
		appWidgetManager.updateAppWidget(thisWidget, remoteViews);
		// ----------------------------------------------------

		// // Update the widgets via the service ----------------------------------------------------
		// context.startService(intent);
		// ----------------------------------------------------

		Log.i(TAG, "MyWidgetProvider::onUpdate - exit");

	}

}
