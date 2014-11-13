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
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.model.ContactEvent;
import ro.ciubex.brgen.util.BirthdayDBUtils;
import ro.ciubex.brgen.util.CalendarUtils;
import ro.ciubex.brgen.util.CalendarUtils.SaveType;
import ro.ciubex.brgen.util.Utilities;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * An AsyncTask used to save birthday informations for a contact.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class BirthdaySaveAsyncTask extends AsyncTask<Void, Void, Boolean> {
	private final static String TAG = BirthdaySaveAsyncTask.class.getName();
	private MainApplication mApplication;
	private CalendarUtils mCalendarUtils;
	private Contact mContact;
	private Responder mResponder;

	/**
	 * Responder used on save process.
	 */
	public interface Responder {
		public void saveProcessResult(boolean result);
	}

	public BirthdaySaveAsyncTask(MainApplication application,
			Responder responder, Contact contact) {
		this.mApplication = application;
		mCalendarUtils = this.mApplication.getCalendarUtils();
		this.mResponder = responder;
		this.mContact = contact;
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected Boolean doInBackground(Void... params) {
		boolean result = doSaveBirthday(mApplication.getContentResolver());
		return Boolean.valueOf(result);
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		mResponder.saveProcessResult(result);
	}

	/**
	 * This is main saving method: save / update the birthday and save / update
	 * calendar and reminder event.
	 * 
	 * @return True if the operation was successfully.
	 */
	private boolean doSaveBirthday(ContentResolver cr) {
		boolean saved = false;
		if (mContact.haveBirthday()) {
			String date = Utilities.getFormattedCalendar(
					mApplication.getDefaultLocale(),
					mApplication.getDateFormat(), mContact.getBirthday());
			long id = BirthdayDBUtils.getBirthdayEventId(cr, mContact.getId());
			if (id > Long.MIN_VALUE) {
				saved = doUpdate(cr, id, date);
			} else {
				id = BirthdayDBUtils.getRawContactId(cr, mContact.getId());
				if (id > Long.MIN_VALUE) {
					saved = doInsert(cr, id, date);
				}
			}
			if (saved) {
				if (mContact.haveEvent() || mContact.haveReminder()) {
					updateContactEventAndReminder(cr);
				}
			}
		}
		return saved;
	}

	/**
	 * Update birthday method
	 * 
	 * @param cr
	 *            The application ContentResolver
	 * @param eventId
	 *            The birthday event id
	 * @return True if the operation was successfully.
	 */
	private boolean doUpdate(ContentResolver cr, long eventId, String date) {
		boolean result = false;
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation
				.newUpdate(ContactsContract.Data.CONTENT_URI)
				.withSelection(ContactsContract.Data._ID + " = ?",
						new String[] { String.valueOf(eventId) })
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Event.START_DATE,
						date)
				.withValue(ContactsContract.CommonDataKinds.Event.TYPE,
						ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
				.build());
		try {
			ContentProviderResult[] results = cr.applyBatch(
					ContactsContract.AUTHORITY, ops);
			if (results.length > 0) {
				result = true;
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (OperationApplicationException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Insert birthday method
	 * 
	 * @param cr
	 *            The application ContentResolver
	 * @param rawContactId
	 *            The contact id
	 * @return True if the operation was successfully.
	 */
	private boolean doInsert(ContentResolver cr, long rawContactId, String date) {
		boolean result = false;
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Event.START_DATE,
						date)
				.withValue(ContactsContract.CommonDataKinds.Event.TYPE,
						ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
				.build());
		try {
			ContentProviderResult[] results = cr.applyBatch(
					ContactsContract.AUTHORITY, ops);
			if (results.length > 0) {
				result = true;
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (OperationApplicationException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

	/**
	 * This method is used to update generated calendar event and reminder.
	 * 
	 * @param cr
	 *            The application ContentResolver
	 */
	private void updateContactEventAndReminder(ContentResolver cr) {
		ContactEvent contactEvent = new ContactEvent();
		contactEvent.contactId = mContact.getId();
		contactEvent.eventId = mContact.getEventId();
		contactEvent.reminderId = mContact.getReminderId();

		SaveType saveType = SaveType.NOTHING;
		if (mContact.haveEvent()) {
			saveType = mCalendarUtils.saveContactEvent(mContact, contactEvent);
		}
		if (mContact.haveReminder() && saveType != SaveType.NOTHING) {
			saveType = mCalendarUtils.saveEventReminder(mContact, contactEvent);
		}
		if (saveType != SaveType.NOTHING) {
			updateContactEvent(contactEvent);
		}
	}

	/**
	 * Update a contact event to the list of events.
	 * 
	 * @param model
	 *            Contact model to be updated.
	 */
	private void updateContactEvent(ContactEvent model) {
		List<ContactEvent> list = mApplication.getApplicationPreferences()
				.getContactEvents();
		List<ContactEvent> newList = new ArrayList<ContactEvent>(list);
		int index = newList.indexOf(model);
		if (index > -1) {
			newList.set(index, model);
		} else {
			newList.add(model);
		}
		mApplication.getApplicationPreferences().replaceContactEvents(newList);
	}
}
