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

import java.util.ArrayList;
import java.util.List;

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.R;
import ro.ciubex.brgen.model.Constants;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.model.ContactEvent;
import ro.ciubex.brgen.util.ApplicationPreferences;
import ro.ciubex.brgen.util.CalendarUtils;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class SyncRemindersAsyncTask extends
		AsyncTask<Void, Long, DefaultAsyncTaskResult> {
	private static final String TAG = SyncRemindersAsyncTask.class.getName();
	private MainApplication mApplication;
	private Responder mListener;
	private BaseAdapter mAdapter;
	private List<ContactEvent> mGenerated;

	/**
	 * Responder used on sync process.
	 */
	public interface Responder {
		public void startSyncReminders();

		public void endSyncReminders(DefaultAsyncTaskResult result);
	}

	public SyncRemindersAsyncTask(Responder listener,
			MainApplication application, BaseAdapter adapter) {
		mListener = listener;
		mApplication = application;
		mAdapter = adapter;
	}

	/**
	 * Method invoked on the UI thread before the task is executed.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mListener.startSyncReminders();
	}

	/**
	 * This method is used to update the UI during this thread.
	 */
	@Override
	protected void onProgressUpdate(Long... values) {
		super.onProgressUpdate(values);
		if (values[0] == 1L) {
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
		mListener.endSyncReminders(result);
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected DefaultAsyncTaskResult doInBackground(Void... params) {
		DefaultAsyncTaskResult result = new DefaultAsyncTaskResult();
		result.resultId = Constants.OK;
		mGenerated = new ArrayList<ContactEvent>();
		if (mApplication.getCalendarUtils().isCalendarSupported()) {
			if (mApplication.getApplicationPreferences().haveCalendarSelected()) {
				syncReminders(result);
			} else {
				result.resultId = Constants.ERROR;
				result.resultMessage = mApplication
						.getString(R.string.select_a_calendar);
			}
		} else {
			result.resultId = Constants.ERROR;
			result.resultMessage = mApplication
					.getString(R.string.no_calendar_access);
		}
		return result;
	}

	/**
	 * Synchronize local contacts with stored reminders.
	 * 
	 * @param result
	 *            The process result.
	 */
	private void syncReminders(DefaultAsyncTaskResult result) {
		CalendarUtils calendarUtils = mApplication.getCalendarUtils();
		if (syncContactEvents(calendarUtils, result)) {
			syncContactReminders(calendarUtils, result);
		}
		if (mGenerated.isEmpty()) {
			result.resultMessage = mApplication.getString(R.string.sync_no);
		} else {
			mApplication.getApplicationPreferences().setContactEvents(
					mGenerated);
			if (mGenerated.size() == 1) {
				result.resultMessage = mApplication
						.getString(R.string.sync_one);
			} else {
				result.resultMessage = mApplication.getString(
						R.string.sync_more, mGenerated.size());
			}
		}
	}

	/**
	 * Synchronize the contacts with stored events.
	 * 
	 * @param calendarUtils
	 *            The calendar utilities.
	 * @param result
	 *            The process result.
	 * @return True if the process finished successfully.
	 */
	private boolean syncContactEvents(CalendarUtils calendarUtils,
			DefaultAsyncTaskResult result) {
		boolean bool = true;
		Cursor cursor = null;
		String[] columns = new String[] { "calendar_id", "_id", "title",
				"rrule", "hasAlarm" };
		ApplicationPreferences preferences = mApplication
				.getApplicationPreferences();
		String selection = "calendar_id = " + preferences.getCalendarSelected();
		try {
			cursor = mApplication.getContentResolver().query(
					calendarUtils.getCalendarEvents(), columns, selection,
					null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					processCalendarEventsCursor(cursor, preferences);
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "syncContactEvents Exception: " + ex.getMessage(), ex);
			bool = false;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return bool;
	}

	/**
	 * Process a calendar event cursor.
	 * 
	 * @param cursor
	 *            The event cursor.
	 * @param preferences
	 *            Application preferences.
	 */
	private void processCalendarEventsCursor(Cursor cursor,
			ApplicationPreferences preferences) {
		long eventId;
		String title, rrule, temp;
		int hasAlarm;
		title = cursor.getString(cursor.getColumnIndex("title"));
		hasAlarm = cursor.getInt(cursor.getColumnIndex("hasAlarm"));
		rrule = cursor.getString(cursor.getColumnIndex("rrule"));
		if (hasAlarm == 1 && rrule != null && rrule.contains("FREQ=YEARLY")) {
			for (Contact contact : mApplication.getContacts()) {
				temp = preferences.getReminderTitle(contact.getContactName());
				if (temp.equals(title)) {
					eventId = cursor.getLong(cursor.getColumnIndex("_id"));
					contact.setEventId(eventId);
					break;
				}
			}
		}
	}

	/**
	 * Synchronize the contacts with stored reminders.
	 * 
	 * @param calendarUtils
	 *            The calendar utilities.
	 * @param result
	 *            The process result.
	 * @return True if the process finished successfully.
	 */
	private boolean syncContactReminders(CalendarUtils calendarUtils,
			DefaultAsyncTaskResult result) {
		boolean bool = true;
		Cursor cursor = null;
		String[] columns = new String[] { "_id", "event_id", };
		try {
			cursor = mApplication.getContentResolver().query(
					calendarUtils.getCalendarReminders(), columns, null, null,
					null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					processCalendarRemindersCursor(cursor);
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "syncContactReminders Exception: " + ex.getMessage(), ex);
			bool = false;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return bool;
	}

	/**
	 * Process a calendar reminder event cursor.
	 * 
	 * @param cursor
	 *            The reminder event cursor.
	 */
	private void processCalendarRemindersCursor(Cursor cursor) {
		long reminderId = cursor.getLong(cursor.getColumnIndex("_id"));
		long eventId = cursor.getLong(cursor.getColumnIndex("event_id"));
		for (Contact contact : mApplication.getContacts()) {
			if (contact.getEventId() == eventId) {
				contact.setReminderId(reminderId);
				contact.setChecked(true);

				ContactEvent contactEvent = new ContactEvent();
				contactEvent.contactId = contact.getId();
				contactEvent.eventId = contact.getEventId();
				contactEvent.reminderId = contact.getReminderId();
				mGenerated.add(contactEvent);
				publishProgress(1L);
			}
		}
	}

}
