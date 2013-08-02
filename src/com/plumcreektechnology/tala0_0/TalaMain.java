package com.plumcreektechnology.tala0_0;

import java.util.TreeMap;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.plumcreektechnology.tala0_0.SettingsFragment.OnOffReceiver;
import com.plumcreektechnology.tala0_0.SliderView.SliderReceiver;

public class TalaMain extends FragmentActivity implements SliderReceiver, OnOffReceiver {

	private final String TAG = getClass().getName();
	private FragmentManager fragMan;
	private SettingsFragment settingsFragment;
	private CategoriesFragment catFragment;
	private TreeMap<String, Integer> categoryRadii;
	
	// -----------------------------------LIFECYCLE UTILITIES----------------------------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tala_main);
		fragMan = getFragmentManager();
		if (savedInstanceState == null) {
			categoryRadii = new TreeMap<String, Integer>();
			settingsFragment = new SettingsFragment();
			catFragment = new CategoriesFragment();
//			fragAdder(catFragment);
//			fragRemover(catFragment);
			fragAdder(settingsFragment);
		}
	}

	protected void onStart() {
		super.onStart();
		Toast.makeText(this, makePlaceSpecification(), Toast.LENGTH_LONG).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tala_main, menu);
		return true;
	}
	
	 /** 
	  * when a menu item is selected starts its corresponding fragment
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case (R.id.action_settings):
			fragReplacer(settingsFragment);
			break;
		case (R.id.action_map):
			//TODO mapthings
			break;
		case (R.id.action_temp):
			break;
		}
		return super.onOptionsItemSelected(item);
	}

// -----------------------------------FRAGMENT UTILITIES----------------------------------------------
	
	/**
	 * private utility for adding fragments
	 * @param frag
	 */
	private void fragAdder(Fragment frag) {
		FragmentTransaction trans = fragMan.beginTransaction();
		trans.add(R.id.main_parent, frag);
		trans.addToBackStack(null);
		trans.commit();
	}
	
	/**
	 * private utility for removing fragments
	 * @param frag
	 */
	private void fragRemover(Fragment frag) {
		FragmentTransaction trans = fragMan.beginTransaction();
		trans.remove(frag);
		trans.addToBackStack(null);
		trans.commit();
	}
	
	/**
	 * private utility for replacing fragments
	 * @param frag
	 */
	private void fragReplacer(Fragment frag) {
		FragmentTransaction trans = fragMan.beginTransaction();
		trans.replace(R.id.main_parent, frag);
		trans.addToBackStack(null);
		trans.commit(); 
	}

// -----------------------------------SETTINGS UTILITIES----------------------------------------------
	public void onSubgroupClick(View v){
		fragReplacer(catFragment);
	}
	
	@Override
	public void sliderChanged(String title, int value, boolean active) {
		if(active) {
			categoryRadii.put(title, value);
		} else {
			if(categoryRadii.containsKey(title)) categoryRadii.remove(title);
		}
	}
	
// -----------------------------------PLACES UTILITIES----------------------------------------------
	
	private String makePlaceSpecification() {
		String places = categoryRadii.keySet().toString();
		places = places.substring(1, places.length()-1);
		places = places.replaceAll(", ", "|");
		return places;
	}

	@Override
	public void onSwitchChanged(boolean status) {
		Toast.makeText(this, "updates should be "+status, Toast.LENGTH_SHORT).show();
	}
	
}
