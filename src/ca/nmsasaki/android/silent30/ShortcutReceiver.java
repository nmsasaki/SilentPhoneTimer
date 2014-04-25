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
   
   Class: ShortcutReceiver
   Description: Named after Application "Shortcut" which triggers actions
	as opposed to the Widget originally implemented to handle user actions
	
	This broadcast receiver is used to handle events for the application like:
	* Timer expiring
	* User clicking on cancel from the Notification drawer
      
 */

package ca.nmsasaki.android.silent30;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ShortcutReceiver extends BroadcastReceiver {

	private static final String TAG = "Silent30";
	
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i(TAG, "ShortcutReceiver::onReceive - enter");

		final String curIntentAction = intent.getAction();
		Log.i(TAG, "onReceive Intent=" + curIntentAction);

		// ----------------------------------------------------
		// Update the widgets via the service
		// ----------------------------------------------------
		Log.i(TAG, "ShortcutReceiver::Start Service");
		Intent serviceIntent = new Intent(context, WidgetService.class);
		serviceIntent.setAction(intent.getAction());
		context.startService(serviceIntent);
		// ----------------------------------------------------

		
		Log.i(TAG, "ShortcutReceiver::onReceive - exit");
		
	}
	
}
