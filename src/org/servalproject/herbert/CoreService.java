/*
 * Copyright (C) 2012 The Serval Project
 *
 * This file is part of the Serval Herbert Software
 *
 * Serval Herbert Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.servalproject.herbert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

/**
 * The CoreService class manages access to the IOIO board and responds to 
 * input from the two buttons
 */
public class CoreService extends IOIOService {
	
	/*
	 * private class level constants
	 */
	private static final Boolean sVLog = true;
	private static final String sTag = "CoreService";
	
	private static final int sRedButtonPin = 35;
	private static final int sGreenButtonPin = 34;
	
	private static final int sSleepTime = 10;
	private static final int sLongerSleepTime = 30000;
	
	/*
	 * private class level variables
	 */
	private String redPhoneNumber;
	private String greenPhoneNumber;
	
	private TelephonyManager telephonyManager = null;
	private AudioManager audioManager = null;
	
	private static volatile boolean inCall = false;
	
	private WakeLock wakeLock = null;
	
	/*
	 * (non-Javadoc)
	 * @see ioio.lib.util.android.IOIOService#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		// get the red and green button phone numbers
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        redPhoneNumber = mPreferences.getString("preferences_red_button", null);
        greenPhoneNumber = mPreferences.getString("preferences_green_button", null);
        
        if(TextUtils.isEmpty(redPhoneNumber) && TextUtils.isEmpty(greenPhoneNumber)) {
        	Log.e(sTag, "phone number preferences are missing");
        } else {
	        if(TextUtils.isEmpty(redPhoneNumber)) {
	        	Log.e(sTag, "red phone number preference is missing");
	        } 
	        
	        if(TextUtils.isEmpty(greenPhoneNumber)) {
	        	Log.e(sTag, "green phone number preference is missing");
	        } 
        }
        
        // setup a link to the AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        // setup a listener for call state information
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        
        if(sVLog) {
        	Log.v(sTag, "service onCreate()");
        }
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		super.onStartCommand(intent,  flags, startId);
		
		PowerManager mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "org.servalproject.herbet.CoreService");
		wakeLock.acquire();
		
		return android.app.Service.START_STICKY;
	}
	
	/*
	 * (non-Javadoc)
	 * @see ioio.lib.util.android.IOIOService#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		wakeLock.release();
	}
	
	/*
	 * 
	 * call state related code
	 * 
	 */
	
	PhoneStateListener phoneStateListener = new PhoneStateListener() {
		
		/*
		 * (non-Javadoc)
		 * @see android.telephony.PhoneStateListener#onCallStateChanged(int, java.lang.String)
		 */
		@Override
	    public void onCallStateChanged(int state, String incomingNumber) {
		
			switch (state) {
			case TelephonyManager.CALL_STATE_OFFHOOK:
				// turn the speaker phone on
				audioManager.setSpeakerphoneOn(true);
				
				//set the in call flag
				inCall = true;
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				// turn the speaker phone off
				audioManager.setSpeakerphoneOn(false);
				
				// set the in call flag
				inCall = false;
				break;
			}
		}
	};
	
	
	/*
	 * 
	 * ioio related code
	 * 
	 */
	
	/*
	 * a private looper inner class for responding to button events
	 */
	private class Looper extends BaseIOIOLooper {
		
		private DigitalInput mRedButton;
		private DigitalInput mGreenButton;
		
		/*
		 * (non-Javadoc)
		 * @see ioio.lib.util.BaseIOIOLooper#setup()
		 */
		@Override
        protected void setup() throws ConnectionLostException {
			
			try {
				mRedButton   = ioio_.openDigitalInput(sRedButtonPin, DigitalInput.Spec.Mode.PULL_UP);
				mGreenButton = ioio_.openDigitalInput(sGreenButtonPin, DigitalInput.Spec.Mode.PULL_UP);
			} catch (ConnectionLostException e) {
				Log.e(sTag, "connection to ioio lost during setup", e);
				throw e;
			}
			
			if(sVLog) {
	        	Log.v(sTag, "ioio looper setup()");
	        }
			
		}
		
		/*
		 * (non-Javadoc)
		 * @see ioio.lib.util.BaseIOIOLooper#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {
			
			try {
				
				// see which button has been pressed
				final boolean mReadingRed = mRedButton.read();
				final boolean mReadingGreen = mGreenButton.read();
				
				// process any button presses
				buttonPress(!mReadingRed, !mReadingGreen);
				
				// sleep for a longer time if a button has been pressed
				if(!mReadingRed || !mReadingGreen) {
					Thread.sleep(sLongerSleepTime);
				} else {
					Thread.sleep(sSleepTime);
				}
				
			} catch (InterruptedException e) {
				Log.w(sTag, "connection to ioio was interrupted during loop");
				ioio_.disconnect();
			} catch (ConnectionLostException e) {
				Log.e(sTag, "connection to ioio lost during loop", e);
				throw e;
			}
			
		}
		
	};
	
	/*
	 * (non-Javadoc)
	 * @see ioio.lib.util.android.IOIOService#createIOIOLooper()
	 */
	@Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }
	
	/*
	 * method to undertake tasks in a seperate thread
	 */
	private void buttonPress(final boolean redButton, final boolean greenButton) {
		Runnable workerRunnable = new Runnable() {
			
			/*
			 * (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				
				if(inCall == false) {
					placeCall(redButton, greenButton);
				} 
				
			}
			
			// private method to place a call
			private void placeCall(final boolean redButton, final boolean greenButton) {
				
				if(sVLog) {
		        	Log.v(sTag, "placing a call");
		        }

				if(redButton) {
					if(!TextUtils.isEmpty(redPhoneNumber)) {
						// place the call
						Intent mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + redPhoneNumber));
						mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(mIntent);
						
						inCall = true;
					} else {
						Log.e(sTag, "red phone number preference is missing");
					}
					
					if(sVLog) {
			        	Log.v(sTag, "ioio red button pressed");
			        }
				}
				
				if(greenButton) {
					if(!TextUtils.isEmpty(greenPhoneNumber)) {
						
						// place the call
						Intent mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + greenPhoneNumber));
						mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(mIntent);
						
						inCall = true;
					} else {
						Log.e(sTag, "green phone number preference is missing");
					}
					
					if(sVLog) {
			        	Log.v(sTag, "ioio green button pressed");
			        }
				}
			}
		
		};
		
		Thread workerThread = new Thread(workerRunnable);
		workerThread.start();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// this service isn't one that is bound to so need to return anything
		return null;
	}

}
