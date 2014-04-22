package ca.nmsasaki.silenttouch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

	private static final String TAG = "SilentTouch";
	public static final String INTENT_ACTION_WIDGET_CLICK = "ca.nmsasaki.silenttouch.INTENT_ACTION_WIDGET_CLICK";

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
	
}
