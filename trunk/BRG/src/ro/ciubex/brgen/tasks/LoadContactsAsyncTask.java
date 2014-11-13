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

import java.util.Collections;
import java.util.List;

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.R;
import ro.ciubex.brgen.model.Constants;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.model.ContactEvent;
import ro.ciubex.brgen.model.ContactsComparator;
import ro.ciubex.brgen.util.Utilities;
import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * This is an AsyncTask used to load all contacts from the phone.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class LoadContactsAsyncTask extends
		AsyncTask<Void, Void, DefaultAsyncTaskResult> {
	private final static String TAG = LoadContactsAsyncTask.class.getName();

	/**
	 * Responder used on loading process.
	 */
	public interface Responder {
		public Application getApplication();

		public void startLoadContacts();

		public void endLoadContacts(DefaultAsyncTaskResult result);
	}

	private List<Contact> mContacts;
	private Responder mResponder;

	public LoadContactsAsyncTask(Responder responder, List<Contact> contacts) {
		this.mResponder = responder;
		this.mContacts = contacts;
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected DefaultAsyncTaskResult doInBackground(Void... params) {
		DefaultAsyncTaskResult result = new DefaultAsyncTaskResult();
		getContacts(result);
		getContactsEvents();
		return result;
	}

	/**
	 * Method invoked on the UI thread before the task is executed.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mResponder.startLoadContacts();
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(DefaultAsyncTaskResult result) {
		super.onPostExecute(result);
		mResponder.endLoadContacts(result);
	}

	/**
	 * This method is used to load all contacts from the phone.
	 * 
	 * @param result
	 *            The process result.
	 */
	private void getContacts(DefaultAsyncTaskResult result) {
		result.resultId = Constants.OK;
		if (mContacts.size() > 0) {
			mContacts.clear();
		}
		Application app = mResponder.getApplication();
		Cursor cursor = null;
		try {
			ContentResolver cr = app.getContentResolver();

			String[] columns = new String[] { ContactsContract.Contacts._ID,
					ContactsContract.Contacts.DISPLAY_NAME,
					ContactsContract.Contacts.HAS_PHONE_NUMBER };

			String where = ContactsContract.Contacts.IN_VISIBLE_GROUP
					+ " = '1'";

			String[] selectionArgs = null;

			String sortOrder = null;

			cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, columns,
					where, selectionArgs, sortOrder);
			if (cursor != null) {
				long contactId;
				String contactName;
				while (cursor.moveToNext()) {
					contactName = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					if (contactName != null && contactName.length() > 0) {
						contactId = cursor.getLong(cursor
								.getColumnIndex(ContactsContract.Contacts._ID));

						Contact contact = new Contact();
						contact.setId(contactId);
						contact.setContactName(contactName);
						mContacts.add(contact);
					}
				}
			}
			String resultMessage = app.getString(R.string.no_contacts);
			if (mContacts.size() > 0) {
				resultMessage = app.getString(R.string.contacts_loaded,
						mContacts.size());
			} else {
				result.resultId = Constants.ERROR;
			}
			result.resultMessage = resultMessage;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			result.resultId = Constants.ERROR;
			result.resultMessage = e.getMessage();
		} finally {
			Utilities.closeCursor(cursor);
		}
		Collections.sort(mContacts, new ContactsComparator());
	}

	/**
	 * Populate the contact model with event and reminder id.
	 */
	private void getContactsEvents() {
		if (mContacts.size() > 0) {
			Application app = mResponder.getApplication();
			List<ContactEvent> generated = ((MainApplication) app)
					.getApplicationPreferences().getContactEvents();
			if (generated.size() > 0) {
				for (ContactEvent cem : generated) {
					for (Contact contact : mContacts) {
						if (contact.getId() == cem.contactId) {
							contact.setChecked(true);
							contact.setEventId(cem.eventId);
							contact.setReminderId(cem.reminderId);
							break;
						}
					}
				}
			}
		}
	}
}
