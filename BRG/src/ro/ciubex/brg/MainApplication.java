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
package ro.ciubex.brg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ro.ciubex.brg.model.Contact;
import ro.ciubex.brg.util.ApplicationPreferences;
import ro.ciubex.brg.util.CalendarUtils;
import ro.ciubex.brg.util.Utilities;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

/**
 * This is main application class.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class MainApplication extends Application {
	private ApplicationPreferences mApplicationPreferences;
	private CalendarUtils mCalendarUtils;
	private List<Contact> mContacts;
	private boolean mContactsLoaded;
	private ProgressDialog mProgressDialog;

	/**
	 * This method is invoked when the application is created.
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		mContactsLoaded = false;
		mContacts = new ArrayList<Contact>();
		mApplicationPreferences = new ApplicationPreferences(this);
		mCalendarUtils = new CalendarUtils(this);
	}

	/**
	 * Get the contacts list.
	 * 
	 * @return the contacts
	 */
	public List<Contact> getContacts() {
		return mContacts;
	}

	/**
	 * Retrieve default application locale
	 * 
	 * @return Default locale used on application
	 */
	public Locale getDefaultLocale() {
		return mApplicationPreferences.getDefaultLocale();
	}

	/**
	 * Check the if the contact list is loaded.
	 * 
	 * @return the contactsLoaded
	 */
	public boolean isContactsLoaded() {
		return mContactsLoaded;
	}

	/**
	 * Obtain date format string.
	 * 
	 * @return The date format string.
	 */
	public String getDateFormat() {
		return mApplicationPreferences.getDateFormat();
	}

	/**
	 * Obtain the date format for the birthday.
	 * 
	 * @return
	 */
	public String getDisplayDateFormat() {
		return mApplicationPreferences.getDisplayDateFormat();
	}

	/**
	 * Format a provided calendar to a preferred formatted string.
	 * 
	 * @param calendar
	 *            The calendar to be formatted.
	 * @return The formatted calendar string.
	 */
	public String getFormattedCalendar(Calendar calendar) {
		return Utilities.getFormattedCalendar(getDefaultLocale(),
				getDisplayDateFormat(), calendar);
	}

	/**
	 * Set the flag used to check if contacts list is loaded.
	 * 
	 * @param contactsLoaded
	 *            the contactsLoaded to set.
	 */
	public void setContactsLoaded(boolean contactsLoaded) {
		this.mContactsLoaded = contactsLoaded;
	}

	/**
	 * This will show a progress dialog using a context and a message ID from
	 * application string resources.
	 * 
	 * @param context
	 *            The context where should be displayed the progress dialog.
	 * @param messageId
	 *            The string resource id.
	 */
	public void showProgressDialog(Context context, int messageId) {
		showProgressDialog(context, getString(messageId));
	}

	/**
	 * This will show a progress dialog using a context and the message to be
	 * showed on the progress dialog.
	 * 
	 * @param context
	 *            The context where should be displayed the progress dialog.
	 * @param message
	 *            The message displayed inside of progress dialog.
	 */
	public void showProgressDialog(Context context, String message) {
		hideProgressDialog();
		mProgressDialog = ProgressDialog.show(context,
				getString(R.string.please_wait), message);
	}

	/**
	 * Method used to hide the progress dialog.
	 */
	public void hideProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = null;
	}

	/**
	 * Method used to show the informations.
	 * 
	 * @param context
	 *            The context where should be displayed the message.
	 * @param resourceMessageId
	 *            The string resource id.
	 */
	public void showMessageInfo(Context context, int resourceMessageId) {
		String message = getString(resourceMessageId);
		showMessageInfo(context, message);
	}

	/**
	 * Method used to show formated informations.
	 * 
	 * @param context
	 *            The context where should be displayed the message.
	 * @param resourceMessageId
	 *            The string resource id.
	 * @param formatArgs
	 *            The arguments used on formated message.
	 */
	public void showMessageInfo(Context context, int resourceMessageId,
			Object... formatArgs) {
		String message = getString(resourceMessageId, formatArgs);
		showMessageInfo(context, message);
	}

	/**
	 * This method is used to show on front of a context a toast message.
	 * 
	 * @param context
	 *            The context where should be showed the message.
	 * @param message
	 *            The message used to be displayed on the information box.
	 */
	public void showMessageInfo(Context context, String message) {
		if (message != null && message.length() > 0) {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Method used to show the errors.
	 * 
	 * @param context
	 *            The context where should be displayed the error message.
	 * @param resourceMessageId
	 *            The string resource id.
	 */
	public void showMessageError(Context context, int resourceMessageId) {
		String message = getString(resourceMessageId);
		showMessageError(context, message);
	}

	/**
	 * Method used to show error formated messages.
	 * 
	 * @param context
	 *            The context where should be displayed the error message.
	 * @param resourceMessageId
	 *            The string resource id.
	 * @param formatArgs
	 *            The arguments used on formated message.
	 */
	public void showMessageError(Context context, int resourceMessageId,
			Object... formatArgs) {
		String message = getString(resourceMessageId, formatArgs);
		showMessageError(context, message);
	}

	/**
	 * This method is used to show on front of a context a toast message
	 * containing applications errors.
	 * 
	 * @param context
	 *            The context where should be showed the message.
	 * @param message
	 *            The error message used to be displayed on the information box.
	 */
	public void showMessageError(Context context, String message) {
		if (message != null && message.length() > 0) {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Method used to obtain the age based on a birthday calendar.
	 * 
	 * @param birthday
	 *            The birthday calendar.
	 * @return The age as a string.
	 */
	public String getAge(Calendar birthday) {
		Calendar now = Calendar.getInstance(getDefaultLocale());
		int age = now.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
		if (now.get(Calendar.DAY_OF_YEAR) < birthday.get(Calendar.DAY_OF_YEAR)) {
			age -= 1;
		}
		return getString(R.string.age, age);
	}

	/**
	 * Obtain the application preferences.
	 * 
	 * @return the applicationPreferences
	 */
	public ApplicationPreferences getApplicationPreferences() {
		return mApplicationPreferences;
	}

	/**
	 * Obtain the calendar utils.
	 * 
	 * @return the calendarUtils
	 */
	public CalendarUtils getCalendarUtils() {
		return mCalendarUtils;
	}
}
