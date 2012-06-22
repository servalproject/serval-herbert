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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Main activity for the Serval Herbert application
 *
 */
public class MainActivity extends Activity implements OnClickListener {
	
	/*
	 * private class level constants
	 */
	private static final String sTag = "MainActivity";
	
	private static final int sNoRedPhoneNumber = 0;
	private static final int sNoGreenPhoneNumber = 1;
	private static final int sNoPhoneNumbers = 2;
	
	/*
	 * private class level variables
	 */
	private String redPhoneNumber;
	private String greenPhoneNumber;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // configure the buttons
        Button mButton = (Button) findViewById(R.id.main_activity_ui_btn_settings);
        mButton.setOnClickListener(this);
        
        mButton = (Button) findViewById(R.id.main_activity_ui_btn_close);
        mButton.setOnClickListener(this);
        
        // get the red and green button phone numbers
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        redPhoneNumber = mPreferences.getString("preferences_red_button", null);
        greenPhoneNumber = mPreferences.getString("preferences_green_button", null);
        
        if(TextUtils.isEmpty(redPhoneNumber) && TextUtils.isEmpty(greenPhoneNumber)) {
        	showDialog(sNoPhoneNumbers);
        } else {
	        if(TextUtils.isEmpty(redPhoneNumber)) {
	        	showDialog(sNoRedPhoneNumber);
	        } 
	        
	        if(TextUtils.isEmpty(greenPhoneNumber)) {
	        	showDialog(sNoGreenPhoneNumber);
	        } 
        }
        
        Intent mIntent = new Intent(this, org.servalproject.herbert.CoreService.class);
        startService(mIntent);
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
	@Override
	public void onClick(View v) {
		
		Intent mIntent;
		
		// work out which view was touched
		switch(v.getId()) {
		case R.id.main_activity_ui_btn_settings:
			// settings button
			//debug code
			Log.v(sTag, "show settings activity");
			mIntent = new Intent(this, org.servalproject.herbert.SettingsActivity.class);
			startActivity(mIntent);
			break;
		case R.id.main_activity_ui_btn_close:
			// close the activity
			finish();
			break;
		default:
			Log.w(sTag, "an unknown view fired the onClick event");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
		Dialog mDialog = null;
		
		int mMessageId;
		
		switch(id) {
		case sNoRedPhoneNumber:
			mMessageId = R.string.main_activity_ui_dialog_no_red_phone_number;
			break;
		case sNoGreenPhoneNumber:
			mMessageId = R.string.main_activity_ui_dialog_no_green_phone_number;
			break;
		case sNoPhoneNumbers:
			mMessageId = R.string.main_activity_ui_dialog_no_phone_numbers;
			break;
		default:
			return super.onCreateDialog(id);
		}
		
		// build the dialog
		mBuilder.setMessage(mMessageId)
		.setCancelable(false)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				return;
			}
		});
		
		// create and return the dialog
		mDialog = mBuilder.create();
		return mDialog;
		
	}
}