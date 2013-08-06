package com.plumcreektechnology.tala0_0;

import java.util.TreeMap;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.MapFragment;
import com.plumcreektechnology.tala0_0.SettingsFragment.OnOffReceiver;
import com.plumcreektechnology.tala0_0.SliderView.SliderReceiver;
import com.plumcreektechnology.tala0_0.TemporaryFragment.PlaceSpecification;

public class TalaMain extends Activity implements SliderReceiver, OnOffReceiver, PlaceSpecification, Tala_Constants {

	private final String TAG = getClass().getName();
	private TreeMap<String, Integer> categoryRadii;
	
	// -----------------------------------LIFECYCLE UTILITIES----------------------------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tala_main);
		categoryRadii = new TreeMap<String, Integer>();
		if (checkPlayServices()) { // only add SettingsFragment (which might start the LocationService) if google play is installed
			fragAdder(new SettingsFragment(), false);
		}
	}

	protected void onStart() {
		super.onStart();
		Toast.makeText(this, makePlaceSpecification(), Toast.LENGTH_LONG).show();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tala_main, menu);
		return true;
	}

// -----------------------------------FRAGMENT UTILITIES----------------------------------------------
	
	/**
	 * when a menu item is selected starts its corresponding fragment
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemID = item.getItemId();
		switch(itemID) { // always assume that a fragment is active
		case(R.id.action_settings):
			fragReplacer(new SettingsFragment(), true);
			break;
		case(R.id.action_map):
			MapFragment mapfrag = MapFragment.newInstance();
			if(mapfrag != null){
				
			}
			fragReplacer(mapfrag, true);
			break;
		case(R.id.action_temp):
			fragReplacer(new TemporaryFragment(), true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * private utility for adding fragments
	 * @param frag
	 */
	private void fragAdder(Fragment frag, boolean backStack) {
		FragmentTransaction trans = getFragmentManager().beginTransaction();
		trans.add(R.id.main_parent, frag);
		if(backStack) trans.addToBackStack(null);
		trans.commit();
	}
	
	/**
	 * private utility for removing fragments
	 * @param frag
	 */
	private void fragRemover(Fragment frag, boolean backStack) {
		FragmentTransaction trans = getFragmentManager().beginTransaction();
		trans.remove(frag);
		if(backStack) trans.addToBackStack(null);
		trans.commit();
	}
	
	/**
	 * private utility for replacing fragments
	 * @param frag
	 */
	private void fragReplacer(Fragment frag, boolean backStack) {
		FragmentTransaction trans = getFragmentManager().beginTransaction();
		trans.replace(R.id.main_parent, frag);
		if(backStack) trans.addToBackStack(null);
		trans.commit(); 
	}

// -----------------------------------SETTINGS UTILITIES----------------------------------------------
	
	/**
	 * this is called every time the switch is changed PLUS every time the
	 * settings fragment is loaded
	 */
	@Override
	public void onSwitchChanged(boolean status) {
		// TODO figure out if it is necessary to see if the service is running
		// supposedly, android won't let a service be activated twice
		/**
		 *  if the service should be running, and it is not, start it
		 *  if the service should not be running, and it is, stop it
		 *  if the service should be running and it is, do nothing
		 *  if the service should not be running, and it is not, do nothing
		 */
		boolean serviceRunning = isServiceRunning(LocationService.class);
		if (status) {
			if (!serviceRunning) {
				Intent intent = new Intent(this, LocationService.class);
				startService(intent);
				Log.d(TAG, "location service was not running, and was started");
			} else
				Log.d(TAG, "location service was running and continues to run");
		} else if (serviceRunning) {
			Intent intent = new Intent(this, LocationService.class);
			stopService(intent);
			Log.d(TAG, "location service was running, and was stopped");
		} else
			Log.d(TAG, "service was not running and still is not running");
	}
	
	@Override
	public void sliderChanged(String title, int value, boolean active) {
		if(active) {
			categoryRadii.put(title, value);
		} else {
			if(categoryRadii.containsKey(title)) categoryRadii.remove(title);
		}
		//Log.d(TAG, "sliderChanged "+title+" "+value+" "+active);
	}

// -----------------------------------LOCATION UTILITIES----------------------------------------------

	/**
	 * sample play services utility from
	 * http://www.androiddesignpatterns.com/2013/01/google-play-services-setup.html
	 * @return
	 */
	private boolean checkPlayServices() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (status != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
				showErrorDialog(status);
			} else {
				Toast.makeText(this, "This device is not supported.",
						Toast.LENGTH_LONG).show();
				finish(); // end the service if device is not supported
			}
			return false;
		}
		return true;
	}

	private void showErrorDialog(int code) {
		GooglePlayServicesUtil.getErrorDialog(code, this,
				REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}
	 
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  switch (requestCode) {
	    case REQUEST_CODE_RECOVER_PLAY_SERVICES:
	      if (resultCode == RESULT_CANCELED) {
	        Toast.makeText(this, "Google Play Services must be installed.",
	            Toast.LENGTH_SHORT).show();
	        finish();
	      }
	      return;
	  }
	  super.onActivityResult(requestCode, resultCode, data);
	}
	
// -----------------------------------PLACES UTILITIES----------------------------------------------
	
	public String makePlaceSpecification() {
		String places = categoryRadii.keySet().toString();
		places = places.substring(1, places.length()-1);
		places = places.replaceAll(", ", "|");
		return places;
	}
	
// -----------------------------------SERVICE UTILITIES----------------------------------------------
	
	private boolean isServiceRunning(Class myService) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (myService.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
}
