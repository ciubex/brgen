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
package ro.ciubex.brg.fragment;

import java.util.List;

import ro.ciubex.brg.MainApplication;
import ro.ciubex.brg.R;
import ro.ciubex.brg.model.GoogleCalendar;
import ro.ciubex.brg.util.CalendarUtils;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Birthday application preferences
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	private MainApplication mApplication;
	private ListPreference mCalendarList;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_preferences);
		mApplication = (MainApplication) getActivity().getApplication();
		mCalendarList = (ListPreference) findPreference("calendarList");
		populateAvailableCalendars();
	}

	/**
	 * Prepare all informations when the activity is resuming
	 */
	@Override
	public void onResume() {
		super.onResume();
		prepareSummaries();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	/**
	 * Method used to populate calendar list.
	 */
	private void populateAvailableCalendars() {
		CalendarUtils cu = mApplication.getCalendarUtils();
		if (cu.isCalendarSupported()) {
			List<GoogleCalendar> calendars = cu.getCalendars();
			int i = 0, count = calendars.size();
			String[] entries = new String[count];
			String[] values = new String[count];
			for (GoogleCalendar calendar : calendars) {
				entries[i] = calendar.getName();
				values[i] = String.valueOf(calendar.getId());
				i++;
			}
			mCalendarList.setEntries(entries);
			mCalendarList.setEntryValues(values);
			mCalendarList.setDefaultValue(values[0]);
		}
	}

	/**
	 * Unregister the preference changes when the activity is on pause
	 */
	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	/**
	 * Prepare preferences summaries
	 */
	private void prepareSummaries() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplication);
		EditTextPreference editText = (EditTextPreference) findPreference("displayDateFormat");
		editText.setSummary(sp.getString("displayDateFormat",
				String.valueOf(editText.getSummary())));
		editText = (EditTextPreference) findPreference("dateFormat");
		editText.setSummary(sp.getString("dateFormat",
				String.valueOf(editText.getSummary())));
		editText = (EditTextPreference) findPreference("reminderTitleFormat");
		editText.setSummary(sp.getString("reminderTitleFormat",
				String.valueOf(editText.getSummary())));
		editText = (EditTextPreference) findPreference("reminderDescriptionFormat");
		editText.setSummary(sp.getString("reminderDescriptionFormat",
				String.valueOf(editText.getSummary())));

		String value = (String) mCalendarList.getValue();
		if (!"none".equals(value)) {
			try {
				Long.parseLong(value);
				mCalendarList.setSummary(mCalendarList.getEntry());
			} catch (NumberFormatException exception) {
			}
		}

		ListPreference listPref = (ListPreference) findPreference("reminderType");
		value = (String) listPref.getEntry();
		listPref.setSummary(value);

		listPref = (ListPreference) findPreference("reminderBefore");
		value = (String) listPref.getEntry();
		listPref.setSummary(value);
	}

	/**
	 * This method is invoked when a preference is changed
	 * 
	 * @param sp
	 *            The shared preference
	 * @param key
	 *            Key of changed preference
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		// Update summary value
		Preference obj = findPreference(key);
		if (obj instanceof EditTextPreference) {
			EditTextPreference pref = (EditTextPreference) obj;
			pref.setSummary(sp.getString(key, pref.getText()));
		} else if (obj instanceof ListPreference) {
			ListPreference pref = (ListPreference) obj;
			pref.setSummary((String) pref.getEntry());
		}
	}
}
