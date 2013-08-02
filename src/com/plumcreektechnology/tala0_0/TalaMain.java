package com.plumcreektechnology.tala0_0;

import java.util.TreeMap;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.plumcreektechnology.tala0_0.SettingsFragment.OnOffReceiver;
import com.plumcreektechnology.tala0_0.SliderView.SliderReceiver;

public class TalaMain extends FragmentActivity implements SliderReceiver, OnOffReceiver, Tala_Constants, GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{

	private final String TAG = getClass().getName();
	private TreeMap<String, Integer> categoryRadii;
	
	private FragmentManager fragMan;
	private SettingsFragment settingsFragment;
	private CategoriesFragment catFragment;
	private MapFragment mapFragment;
	private GoogleMap map;
	private boolean followUser;
	private LocationClient locClient;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private Location currentLoc; // maybe use this later for requesting database stuff...
	private LocationRequest locRequest;

	private static final long UPDATE_INTERVAL_MS = 15000;
	private static final long FASTEST_INTERVAL_MS = 5000;
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
		
		// Create new location client
		if (servicesConnected()) {
			locClient = new LocationClient(this, this, this);
		}
		currentLoc = new Location("whatever"); // blank location for now
		followUser = true;

		// do all of the right things to make a location request for repeated
		// updates
		locRequest = LocationRequest.create();
		locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locRequest.setInterval(UPDATE_INTERVAL_MS);
		locRequest.setFastestInterval(FASTEST_INTERVAL_MS);

		//fragments
		fragMan = getFragmentManager();
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
			fragAdder(settingsFragment, false);
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

// -----------------------------------FRAGMENT UTILITIES----------------------------------------------
	
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
		FragmentTransaction trans = fragMan.beginTransaction();
		trans.remove(frag);
		if (backStack)
			trans.addToBackStack(null);
		trans.commit();
	}
	
	/**
	 * private utility for replacing fragments
	 * @param frag
	 */
	private void fragReplacer(Fragment frag, boolean backStack) {
		FragmentTransaction trans = fragMan.beginTransaction();
		trans.replace(R.id.main_parent,frag);
		if (backStack)
			trans.addToBackStack(null);
		trans.commit(); 
	}
	
	private void checkMapFrag() {
//		if (locClient != null) {
//			if (locClient.isConnected()) {
//				locClient.disconnect();
//			}
//		}
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

// -----------------------------------LOCATION UTILITIES----------------------------------------------
		
	/**
	 * checks whether the device is connected to Google Play Services and
	 * displays an error message if not
	 * 
	 * @return
	 */
	private boolean servicesConnected() {
		// check for Google Play
		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (ConnectionResult.SUCCESS == result) { // it's available
			Log.d(this.toString(), "Google Play services is available.");
			return true;
		} else { // not available
			Log.d(this.toString(), "Google Play services is not available.");
			return false;
		}
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
		} else {
			// disconnect from updates, remove fences if necessary
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

	/**
	 * Resolves connection error if possible; otherwise displays error dialog
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace(); // log the error
			}
		} else {
			Log.e(this.toString(), "GooglePlayServices error code: "
					+ result.getErrorCode());
		}
	}

	/**
	 * When Location Services is connected, get most recent location
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		locClient.requestLocationUpdates(locRequest, this);
	}

	/**
	 * When Location Services is disconnected, do nothing
	 */
	@Override
	public void onDisconnected() {
		
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLoc = location;
		if (mapFragment.isVisible() && followUser) {
			map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location
					.getLatitude(), location.getLongitude())));
		}
	}
}


