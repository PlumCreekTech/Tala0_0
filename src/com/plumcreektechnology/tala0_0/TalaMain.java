package com.plumcreektechnology.tala0_0;

import java.util.TreeMap;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.plumcreektechnology.tala0_0.SettingsFragment.OnOffReceiver;
import com.plumcreektechnology.tala0_0.SliderView.SliderReceiver;

public class TalaMain extends FragmentActivity implements SliderReceiver, OnOffReceiver, Tala_Constants {

	private final String TAG = getClass().getName();
	private TreeMap<String, Integer> categoryRadii;
	
	private FragmentManager fragMan;
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
//			if (map != null) {
//				map.setMyLocationEnabled(true);
//			} else {
//				Toast.makeText(this, "map is null !!! ", Toast.LENGTH_SHORT)
//						.show();
//			}
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

// -----------------------------------SETTINGS UTILITIES----------------------------------------------
	public void onSubgroupClick(View v){
		fragAdder(catFragment, true);
	}
	
	@Override
	public void sliderChanged(String title, int value, boolean active) {
		if(active) {
			categoryRadii.put(title, value);
		} else {
			if(categoryRadii.containsKey(title)) categoryRadii.remove(title);
		}
	}

	@Override
	public void onSwitchChanged(boolean status) {
		Toast.makeText(this, "updates should be "+status, Toast.LENGTH_SHORT).show();
		if(status){ 
			//TODO create geofences, receive updates
		}else{
			//disconnect from updates, remove fences if necessary
		}
	}
	
// -----------------------------------PLACES UTILITIES----------------------------------------------
	
	private String makePlaceSpecification() {
		String places = categoryRadii.keySet().toString();
		places = places.substring(1, places.length()-1);
		places = places.replaceAll(", ", "|");
		return places;
	}
	
}
