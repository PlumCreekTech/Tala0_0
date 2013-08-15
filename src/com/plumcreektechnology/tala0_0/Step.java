package com.plumcreektechnology.tala0_0;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

public class Step {

	// specify for which ones to get the text vs for which ones to get the value
	// private String maneuver; //
	// private String travel_mode; //
	private String distance; // in meters
	private String duration; // in seconds
	private Location start; // latitude and longitude
	private Location end; // latitude and longitude
	private String instructions; // html text string
	private String polyline; // encoded string that encompasses this step

	static Step jsonToStep(JSONObject json) {
		try {
			Step result = new Step();

			result.setDistance(json.getString("distance"));
			result.setDuration(json.getString("duration"));

			// set northeast
			JSONObject jsonLocation = json.getJSONObject("start_location");
			Location location = new Location("blah");
			location.setLatitude(jsonLocation.getDouble("lat"));
			location.setLongitude(jsonLocation.getDouble("lng"));
			result.setStart(location);
			// set southwest
			jsonLocation = json.getJSONObject("end_location");
			location.setLatitude(jsonLocation.getDouble("lat"));
			location.setLongitude(jsonLocation.getDouble("lng"));
			result.setEnd(location);

			result.setInstructions(json.getString("html_instructions"));
			result.setPolyline(json.getJSONObject("polyline").getString("points"));
			return result;
		} catch (JSONException ex) {
			Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public Location getStart() {
		return start;
	}

	public void setStart(Location start) {
		this.start = start;
	}

	public Location getEnd() {
		return end;
	}

	public void setEnd(Location end) {
		this.end = end;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public String getPolyline() {
		return polyline;
	}

	public void setPolyline(String polyline) {
		this.polyline = polyline;
	}

	@Override
	public String toString() {
		return "Step [distance=" + distance + ", duration=" + duration
				+ ", start=" + start + ", end=" + end + ", instructions="
				+ instructions + ", polyline=" + polyline + "]";
	}

}
