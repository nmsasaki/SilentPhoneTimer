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
   
   End of Copyright notice
   
   NOTE: Code based on com.swijaya.android.rotatecontrol.SettingsContentObserver
	Authored in 2012 by Santoso Wijaya
	{@link https://github.com/santa4nt/Rotate-Control}
	
	Here is the copywrite for the original code:
	 * ------------------------------------------------------
	 * Copyright (c) 2012 Santoso Wijaya
	 *
	 * Permission is hereby granted, free of charge, to any person obtaining
	 * a copy of this software and associated documentation files (the
	 * "Software"), to deal in the Software without restriction, including
	 * without limitation the rights to use, copy, modify, merge, publish,
	 * distribute, sublicense, and/or sell copies of the Software, and to
	 * permit persons to whom the Software is furnished to do so, subject to
	 * the following conditions:
	 *
	 * The above copyright notice and this permission notice shall be
	 * included in all copies or substantial portions of the Software.
	 *
	 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
	 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
	 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
	 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
	 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
	 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
	 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
	 * ------------------------------------------------------
 

 	Class: RingerModeListener
 	Description: ContentObserver to listen to android settings.
 	 To use this class, you must implement the RingerModeListenerHandler interface
	 to handle call backs when a setting change is detected.
   
 */

package ca.nmsasaki.android.mute30;

import android.annotation.SuppressLint;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/* 
 * @SupressLint("NewApi") forces android to use the newer onChange method rather than
 * the older one. This means this code will NOT work on older devices
*/
@SuppressLint("NewApi")

// TODO: CLEANUP - should be more generically named really...
public class RingerModeListener extends ContentObserver {
	
	private static final String TAG = "Mute30";

	public interface RingerModeListenerHandler {
		public void onChange(boolean SelfChange, Uri uri);
	}
	
	private RingerModeListenerHandler mRingerModeListenerHandler = null;

	public RingerModeListener(Handler handler, RingerModeListenerHandler ringerModeListenerHandler ) {
		super(handler);
		
		Log.d(TAG, "RingerModeListener::Constructor");
		mRingerModeListenerHandler = ringerModeListenerHandler;
	}

	/* older method deprecated and not used because of @SuppressLint above */
	@Override
	public void onChange(boolean SelfChange) {
		this.onChange(SelfChange, null);
		Log.w(TAG, String.format("RingerModeListener::onChange(selfChange), %b", SelfChange));
	}
	
	@Override
	public void onChange(boolean SelfChange, Uri uri) {
		
		Log.d(TAG, "RingerModeListener::onChange(selfChange, URI)");
		mRingerModeListenerHandler.onChange(SelfChange, uri);
		
	}
	
}
