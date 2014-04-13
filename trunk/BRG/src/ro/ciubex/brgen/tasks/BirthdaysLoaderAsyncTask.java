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

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.util.Utilities;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.BaseAdapter;

/**
 * An AsyncTask used to load birthday's for all contacts.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class BirthdaysLoaderAsyncTask extends AsyncTask<Void, Long, Boolean> {
	private MainApplication application;
	private ContentResolver contentResolver;
	private BaseAdapter adapter;

	public BirthdaysLoaderAsyncTask(MainApplication application,
			BaseAdapter adapter) {
		this.application = application;
		this.adapter = adapter;
		contentResolver = application.getContentResolver();
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected Boolean doInBackground(Void... params) {
		for (Contact contact : application.getContacts()) {
			String birthday = null;
			if (!contact.isLoadedBirthday()) {
				contact.setLoadedBirthday(true);
				long contactId = contact.getId();
				birthday = getContactBirthday(contentResolver, contactId);
				if (birthday != null) {
					contact.setBirthday(Utilities.parseCalendarString(
							application.getDefaultLocale(),
							application.getDateFormat(), birthday));
					publishProgress(contactId);
				}
			}
		}
		return Boolean.TRUE;
	}

	/**
	 * This method is used to update the UI during this thread.
	 */
	@Override
	protected void onProgressUpdate(Long... values) {
		super.onProgressUpdate(values);
		adapter.notifyDataSetChanged();
	}

	/**
	 * This method is used to load for the birthday for a contact.
	 * 
	 * @param cr
	 *            The application ContentResolver
	 * @param contactId
	 *            The contact id.
	 * @return The birthday information. If the contact don't have birthday info
	 *         will be returned NULL.
	 */
	private String getContactBirthday(ContentResolver cr, long contactId) {
		String birthday = null;
		Cursor cursor = null;
		try {

			String[] projection = new String[] {
					ContactsContract.Data.CONTACT_ID,
					ContactsContract.CommonDataKinds.Event.START_DATE,
					ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Event.TYPE };

			String where = ContactsContract.Data.CONTACT_ID + "=?" + " AND "
					+ ContactsContract.Data.MIMETYPE + "=?" + " AND "
					+ ContactsContract.CommonDataKinds.Event.TYPE + "=?";

			String[] selectionArgs = new String[] {
					String.valueOf(contactId),
					ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
					String.valueOf(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) };

			String sortOrder = null;

			cursor = cr.query(ContactsContract.Data.CONTENT_URI, projection,
					where, selectionArgs, sortOrder);
			if (cursor.moveToNext()) {
				birthday = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
			}
		} catch (Exception ex) {
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return birthday;
	}

}
