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
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ciubex.brg.MainApplication;
import ro.ciubex.brg.model.Contact;
import ro.ciubex.brg.model.ContactEvent;
import ro.ciubex.brg.model.GoogleCalendar;
import ro.ciubex.brg.model.ReminderTime;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * This is an utility class used to work with phone calendar.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class CalendarUtils {
	private static Logger logger = Logger.getLogger(CalendarUtils.class
			.getName());
	private MainApplication mApplication;
	private ApplicationPreferences applicationPreferences;
	private ContentResolver contentResolver;
	private List<GoogleCalendar> mCalendars;
	private boolean mCalendarSupported;
	private String mCalendarUriBase;

	public enum SaveType {
		UPDATE, INSERT, NOTHING
	};

	public CalendarUtils(MainApplication mApplication) {
		this.mApplication = mApplication;
		applicationPreferences = this.mApplication.getApplicationPreferences();
		initCalendars();
	}

	/**
	 * Initialize all calendars details.
	 */
	public void initCalendars() {
		mCalendars = new ArrayList<GoogleCalendar>();
		contentResolver = mApplication.getContentResolver();
		Cursor cursor = null;
		String baseUri = "content://com.android.calendar/";
		Uri calendarUri = null;
		try {
			calendarUri = Uri.parse(baseUri + "calendars");
			cursor = contentResolver.query(calendarUri, new String[] { "_id",
					"calendar_displayName" }, null, null, null);
			mCalendarUriBase = baseUri;
		} catch (Exception e) {
		}
		if (cursor == null) {
			baseUri = "content://calendar/";
			try {
				calendarUri = Uri.parse(baseUri + "calendars");
				cursor = contentResolver.query(calendarUri, new String[] {
						"_id", "displayName" }, null, null, null);
				mCalendarUriBase = baseUri;
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
	 * Obtain the calendars URI base.
	 * 
	 * @return the mCalendarUriBase
	 */
	public String getCalendarUriBase() {
		return mCalendarUriBase;
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
		m.put("calendar_id", applicationPreferences.getCalendarSelected());
		m.put("title", applicationPreferences.getReminderTitle(contact
				.getContactName()));
		m.put("description", applicationPreferences
				.getReminderDescription(contact.getContactName()));
		m.put("eventLocation", "home");
		m.put("eventStatus", 1);
		m.put("allDay", applicationPreferences.isAllDay() ? 1 : 0);
		m.put("eventTimezone", cal.getTimeZone().getID());

		ReminderTime rtm = applicationPreferences.getReminderStartTime();

		cal.set(Calendar.HOUR_OF_DAY, rtm.hour);
		cal.set(Calendar.MINUTE, rtm.minute);
		long startTime = cal.getTimeInMillis();
		m.put("dtstart", startTime);

		m.put("rrule", "FREQ=YEARLY");

		String duration = "P1D";
		if (!applicationPreferences.isAllDay()) {
			rtm = applicationPreferences.getReminderEndTime();
			cal.set(Calendar.HOUR_OF_DAY, rtm.hour);
			cal.set(Calendar.MINUTE, rtm.minute);
			long endTime = cal.getTimeInMillis();
			int dur = (int) (endTime - startTime) / 1000;
			duration = "P" + dur + "S";
		}
		m.put("duration", duration);
		m.put("hasAlarm", 1);

		boolean doInsert = true;
		Uri uri = Uri.parse(mCalendarUriBase + "/events");
		if (contactEvent.eventId > -1) {
			Uri updateUri = ContentUris.withAppendedId(uri,
					contactEvent.eventId);
			int rows = contentResolver.update(updateUri, m, null, null);
			doInsert = (rows == 0); // no row updated
			saveType = SaveType.UPDATE;
		}
		if (doInsert) {
			Uri eventUri = contentResolver.insert(uri, m);
			contactEvent.eventId = Long
					.parseLong(eventUri.getLastPathSegment());
			saveType = SaveType.INSERT;
			contact.setEventId(contactEvent.eventId);
		}
		return saveType;
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
		m.put("method", applicationPreferences.getReminderType()); // Reminders.METHOD_ALERT
		m.put("minutes", applicationPreferences.getReminderBefore()); //

		boolean doInsert = true;
		Uri uri = Uri.parse(mCalendarUriBase + "/reminders");
		cleanupRemindersForEvent(contentResolver, uri, contactEvent.eventId,
				contactEvent.reminderId);
		if (contactEvent.reminderId > -1) {
			Uri updateUri = ContentUris.withAppendedId(uri,
					contactEvent.reminderId);
			int rows = contentResolver.update(updateUri, m, null, null);
			doInsert = (rows == 0); // no row updated
			saveType = SaveType.UPDATE;
		}
		if (doInsert) {
			Uri eventUri = contentResolver.insert(uri, m);
			contactEvent.reminderId = Long.parseLong(eventUri
					.getLastPathSegment());
			contact.setReminderId(contactEvent.reminderId);
			saveType = SaveType.INSERT;
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
			logger.log(Level.SEVERE, "Querying uri:" + uri.toString() + " for:"
					+ eventId, e);
			e.printStackTrace();
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
				contentResolver.delete(delUri, null, null);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Removing entryId:" + entryId
						+ " uri:" + uri.toString(), e);
			}
		}
	}
}
