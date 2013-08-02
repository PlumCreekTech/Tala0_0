package com.plumcreektechnology.tala0_0;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class SettingsFragment extends Fragment implements Tala_Constants {
	SharedPreferences prefs;
	private OnOffReceiver switchReceiver;
	public final String PREFERENCE_KEY = "settings_preferences";
	
	public interface OnOffReceiver {
		public void onSwitchChanged(boolean status);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		prefs = getActivity().getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
		
		try {
			switchReceiver = (OnOffReceiver) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnOffReceiver");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout =  inflater.inflate(R.layout.settings_frag, container, false);
		
		//on/off switch
		Switch onOff = (Switch) layout.findViewById(R.id.on_off_switch);
		onOff.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				switchReceiver.onSwitchChanged(isChecked);
				Editor ed = prefs.edit();
				ed.putBoolean(UPDATE_KEY, isChecked);
				ed.commit();
			}
			
		});
		if(prefs.getBoolean(UPDATE_KEY, true)){
			onOff.setChecked(true);
		}else{
			onOff.setChecked(false);
		}
		//deal with switch in main activity
		switchReceiver.onSwitchChanged(onOff.isChecked());
		
		return layout;
	}
	
	
}
