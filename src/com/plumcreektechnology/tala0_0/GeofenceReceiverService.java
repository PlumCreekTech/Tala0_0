package com.plumcreektechnology.tala0_0;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class GeofenceReceiverService extends IntentService {

	private final String TAG = getClass().getName();

	public GeofenceReceiverService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public GeofenceReceiverService() {
		super("GeofenceReceiverService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "in geofence receiver!");
		Log.d(TAG, intent.toString());
		// check for errors
		if (LocationClient.hasError(intent)) {
			int errorCode = LocationClient.getErrorCode(intent);
			Log.e("ReceiveTransitionsIntentService",
					"Location Services error: " + Integer.toString(errorCode));
			/*
			 * If there's no error, get the transition type and the IDs of the
			 * geofence or geofences that triggered the transition
			 */
		} else {
			int transitionType = LocationClient.getGeofenceTransition(intent);
			// Test that a valid transition was reported
			if ((transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
					|| (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)) {
				List<Geofence> triggerList = LocationClient.getTriggeringGeofences(intent);

				String[] triggerIds = new String[triggerList.size()];

				for (int i = 0; i < triggerIds.length; i++) {
					// Store the Id of each geofence
					triggerIds[i] = triggerList.get(i).getRequestId();
				}
				
				// start application
				Intent intend = new Intent(getApplicationContext(), TalaMain.class);
				intend.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intend.putExtra("triggers", triggerIds);
				intend.putExtra("popup", true);
				getApplicationContext().startActivity(intend);
				
				Toast.makeText(getApplicationContext(), "these triggers "+triggerIds, Toast.LENGTH_LONG).show();

				
			} else { // An invalid transition was reported
				Log.e("ReceiveTransitionsIntentService",
						"Geofence transition error: "
								+ Integer.toString(transitionType));
			}
		}
	}
}