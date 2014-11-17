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

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.model.BirthdayComparator;
import ro.ciubex.brgen.model.Contact;
import android.os.AsyncTask;

/**
 * This task is used to sort the birthdays list.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class BirthdaySortAsyncTask extends AsyncTask<Void, Void, Boolean> {
	private MainApplication mApplication;
	private SortListener mListener;

	public static interface SortListener {
		public void sortStarted();

		public void sortFinished();
	}

	public BirthdaySortAsyncTask(MainApplication application,
			SortListener listener) {
		mApplication = application;
		mListener = listener;
	}

	/**
	 * Method invoked on the UI thread before the task is executed.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mListener != null) {
			mListener.sortStarted();
		}
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected Boolean doInBackground(Void... params) {
		sortBirthdays();
		return Boolean.TRUE;
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (mListener != null) {
			mListener.sortFinished();
		}
	}

	/**
	 * Method used to sort the birthdays list.
	 */
	private void sortBirthdays() {
		BirthdayComparator birthdayComparator = new BirthdayComparator();
		Calendar now = Calendar.getInstance(mApplication.getDefaultLocale());
		List<Contact> contacts = mApplication.getContacts();
		List<Contact> birthdays = mApplication.getBirthdays();

		if (!birthdays.isEmpty()) {
			birthdays.clear();
		}
		if (!contacts.isEmpty()) {
			for (Contact contact : contacts) {
				if (contact.haveBirthday()) {
					birthdays.add(contact);
				}
			}
			// lets sort birthdays
			if (!birthdays.isEmpty()) {
				int pivot = 0;
				int idx;
				int m1 = now.get(Calendar.MONTH);
				int m2 = 0;
				int d1 = now.get(Calendar.DAY_OF_MONTH);
				int d2;
				Calendar c;
				Collections.sort(birthdays, birthdayComparator);
				for (Contact contact : birthdays) {
					c = contact.getBirthday();
					m2 = c.get(Calendar.MONTH);
					if (m1 == m2) {
						d2 = c.get(Calendar.DAY_OF_MONTH);
						if (d2 >= d1) {
							break;
						}
					}
					pivot++;
				}
				if (pivot > 0) {
					idx = 0;
					Contact contact;
					while (idx < pivot) {
						contact = birthdays.remove(0);
						birthdays.add(contact);
						idx++;
					}
				}
			}
		}
	}

}
