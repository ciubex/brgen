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

import ro.ciubex.brg.MainApplication;
import ro.ciubex.brg.model.Contact;
import ro.ciubex.brg.model.ContactEvent;
import ro.ciubex.brg.util.BirthdayDBUtils;
import ro.ciubex.brg.util.CalendarUtils;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;

/**
 * This is an AsyncTask used to remove birthday event and reminder for a
 * contact.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class BirthdayRemoveAsyncTask extends AsyncTask<Void, Void, Boolean> {
	private MainApplication mApplication;
	private ContentResolver mContentResolver;
	private CalendarUtils mCalendarUtils;
	private Contact mContact;
	private Responder mResponder;

	public interface Responder {
		public void removeProcessResult(boolean result, Contact contact);
	}

	public BirthdayRemoveAsyncTask(MainApplication application,
			Responder responder, Contact contact) {
		this.mApplication = application;
		mContentResolver = this.mApplication.getContentResolver();
		mCalendarUtils = this.mApplication.getCalendarUtils();
		this.mResponder = responder;
		this.mContact = contact;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Boolean doInBackground(Void... nothing) {
		boolean result = removeBirthdayEvent(mContentResolver);
		if (result) {
			if (mContact.haveEvent() || mContact.haveReminder()) {
				removeEventsReminders(mContentResolver,
						mCalendarUtils.getCalendarUriBase());
				removeFromContactEvents();
			}
		}
		return Boolean.valueOf(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		mResponder.removeProcessResult(result, mContact);
	}

	/**
	 * This method is used to delete the birthday information for provided
	 * contact.
	 * 
	 * @param cr
	 *            The application ContentResolver
	 * @return True if the operation was successfully.
	 */
	private boolean removeBirthdayEvent(ContentResolver cr) {
		boolean result = false;
		long id = BirthdayDBUtils.getBirthdayEventId(cr, mContact.getId());
		if (id == Long.MIN_VALUE) {
			id = BirthdayDBUtils.getRawContactId(cr, mContact.getId());
		}
		if (id > Long.MIN_VALUE) {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			ops.add(ContentProviderOperation
					.newDelete(ContactsContract.Data.CONTENT_URI)
					.withSelection(ContactsContract.Data._ID + "=?",
							new String[] { String.valueOf(id) }).build());
			try {
				ContentProviderResult[] results = cr.applyBatch(
						ContactsContract.AUTHORITY, ops);
				if (results.length > 0) {
					mContact.setBirthday(null);
					result = true;
				}
			} catch (RemoteException e) {
			} catch (OperationApplicationException e) {
			}
		}
		return result;
	}

	/**
	 * Remove the contact calendar event and reminder.
	 * 
	 * @param cr
	 *            The application ContentResolver
	 * @param calendarUriBase
	 *            The calendar URI base path.
	 */
	private void removeEventsReminders(ContentResolver cr,
			String calendarUriBase) {
		if (calendarUriBase != null) {
			Uri uriEvents = Uri.parse(calendarUriBase + "/events");
			Uri uriReminders = Uri.parse(calendarUriBase + "/reminders");
			if (mContact.haveEvent()) {
				mCalendarUtils.deleteEntry(uriEvents, mContact.getEventId());
				mContact.setEventId(-1);
			}
			if (mContact.haveReminder()) {
				mCalendarUtils.deleteEntry(uriReminders,
						mContact.getReminderId());
				mContact.setReminderId(-1);
			}
			mContact.setChecked(false);
		}
	}

	/**
	 * This method is used to remove old generated event reminder for this
	 * contact from application shared preferences.
	 */
	private void removeFromContactEvents() {
		ContactEvent contactEvent = new ContactEvent();
		contactEvent.contactId = mContact.getId();
		mApplication.getApplicationPreferences().removeContactEvent(
				contactEvent);
	}
}