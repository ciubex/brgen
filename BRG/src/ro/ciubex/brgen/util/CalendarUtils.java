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
package ro.ciubex.brgen.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.model.ContactEvent;
import ro.ciubex.brgen.model.GoogleCalendar;
import ro.ciubex.brgen.model.ReminderTime;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * This is an utility class used to work with phone calendar.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class CalendarUtils {
	private static final String TAG = CalendarUtils.class.getName();
	private MainApplication mApplication;
	private ApplicationPreferences mApplicationPreferences;
	private ContentResolver mContentResolver;
	private List<GoogleCalendar> mCalendars;
	private boolean mCalendarSupported;
	private Uri mCalendarEvents;
	private Uri mCalendarReminders;

	public enum SaveType {
		UPDATE, INSERT, NOTHING
	};

	public CalendarUtils(MainApplication mApplication) {
		this.mApplication = mApplication;
		mApplicationPreferences = this.mApplication.getApplicationPreferences();
		initCalendars();
	}

	/**
	 * Initialize all calendars details.
	 */
	public void initCalendars() {
		mCalendars = new ArrayList<GoogleCalendar>();
		mContentResolver = mApplication.getContentResolver();
		Cursor cursor = null;
		String baseUri = "content://com.android.calendar/";
		Uri calendarUri = null;
		String calendarBaseUri = null;
		try {
			calendarUri = Uri.parse(baseUri + "calendars");
			cursor = mContentResolver.query(calendarUri, new String[] { "_id",
					"calendar_displayName" }, null, null, null);
			calendarBaseUri = baseUri;
		} catch (Exception e) {
		}
		if (cursor == null) {
			baseUri = "content://calendar/";
			try {
				calendarUri = Uri.parse(baseUri + "calendars");
				cursor = mContentResolver.query(calendarUri, new String[] {
						"_id", "displayName" }, null, null, null);
				calendarBaseUri = baseUri;
			} catch (Exception e) {
				cursor = null;
			}
		}
		if (cursor != null) {
			while (cursor.moveToNext()) {
				GoogleCalendar gc = new GoogleCalendar(cursor.getLong(0),
						cursor.getString(1));
				mCalendars.add(gc);
			}
			if (calendarBaseUri != null) {
				mCalendarEvents = Uri.parse(calendarBaseUri + "/events");
				mCalendarReminders = Uri.parse(calendarBaseUri + "/reminders");
			}
		}
		if (!mCalendars.isEmpty()) {
			mCalendarSupported = true;
		}
	}

	/**
	 * Get the list of available calendars.
	 * 
	 * @return the mCalendars
	 */
	public List<GoogleCalendar> getCalendars() {
		return mCalendars;
	}

	/**
	 * True if any calendar is available.
	 * 
	 * @return the mCalendarSupported
	 */
	public boolean isCalendarSupported() {
		return mCalendarSupported;
	}

	/**
	 * Retrieve the calendar events URI.
	 * 
	 * @return The calendar events URI.
	 */
	public Uri getCalendarEvents() {
		return mCalendarEvents;
	}

	/**
	 * Retrieve the calendar reminders URI.
	 * 
	 * @return The calendar reminders URI.
	 */
	public Uri getCalendarReminders() {
		return mCalendarReminders;
	}

	/**
	 * Method used to save a contact event to calendar data base.
	 * 
	 * @param contact
	 *            The contact model used to obtain the event.
	 * @param contactEvent
	 *            The contact event model used to store event and reminder of
	 *            the provided contact model.
	 * @return Process save type: nothing, inserted or updated.
	 */
	public SaveType saveContactEvent(Contact contact, ContactEvent contactEvent) {
		SaveType saveType = SaveType.NOTHING;
		Calendar cal = contact.getBirthday();
		ContentValues m = new ContentValues();
		m.put("calendar_id", mApplicationPreferences.getCalendarSelected());
		m.put("title", mApplicationPreferences.getReminderTitle(contact
				.getContactName()));
		m.put("description", mApplicationPreferences
				.getReminderDescription(contact.getContactName()));
		m.put("eventLocation", "home");
		m.put("eventStatus", 1);
		m.put("allDay", mApplicationPreferences.isAllDay() ? 1 : 0);
		m.put("eventTimezone", cal.getTimeZone().getID());

		ReminderTime rtm = mApplicationPreferences.getReminderStartTime();

		cal.set(Calendar.HOUR_OF_DAY, rtm.hour);
		cal.set(Calendar.MINUTE, rtm.minute);
		long startTime = cal.getTimeInMillis();
		m.put("dtstart", startTime);

		m.put("rrule", "FREQ=YEARLY");

		String duration = "P1D";
		if (!mApplicationPreferences.isAllDay()) {
			rtm = mApplicationPreferences.getReminderEndTime();
			cal.set(Calendar.HOUR_OF_DAY, rtm.hour);
			cal.set(Calendar.MINUTE, rtm.minute);
			long endTime = cal.getTimeInMillis();
			int dur = (int) (endTime - startTime) / 1000;
			duration = "P" + dur + "S";
		}
		m.put("duration", duration);
		m.put("hasAlarm", 1);

		boolean doInsert = true;
		if (contactEvent.eventId > -1) {
			try {
				Uri updateUri = ContentUris.withAppendedId(mCalendarEvents,
						contactEvent.eventId);
				int rows = mContentResolver.update(updateUri, m, null, null);
				doInsert = (rows == 0); // no row updated
				saveType = (rows > 0) ? SaveType.UPDATE : SaveType.NOTHING;
			} catch (Exception e) {
				Log.e(TAG, "Update contact event: " + e.getMessage(), e);
			}
		}
		if (doInsert) {
			try {
				Uri eventUri = mContentResolver.insert(mCalendarEvents, m);
				contactEvent.eventId = Long.parseLong(eventUri
						.getLastPathSegment());
				saveType = SaveType.INSERT;
				contact.setEventId(contactEvent.eventId);
			} catch (Exception e) {
				Log.e(TAG, "Insert contact event: " + e.getMessage(), e);
			}
		}
		return saveType;
	}

	/**
	 * Method used to remove an event.
	 * 
	 * @param eventId
	 *            The event ID to be removed.
	 */
	public void removeEvent(long eventId) {
		deleteEntry(mCalendarEvents, eventId);
	}

	/**
	 * Method used to remove a reminder.
	 * 
	 * @param reminderId
	 *            The reminder ID to be removed.
	 */
	public void removeReminder(long reminderId) {
		deleteEntry(mCalendarReminders, reminderId);
	}

	/**
	 * Method used to save a reminder for an event.
	 * 
	 * @param contact
	 *            The contact model.
	 * @param contactEvent
	 *            The contact event model.
	 * @return Process save type: nothing, inserted or updated.
	 */
	public SaveType saveEventReminder(Contact contact, ContactEvent contactEvent) {
		SaveType saveType = SaveType.NOTHING;
		ContentValues m = new ContentValues();
		m.put("event_id", contactEvent.eventId);
		m.put("method", mApplicationPreferences.getReminderType()); // Reminders.METHOD_ALERT
		m.put("minutes", mApplicationPreferences.getReminderBefore()); //

		boolean doInsert = true;
		cleanupRemindersForEvent(mContentResolver, mCalendarReminders,
				contactEvent.eventId, contactEvent.reminderId);
		if (contactEvent.reminderId > -1) {
			try {
				Uri updateUri = ContentUris.withAppendedId(mCalendarReminders,
						contactEvent.reminderId);
				int rows = mContentResolver.update(updateUri, m, null, null);
				doInsert = (rows == 0); // no row updated
				saveType = (rows > 0) ? SaveType.UPDATE : SaveType.NOTHING;
			} catch (Exception e) {
				Log.e(TAG, "Update event reminder: " + e.getMessage(), e);
			}
		}
		if (doInsert) {
			try {
				Uri eventUri = mContentResolver.insert(mCalendarReminders, m);
				contactEvent.reminderId = Long.parseLong(eventUri
						.getLastPathSegment());
				contact.setReminderId(contactEvent.reminderId);
				saveType = SaveType.INSERT;
			} catch (Exception e) {
				Log.e(TAG, "Insert event reminder: " + e.getMessage(), e);
			}
		}
		return saveType;
	}

	/**
	 * This method is used to remove all reminders for an event.
	 * 
	 * @param cr
	 *            The application ContentResolver
	 * @param uri
	 *            The reminder URI path.
	 * @param eventId
	 *            The event ID.
	 * @param reminderId
	 *            The remaining reminder ID. This reminder should not be
	 *            deleted.
	 */
	private void cleanupRemindersForEvent(ContentResolver cr, Uri uri,
			long eventId, long reminderId) {
		Cursor cursor = null;
		long[] ids = null;
		int i, len;
		try {
			cursor = cr.query(uri, new String[] { "_id" }, "event_id = "
					+ eventId, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				len = cursor.getCount();
				ids = new long[len];
				for (i = 0; i < len; i++) {
					ids[i] = cursor.getLong(0);
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Querying uri:" + uri.toString() + " for:" + eventId, e);
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		if (ids != null && ids.length > 1) {
			len = ids.length;
			for (i = 0; i < len; i++) {
				if (ids[i] != reminderId) {
					deleteEntry(uri, ids[i]);
				}
			}
		}
	}

	/**
	 * Method used to delete an entry from DB.
	 * 
	 * @param uri
	 *            URI of data base.
	 * @param entryId
	 *            Entry to be deleted.
	 */
	public void deleteEntry(Uri uri, long entryId) {
		if (entryId > -1) {
			try {
				Uri delUri = ContentUris.withAppendedId(uri, entryId);
				mContentResolver.delete(delUri, null, null);
			} catch (Exception e) {
				Log.e(TAG,
						"Removing entryId:" + entryId + " uri:"
								+ uri.toString(), e);
			}
		}
	}
}
