package com.plumcreektechnology.tala0_0;

import android.content.Context;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SliderView extends RelativeLayout implements OnSeekBarChangeListener, OnCheckedChangeListener, Tala_Constants {
	
	protected interface SliderReceiver {
		public void sliderChanged(String title, int value, boolean active);
	}
	
	// attribute set gets values from the place it is invoked in XML!!!
	
	private final String TAG = getClass().getName();
	private static final String ANDROIDNS="http://schemas.android.com/apk/res/android";
	private static final String PCTNS="http://plumcreektechnology.com";
	private static final int DEFAULT_INTEGER = 1000;
	private static final boolean DEFAULT_BOOLEAN = false;
	private static final String INTEGER_KEY = "slider_value";
	private static final String BOOLEAN_KEY = "category_checked";
	
	private Context context;
	private SliderReceiver receiver;
	private SeekBar seekBar;
	private TextView status;
	private TextView units;
	private CheckBox cb;
	private boolean currentCheck;
	
	private SharedPreferences prefs;
	private String title;
	private String metric;
	private int maximum;
	private int minimum;
	private int interval;
	private int currentValue;
	
	
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
	 * @param context
	 * @param attrs
	 */
	private void initView(Context context, AttributeSet attrs) {
		try{
			receiver = (SliderReceiver) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement SliderReceiver");
		}
		this.context=context;
		setValuesFromXML(attrs);
		prefs = context.getSharedPreferences(PACKAGE+"_"+title, Context.MODE_PRIVATE);
		// constructs a new seekbar
		seekBar = new SeekBar(context, attrs);
		// sets its max value
		seekBar.setMax(maximum - minimum);
		// assigns a listener
		seekBar.setOnSeekBarChangeListener(this);
//		View.inflate(context, R.layout.slider_layout, this);
	}
	
	private void setValuesFromXML(AttributeSet attrs) {
		title = attrs.getAttributeValue(ANDROIDNS, "title");
		interval = attrs.getAttributeIntValue(PCTNS,  "interval", 1);
		metric = attrs.getAttributeValue(PCTNS, "metric");
		maximum = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
		minimum = attrs.getAttributeIntValue(PCTNS, "min", 0);
	}
	
	@Override
	protected void onMeasure(int width, int height) {
		super.onMeasure(width, height);
		int pWidth = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(width), MeasureSpec.EXACTLY);
		int pHeight = 0;
		if(currentCheck) {
			pHeight = MeasureSpec.makeMeasureSpec((int)(MeasureSpec.getSize(width)*0.25), MeasureSpec.AT_MOST);
		} else {
			pHeight = MeasureSpec.makeMeasureSpec((int)(MeasureSpec.getSize(width)*0.1), MeasureSpec.AT_MOST);
		}
		this.setMeasuredDimension(pWidth, pHeight);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		View layout = ((Activity) context).getLayoutInflater().inflate(R.layout.slider_layout, this);
		this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		status = (TextView) layout.findViewById(R.id.slider_value);
		units = (TextView) layout.findViewById(R.id.slider_metric);
		cb = (CheckBox) layout.findViewById(R.id.check_box);
		cb.setText(title);
		cb.setOnCheckedChangeListener(this);
		units.setText(metric);
		// move our seekBar into the view
		ViewGroup newContainer = (ViewGroup) findViewById(R.id.slider_container);
		newContainer.addView(seekBar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		restoreInt(true, DEFAULT_INTEGER); // restore the old values before updating the view
		restoreBool(true, DEFAULT_BOOLEAN);
		receiver.sliderChanged(title, currentValue, currentCheck);
		updateView();
	}
	
	/**
	 * update the slider_preference view with our current state
	 * @param view
	 */
	protected void updateView() {
		try {
			cb.setChecked(currentCheck);
			changeVisibility(currentCheck);
			status.setText(String.valueOf(currentValue));
			status.setMinimumWidth(30);
			seekBar.setProgress(currentValue-minimum);
		} catch (Exception e) {
			Log.e(TAG, "Error updating seek bar preference", e);
		}
		invalidate();
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBarArg, int progress, boolean fromUser) {
		int newValue = progress + minimum;
		
		if(newValue > maximum) newValue = maximum;
		else if(newValue < minimum) newValue = minimum;
		else if(interval != 1 && newValue % interval != 0) newValue = Math.round(((float) newValue) / interval) * interval;
		
		// should be ommitted probably
		// seekBarArg.setProgress(currentValue - minimum);
		// TODO maybe we'll have to call updateView
		
		currentValue = newValue;
		status.setText(String.valueOf(newValue));
		receiver.sliderChanged(title, currentValue, currentCheck);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putInt(INTEGER_KEY, newValue);
		ed.commit();
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		currentCheck = isChecked;
		cb.setChecked(isChecked);
		changeVisibility(isChecked);
		receiver.sliderChanged(title, currentValue, currentCheck);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putBoolean(BOOLEAN_KEY, isChecked);
		ed.commit();
		invalidate();
	}
		
	@Override
	public void onStartTrackingTouch(SeekBar seekBarArg) {}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBarArg) {}
	
	protected void onSetInitialValue(Boolean restoreValue, Object defaultValue) {
		restoreInt(restoreValue, defaultValue);
		restoreBool(true, false);
	}
	
	private void changeVisibility(Boolean visible) {
		if(visible) {
			seekBar.setVisibility(View.VISIBLE);
			status.setVisibility(View.VISIBLE);
			units.setVisibility(View.VISIBLE);
		} else {
			seekBar.setVisibility(View.GONE);
			status.setVisibility(View.GONE);
			units.setVisibility(View.GONE);
		}
	}
	
	private void restoreInt(Boolean restoreValue, Object defaultValue) {
		if(restoreValue) {
			currentValue = prefs.getInt(INTEGER_KEY, (Integer) defaultValue);
		} else {
			SharedPreferences.Editor ed = prefs.edit();
			int temp = 0;
			try {
				temp = (Integer)defaultValue;
			} catch(Exception e) {
				Log.e(TAG, "Invalid default value: " + defaultValue.toString());
			}
			ed.putInt(INTEGER_KEY, temp);
			currentValue = temp;
		}
	}
	
	private void restoreBool(Boolean restoreValue, Object defaultValue) {
		if(restoreValue) {
			currentCheck = prefs.getBoolean(BOOLEAN_KEY, (Boolean) defaultValue);
		} else {
			SharedPreferences.Editor ed = prefs.edit();
			boolean temp = false;
			try {
				temp = (Boolean)defaultValue;
			} catch(Exception e) {
				Log.e(TAG, "Invalid default value: " + defaultValue.toString());
			}
			ed.putBoolean(BOOLEAN_KEY, temp);
			currentCheck = temp;
		}
	}
}
