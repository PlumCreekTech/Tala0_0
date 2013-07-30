package com.plumcreektechnology.tala0_0;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

public class Leg {

	// specify for which ones to get the text vs for which ones to get the value
	private String distance; // in meters
	private String duration; // in seconds
	private Location startLocation; // latitude and longitude
	private Location endLocation; // latitude and longitude
	private String startAddress; // human readable address
	private String endAddress; // human readable address
	private ArrayList<Step> steps;

	static Leg jsonToLeg(JSONObject json) {
		try {
			Leg result = new Leg();
			result.setDistance(json.getString("distance"));
			result.setDuration(json.getString("duration"));

			// set start
			JSONObject jsonLocation = json.getJSONObject("start_location");
			Location location = new Location("blah");
			location.setLatitude(jsonLocation.getDouble("latitude"));
			location.setLongitude(jsonLocation.getDouble("longitude"));
			result.setStartLocation(location);
			// set end
			jsonLocation = json.getJSONObject("end_location");
			location.setLatitude(jsonLocation.getDouble("latitude"));
			location.setLongitude(jsonLocation.getDouble("longitude"));
			result.setEndLocation(location);

			result.setStartAddress(json.getString("start_address"));
			result.setEndAddress(json.getString("end_address"));
			result.setSteps(new ArrayList<Step>());
			JSONArray steps = json.getJSONArray("steps");
			for (int i = 0; i < steps.length(); i++) {
				result.addStep(Step.jsonToStep(steps.getJSONObject(i)));
			}
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

	public Location getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(Location startLocation) {
		this.startLocation = startLocation;
	}

	public Location getEndLocation() {
		return endLocation;
	}

	public void setEndLocation(Location endLocation) {
		this.endLocation = endLocation;
	}

	public String getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(String startAddress) {
		this.startAddress = startAddress;
	}

	public String getEndAddress() {
		return endAddress;
	}

	public void setEndAddress(String endAddress) {
		this.endAddress = endAddress;
	}

	public ArrayList<Step> getSteps() {
		return steps;
	}

	public void setSteps(ArrayList<Step> steps) {
		this.steps = steps;
	}

	public void addStep(Step step) {
		steps.add(step);
	}

	@Override
	public String toString() {
		return "Leg [distance=" + distance + ", duration=" + duration
				+ ", startLocation=" + startLocation + ", endLocation="
				+ endLocation + ", startAddress=" + startAddress
				+ ", endAddress=" + endAddress + ", steps=" + steps + "]";
	}
}
