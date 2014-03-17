/**
 * This file is part of BRG application.
 * 
 * Copyright (C) 2014 Claudiu Ciobotariu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ro.ciubex.brg.form;

import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ciubex.brg.util.Utilities;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * A custom time preference control, used to define a customized preference time
 * view.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class TimePreference extends DialogPreference implements
		TimePicker.OnTimeChangedListener {
	private static Logger logger = Logger.getLogger(TimePreference.class
			.getName());
	/** The widget for picking a time */
	private Context mContext;
	private AttributeSet mAttributes;
	private TimePicker timePicker;
	private TextView timeDisplay;
	private boolean is24HourFormat;

	/** Default hour */
	private static final int DEFAULT_HOUR = 8;

	/** Default minute */
	private static final int DEFAULT_MINUTE = 0;
	private int timeHour = DEFAULT_HOUR;
	private int timeMinute = DEFAULT_MINUTE;
	/** Minimum and maximum times */
	private int minTime = 0;
	private int maxTime = (23 * 60) + 59;
	private Button okButton;

	/**
	 * Creates a preference for choosing a time based on its XML declaration.
	 * 
	 * @param context
	 * @param attributes
	 */
	public TimePreference(Context context, AttributeSet attributes) {
		super(context, attributes);
		mContext = context;
		mAttributes = attributes;
		setPersistent(false);
		is24HourFormat = DateFormat.is24HourFormat(context);

	}

	/**
	 * Set a minimum time interval
	 * 
	 * @param minTimeHour
	 *            Minimum time hour.
	 * @param minTimeMinute
	 *            Minimum time minute.
	 */
	public void setMinimumTime(int minTimeHour, int minTimeMinute) {
		minTime = (minTimeHour * 60) + minTimeMinute;
	}

	/**
	 * Set a maximum time interval
	 * 
	 * @param maxTimeHour
	 *            The maximum time hour.
	 * @param maxTimeMinute
	 *            The maximum time minute.
	 */
	public void setMaximumTime(int maxTimeHour, int maxTimeMinute) {
		maxTime = (maxTimeHour * 60) + maxTimeMinute;
	}

	/**
	 * This method is used when should be created the control dialog. Here is
	 * created the Time Picker.
	 */
	@Override
	protected View onCreateDialogView() {
		timePicker = new TimePicker(mContext, mAttributes);
		timePicker.setOnTimeChangedListener(this);
		return (timePicker);
	}

	/**
	 * Initialize time picker to currently stored time preferences.
	 * 
	 * @param view
	 *            The dialog preference's host view
	 */
	@Override
	public void onBindDialogView(View view) {
		super.onBindDialogView(view);
		timePicker.setCurrentHour(timeHour);
		timePicker.setCurrentMinute(timeMinute);
		timePicker.setIs24HourView(is24HourFormat);
	}

	/**
	 * Method invoked to create the control view into the preference activity.
	 * Here is added into the summary row of to the original layout another text
	 * view used to display chosen time.
	 * 
	 * @param parent
	 *            The parent view of the control.
	 */
	@Override
	protected View onCreateView(ViewGroup parent) {
		View prefView = super.onCreateView(parent);
		LinearLayout layout = new LinearLayout(parent.getContext());
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 2);
		layout.addView(prefView, lp);
		timeDisplay = new TextView(parent.getContext());
		timeDisplay.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
		timeDisplay.setText(toString());
		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 1);
		layout.addView(timeDisplay, lp2);
		return layout;
	}

	/**
	 * Handles closing of dialog. If user intended to save the settings,
	 * selected hour and minute are stored in the preferences with keys KEY.hour
	 * and KEY.minute, where KEY is the preference's KEY.
	 * 
	 * @param okToSave
	 *            True if user wanted to save settings, false otherwise
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			timePicker.clearFocus();
			timeHour = timePicker.getCurrentHour();
			timeMinute = timePicker.getCurrentMinute();
			String time = String.valueOf(timeHour) + ":"
					+ String.valueOf(timeMinute);

			if (callChangeListener(time)) {
				persistString(time);
				timeDisplay.setText(toString());
			}
			persistTimeValue();
		}
	}

	/**
	 * Store the edited time value.
	 */
	private void persistTimeValue() {
		SharedPreferences.Editor editor = getEditor();
		editor.putString(getKey(), getTimeValue());
		editor.commit();
	}

	/**
	 * Convert selected time to the string into a time value used to be stored.
	 * 
	 * @return Return the value of selected time.
	 */
	private String getTimeValue() {
		String timeValue = Utilities.numberPadding(timeHour) + ":"
				+ Utilities.numberPadding(timeMinute);
		return timeValue;
	}

	/**
	 * Convert selected time to an human readable string.
	 * 
	 * @return An human readable string of selected time.
	 */
	@Override
	public String toString() {
		String timeString = getTimeValue();
		if (!is24HourFormat) {
			int amPmHour = timeHour % 12;
			timeString = Utilities.numberPadding(amPmHour) + ":"
					+ Utilities.numberPadding(timeMinute);
			timeString += ((timeHour >= 12) ? " PM" : " AM");
		}
		return timeString;
	}

	/**
	 * Internal method used to obtain default preference value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return (a.getString(index));
	}

	/**
	 * Method invoked at control initialization.
	 * 
	 * @see android.preference.Preference#onSetInitialValue(boolean,
	 *      java.lang.Object)
	 */
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this.getContext());
		String time = null;
		String defaultTime = null;

		if (defaultValue == null) {
			defaultTime = Utilities.numberPadding(DEFAULT_HOUR) + ":"
					+ Utilities.numberPadding(DEFAULT_MINUTE);
		} else {
			defaultTime = defaultValue.toString();
		}

		if (restoreValue) {
			if (defaultValue == null) {
				time = getPersistedString(defaultTime);
			} else {
				time = getPersistedString(defaultValue.toString());
			}
		} else {
			if (sp != null) {
				time = sp.getString(getKey(), defaultTime);
			} else {
				time = defaultTime;
			}
			if (shouldPersist()) {
				persistString(time);
			}
		}

		String[] timeParts = time.split(":");
		timeHour = Integer.parseInt(timeParts[0]);
		timeMinute = Integer.parseInt(timeParts[1]);
	}

	/**
	 * This method is invoked when the time is changed. And is used to enable or
	 * disable the positive dialog button.
	 * 
	 * @see android.widget.TimePicker.OnTimeChangedListener#onTimeChanged(android.widget.TimePicker,
	 *      int, int)
	 */
	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		int currTime = (hourOfDay * 60) + minute;
		boolean flag = (minTime < currTime && currTime < maxTime);
		logger.log(Level.INFO, "onTimeChanged(" + hourOfDay + ":" + minute
				+ "): " + flag);
		setPositiveButtonEnabled(flag);
	}

	/**
	 * Internal method used to enable or disable the positive (OK) button of
	 * time picker dialog.
	 * 
	 * @param flag
	 *            The enable / disable flag.
	 */
	private void setPositiveButtonEnabled(boolean flag) {
		if (okButton == null) {
			AlertDialog ad = (AlertDialog) getDialog();
			if (ad != null) {
				okButton = ad.getButton(AlertDialog.BUTTON_POSITIVE);
			}
		}
		if (okButton != null) {
			okButton.setEnabled(flag);
		}
	}
}
