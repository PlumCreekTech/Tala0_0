package com.plumcreektechnology.tala0_0;

import java.util.Timer;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class LocationService extends Service implements Tala_Constants,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {
	
	// because it is registered in XML as remote is SHOULD run on its own process (background)
	// should we use a timer to get the most recent location or should we have a listener for gettting updates?

	// nora's constants
	boolean followUser;
	// my constants
	private final String TAG = getClass().getName();
	private String placesSpecification;
	private Timer timer;
	private LocationRequest locationRequest;
	private LocationClient locationClient;
	
// -----------------------------------BINDER METHODS----------------------------------------------
	
	/**
	 * Returns IBinder so activities can bind to this for 
	 * communication purposes
	 */
	public class LocationBinder extends Binder{
		public LocationService getService(){
			return LocationService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO do we want to bind the service? if not overriding is unnecessary.
		return new LocationBinder();
	}

// -----------------------------------CONTSTRUCTOR AND ON HANDLE INTENT----------------------------------------------
	

	/**
	 * Constructor; calls super with name for the worker thread it's
	 * running on.
	 */
	public LocationService() {
		//super("LocationService");
		super();
	}
	
//	/**
//	 * Initializes location things
//	 */
//	@Override
//	protected void onHandleIntent(Intent intent) {
//		// TODO Auto-generated method stub
//		// followUser = getActivity().isMapVisible(); services do not have a getActivity
//		
//	}

// -----------------------------------LIFECYCLE METHODS----------------------------------------------
	
	@Override
	public void onCreate() {
		super.onCreate();
		// MAKE SURE THAT YOU CHECK THAT THE DEVICE HAS GOOGLE SERVICES IN THE MAIN BEFORE STARTING THIS SERVICE
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(UPDATE_INTERVAL_MS);
		locationRequest.setFastestInterval(FASTEST_INTERVAL_MS);
		locationClient = new LocationClient(this, this, this);
		Log.d(TAG, "created location request and client");
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		locationClient.connect();
		Log.d(TAG, "tried to connect client");
		return super.onStartCommand(intent, flags, startId);
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(locationClient.isConnected()) locationClient.disconnect();
		// TODO delete location things
	}

// -----------------------------------LOCATION SERVICES CALLBACKS----------------------------------------------
	
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e(TAG, "location connection failed!");
		// TODO check if the main is running or activate it if it is not, don't die silently!
//        if (connectionResult.hasResolution()) {
//            try {
//                connectionResult.startResolutionForResult( this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
//            } catch (IntentSender.SendIntentException e) {
//                e.printStackTrace();
//            }
//        } else {
//             showErrorDialog(connectionResult.getErrorCode());
//        }
	}

	@Override
	public void onConnected(Bundle bundle) {
		locationClient.requestLocationUpdates(locationRequest, this);
		Toast.makeText(this, "Location Services Connected.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Location Services Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
		
	}

// -----------------------------------LOCATION LISTENER CALLBACKS----------------------------------------------


	@Override
	public void onLocationChanged(Location location) {
		// TODO call PlacesService
		//PlacesService pserv = new PlacesService();
		//ArrayList<Place> plist = pserv.findPlaces(location.getLatitude(), location.getLongitude(), placesSpecification);
		Toast.makeText(this, "location changed: "+location.getLatitude()+" - "+location.getLongitude(), Toast.LENGTH_SHORT).show();
		
	}

}