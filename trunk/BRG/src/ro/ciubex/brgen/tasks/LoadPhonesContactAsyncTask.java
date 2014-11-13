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

import ro.ciubex.brgen.R;
import ro.ciubex.brgen.model.Constants;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.util.Utilities;
import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * This async task is used to load for a contact the phone numbers.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class LoadPhonesContactAsyncTask extends
		AsyncTask<Void, Void, DefaultAsyncTaskResult> {
	private final static String TAG = LoadPhonesContactAsyncTask.class
			.getName();

	/**
	 * Responder used on loading process.
	 */
	public interface Responder {
		public Application getApplication();

		public void startLoadPhonesContact();

		public void endLoadPhonesContact(DefaultAsyncTaskResult result);
	}

	private Responder mResponder;
	private Contact mContact;
	private int mTaskId;

	public LoadPhonesContactAsyncTask(Responder responder, Contact contact,
			int taskId) {
		this.mResponder = responder;
		this.mContact = contact;
		this.mTaskId = taskId;
	}

	/**
	 * Method invoked on the UI thread before the task is executed.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mResponder.startLoadPhonesContact();
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(DefaultAsyncTaskResult result) {
		super.onPostExecute(result);
		mResponder.endLoadPhonesContact(result);
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected DefaultAsyncTaskResult doInBackground(Void... params) {
		return loadPhones();
	}

	/**
	 * Method used to load phone numbers for a contact.
	 * 
	 * @return result
	 */
	private DefaultAsyncTaskResult loadPhones() {
		DefaultAsyncTaskResult result = new DefaultAsyncTaskResult();
		result.taskId = mTaskId;
		result.object = mContact;
		Application app = mResponder.getApplication();
		Cursor cursor = null;
		List<String> list = new ArrayList<String>();
		try {
			ContentResolver cr = app.getContentResolver();
			String[] columns = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
			String where = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
					+ "=" + mContact.getId();
			String[] selectionArgs = null;
			String sortOrder = null;
			String phone;
			cursor = cr.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					columns, where, selectionArgs, sortOrder);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					phone = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					if (phone != null) {
						phone = phone.trim();
						if (phone.length() > 0) {
							list.add(phone);
						}
					}
				}
			}
			if (list.isEmpty()) {
				result.resultId = Constants.ERROR;
				result.resultMessage = app.getString(R.string.phone_list_empty);
			} else {
				result.resultId = Constants.OK;
				String[] arr = new String[list.size()];
				int i = 0;
				for (String number : list) {
					arr[i++] = number;
				}
				mContact.setPhoneNumbers(arr);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			result.resultId = Constants.ERROR;
			result.resultMessage = e.getMessage();
		} finally {
			Utilities.closeCursor(cursor);
		}
		return result;
	}
}
