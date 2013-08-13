package com.plumcreektechnology.tala0_0;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class SettingsFragment extends Fragment implements Tala_Constants{
	
	private OnOffReceiver switchReceiver;
	private Switch onOff;
	
	public interface OnOffReceiver {
		public void onSwitchChanged(boolean status);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			switchReceiver = (OnOffReceiver) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnOffReceiver");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout =  inflater.inflate(R.layout.settings_frag, container, false);
		SharedPreferences prefs = ((Context) switchReceiver).getSharedPreferences(PACKAGE, Context.MODE_PRIVATE);
		onOff = (Switch) layout.findViewById(R.id.on_off_switch);
		onOff.setChecked(prefs.getBoolean(ON_OFF_KEY,false));
		onOff.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				switchReceiver.onSwitchChanged(isChecked);
				
				SharedPreferences.Editor ed = ((Context) switchReceiver).getSharedPreferences(PACKAGE, Context.MODE_PRIVATE).edit();
				ed.putBoolean(ON_OFF_KEY, isChecked);
				ed.commit();
			}
			
		});
		// switchReceiver.onSwitchChanged(onOff.isChecked());
		return layout;
	}
	
//	@Override
//	public void onResume() {
//		super.onResume();
//		SharedPreferences prefs = ((Context) switchReceiver).getSharedPreferences(PACKAGE, Context.MODE_PRIVATE);
//		onOff.setChecked(prefs.getBoolean(ON_OFF_KEY, false));
//	}
//	
//	@Override
//	public void onPause() {
//		super.onPause();
//		SharedPreferences.Editor ed = ((Context) switchReceiver).getSharedPreferences(PACKAGE, Context.MODE_PRIVATE).edit();
//		ed.putBoolean(ON_OFF_KEY, onOff.isChecked());
//		ed.commit();
//	}

}
