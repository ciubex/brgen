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

import java.io.IOException;
import java.io.InputStream;

import ro.ciubex.brg.model.Contact;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.BaseAdapter;

/**
 * An AsyncTask used to load contacts pictures.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class ContactImageLoaderAsyncTask extends
		AsyncTask<Contact, Long, Boolean> {

	private ContentResolver contentResolver;
	private BaseAdapter adapter;

	public ContactImageLoaderAsyncTask(Application application,
			BaseAdapter adapter) {
		this.adapter = adapter;
		contentResolver = application.getContentResolver();
	}

	/**
	 * Method invoked on the background thread.
	 */
	@Override
	protected Boolean doInBackground(Contact... contacts) {
		Boolean flag = Boolean.FALSE;
		if (contacts != null && contacts.length > 0) {
			for (Contact contact : contacts) {
				contact.setPictureLoading(true);
				if (!contact.isPictureLoaded()) {
					loadContactImage(contentResolver, contact);
				}
			}
			flag = Boolean.TRUE;
		}
		return flag;
	}

	/**
	 * Method invoked on the UI thread after the background computation
	 * finishes.
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (Boolean.TRUE.equals(result)) {
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Method used to load the bitmap picture for a contact.
	 * 
	 * @param cr
	 *            The application ContentResolver.
	 * @param contact
	 *            The contact where should be loaded the picture.
	 */
	private void loadContactImage(ContentResolver cr, Contact contact) {
		Bitmap thumbnail = null;
		InputStream input = null;
		try {
			Uri uri = ContentUris.withAppendedId(
					ContactsContract.Contacts.CONTENT_URI, contact.getId());
			input = ContactsContract.Contacts.openContactPhotoInputStream(cr,
					uri);
			if (input != null) {
				thumbnail = BitmapFactory.decodeStream(input);
				if (thumbnail != null && thumbnail.getByteCount() > 0) {
					contact.setPicture(thumbnail);
				}
			}
		} catch (Exception ex) {

		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
		contact.setPictureLoaded(true);
	}
}
