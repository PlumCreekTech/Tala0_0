package com.plumcreektechnology.tala0_0;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

public class GetDirections extends AsyncTask<DirectionsQuery, Void, Route>
		implements Tala_Constants {

	private String TAG = getClass().getName();

	@Override
	protected Route doInBackground(DirectionsQuery... params) {
		DirectionsQuery dq = params[0];
		String urlString = makeUrl(dq.getOrigin(), dq.getDestination(), dq.getMode());
		Log.d(TAG, "url string is ::: "+urlString);
		try {
			String json = getJSON(urlString);
			Log.d(TAG, "JSON ::: "+json);
			JSONObject object = new JSONObject(json);
			JSONArray routes = object.getJSONArray("routes");
			Route route = Route.jsonToRoute(routes.getJSONObject(0));
			return route;
		} catch (JSONException ex) {
			Logger.getLogger(LocationService.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Route route) {
		Log.d(TAG, route.toString());
	}

	// https://maps.googleapis.com/maps/api/place/search/json?location=28.632808,77.218276&radius=500&types=atm&sensor=false&key=apikey
	private String makeUrl(Location origin, Location destination, String mode) {
		StringBuilder urlString = new StringBuilder(
				"https://maps.googleapis.com/maps/api/directions/json?");
		urlString.append("origin=" + origin.getLatitude());
		urlString.append("," + origin.getLongitude());
		urlString.append("&destination=" + destination.getLatitude());
		urlString.append("," + destination.getLongitude());
		urlString.append("&sensor=true");
		urlString.append("&mode=" + mode);
		Log.d(TAG, urlString.toString());
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
		// Log.d(TAG, "get UrlContents ::: "+content.toString());
		return content.toString();
	}
}
