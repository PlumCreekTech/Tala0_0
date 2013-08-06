package com.plumcreektechnology.tala0_0;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SliderView extends LinearLayout implements
		OnSeekBarChangeListener, OnCheckedChangeListener, Tala_Constants {

	protected interface SliderReceiver {
		public void sliderChanged(String title, int value, boolean active);
	}

	private final String TAG = getClass().getName();
	private static final boolean DEFAULT_BOOLEAN = false;
	private static final String INTEGER_KEY = "slider_value";
	private static final String BOOLEAN_KEY = "category_checked";

	private SharedPreferences prefs;
	private SliderReceiver receiver;
	private SeekBar seekBar;
	private TextView value;
	private TextView units;
	private CheckBox checkBox;
	private boolean currentCheck;
	private int currentValue;
	
	// these things are set in XML attributes
	private String title;
	private String metric;
	private int maximum;
	private int minimum;
	private int interval;
	private int defaultInt;
	

	public SliderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context, attrs);
	}

	public SliderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context, attrs);
	}

	/**
	 * custom class for initializing our preferences
	 * 
	 * @param context
	 * @param attrs
	 */
	private void initView(Context context, AttributeSet attrs) {
		try {
			receiver = (SliderReceiver) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString()
					+ " must implement SliderReceiver");
		}
		
		// INITIALIZATION
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.SliderOptions, 0, 0);
		interval = a.getInt(R.styleable.SliderOptions_interval, 100);
		minimum = a.getInt(R.styleable.SliderOptions_min, 200);
		maximum = a.getInt(R.styleable.SliderOptions_max, 50000);
		defaultInt = a.getInt(R.styleable.SliderOptions_defaultValue, 1000);
		metric = a.getString(R.styleable.SliderOptions_metric);
		title = a.getString(R.styleable.SliderOptions_title);
		a.recycle();
		
		// SHARED PREFERENCES INITIALIZATION
		prefs = context.getSharedPreferences(PACKAGE + "_" + title,
				Context.MODE_PRIVATE);
		
		this.setOrientation(LinearLayout.VERTICAL);
		// add the views that are always visible to the horizontal layout
		LinearLayout horizontal = new LinearLayout(context);
		horizontal.setOrientation(LinearLayout.HORIZONTAL);
		horizontal.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		// check box
		checkBox = new CheckBox(context);
		checkBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 8));
		checkBox.setText(title);
		checkBox.setOnCheckedChangeListener(this);
		checkBox.setFreezesText(true);
		horizontal.addView(checkBox);
		// value
		value = new TextView(context);
		value.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
		value.setFreezesText(true);
		horizontal.addView(value);
		// units
		units = new TextView(context);
		units.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		units.setText(metric);
		units.setFreezesText(true);
		horizontal.addView(units);
		
		// construct the seek bar
		seekBar = new SeekBar(context);
		seekBar.setMax(maximum - minimum);
		seekBar.setSaveEnabled(false);
		seekBar.setOnSeekBarChangeListener(this);
		
		this.addView(horizontal);
		this.addView(seekBar);
		
		// RESTORE VALUES
		restoreInt(true, defaultInt);
		restoreBool(true, DEFAULT_BOOLEAN);
		// SEND VALUES
		receiver.sliderChanged(title, currentValue, currentCheck);
		try {
			checkBox.setChecked(currentCheck);
			changeVisibility(currentCheck);
			value.setText(String.valueOf(currentValue));
			value.setMinimumWidth(30);
			seekBar.setProgress(currentValue - minimum);
		} catch (Exception e) {
			Log.e(TAG, "Error updating seek bar preference", e);
		}
	}
	

	@Override
	public void onProgressChanged(SeekBar seekBarArg, int progress,
			boolean fromUser) {
		int newValue = progress + minimum;

		if (newValue > maximum)
			newValue = maximum;
		else if (newValue < minimum)
			newValue = minimum;
		else if (interval != 1 && newValue % interval != 0)
			newValue = Math.round(((float) newValue) / interval) * interval;

		currentValue = newValue;
		value.setText(String.valueOf(newValue));
		receiver.sliderChanged(title, currentValue, currentCheck);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putInt(INTEGER_KEY, newValue);
		ed.commit();
	}

	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		currentCheck = isChecked;
		//cb.setChecked(isChecked);
		changeVisibility(isChecked);
		receiver.sliderChanged(title, currentValue, currentCheck);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putBoolean(BOOLEAN_KEY, isChecked);
		ed.commit();
		invalidate();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBarArg) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBarArg) {
	}

	private void changeVisibility(Boolean visible) {
		if (visible) {
			seekBar.setVisibility(View.VISIBLE);
			value.setVisibility(View.VISIBLE);
			units.setVisibility(View.VISIBLE);
		} else {
			seekBar.setVisibility(View.GONE);
			value.setVisibility(View.GONE);
			units.setVisibility(View.GONE);
		}
	}

	private void restoreInt(Boolean restoreValue, Object defaultValue) {
		if (restoreValue) {
			currentValue = prefs.getInt(INTEGER_KEY, (Integer) defaultValue);
		} else {
			SharedPreferences.Editor ed = prefs.edit();
			int temp = 0;
			try {
				temp = (Integer) defaultValue;
			} catch (Exception e) {
				Log.e(TAG, "Invalid default value: " + defaultValue.toString());
			}
			ed.putInt(INTEGER_KEY, temp);
			currentValue = temp;
			ed.commit();
		}
	}

	private void restoreBool(Boolean restoreValue, Object defaultValue) {
		if (restoreValue) {
			currentCheck = prefs
					.getBoolean(BOOLEAN_KEY, (Boolean) defaultValue);
		} else {
			SharedPreferences.Editor ed = prefs.edit();
			boolean temp = false;
			try {
				temp = (Boolean) defaultValue;
			} catch (Exception e) {
				Log.e(TAG, "Invalid default value: " + defaultValue.toString());
			}
			ed.putBoolean(BOOLEAN_KEY, temp);
			currentCheck = temp;
			ed.commit();
		}
	}
	
}
