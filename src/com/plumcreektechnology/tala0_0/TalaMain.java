package com.plumcreektechnology.tala0_0;

import java.util.ArrayList;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.plumcreektechnology.tala0_0.LocationService.LocationBinder;
import com.plumcreektechnology.tala0_0.PopUpAdapter.PopUpCallbacks;
import com.plumcreektechnology.tala0_0.SettingsFragment.OnOffReceiver;
import com.plumcreektechnology.tala0_0.SliderView.SliderReceiver;

/**
 * SERVICES DO NOT RUN ON DIFFERENT THREADS JUST BECAUSE YOU START THEM ON A DIFFERENT THREAD
 * MULTITHREADING IS NOT CURRENTLY HAPPENING
 * @author devinfrenze
 *
 */

public class TalaMain extends Activity implements SliderReceiver, OnOffReceiver, Tala_Constants, PopUpCallbacks {

	private final String TAG = getClass().getName();
	private TreeMap<String, Integer> categoryRadii;
	private boolean onOff;
	private ListView popupList;
	
	// -----------------------------------UTILITIES TO BIND----------------------------------------------
	private LocationService locationService;
	private boolean locationBound = false;
	private ServiceConnection connection = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        LocationBinder binder = (LocationBinder) service;
	        locationService = binder.getService();
	        locationBound = true;
	        onBound();
	    }
	    
		public void onServiceDisconnected(ComponentName arg0) {
			Log.d(TAG, "in on service disconnected");
			locationBound = false;
		}

	};

	@SuppressLint("HandlerLeak")
	Handler locationHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			locationUpdate(bundle.getDouble("latitude", 0.0),
					bundle.getDouble("longitude", 180.0));
		}
	};

// -----------------------------------LIFECYCLE----------------------------------------------
			 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tala_main);
		categoryRadii = new TreeMap<String, Integer>();
		onOff = getSharedPreferences(PACKAGE, Context.MODE_PRIVATE).getBoolean(ON_OFF_KEY, false);
		
		if( getIntent().getBooleanExtra("popup", false)) {
			// TODO display popup
			Log.d(TAG, "should display popup fragment from onCreate");
			displayPopUp(getIntent().getStringArrayExtra("triggers"), true);
		} else {
			// open the last open fragment (not including popup)
			Toast.makeText(this, "should display last open fragment", Toast.LENGTH_SHORT).show();
			// TODO this if placement is in a WEIRD place
			if (checkPlayServices()) { // only add SettingsFragment (which might start the LocationService) if google play is installed
				SharedPreferences prefs = getSharedPreferences(KEY_MAIN_PREFERENCE, Context.MODE_PRIVATE);
				int fragmentId = prefs.getInt(LAST_OPEN_FRAGMENT_KEY, R.id.action_settings);
				switch(fragmentId) { // always assume that a fragment is active
				case(R.id.action_settings):
					fragAdder(new SettingsFragment(), false);
					prefs.edit().putInt(LAST_OPEN_FRAGMENT_KEY, R.id.action_settings); // add frag as last open
					break;
				case(R.id.action_temp):
					fragAdder(new TemporaryFragment(), false);
					prefs.edit().putInt(LAST_OPEN_FRAGMENT_KEY, R.id.action_temp); // add frag as last open
					break;
				case(R.id.action_map):
					MapFragment mapFrag = instantiateMapFragment();
					fragAdder(mapFrag, false);
					GoogleMap map = mapFrag.getMap();
					if(map!=null) map.setMyLocationEnabled(true);
					prefs.edit().putInt(LAST_OPEN_FRAGMENT_KEY, R.id.action_map); // add frag as last open
					break;
				}
			}
		}
	}

	protected void onStart() {
		super.onStart();
		if(onOff) {
			Intent intent = new Intent(this, LocationService.class);
			getApplicationContext().bindService(intent, connection, 0); // TODO bind to running service
		}
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// onCreate is SKIPPED if onNewIntent() is invoked
		// make sure on NewIntent() is skipped when onCreate() is invoked
		Log.d(TAG, "entered onNewIntent()");
		if( intent.getBooleanExtra("popup", false)) {
			// TODO display popup
			Log.d(TAG, "should display popup fragment from newIntent");
			displayPopUp(getIntent().getStringArrayExtra("triggers"), false);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tala_main, menu);
		return true;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(locationBound) { // if the service is bound, unbind before quitting
			onUnbound();
			getApplicationContext().unbindService(connection); // TODO
		}
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
			getSharedPreferences(KEY_MAIN_PREFERENCE, Context.MODE_PRIVATE).edit().putInt(LAST_OPEN_FRAGMENT_KEY, R.id.action_settings); // add frag as last open
			break;
		case(R.id.action_temp):
			fragReplacer(new TemporaryFragment(), true);
			getSharedPreferences(KEY_MAIN_PREFERENCE, Context.MODE_PRIVATE).edit().putInt(LAST_OPEN_FRAGMENT_KEY, R.id.action_temp); // add frag as last open

			break;
		case(R.id.action_map):
			MapFragment mapFrag = instantiateMapFragment();
			fragReplacer(mapFrag, true);
			GoogleMap map = mapFrag.getMap();
			if(map!=null) map.setMyLocationEnabled(true);
			getSharedPreferences(KEY_MAIN_PREFERENCE, Context.MODE_PRIVATE).edit().putInt(LAST_OPEN_FRAGMENT_KEY, R.id.action_map); // add frag as last open
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

	private MapFragment instantiateMapFragment() {
		SharedPreferences prefs = getSharedPreferences(KEY_MAP_PREFERENCE, Context.MODE_PRIVATE);
		return MapFragment // instantiate defaults for map
				.newInstance((new GoogleMapOptions())
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
	}
	
	/**
	 * generate consistent item keys for saving the lists state in SharedPreferences
	 * @param pos
	 * @return
	 */
	protected static String getLocationPreferenceKey(String KEY) {
		return PACKAGE + "_" + KEY ;
	}
	
	private void displayPopUp(String[] triggers, boolean dismiss) {
		ArrayList<String> activeTrigs = locationService.getActiveIds();

		// combine the arrays without repeats
		for(int i=0; i<triggers.length; i++) {
			String trigger = triggers[i];
			boolean add = true;
			for(int j=0; j<activeTrigs.size(); j++) {
				if(trigger.equals(activeTrigs.get(j))) { // try it without maybe ? pointers ?
					add=false;
				}
			}
			if(add) {
				activeTrigs.add(trigger);
			}
		}
		
		
		String[] names = new String[activeTrigs.size()];
		for(int i=0; i<triggers.length; i++) {
			names[i] = locationService.getReadableFromId(activeTrigs.get(i));
		}
		PopUpFragment popup = PopUpFragment.newInstance( (String[]) activeTrigs.toArray(), names, dismiss);
		popupList = ((AlertDialog) popup.getDialog()).getListView();
	    popup.show(getFragmentManager(), TAG);
	    locationService.setActiveIds(activeTrigs);
	}
	
// -----------------------------------SETTINGS UTILITIES----------------------------------------------
	
	/**
	 * VERSION FOR HANDLER!!!
	 * this is called every time the switch is changed PLUS every time the
	 * settings fragment is loaded
	 */
	@Override
	public void onSwitchChanged(boolean status) {
		onOff = status;
		// TODO figure out if it is necessary to see if the service is running
		// supposedly, android won't let a service be activated twice
		/**
		 *  if the service should be running, and it is not, start it
		 *  if the service should not be running, and it is, stop it
		 *  if the service should be running and it is, do nothing
		 *  if the service should not be running, and it is not, do nothing
		 */
		boolean serviceRunning = isServiceRunning(LocationService.class);
		Intent intent = new Intent(getApplicationContext(), LocationService.class);
		if (status) {
			if (!serviceRunning) {
				startService(intent);
				getApplicationContext().bindService(intent, connection, 0); // TODO
				Log.d(TAG, "location service was not running, and was started");
			} else {
				getApplicationContext().bindService(intent, connection, 0); // TODO
				Log.d(TAG, "location service was running and continues to run");
			}
		} else if (serviceRunning) {
			onUnbound();
			getApplicationContext().unbindService(connection); // TODO
			stopService(intent);
			Log.d(TAG, "location service was running, and was stopped");
		} else
			Log.d(TAG, "service was not running and still is not running");
	}
	
	@Override
	public void sliderChanged(String type, int value, boolean active) {
		if(active) {
			categoryRadii.put(type, (int) (1609.34*value));
		} else {
			if(categoryRadii.containsKey(type)) categoryRadii.remove(type);
		}
		if(locationBound) locationService.setCategoryRadii(categoryRadii);
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
	
	/**
	 * when the service is bound, give it this activity to talk back to
	 */
	public void onBound() {
		locationService.setHandler(locationHandler);
		locationService.setCategoryRadii(categoryRadii);
	}
	
	/**
	 * when the service is unboundbound, give it this activity to talk back to
	 */
	public void onUnbound() {
		locationService.setHandler(null);
		locationBound=false;
	}
	
	/**
	 * toast made to know that the service is communicating with the handler
	 * @param latitude
	 * @param longitude
	 */
	public void locationUpdate(double latitude, double longitude) {
		
		//Toast.makeText(this, "location changed in activity "+latitude+" - "+longitude, Toast.LENGTH_SHORT).show();
	}
	
// -----------------------------------SERVICE UTILITIES----------------------------------------------
	
	@SuppressWarnings("rawtypes")
	private boolean isServiceRunning(Class myService) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (myService.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

// -----------------------------------POP UP CALLBACKS----------------------------------------------	
	
	@Override
	public void removeButton(String geoId, View v) {
		Log.d(TAG, "removeButton callback");
		popupList.removeView(v);
		locationService.removeActiveId(geoId); // pointers SHOULD match but hey, who knows
	}

	@Override
	public void infoButton(String geoId) {
		Log.d(TAG, "infoButton callback");
		
		// TODO get and display info with async task
	}

	@Override
	public void visitButton(String geoId) {
		Log.d(TAG, "visitButton callback");
		
		// TODO get and diplay directions on the map with async task
	}

}
