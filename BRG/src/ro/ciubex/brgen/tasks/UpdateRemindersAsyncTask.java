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
package ro.ciubex.brgen.tasks;

import java.util.List;

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.R;
import ro.ciubex.brgen.model.Constants;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.model.ContactEvent;
import ro.ciubex.brgen.util.ApplicationPreferences;
import ro.ciubex.brgen.util.CalendarUtils;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.widget.BaseAdapter;

/**
 * This is an asynchronous task used to update calendar events and reminders for
 * provided contacts.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class UpdateRemindersAsyncTask extends
		AsyncTask<Void, Long, DefaultAsyncTaskResult> {

	/**
	 * Responder used on generate process.
	 */
	public interface Responder {
		public void startUpdateReminders();

		public void endUpdateReminders(DefaultAsyncTaskResult result);
	}

	private Contact[] mContacts;
	private Responder mResponder;
	private MainApplication mApplication;
	private BaseAdapter mAdapter;
	private ApplicationPreferences mApplicationPreferences;
	private CalendarUtils mCalendarUtils;
	private int mCountInsert;
	private int mCountUpdate;
	private int mCountDelete;
	private List<ContactEvent> mOldContactEvents;

	public UpdateRemindersAsyncTask(Responder responder,
			MainApplication application, BaseAdapter adapter,
			Contact... contacts) {
		this.mResponder = responder;
		this.mContacts = contacts;
		mApplication = application;
		mAdapter = adapter;
		mApplicationPreferences = mApplication.getApplicationPreferences();
		mCalendarUtils = mApplication.getCalendarUtils();
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected DefaultAsyncTaskResult doInBackground(Void... params) {
		DefaultAsyncTaskResult result = new DefaultAsyncTaskResult();
		ContentResolver cr = mApplication.getContentResolver();
		result.resultId = Constants.OK;
		if (mCalendarUtils.isCalendarSupported()) {
			mOldContactEvents = mApplicationPreferences.getContactEvents();
			updateReminders(cr, result);
		} else {
			result.resultId = Constants.ERROR;
			result.resultMessage = mApplication.getString(R.string.no_calendar);
		}
		return result;
	}

	/**
	 * Method invoked on the UI thread before the task is executed.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mResponder.startUpdateReminders();
	}

	/**
	 * This method is used to update the UI during this thread.
	 */
	@Override
	protected void onProgressUpdate(Long... values) {
		super.onProgressUpdate(values);
		if (values[0] > 0L) {
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(DefaultAsyncTaskResult result) {
		super.onPostExecute(result);
		mResponder.endUpdateReminders(result);
	}

	/**
	 * Main method used to generate reminders for all checked contacts.
	 * 
	 * @param cr
	 *            The application ContentResolver
	 * @param result
	 *            The process result.
	 */
	private void updateReminders(ContentResolver cr,
			DefaultAsyncTaskResult result) {
		mCountUpdate = 0;
		mCountInsert = 0;
		mCountDelete = 0;
		for (Contact contact : mContacts) {
			if (contact.isModified()) {
				if (contact.isChecked() && contact.haveBirthday()) {
					generateBirthdayReminderEvent(cr, contact);
				} else {
					if (contact.haveEvent()) {
						mCalendarUtils.removeEvent(contact.getEventId());
						contact.setEventId(-1);
					}
					if (contact.haveReminder()) {
						mCountDelete++;
						mCalendarUtils.removeReminder(contact.getReminderId());
						contact.setReminderId(-1);
					}
					removeContactEvent(getContactEvent(contact));
					contact.setChecked(false);
					publishProgress(contact.getId());
				}
			}
		}
		generateResultMessages(result);
	}

	/**
	 * Remove the contact event from the generated list.
	 * 
	 * @param contactEvent
	 *            The contact event to be removed.
	 */
	private void removeContactEvent(ContactEvent contactEvent) {
		int index = mOldContactEvents.indexOf(contactEvent);
		if (index > -1) {
			mOldContactEvents.remove(index);
		}
	}

	/**
	 * Update the contact event from the generated list.
	 * 
	 * @param contactEvent
	 *            The contact event to be updated.
	 */
	private void updateContactEventList(ContactEvent contactEvent) {
		int index = mOldContactEvents.indexOf(contactEvent);
		if (index > -1) {
			mOldContactEvents.set(index, contactEvent);
		} else {
			mOldContactEvents.add(contactEvent);
		}
	}

	/**
	 * This method is used to generate an human readable message with all
	 * informations about the process: updated, inserted and deleted events.
	 * 
	 * @param result
	 *            The process result.
	 */
	private void generateResultMessages(DefaultAsyncTaskResult result) {
		if (mCountInsert == 0 && mCountUpdate == 0 && mCountDelete == 0) {
			String text = mApplication.getString(R.string.no_changes);
			addToMessage(result, text);
		} else {
			if (mCountInsert > 0) {
				generateResultMessage(result, R.string.reminder_inserted,
						R.string.reminders_inserted, mCountInsert);
			}
			if (mCountUpdate > 0) {
				generateResultMessage(result, R.string.reminder_updated,
						R.string.reminders_updated, mCountUpdate);
			}
			if (mCountDelete > 0) {
				generateResultMessage(result, R.string.reminder_deleted,
						R.string.reminders_deleted, mCountDelete);
			}
			mApplicationPreferences.replaceContactEvents(mOldContactEvents);
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
			text = mApplication.getString(stringIdSingular);
		} else {
			text = mApplication.getString(stringId, count);
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
		boolean cntInsert = false, cntUpdate = false;
		ContactEvent contactEvent = getContactEvent(contact);
		CalendarUtils.SaveType saveType;

		saveType = mCalendarUtils.saveContactEvent(contact, contactEvent);
		if (saveType == CalendarUtils.SaveType.INSERT) {
			cntInsert = true;
		} else if (saveType == CalendarUtils.SaveType.UPDATE) {
			cntUpdate = true;
		}

		if (saveType != CalendarUtils.SaveType.NOTHING) {
			saveType = mCalendarUtils.saveEventReminder(contact, contactEvent);
		}

		if (saveType == CalendarUtils.SaveType.NOTHING) {
			result = Constants.ERROR;
			if (cntInsert) {
				mCalendarUtils.removeEvent(contactEvent.eventId);
				contactEvent.eventId = -1;
			}
		} else {
			if (cntInsert || cntUpdate) {
				updateContactEventList(contactEvent);
				mCountInsert += cntInsert ? 1 : 0;
				mCountUpdate += cntUpdate ? 1 : 0;
				publishProgress(contact.getId());
			}
		}
		return result;
	}

	/**
	 * Obtain a contact event based on the contact object.
	 * 
	 * @param contact
	 *            Contact object.
	 * @return Contact event object.
	 */
	private ContactEvent getContactEvent(Contact contact) {
		ContactEvent contactEvent = new ContactEvent();
		contactEvent.contactId = contact.getId();
		contactEvent.eventId = contact.getEventId();
		contactEvent.reminderId = contact.getReminderId();
		return contactEvent;
	}
}
