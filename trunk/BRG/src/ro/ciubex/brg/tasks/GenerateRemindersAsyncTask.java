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
package ro.ciubex.brg.tasks;

import java.util.ArrayList;
import java.util.List;

import ro.ciubex.brg.MainApplication;
import ro.ciubex.brg.R;
import ro.ciubex.brg.model.Constants;
import ro.ciubex.brg.model.Contact;
import ro.ciubex.brg.model.ContactEvent;
import ro.ciubex.brg.util.ApplicationPreferences;
import ro.ciubex.brg.util.CalendarUtils;
import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class GenerateRemindersAsyncTask extends
		AsyncTask<Void, Void, DefaultAsyncTaskResult> {

	/**
	 * Responder used on generate process.
	 */
	public interface Responder {
		public Application getApplication();

		public void startGenerateReminders();

		public void endGenerateReminders(DefaultAsyncTaskResult result);
	}

	private Contact[] contacts;
	private List<ContactEvent> generated;
	private Responder responder;
	private MainApplication application;
	private ApplicationPreferences applicationPreferences;
	private CalendarUtils calendarUtils;
	private int countInsert;
	private int countUpdate;
	private int countDelete;

	public GenerateRemindersAsyncTask(Responder responder,
				Contact... contacts) {
			this.responder = responder;
			this.contacts = contacts;
			generated = new ArrayList<ContactEvent>();
			application = (MainApplication) responder
					.getApplication();
			applicationPreferences = application.getApplicationPreferences();
			calendarUtils = application.getCalendarUtils();
		}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected DefaultAsyncTaskResult doInBackground(Void... params) {
		DefaultAsyncTaskResult result = new DefaultAsyncTaskResult();
		ContentResolver cr = responder.getApplication().getContentResolver();
		result.resultId = Constants.OK;
		if (calendarUtils.isCalendarSupported()) {
			generateReminders(cr, result);
		} else {
			result.resultId = Constants.ERROR;
			result.resultMessage = responder.getApplication().getString(
					R.string.no_calendar);
		}
		return result;
	}

	/**
	 * Method invoked on the UI thread before the task is executed.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		responder.startGenerateReminders();
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(DefaultAsyncTaskResult result) {
		super.onPostExecute(result);
		responder.endGenerateReminders(result);
	}

	/**
	 * Main method used to generate reminders for all checked contacts.
	 * 
	 * @param cr
	 *            The application ContentResolver
	 * @param result
	 *            The process result.
	 */
	private void generateReminders(ContentResolver cr,
			DefaultAsyncTaskResult result) {
		Uri uriEvents = Uri.parse(calendarUtils.getCalendarUriBase()
				+ "/events");
		Uri uriReminders = Uri.parse(calendarUtils.getCalendarUriBase()
				+ "/reminders");
		countUpdate = 0;
		countInsert = 0;
		countDelete = 0;
		for (Contact contact : contacts) {
			if (contact.haveBirthday()) {
				if (contact.isChecked()) {
					generateBirthdayReminderEvent(cr, contact);
				} else {
					if (contact.haveEvent()) {
						countDelete++;
						calendarUtils.deleteEntry(uriEvents,
								contact.getEventId());
						contact.setEventId(-1);
					}
					if (contact.haveReminder()) {
						calendarUtils.deleteEntry(uriReminders,
								contact.getReminderId());
						contact.setReminderId(-1);
					}
				}
			}
		}
		generateResultMessages(result);
		saveGeneratedReminders();
	}

	/**
	 * This method is used to generate an human readable message with all
	 * informations about the process: updated, inserted and deleted events.
	 * 
	 * @param result
	 *            The process result.
	 */
	private void generateResultMessages(DefaultAsyncTaskResult result) {
		if (countInsert == 0 && countUpdate == 0 && countDelete == 0) {
			String text = responder.getApplication().getString(
					R.string.no_changes);
			addToMessage(result, text);
		} else {
			if (countInsert > 0) {
				generateResultMessage(result, R.string.reminder_inserted,
						R.string.reminders_inserted, countInsert);
			}
			if (countUpdate > 0) {
				generateResultMessage(result, R.string.reminder_updated,
						R.string.reminders_updated, countUpdate);
			}
			if (countDelete > 0) {
				generateResultMessage(result, R.string.reminder_deleted,
						R.string.reminders_deleted, countDelete);
			}
		}
	}

	/**
	 * This method is used to format a message using applications resources
	 * strings.
	 * 
	 * @param result
	 *            The process result.
	 * @param stringIdSingular
	 *            The string ID used for singular cases.
	 * @param stringId
	 *            The string ID used for plural cases.
	 * @param count
	 *            The number used on the formated string.
	 */
	private void generateResultMessage(DefaultAsyncTaskResult result,
			int stringIdSingular, int stringId, int count) {
		String text = null;
		if (count == 1) {
			text = responder.getApplication().getString(stringIdSingular);
		} else {
			text = responder.getApplication().getString(stringId, count);
		}
		addToMessage(result, text);
	}

	/**
	 * Method used to prepare the strings for the result message.
	 * 
	 * @param result
	 *            The process result.
	 * @param text
	 *            The text added to the result message.
	 */
	private void addToMessage(DefaultAsyncTaskResult result, String text) {
		if (result.resultMessage == null) {
			result.resultMessage = "";
		} else if (result.resultMessage.length() > 0) {
			result.resultMessage += "\n";
		}
		result.resultMessage += text;
	}

	/**
	 * Save the generated reminder on application shared preferences.
	 */
	private void saveGeneratedReminders() {
		applicationPreferences.setContactEvents(generated);
	}

	/**
	 * Here are saved the contact event and reminder.
	 * 
	 * @param cr
	 *            The application ContentResolver
	 * @param contact
	 *            The contact model used to extract birthday information and
	 *            reminder.
	 * @return The result ID (OK or ERROR)
	 */
	private int generateBirthdayReminderEvent(ContentResolver cr,
			Contact contact) {
		int result = Constants.OK;
		ContactEvent contactEvent = new ContactEvent();
		contactEvent.contactId = contact.getId();
		contactEvent.eventId = contact.getEventId();
		contactEvent.reminderId = contact.getReminderId();

		CalendarUtils.SaveType saveType;

		saveType = calendarUtils.saveContactEvent(contact, contactEvent);
		if (saveType == CalendarUtils.SaveType.INSERT) {
			countInsert++;
		} else if (saveType == CalendarUtils.SaveType.UPDATE) {
			countUpdate++;
		}

		if (saveType != CalendarUtils.SaveType.NOTHING) {
			saveType = calendarUtils
					.saveEventReminder(contact, contactEvent);
		}

		if (saveType == CalendarUtils.SaveType.NOTHING) {
			result = Constants.ERROR;
		} else {
			generated.add(contactEvent);
		}
		return result;
	}
}
