package com.plumcreektechnology.tala0_0;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class LocationService extends Service implements Tala_Constants,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {

	// constants
	private final String TAG = getClass().getName();
	private LocationRequest locationRequest;
	private LocationClient locationClient;
	private TreeMap<String, Integer> categoryRadii;
//	private String placeSpecification;
	private TreeMap<String, Place> activePlaces;
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
		activePlaces = new TreeMap<String, Place>();
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
		Log.d(TAG, "location changed");
		currentLocation = location;
		// if there are categories checked, get the nearby places that fit the criteria
		if(categoryRadii.size()>0) new GetPlaces().execute(new PlaceQuery(location.getLatitude(), location.getLongitude(), makePlaceSpecification(), 50000));
		else Log.d(TAG, "categories has size "+categoryRadii.size());
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
	
	public TreeMap<String, Integer> getCategoryRadii() {
		return categoryRadii;
	}
	
	public String getReadableFromId(String id) {
		return activePlaces.get(id).getName();
	}

	
// -----------------------------------PROXIMITY METHODS----------------------------------------------
	
	private class MakeFences extends
			AsyncTask< TreeMap<String, Place>, Void, TreeMap<String, Place>> {
		
		@Override
		protected TreeMap<String, Place> doInBackground(TreeMap<String, Place>... params) {
			
			//if(!client.isConnected()) return new TreeMap<String, Place>();
			TreeMap<String, Place> newPlaces = (TreeMap<String, Place>) params[0];
			
			// if distance between a potential new fence, and current location is less than 
			// category radii distance of its type, don't make a geofence for it, make an alert for it
			TreeMap<String, Place> triggerMap = new TreeMap<String, Place>();
			ArrayList<String> triggerArrayList = new ArrayList<String>();
			for( Entry<String, Place> entry : newPlaces.entrySet()) {
				Location dest = new Location("blah");
				dest.setLatitude(entry.getValue().getLatitude());
				dest.setLongitude(entry.getValue().getLongitude());
				int dist = (int) currentLocation.distanceTo(dest);
				//Log.d(TAG, "distance to "+entry.getValue().getName()+" is "+(float)(dist/1609.34));
				if( dist < typesAverage(entry.getValue().getTypes())) {
					//Log.v(TAG, entry.getValue().getName()+" is already within proximity");
					triggerMap.put(entry.getKey(), entry.getValue());
					triggerArrayList.add(entry.getKey());
				}
			}
			
			Log.d(TAG, "size is "+triggerArrayList.size());
			String[] sArray = triggerArrayList.toArray(new String[triggerArrayList.size()]);
			
			Log.d(TAG, Arrays.toString(sArray));
			
			if(sArray.length > 0) {
				activePlaces = triggerMap;

				// start application
				Intent intend = new Intent(getApplicationContext(),
						TalaMain.class);
				intend.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK
						| /* Intent.FLAG_ACTIVITY_CLEAR_TOP
						| */ Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intend.putExtra("triggers", sArray);
				intend.putExtra("popup", true);
				getApplicationContext().startActivity(intend);
			}
			
			return triggerMap;
		}
				
		@Override
		protected void onPostExecute(TreeMap<String, Place> map) {
			//activePlaces = map;
		}
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
		
		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(TreeMap<String, Place> map) {
			new MakeFences().execute(map);
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
		//Log.d(TAG, urlString.toString());
		return urlString.toString();
	}
	
	private String makeTokenUrl(String token) {
		//Log.d(TAG, "making token url");
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

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public Location getLocationOfPlace(String geoId) {
		Location loc = new Location("blah");
		loc.setLatitude(activePlaces.get(geoId).getLatitude());
		loc.setLongitude(activePlaces.get(geoId).getLongitude());
		return loc;
	}
}
