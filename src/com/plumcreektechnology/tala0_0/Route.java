package com.plumcreektechnology.tala0_0;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

public class Route {

	private String summary; // main route
	private String overview_polyline; // encoded string that encompasses entire route
	private Location boundNE; // location of north east bound
	private Location boundSW; // location of south west bound
	private String copyright; // copyright info
	private String warnings; // array of warnings TODO should be an array but it looked complicated from JSON
	private Leg leg; // arraylist of legs of this route
	
	static Route jsonToRoute(JSONObject json) {
		try {
			Route result = new Route();
			
			result.setSummary(json.getString("summary"));
			result.setOverview_polyline(json.getString("overview_polyline"));
			
			// set bounds
			JSONObject bounds = json.getJSONObject("bounds");
			// set northeast
			JSONObject jsonLocation = bounds.getJSONObject("northeast");
			Location location =  new Location("blah");
			location.setLatitude(jsonLocation.getDouble("latitude"));
			location.setLongitude(jsonLocation.getDouble("longitude"));
			result.setBoundNE(location);
			// set southwest
			jsonLocation = bounds.getJSONObject("southwest");
			location.setLatitude(jsonLocation.getDouble("latitude"));
			location.setLongitude(jsonLocation.getDouble("longitude"));
			result.setBoundSW(location);
			
			result.setCopyright(json.getString("copyrights"));
			result.setWarnings(json.getJSONArray("warnings").toString(4));

			JSONArray legs = json.getJSONArray("legs");
			result.setLeg( Leg.jsonToLeg(legs.getJSONObject(0)));
			return result;
		} catch (JSONException ex) {
			Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	public Route() {
		
	}
	
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getOverview_polyline() {
		return overview_polyline;
	}
	public void setOverview_polyline(String overview_polyline) {
		this.overview_polyline = overview_polyline;
	}
	public Location getBoundNE() {
		return boundNE;
	}
	public void setBoundNE(Location boundNE) {
		this.boundNE = boundNE;
	}
	public Location getBoundSW() {
		return boundSW;
	}
	public void setBoundSW(Location boundSW) {
		this.boundSW = boundSW;
	}
	public String getCopyright() {
		return copyright;
	}
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	public String getWarnings() {
		return warnings;
	}
	public void setWarnings(String warnings) {
		this.warnings = warnings;
	}
	public Leg getLegs() {
		return leg;
	}
	public void setLeg(Leg leg) {
		this.leg = leg;
	}

	@Override
	public String toString() {
		return "Route [summary=" + summary + ", overview_polyline="
				+ overview_polyline + ", boundNE=" + boundNE + ", boundSW="
				+ boundSW + ", copyright=" + copyright + ", warnings="
				+ warnings + ", leg=" + leg + "]";
	}
	
}
