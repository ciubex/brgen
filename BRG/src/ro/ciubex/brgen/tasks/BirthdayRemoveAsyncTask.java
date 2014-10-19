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

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.model.ContactEvent;
import ro.ciubex.brgen.util.BirthdayDBUtils;
import ro.ciubex.brgen.util.CalendarUtils;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
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
				removeEventsReminders();
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
	 */
	private void removeEventsReminders() {
		if (mContact.haveEvent()) {
			mCalendarUtils.removeEvent(mContact.getEventId());
			mContact.setEventId(-1);
			mContact.setChecked(false);
		}
		if (mContact.haveReminder()) {
			mCalendarUtils.removeReminder(mContact.getReminderId());
			mContact.setReminderId(-1);
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
