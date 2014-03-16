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
package ro.ciubex.brg.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * This is a birthday utilities class, used to obtain birthday ID and the
 * contact raw ID for a provided contact ID.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class BirthdayDBUtils {
	private static Logger logger = Logger.getLogger(BirthdayDBUtils.class
			.getName());

	/**
	 * Used to obtain the birthday event ID for a provided contact ID.
	 * 
	 * @param cr
	 *            The application ContentResolver
	 * @param contactId
	 *            Provided contact ID
	 * @return The birthday event ID
	 */
	public static long getBirthdayEventId(ContentResolver cr, long contactId) {
		long result = Long.MIN_VALUE;
		Cursor cursor = null;
		try {
			String[] projection = new String[] {
					ContactsContract.CommonDataKinds.Event._ID,
					ContactsContract.CommonDataKinds.Event.RAW_CONTACT_ID };
			String selection = ContactsContract.CommonDataKinds.Event.CONTACT_ID
					+ "=? AND "
					+ ContactsContract.CommonDataKinds.Event.MIMETYPE
					+ "=? AND "
					+ ContactsContract.CommonDataKinds.Event.TYPE
					+ "=?";
			String[] selectionArgs = new String[] {
					String.valueOf(contactId),
					String.valueOf(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE),
					String.valueOf(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY) };

			cursor = cr.query(ContactsContract.Data.CONTENT_URI, projection,
					selection, selectionArgs, null);

			if (cursor.moveToFirst()) {
				int index = cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Event._ID);
				result = cursor.getLong(index);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "getBirthdayEventId(" + contactId + ")", e);
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * Used to obtain the contact RAW ID for a provided contact ID.
	 * 
	 * @param cr
	 *            The application ContentResolver
	 * @param contactId
	 *            Provided contact ID
	 * @return The contact raw ID
	 */
	public static long getRawContactId(ContentResolver cr, long contactId) {
		long result = Long.MIN_VALUE;
		Cursor cursor = null;
		try {
			String[] projection = new String[] {
					ContactsContract.CommonDataKinds.Event._ID,
					ContactsContract.CommonDataKinds.Event.RAW_CONTACT_ID };
			String selection = ContactsContract.CommonDataKinds.Event.CONTACT_ID
					+ "=?";
			String[] selectionArgs = new String[] { String.valueOf(contactId) };

			cursor = cr.query(ContactsContract.Data.CONTENT_URI, projection,
					selection, selectionArgs, null);

			if (cursor.moveToFirst()) {
				int index = cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Event.RAW_CONTACT_ID);
				result = cursor.getLong(index);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "getRawContactId(" + contactId + ")", e);
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return result;
	}
}