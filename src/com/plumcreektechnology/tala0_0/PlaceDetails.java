package com.plumcreektechnology.tala0_0;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaceDetails {

	private String id;
	private String icon;
	private String name;
	private Boolean open;
	private int priceLevel;
	private double rating;
	private String[] reviewsText;
	private String url;
	
	static PlaceDetails jsonToPontoReferencia(JSONObject pontoReferencia) {
		try {
			PlaceDetails result = new PlaceDetails();
			JSONArray reviews = (JSONArray) pontoReferencia.get("reviews");
			String[] reviewArray = new String[reviews.length()];
			for(int i=0; i<reviews.length(); i++) {
				reviewArray[i] = reviews.getString(i);
			}
			result.setReviewsText(reviewArray);
			
			result.setId(pontoReferencia.getString("id"));
			result.setIcon(pontoReferencia.getString("icon"));
			result.setName(pontoReferencia.getString("name"));
			
			JSONObject opening_hours = (JSONObject) pontoReferencia.get("opening_hours");
			result.setOpen(opening_hours.getBoolean("open_now"));
			
			result.setPriceLevel(pontoReferencia.getInt("price_level"));
			result.setRating(pontoReferencia.getDouble("rating"));
			result.setUrl(pontoReferencia.getString("url"));
			
			return result;
		} catch (JSONException ex) {
			Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "PlaceDetails [id=" + id + ", icon=" + icon + ", name=" + name
				+ ", open=" + open + ", priceLevel=" + priceLevel + ", rating="
				+ rating + ", reviewsText=" + Arrays.toString(reviewsText)
				+ ", url=" + url + "]";
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getOpen() {
		return open;
	}
	public void setOpen(Boolean open) {
		this.open = open;
	}
	public int getPriceLevel() {
		return priceLevel;
	}
	public void setPriceLevel(int priceLevel) {
		this.priceLevel = priceLevel;
	}
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	public String[] getReviewsText() {
		return reviewsText;
	}
	public void setReviewsText(String[] reviewsText) {
		this.reviewsText = reviewsText;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
}
