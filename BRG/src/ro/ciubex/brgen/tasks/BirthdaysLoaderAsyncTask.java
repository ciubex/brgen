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
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.util.Utilities;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.BaseAdapter;

/**
 * An AsyncTask used to load birthday's for all contacts.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class BirthdaysLoaderAsyncTask extends AsyncTask<Void, Long, Boolean> {
	private static final String TAG = BirthdaysLoaderAsyncTask.class.getName();
	private MainApplication mApplication;
	private ContentResolver mContentResolver;
	private BaseAdapter mAdapter;

	public BirthdaysLoaderAsyncTask(MainApplication application,
			BaseAdapter adapter) {
		this.mApplication = application;
		this.mAdapter = adapter;
		mContentResolver = application.getContentResolver();
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected Boolean doInBackground(Void... params) {
		getContactsBirthdays();
		return Boolean.TRUE;
	}

	/**
	 * This method is used to update the UI during this thread.
	 */
	@Override
	protected void onProgressUpdate(Long... values) {
		super.onProgressUpdate(values);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Method invoked on the UI thread before the task is executed.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mApplication.setBirthdaysLoaded(false);
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		mApplication.setBirthdaysLoaded(true);
	}

	/**
	 * This method is used to load the birthdays for all contacts.
	 */
	private void getContactsBirthdays() {
		Cursor cursor = null;
		try {

			String[] projection = new String[] {
					ContactsContract.Data.CONTACT_ID,
					ContactsContract.CommonDataKinds.Event.START_DATE,
					ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Event.TYPE };

			String where = ContactsContract.Data.MIMETYPE + "=?" + " AND "
					+ ContactsContract.CommonDataKinds.Event.TYPE + "=?";

			String[] selectionArgs = new String[] {
					ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
					String.valueOf(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) };

			String sortOrder = null;

			cursor = mContentResolver.query(ContactsContract.Data.CONTENT_URI,
					projection, where, selectionArgs, sortOrder);
			if (cursor != null) {
				prepareContactsBirtdays(cursor);
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage(), ex);
		} finally {
			Utilities.closeCursor(cursor);
		}
	}

	/**
	 * Prepare each contact with the birthday info.
	 * 
	 * @param cursor
	 *            Cursor with the DB connection.
	 */
	private void prepareContactsBirtdays(Cursor cursor) {
		Long contactId = null;
		String birthday = null;
		List<Contact> contacts = mApplication.getContacts();
		List<Contact> birthdays = mApplication.getBirthdays();
		while (cursor.moveToNext()) {
			contactId = cursor.getLong(cursor
					.getColumnIndex(ContactsContract.Data.CONTACT_ID));
			birthday = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));

			for (Contact contact : contacts) {
				if (contactId == contact.getId()) {
					if (birthday != null && birthday.trim().length() > 0) {
						contact.setBirthday(Utilities.parseCalendarString(
								mApplication.getDefaultLocale(),
								mApplication.getDateFormat(), birthday));
						birthdays.add(contact);
					}
					contact.setLoadedBirthday(true);
					publishProgress(contactId);
					break;
				}
			}
		}
		// lets change the loading flag for all contacts
		for (Contact contact : contacts) {
			if (!contact.isLoadedBirthday()) {
				contact.setLoadedBirthday(true);
				publishProgress(contactId);
			}
		}
	}
}
