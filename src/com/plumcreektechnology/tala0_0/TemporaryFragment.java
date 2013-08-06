package com.plumcreektechnology.tala0_0;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TemporaryFragment extends Fragment {

	private PlaceSpecification placeSpecifier;
	
	public interface PlaceSpecification {
		public String makePlaceSpecification();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			placeSpecifier = (PlaceSpecification) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement PlaceSpecification");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout =  inflater.inflate(R.layout.temp_frag, container, false);
		TextView tv = (TextView) layout.findViewById(R.id.temp_text);
		tv.setText(placeSpecifier.makePlaceSpecification());
		return layout;
	}
	
	
}