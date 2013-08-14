package com.plumcreektechnology.tala0_0;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;

public class LocationService extends Service implements Tala_Constants,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener,
		OnAddGeofencesResultListener,
		LocationClient.OnRemoveGeofencesResultListener {

	// constants
	private final String TAG = getClass().getName();
	private LocationRequest locationRequest;
	private LocationClient locationClient;
	private TreeMap<String, Integer> categoryRadii;
//	private String placeSpecification;
	private TreeMap<String, Place> activeFences;
	private ArrayList<String> activeIds = new ArrayList<String>();
	private Location currentLocation;

// -----------------------------------BINDER METHODS----------------------------------------------

	private Handler locationHandler;
	private final IBinder locationBinder = new LocationBinder();
			
	/**
	 * Returns IBinder so activities can bind to this for 
	 * communication purposes
	 */
	public class LocationBinder extends Binder{
		public LocationService getService(){
			return LocationService.this;
		}
	}

	/**
	 * Returns IBinder that activity that calls service
	 * can access
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return locationBinder;
	}
	
	/**
	 * Initializes handler so that service can send messsages
	 * to activity
	 * @param handle
	 */
	public void setHandler(Handler handle) {
		locationHandler = handle;
	}

// -----------------------------------CONSTRUCTOR----------------------------------------------
	
	/**
	 * Constructor; calls super with name for the worker thread it's
	 * running on.
	 */
	public LocationService() {
		super();
	}

// -----------------------------------LIFECYCLE METHODS----------------------------------------------
	
	/**
	 * Initialization of basic necessities (list of active Geofences,
	 * set up LocationClient)
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		// MAKE SURE THAT YOU CHECK THAT THE DEVICE HAS GOOGLE SERVICES IN THE MAIN BEFORE STARTING THIS SERVICE
		activeFences = new TreeMap<String, Place>();
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(UPDATE_INTERVAL_MS);
		locationRequest.setFastestInterval(FASTEST_INTERVAL_MS);
		locationClient = new LocationClient(this, this, this);
		Log.d(TAG, "created location request and client");
	}

	/**
	 * Connects LocationClient
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		locationClient.connect();
		return Service.START_REDELIVER_INTENT; // if it is stopped, restart it with the same intent it currently has
	}

	/**
	 * Disconnects LocationClient if connected
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(locationClient.isConnected()) {
			if(activeFences.size()>0) {
				Log.d(TAG, "should be removing geofences");
				locationClient.removeGeofences(placeMapToIDList(activeFences), new OnRemoveGeofencesResultListener() {

					@Override
					public void onRemoveGeofencesByPendingIntentResult(
							int arg0, PendingIntent arg1) {
						Log.d(TAG, "remove geofences by pending intent... we shouldn't be here");
					}

					@Override
					public void onRemoveGeofencesByRequestIdsResult(int status,
							String[] requestIDs) {
						switch(status) {
						case (LocationStatusCodes.SUCCESS):
							Log.d(TAG, "removed "+requestIDs.length+" geofences successfully");
							break;
						case (LocationStatusCodes.ERROR):
							Log.d(TAG, "didn't remove "+requestIDs.length+" geofences due to error");
							break;
						case (LocationStatusCodes.GEOFENCE_NOT_AVAILABLE):
							Log.d(TAG, "didn't remove "+requestIDs.length+" geofences due to geofence not available");
							break;
						}
					}
					
				});
			}
			//new RemoveFences().execute(activeFences);
			locationClient.removeLocationUpdates(this);
			locationClient.disconnect();
		}
	}

// -----------------------------------LOCATION SERVICES CALLBACKS----------------------------------------------
	/**
	 * Handles connection failure...currently doesn't do anything...TODO?
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e(TAG, "failed to connect to location services");
	}

	/**
	 * Requests location updates once LocationService is connected
	 */
	@Override
	public void onConnected(Bundle bundle) {
		locationClient.requestLocationUpdates(locationRequest, this);
		Log.d(TAG, "Location Services Connected.");
	}

	/**
	 * Toasts when service is disconnected TODO?
	 */
	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Location Services Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
		
	}

// -----------------------------------LOCATION LISTENER CALLBACKS----------------------------------------------

	@SuppressWarnings("unchecked") // TODO is this bad?
	/**
	 * Requests place list whenever location is changed and sends message containing
	 * user's latitude and longitude to bound main activity.
	 */
	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;
		// if there are categories checked, get the nearby places that fit the criteria
		if(categoryRadii.size()>0) new GetPlaces().execute(new PlaceQuery(location.getLatitude(), location.getLongitude(), makePlaceSpecification(), 50000));
		// if there are not categories checked AND there are fences, call MakeFences in such a way that it removes all fences
		else if (activeFences.size()>0) new MakeFences().execute(new TreeMap<String, Place>());
		// if there are no categories and no fences then do nothing
		
		// if the activity is running, send it this info
		if(locationHandler!=null) {

			Message msg = locationHandler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putDouble("latitude", location.getLatitude());
			bundle.putDouble("longitude", location.getLongitude());
			msg.setData(bundle);
			locationHandler.sendMessage(msg);

		} else {
			Log.d(TAG, "handler is null");
			//Toast.makeText(this, "location changed in service " + location.getLatitude() + " - " + location.getLongitude(), Toast.LENGTH_SHORT).show();
		}
	}

// -----------------------------------BINDER CALLBACKS----------------------------------------------
	
	public void setCategoryRadii(TreeMap<String, Integer> map) {
		categoryRadii = map;
	}
	
	public String getReadableFromId(String id) {
		return activeFences.get(id).getName();
	}

	
// -----------------------------------GEOFENCE METHODS----------------------------------------------
	
	@SuppressWarnings("unchecked") // TODO is this bad?
	private void updateGeofences(TreeMap<String, Place> placeMap) {
		new MakeFences().execute(placeMap);
	}
	
	private class MakeFences extends
			AsyncTask< TreeMap<String, Place>, Void, TreeMap<String, Place>> {
		
		private TreeMap<String, Place> oldFences;
		private LocationClient client;
		private OnAddGeofencesResultListener addListener;
		private LocationClient.OnRemoveGeofencesResultListener removeListener;
		
		@Override
		protected void onPreExecute() {
			oldFences = activeFences;
			client = locationClient;
			addListener = LocationService.this;
			removeListener = LocationService.this;
		}
		
		@Override
		protected TreeMap<String, Place> doInBackground(TreeMap<String, Place>... params) {
			
			//if(!client.isConnected()) return new TreeMap<String, Place>();
			TreeMap<String, Place> newFences = params[0];
			
			// if distance between a potential new fence, and current location is less than 
			// category radii distance of its type, don't make a geofence for it, make an alert for it
			ArrayList<String> removeIDs = new ArrayList<String>();
			for( Entry<String, Place> entry : newFences.entrySet()) {
				Location dest = new Location("blah");
				dest.setLatitude(entry.getValue().getLatitude());
				dest.setLongitude(entry.getValue().getLongitude());
				int dist = (int) currentLocation.distanceTo(dest);
				Log.d(TAG, "distance to "+entry.getValue().getName()+" is "+(float)(dist/1609.34));
				if( dist < typesAverage(entry.getValue().getTypes())) {
					Log.v(TAG, entry.getValue().getName()+" is already within proximity");
					removeIDs.add(entry.getKey());
				}
			}
			Log.v(TAG, "eliminating "+removeIDs.size()+" potential geofences");
			for( String id : removeIDs) {
				newFences.remove(id);
			}
			
			
			TreeMap<String, Place> realFences = new TreeMap<String, Place>();
//			Log.d(TAG, "old fences before loop has "+oldFences.size());
			Log.d(TAG, "new fences before loop has "+newFences.size());
//			Log.d(TAG, "real fences before loop has "+realFences.size());
			removeIDs.clear();
			for( Entry<String, Place> entry : oldFences.entrySet()) {
				if(newFences.containsKey(entry.getKey())) {
					Place place = entry.getValue();
					realFences.put(place.getId(), place);
					newFences.remove(place.getId());
					removeIDs.add(place.getId());
				}
			}
			for( String id : removeIDs) {
				oldFences.remove(id);
			}
			Log.d(TAG, "old fences after loop has "+oldFences.size());
			Log.d(TAG, "new fences after loop has "+newFences.size());
			Log.d(TAG, "real fences after loop has "+realFences.size());
			// need to sort out some stuff
			if(!oldFences.isEmpty()) {
				client.removeGeofences(placeMapToIDList(oldFences), removeListener);
			}
			if(!newFences.isEmpty()) {
				client.addGeofences(placeMapToGeolist(newFences), getGeofenceReceiverIntent(), addListener);
			}
			realFences.putAll(newFences);
			return realFences;
		}
				
		@Override
		protected void onPostExecute(TreeMap<String, Place> list) {
			activeFences = list;
		}
	}
	
	public ArrayList<Geofence> placeMapToGeolist(TreeMap<String, Place> placeList) {
		ArrayList<Geofence> geoList = new ArrayList<Geofence>();
		
		for(Place place : placeList.values()) {
			geoList.add(new Geofence.Builder()
			.setRequestId(place.getId())
			.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
			.setCircularRegion(place.getLatitude(), place.getLongitude(), typesAverage(place.getTypes())) // default radius of 3000
			.setExpirationDuration(Geofence.NEVER_EXPIRE)
			.build() );					// TODO read in radii!!!
		}
		return geoList;
	}
	
	private int typesAverage(String[] types) {
		int sum = 0;
		int num = 0;
		for(int i=0; i<types.length; i++) {
			if(categoryRadii.containsKey(types[i])) {
				sum+=categoryRadii.get(types[i]);
				++num;
			}
		}
		return sum/num; // divide by the number of active categories from the list
	}

	public ArrayList<String> placeMapToIDList(TreeMap<String, Place> placeList) {
		ArrayList<String> iDList = new ArrayList<String>();
		for(Place place : placeList.values()) {
			iDList.add(place.getId());
			}
		return iDList;
	}
	
	public PendingIntent getGeofenceReceiverIntent() {
		Log.d(TAG, "getting Geofence pending intent");
		Intent intent = new Intent(getApplicationContext(), GeofenceReceiverService.class);
		return PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}


// -----------------------------------PLACES API METHODS----------------------------------------------

	public String makePlaceSpecification() {
		String places = categoryRadii.keySet().toString();
		places = places.substring(1, places.length()-1);
		places = places.replaceAll(", ", "|");
		return places;
	}
	
	private class GetPlaces extends AsyncTask< PlaceQuery, Void, TreeMap<String, Place>> {
		
		@Override
		protected TreeMap<String, Place> doInBackground(PlaceQuery... params) {
			PlaceQuery pq = params[0];
			String urlString = makeUrl(pq.getLatitude(), pq.getLongitude(), pq.getPlaces(), pq.getRadius());
			TreeMap<String, Place> treeMap = new TreeMap<String, Place>();
			String nextPageToken = "";

			try {
				String json = getJSON(urlString);
				JSONObject object = new JSONObject(json);
				// get the results
				JSONArray array = object.getJSONArray("results");
				for (int i = 0; i < array.length(); i++) {
					try {
						Place place = Place
								.jsonToPontoReferencia((JSONObject) array.get(i));
						//Log.v("Places Services ", "" + place);
						treeMap.put(place.getId(), place);
					} catch (Exception e) {
						// TODO
					}
				}
				// get next page token
				if(object.has("next_page_token")) {
					nextPageToken = object.getString("next_page_token");
					//Log.d(TAG, "just got a next page token "+nextPageToken);
				}
				
			} catch (JSONException ex) {
				Logger.getLogger(LocationService.class.getName()).log(Level.SEVERE,
						null, ex);
				return null;
			}
			
			int token=2;
			if (!nextPageToken.isEmpty() && nextPageToken!=null && token>0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				--token;
				String tokenUrl = makeTokenUrl(nextPageToken);
				try {
					String json = getJSON(tokenUrl);
					JSONObject object = new JSONObject(json);
					// get the results
					JSONArray array = object.getJSONArray("results");
					for (int i = 0; i < array.length(); i++) {
						try {
							Place place = Place
									.jsonToPontoReferencia((JSONObject) array.get(i));
							//Log.v("Places Services ", "" + place);
							treeMap.put(place.getId(), place);
						} catch (Exception e) {
							// TODO
							Log.e(TAG, "token url error: "+e.toString());
						}
					}
					// get next page token
					if(object.has("next_page_token")) {
						nextPageToken = object.getString("next_page_token");
						//Log.d(TAG, "just got a next page token!!!");
					}
					
				} catch (JSONException ex) {
					Logger.getLogger(LocationService.class.getName()).log(Level.SEVERE,
							null, ex);
					return null;
				}
			}
			Log.d(TAG, "tree map from getPlaces has "+treeMap.size()+" elements");
			return treeMap;
		}
		
		@Override
		protected void onPostExecute(TreeMap<String, Place> map) {
			updateGeofences(map);
		}
	}

	// https://maps.googleapis.com/maps/api/place/search/json?location=28.632808,77.218276&radius=500&types=atm&sensor=false&key=apikey
	private String makeUrl(double latitude, double longitude, String place, int radius) {
		StringBuilder urlString = new StringBuilder(
				"https://maps.googleapis.com/maps/api/place/search/json?");

		if (place.equals("")) {
			urlString.append("&location=");
			urlString.append(Double.toString(latitude));
			urlString.append(",");
			urlString.append(Double.toString(longitude));
			urlString.append("&radius=" + radius);
			// urlString.append("&types="+place);
			urlString.append("&sensor=true&key=" + API_KEY);
		} else {
			urlString.append("&location=");
			urlString.append(Double.toString(latitude));
			urlString.append(",");
			urlString.append(Double.toString(longitude));
			urlString.append("&radius=" + radius);
			urlString.append("&types=" + place);
			urlString.append("&sensor=true&key=" + API_KEY);
		}
		Log.d(TAG, urlString.toString());
		return urlString.toString();
	}
	
	private String makeTokenUrl(String token) {
		Log.d(TAG, "making token url");
		StringBuilder urlString = new StringBuilder("https://maps.googleapis.com/maps/api/place/search/json?");
		urlString.append("pagetoken="+token);
		urlString.append("&sensor=true&key=" + API_KEY);
		return urlString.toString();
	}

	protected String getJSON(String url) {
		return getUrlContents(url);
	}

	private String getUrlContents(String theUrl) {
		StringBuilder content = new StringBuilder();
		try {
			URL url = new URL(theUrl);
			URLConnection urlConnection = url.openConnection();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(urlConnection.getInputStream()), 8);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				content.append(line + "\n");
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Log.d(TAG, "get UrlContents ::: "+content.toString());
		return content.toString();
	}

// -----------------------------------SETTINGS UTILITIES----------------------------------------------
	
	@Override
	public void onAddGeofencesResult(int status, String[] requestIDs) {
		switch(status) {
		case (LocationStatusCodes.SUCCESS):
			Log.d(TAG, "added "+requestIDs.length+" geofences successfully");
			break;
		case (LocationStatusCodes.ERROR):
			Log.d(TAG, "didn't add "+requestIDs.length+" geofences due to error");
			break;
		case (LocationStatusCodes.GEOFENCE_NOT_AVAILABLE):
			Log.d(TAG, "didn't add "+requestIDs.length+" geofences due to geofence not available");
			break;
		case (LocationStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES):
			Log.d(TAG, "didn't add "+requestIDs.length+" geofences due to too many geofences");
			break;
		case (LocationStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS):
			Log.d(TAG, "didn't add "+requestIDs.length+" geofences due to too many pending intents");
			break;
		}
	}

	@Override
	public void onRemoveGeofencesByPendingIntentResult(int status, PendingIntent pendingIntent) {	
		switch(status) {
		case (LocationStatusCodes.SUCCESS):
			Log.d(TAG, "removed "+pendingIntent+" geofences successfully");
			break;
		case (LocationStatusCodes.ERROR):
			Log.d(TAG, "didn't remove "+pendingIntent+" geofences due to error");
			break;
		case (LocationStatusCodes.GEOFENCE_NOT_AVAILABLE):
			Log.d(TAG, "didn't remove "+pendingIntent+" geofences due to geofence not available");
			break;
		}
	}

	@Override
	public void onRemoveGeofencesByRequestIdsResult(int status, String[] requestIDs) {
		switch(status) {
		case (LocationStatusCodes.SUCCESS):
			Log.d(TAG, "removed "+requestIDs.length+" geofences successfully");
			break;
		case (LocationStatusCodes.ERROR):
			Log.d(TAG, "didn't remove "+requestIDs.length+" geofences due to error");
			break;
		case (LocationStatusCodes.GEOFENCE_NOT_AVAILABLE):
			Log.d(TAG, "didn't remove "+requestIDs.length+" geofences due to geofence not available");
			break;
		}
	}

	public ArrayList<String> getActiveIds() {
		return activeIds;
	}

	public void setActiveIds(ArrayList<String> activeIds) {
		this.activeIds = activeIds;
	}
	
	public void removeActiveId(String geoId) {
		Log.d(TAG, "before remove activeIds size is "+activeIds.size());
		activeIds.remove(geoId);
		Log.d(TAG, "after remove activeIds size is "+activeIds.size());
	}


}
