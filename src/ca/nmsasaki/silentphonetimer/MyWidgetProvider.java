package ca.nmsasaki.silentphonetimer;

import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
//import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

//import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	private static final String TAG = "SilentPhoneTimer";

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		Log.i(TAG, "MyWidgetProvider::onEnabled");

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		Log.i(TAG, "MyWidgetProvider::onReceive");

	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.i(TAG, "MyWidgetProvider::onUpdate - enter");

		// Get all ids
		ComponentName thisWidget = new ComponentName(context,
				MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			// create some random data
			// create some random data
			int number = (new Random().nextInt(100));
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			// Set the text
			remoteViews.setTextViewText(R.id.widget_text,
					String.valueOf(number));

			// Register an onClickListener
			Intent intent = new Intent(context, MyWidgetProvider.class);

			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			remoteViews.setOnClickPendingIntent(R.id.widget_text, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}

		// // Update the widgets via the service
		// context.startService(intent);

		Log.i(TAG, "MyWidgetProvider::onUpdate - exit");

	}
}
