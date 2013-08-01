package com.plumcreektechnology.tala0_0;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class SettingsFragment extends Fragment {
	
	private OnOffReceiver switchReceiver;
	
	public interface OnOffReceiver {
		public void onSwitchChanged(boolean status);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout =  inflater.inflate(R.layout.settings_frag, container, false);
		Switch onOff = (Switch) layout.findViewById(R.id.on_off_switch);
		onOff.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				switchReceiver.onSwitchChanged(isChecked);
			}
			
		});
		switchReceiver.onSwitchChanged(onOff.isChecked());
		return layout;
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
}
