package com.plumcreektechnology.tala0_0;

import android.location.Location;

public class DirectionsQuery {
	private Location origin;
	private Location destination;
	private String mode;
	
	public DirectionsQuery(Location origin, Location destination, String mode) {
		super();
		this.origin = origin;
		this.destination = destination;
		this.mode = mode;
	}
	
	public DirectionsQuery(Location origin, Location destination) {
		this(origin, destination, "driving");
	}
	
	public Location getOrigin() {
		return origin;
	}
	public void setOrigin(Location origin) {
		this.origin = origin;
	}
	public Location getDestination() {
		return destination;
	}
	public void setDestination(Location destination) {
		this.destination = destination;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	
}
