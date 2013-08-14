package com.plumcreektechnology.tala0_0;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * this fragment either gives you more options for learning about a point, removes the point, or disappears
 * @author devinfrenze
 *
 */
public class PopUpFragment extends DialogFragment {
	
	private String TAG = getClass().getName();
	private Activity parentActivity;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		parentActivity=activity;
	}
	
	static PopUpFragment newInstance(String[] ids, String[] names, boolean dismiss) {
		PopUpFragment frag = new PopUpFragment();
		Bundle args = new Bundle();
		args.putStringArray("ids", ids);
		args.putStringArray("names", names);
		args.putBoolean("dismiss", dismiss);
		frag.setArguments(args);
		return frag;
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/** The system calls this only when creating the layout in a dialog. */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		// get data from arguments
		Bundle bundle = getArguments();
		String[] ids = bundle.getStringArray("ids");
		String[] names = bundle.getStringArray("names");
		boolean dismiss = bundle.getBoolean("dismiss");
		
		if(ids.length!=names.length) return null;
		
		PopUpRow[] rows = new PopUpRow[ids.length];
		for(int i=0; i<ids.length; i++) {
			rows[i] = new PopUpRow(ids[i], names[i]);
		}
		
		PopUpAdapter adapter = new PopUpAdapter(this.getActivity(), R.layout.fragment_dialog, rows);
		
		// format and build and return the dialog
		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		builder.setTitle("PROXIMITY UPDATE");
		builder.setNeutralButton("DISMISS",
				(new DialogInterface.OnClickListener() {
					
					private boolean closeActivity;
			
					public void onClick(DialogInterface dialog, int id) {
						buttonSelected(true);
					}

					public boolean getCloseActivity() {
						return closeActivity;
					}

					public DialogInterface.OnClickListener setCloseActivity(boolean closeActivity) {
						this.closeActivity = closeActivity;
						return this;
					}
				}).setCloseActivity(dismiss));
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, dialog.toString()+" ::: "+which); // TODO make useful
			}
		});
		return builder.create();
	}
	
	public void buttonSelected(boolean action) { // CHANGED how this responds to adapt for AffirmativeFragment
		Log.d(TAG, "hit button neutral");
		if(action) parentActivity.onBackPressed();
		dismiss();
	}

}
