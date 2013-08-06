package com.plumcreektechnology.tala0_0;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class InfoService extends Service {
	
	private String API_KEY;
	
	public InfoService(String apikey) {
		this.API_KEY = apikey;
	}

	public void setApiKey(String apikey) {
		this.API_KEY = apikey;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}