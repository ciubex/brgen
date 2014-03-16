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
package ro.ciubex.brg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ro.ciubex.brg.R;
import ro.ciubex.brg.model.ContactEvent;
import ro.ciubex.brg.model.ReminderTime;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * This class is used to handle all applications preferences.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class ApplicationPreferences {
	private static final String CONTACT_EVENTS = "contactEvents";
	private Application application;
	private SharedPreferences sharedPreferences;
	private Locale defaultLocale;

	/**
	 * The main constructor used to initialize the application preferences.
	 * 
	 * @param application
	 *            The birthday application.
	 */
	public ApplicationPreferences(Application application) {
		this.application = application;
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(application);
		defaultLocale = Locale.getDefault();
	}

	/**
	 * Retrieve the application shared preferences.
	 * 
	 * @return
	 */
	public SharedPreferences getSharedPreferences() {
		return sharedPreferences;
	}

	/**
	 * Check if is enabled alphabetically fast scroll.
	 * 
	 * @return True if should be displayed alphabetically fast scroll.
	 */
	public boolean isEnabledFastScroll() {
		return sharedPreferences.getBoolean("enableFastScroll", false);
	}

	/**
	 * Sets the default values from an XML preference file by reading the values
	 * defined by each Preference item's android:defaultValue attribute.
	 * 
	 * @param resId
	 *            The resource ID of the preference XML file.
	 * @param readAgain
	 *            Whether to re-read the default values. If false, this method
	 *            sets the default values only if this method has never been
	 *            called in the past.
	 */
	public void setDefaultValues(int resId, boolean readAgain) {
		PreferenceManager.setDefaultValues(application, resId, readAgain);
	}

	/**
	 * Obtain the value stored on the shared preferences or a default value.
	 * 
	 * @param key
	 *            The key from the shared preferences.
	 * @param defaultStringId
	 *            The default string ID for the returned value.
	 * @return The value stored on the shared preferences or a default value.
	 */
	private String getStringValue(String key, int defaultStringId) {
		return getStringValue(key, application.getString(defaultStringId));
	}

	/**
	 * Obtain the value stored on the shared preferences or a default value.
	 * 
	 * @param key
	 *            The key from the shared preferences.
	 * @param defaultValue
	 *            The default string for the returned value.
	 * @return The value stored on the shared preferences or a default value.
	 */
	private String getStringValue(String key, String defaultValue) {
		return sharedPreferences.getString(key, defaultValue);
	}

	/**
	 * Retrieve default application locale.
	 * 
	 * @return Default application locale.
	 */
	public Locale getDefaultLocale() {
		return defaultLocale;
	}

	/**
	 * This method should return reminder title format.
	 * 
	 * @return Reminder title format.
	 */
	public String getReminderTitleFormat() {
		String defValue = application.getString(R.string.reminderTitleFormat);
		return sharedPreferences.getString("reminderTitleFormat", defValue);
	}

	/**
	 * This method return formated reminder title text.
	 * 
	 * @param text
	 *            Text used on formated title.
	 * @return Formated reminder title.
	 */
	public String getReminderTitle(String text) {
		String format = getReminderTitleFormat();
		return String.format(format, text);
	}

	/**
	 * Method used to obtain the reminder description format.
	 * 
	 * @return Reminder description format.
	 */
	public String getReminderDescriptionFormat() {
		String defValue = application
				.getString(R.string.reminderDescriptionFormat);
		return sharedPreferences.getString("reminderDescriptionFormat",
				defValue);
	}

	/**
	 * Create reminder description based on a formated text
	 * 
	 * @param text
	 *            Text to be formated
	 * @return Formated description text
	 */
	public String getReminderDescription(String text) {
		String format = getReminderDescriptionFormat();
		return String.format(format, text);
	}

	/**
	 * Get reminder type.
	 * 
	 * @return Reminder type.
	 */
	public int getReminderType() {
		String value = sharedPreferences.getString("reminderType", "1");
		return Utilities.parseInt(value);
	}

	/**
	 * Get the amount of time before event used for the reminder.
	 * 
	 * @return Amount of time before event, in minutes.
	 */
	public int getReminderBefore() {
		String value = sharedPreferences.getString("reminderBefore", "-1");
		return Utilities.parseInt(value);
	}

	/**
	 * Check if is an all day reminder.
	 * 
	 * @return True if is an all day reminder.
	 */
	public boolean isAllDay() {
		return sharedPreferences.getBoolean("isAllDay", false);
	}

	/**
	 * Get the string of reminder start time.
	 * 
	 * @return String of reminder start time.
	 */
	public String getStringReminderStartTime() {
		String defValue = application.getString(R.string.reminderStartTime);
		return sharedPreferences.getString("reminderStartTime", defValue);
	}

	/**
	 * Get the reminder start time.
	 * 
	 * @return Reminder start time.
	 */
	public ReminderTime getReminderStartTime() {
		return new ReminderTime(getStringReminderStartTime());
	}

	/**
	 * Get the string of reminder end time.
	 * 
	 * @return String of reminder end time.
	 */
	public String getStringReminderEndTime() {
		String defValue = application.getString(R.string.reminderEndTime);
		return sharedPreferences.getString("reminderEndTime", defValue);
	}

	/**
	 * Get the reminder end time.
	 * 
	 * @return Reminder end time.
	 */
	public ReminderTime getReminderEndTime() {
		return new ReminderTime(getStringReminderEndTime());
	}

	/**
	 * Obtain date format string.
	 * 
	 * @return The date format string.
	 */
	public String getDateFormat() {
		return getStringValue("dateFormat", R.string.date_format);
	}

	/**
	 * Obtain the date format for the birthday.
	 * 
	 * @return
	 */
	public String getDisplayDateFormat() {
		return getStringValue("displayDateFormat", R.string.displayDateFormat);
	}

	/**
	 * Check if a calendar is selected for the reminders.
	 * 
	 * @return True if the calendar is selected.
	 */
	public boolean haveCalendarSelected() {
		String value = getStringValue("calendarList", "none");
		boolean result = false;
		try {
			Long.parseLong(value);
			result = true;
		} catch (NumberFormatException exception) {
		}
		return result;
	}

	/**
	 * Get selected calendar ID.
	 * 
	 * @return The selected calendar ID.
	 */
	public long getCalendarSelected() {
		long id = 0L;
		String value = getStringValue("calendarList", "none");
		try {
			id = Long.parseLong(value);
		} catch (NumberFormatException exception) {
		}
		return id;
	}

	/**
	 * Get stored contact events. This is used to store generated reminders on
	 * application preferences.
	 * 
	 * @return Stored contact events list.
	 */
	public List<ContactEvent> getContactEvents() {
		String stored = sharedPreferences.getString(CONTACT_EVENTS, "null");
		List<ContactEvent> list = null;
		if ("null".equals(stored)) {
			list = new ArrayList<ContactEvent>(0);
		} else {
			List<String> strings = Utilities.getStringList(stored);
			list = new ArrayList<ContactEvent>(strings.size());
			if (strings.size() > 0) {
				for (String item : strings) {
					ContactEvent cem = new ContactEvent();
					cem.fromString(item);
					list.add(cem);
				}
			}
		}
		return list;
	}

	/**
	 * Save contact events to the application preferences for future use. Saving
	 * the list so next time, when the application is started, to be known which
	 * reminders were generated.
	 * 
	 * @param list
	 *            List of contact events.
	 */
	public void setContactEvents(List<ContactEvent> list) {
		Editor editor = sharedPreferences.edit();
		if (list.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (ContactEvent cem : list) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(cem.toString());
			}
			editor.putString(CONTACT_EVENTS, sb.toString());
		} else {
			editor.remove(CONTACT_EVENTS);
		}
		editor.commit();
	}

	/**
	 * Save a contact event to the list of events.
	 * 
	 * @param model
	 *            Contact model to be saved.
	 */
	public void setContactEvent(ContactEvent model) {
		List<ContactEvent> list = getContactEvents();
		List<ContactEvent> newList = new ArrayList<ContactEvent>(list);
		int index = newList.indexOf(model);
		if (index > -1) {
			newList.set(index, model);
		} else {
			newList.add(model);
		}
		setContactEvents(newList);
	}

	/**
	 * Remove a contact model from the list of events.
	 * 
	 * @param model
	 *            Contact model to be removed.
	 */
	public void removeContactEvent(ContactEvent model) {
		List<ContactEvent> list = getContactEvents();
		int index = list.indexOf(model);
		if (index > -1) {
			list.remove(index);
			setContactEvents(list);
		}
	}
}
