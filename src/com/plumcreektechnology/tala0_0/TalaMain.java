package com.plumcreektechnology.tala0_0;

import java.util.TreeMap;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.plumcreektechnology.tala0_0.SettingsFragment.OnOffReceiver;
import com.plumcreektechnology.tala0_0.SliderView.SliderReceiver;

/**
 * Main activity for application: manages fragments, ensures
 * settings are followed appropriately, starts and stops location
 * update service, responds to menu clicks.
 * @author norahayes
 *
 */
public class TalaMain extends FragmentActivity implements SliderReceiver, OnOffReceiver, Tala_Constants{

	private final String TAG = getClass().getName();
	private TreeMap<String, Integer> categoryRadii;
	
	private boolean isLocationRunning; //keeps track of whether LocationService is running	
	private SettingsFragment settingsFragment;
	private CategoriesFragment catFragment;
	private MapFragment mapFragment;
	private GoogleMap map;

	protected static final String KEY_THIS_PREFERENCE = "location_preference";
	protected static final String KEY_ITEM_LONGITUDE = "longitude";
	protected static final String KEY_ITEM_LATITUDE = "latitude";
	protected static final String KEY_ITEM_ZOOM = "zoom";
	protected static final String KEY_ITEM_TILT = "tilt";
	protected static final String KEY_ITEM_BEARING = "bearing";
	
	// -----------------------------------LIFECYCLE UTILITIES----------------------------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tala_main);

		//fragments
		if (savedInstanceState == null) {
			SharedPreferences prefs = getSharedPreferences(KEY_THIS_PREFERENCE, Context.MODE_PRIVATE);
			categoryRadii = new TreeMap<String, Integer>();
			settingsFragment = new SettingsFragment();
			catFragment = new CategoriesFragment();
			mapFragment = MapFragment.newInstance((new GoogleMapOptions())
					.mapType(GoogleMap.MAP_TYPE_NORMAL)
					.camera((new CameraPosition.Builder().target(new LatLng(
							Double.parseDouble(prefs
									.getString(
											getLocationPreferenceKey(KEY_ITEM_LATITUDE),
											"90.0")),
							Double.parseDouble(prefs
									.getString(
											getLocationPreferenceKey(KEY_ITEM_LONGITUDE),
											"0.0")))))
							.zoom(prefs
									.getFloat(
											getLocationPreferenceKey(KEY_ITEM_ZOOM),
											10))
							.tilt(prefs
									.getFloat(
											getLocationPreferenceKey(KEY_ITEM_TILT),
											0))
							.bearing(
									prefs.getFloat(
											getLocationPreferenceKey(KEY_ITEM_BEARING),
											0)).build()));
			map = mapFragment.getMap();
			//TODO finish map
		}
		fragAdder(settingsFragment, false);	
		
		//get switch status from sharedpreferences:
		SharedPreferences prefs = getSharedPreferences(PACKAGE, Context.MODE_PRIVATE);
		if(prefs.getBoolean(ON_OFF_KEY, true)){
			//explicit intent to start location service
			Intent intent = new Intent(TalaMain.this, LocationService.class);
			startService(intent);
			isLocationRunning = true;
		}else{
			//switch is off
			isLocationRunning = false;
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
		int itemId = item.getItemId();
		switch (itemId) {
		case (R.id.action_settings):
			checkMapFrag();
			fragReplacer(settingsFragment, true);
			break;
		case (R.id.action_map):
			fragReplacer(mapFragment, true);
			map = mapFragment.getMap();
			if (map != null) {
				map.setMyLocationEnabled(true);
			} else {
				Toast.makeText(this, "map is null !!! ", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case (R.id.action_temp):
			//TODO things...?
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
		if (backStack)
			trans.addToBackStack(null);
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
	
	private void checkMapFrag() {
		if (mapFragment.isVisible()) {
			// save the most recent camera location and params
			SharedPreferences prefs = getSharedPreferences(KEY_THIS_PREFERENCE, Context.MODE_PRIVATE);
			CameraPosition cam = map.getCameraPosition();
			SharedPreferences.Editor ed = prefs.edit();
			ed.putString(getLocationPreferenceKey(KEY_ITEM_LATITUDE),
					((Double) cam.target.latitude).toString());
			ed.putString(getLocationPreferenceKey(KEY_ITEM_LONGITUDE),
					((Double) cam.target.longitude).toString());
			ed.putFloat(getLocationPreferenceKey(KEY_ITEM_ZOOM), cam.zoom);
			ed.putFloat(getLocationPreferenceKey(KEY_ITEM_TILT), cam.tilt);
			ed.putFloat(getLocationPreferenceKey(KEY_ITEM_BEARING), cam.bearing);
			ed.commit();
		}

	}
	
	/**
	 * generate consistent item keys for saving the lists state in
	 * SharedPreferences
	 * 
	 * @param pos
	 * @return
	 */
	protected static String getLocationPreferenceKey(String KEY) {
		return PACKAGE + "_" + KEY;
	}	
	
	protected boolean isMapVisible(){
		return mapFragment.isVisible();
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
	
	private String makePlaceSpecification() {
		String places = categoryRadii.keySet().toString();
		places = places.substring(1, places.length()-1);
		places = places.replaceAll(", ", "|");
		return places;
	}
	
// -----------------------------------SETTINGS UTILITIES----------------------------------------------
	/**
	 * eventually will take you to a list of your 
	 * category options
	 * @param v
	 */
	public void onSubgroupClick(View v) {
		fragAdder(catFragment, true);
	}

	/**
	 * keeps track of whether we should be monitoring geofences
	 * at all.
	 */
	@Override
	public void onSwitchChanged(boolean status) {
		Toast.makeText(this, "updates should be " + status, Toast.LENGTH_SHORT)
				.show();
		if (status) {
			// TODO create geofences, receive updates
			if(!isLocationRunning){
				//explicit intent to start location service
				Intent intent = new Intent(TalaMain.this, LocationService.class);
				startService(intent);
				isLocationRunning = true;
			}
		} else {
			// disconnect from updates, remove fences if necessary
			if (isLocationRunning) {
				//explicit intent to start location service
				Intent intent = new Intent(TalaMain.this, LocationService.class);
				stopService(intent);
				isLocationRunning = false;
			}
		}
	}

	/**
	 * keeps track of categories that we're monitoring
	 * and their radii
	 */
	@Override
	public void sliderChanged(String title, int value, boolean active) {
		if (active) {
			categoryRadii.put(title, value);
		} else {
			if (categoryRadii.containsKey(title))
				categoryRadii.remove(title);
		}
	}

}


