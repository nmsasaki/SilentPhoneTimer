package ca.nmsasaki.silentphonetimer;

//import android.app.PendingIntent;
import android.app.Service;
//import android.appwidget.AppWidgetManager;
//import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class WidgetService extends Service {

	private static final String TAG = "SilentPhoneTimer";

	@Override
	  public void onStart(Intent intent, int startId) {
	    Log.i(TAG, "WidgetService::onStart - enter");

	    Toast.makeText(getApplicationContext(), 
                "Button is clicked", Toast.LENGTH_LONG).show();
	    
//	    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
//	        .getApplicationContext());

//	    int[] allWidgetIds = intent
//	        .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

//	    ComponentName thisWidget = new ComponentName(getApplicationContext(),
//	        MyWidgetProvider.class);

//	    int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
//	    Log.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));
//	    Log.w(LOG, "Direct" + String.valueOf(allWidgetIds2.length));

//	    for (int widgetId : allWidgetIds) {
//	      // create some random data
//	      int number = (new Random().nextInt(100));
//
//	      RemoteViews remoteViews = new RemoteViews(this
//	          .getApplicationContext().getPackageName(),
//	          R.layout.widget_layout);
//	      Log.w("WidgetExample", String.valueOf(number));
//
//	      // Set the text
//	      remoteViews.setTextViewText(R.id.update,
//	          "Random: " + String.valueOf(number));
//
//	      // Register an onClickListener
//	      Intent clickIntent = new Intent(this.getApplicationContext(),
//	          MyWidgetProvider.class);
//
//	      clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//	      clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
//	          allWidgetIds);
//
//	      PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
//	          PendingIntent.FLAG_UPDATE_CURRENT);
//	      remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
//	      appWidgetManager.updateAppWidget(widgetId, remoteViews);
//	    }
	    
	      // Register an onClickListener
//	      Intent clickIntent = new Intent(this.getApplicationContext(),
//	          MyWidgetProvider.class);
//
//	      clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//
//	      PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
//	          PendingIntent.FLAG_UPDATE_CURRENT);

//	      remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
//	      appWidgetManager.updateAppWidget(widgetId, remoteViews);

	    Log.i(TAG, "WidgetService::onStart - exit");
	    stopSelf();

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
}
