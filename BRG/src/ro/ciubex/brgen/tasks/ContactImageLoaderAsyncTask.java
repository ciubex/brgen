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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.model.Contact;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.BaseAdapter;

/**
 * An AsyncTask used to load contacts pictures.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class ContactImageLoaderAsyncTask extends AsyncTask<Void, Long, Boolean> {
	private static final String TAG = ContactImageLoaderAsyncTask.class
			.getName();
	private MainApplication mApplication;
	private ContentResolver mContentResolver;
	private BaseAdapter mAdapter;

	public ContactImageLoaderAsyncTask(MainApplication application,
			BaseAdapter adapter) {
		mApplication = application;
		mAdapter = adapter;
		mContentResolver = application.getContentResolver();
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected Boolean doInBackground(Void... params) {
		loadContactsImages();
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
	 * Method used to load images for all contacts.
	 * 
	 */
	private void loadContactsImages() {
		List<Contact> contacts = mApplication.getContacts();
		for (Contact contact : contacts) {
			if (!contact.havePicture()) {
				prepareContactPhoto(contact);
			}
		}
	}

	/**
	 * Prepare contact photo bitmap.
	 * 
	 * @param contact
	 *            The contact object.
	 */
	private void prepareContactPhoto(Contact contact) {
		Bitmap thumbnail = null;
		InputStream input = null;
		try {
			input = ContactsContract.Contacts.openContactPhotoInputStream(
					mContentResolver, ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI,
							contact.getId()));
			if (input != null) {
				thumbnail = BitmapFactory.decodeStream(input, null, null);
				if (thumbnail != null && thumbnail.getByteCount() > 0) {
					contact.setThumbnail(thumbnail);
					contact.setPictureLoaded(true);
					publishProgress(contact.getId());
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
