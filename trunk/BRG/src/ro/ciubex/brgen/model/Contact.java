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
package ro.ciubex.brgen.model;

import java.util.Calendar;

import android.graphics.Bitmap;

/**
 * On this model are stored information extracted for each contact from the
 * phone.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class Contact implements Comparable<Contact> {
	private long mId;
	private long mEventId;
	private long mReminderId;
	private boolean mChecked;
	private String mContactName;
	private Calendar mBirthday;
	private boolean mLoadedBirthday;
	private Bitmap mThumbnail;
	private boolean mPictureLoaded;
	private boolean mPictureLoading;
	private String[] mPhoneNumbers;

	public Contact() {
		mEventId = -1;
		mReminderId = -1;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	public long getEventId() {
		return mEventId;
	}

	public boolean haveEvent() {
		return mEventId > -1;
	}

	public void setEventId(long eventId) {
		this.mEventId = eventId;
	}

	public long getReminderId() {
		return mReminderId;
	}

	public boolean haveReminder() {
		return mReminderId > -1;
	}

	public void setReminderId(long reminderId) {
		this.mReminderId = reminderId;
	}

	public boolean isChecked() {
		return mChecked;
	}

	public void setChecked(boolean checked) {
		this.mChecked = checked;
	}

	public String getContactName() {
		return mContactName;
	}

	public void setContactName(String contactName) {
		this.mContactName = contactName;
	}

	public Calendar getBirthday() {
		return mBirthday;
	}

	public void setBirthday(Calendar birthday) {
		this.mBirthday = birthday;
	}

	public String[] getPhoneNumbers() {
		return mPhoneNumbers;
	}

	public void setPhoneNumbers(String[] phoneNumbers) {
		mPhoneNumbers = phoneNumbers;
	}

	public boolean havePhoneNumbers() {
		return mPhoneNumbers != null && mPhoneNumbers.length > 0;
	}

	@Override
	public int compareTo(Contact another) {
		int n1 = mContactName != null ? mContactName.length() : 0;
		int n2 = another.mContactName != null ? another.mContactName.length()
				: 0;
		int min = Math.min(n1, n2);
		for (int i = 0; i < min; i++) {
			char c1 = mContactName.charAt(i);
			char c2 = another.mContactName.charAt(i);
			if (c1 != c2) {
				c1 = Character.toUpperCase(c1);
				c2 = Character.toUpperCase(c2);
				if (c1 != c2) {
					c1 = Character.toLowerCase(c1);
					c2 = Character.toLowerCase(c2);
					if (c1 != c2) {
						return c1 - c2;
					}
				}
			}
		}
		return n1 - n2;
	}

	public boolean haveBirthday() {
		return mBirthday != null;
	}

	public boolean isLoadedBirthday() {
		return mLoadedBirthday;
	}

	public void setLoadedBirthday(boolean loadedBirthday) {
		this.mLoadedBirthday = loadedBirthday;
	}

	public boolean havePicture() {
		return (mThumbnail != null) && mThumbnail.getByteCount() > 0;
	}

	/**
	 * @return the thumbnail
	 */
	public Bitmap getThumbnail() {
		return mThumbnail;
	}

	/**
	 * @param thumbnail
	 *            the thumbnail to set
	 */
	public void setThumbnail(Bitmap thumbnail) {
		this.mThumbnail = thumbnail;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ContactModel [");
		builder.append(mContactName);
		builder.append(", ");
		builder.append(mId);
		builder.append(", ");
		builder.append(mBirthday);
		builder.append("]");
		return builder.toString();
	}

	public boolean isPictureLoaded() {
		return mPictureLoaded;
	}

	public void setPictureLoaded(boolean flag) {
		this.mPictureLoaded = flag;
	}

	/**
	 * @return the pictureLoading
	 */
	public boolean isPictureLoading() {
		return mPictureLoading;
	}

	/**
	 * @param pictureLoading
	 *            the pictureLoading to set
	 */
	public void setPictureLoading(boolean pictureLoading) {
		this.mPictureLoading = pictureLoading;
	}

	public void toggleChecked() {
		mChecked = !mChecked;
	}
	
	public boolean isModified() {
		if (!mChecked) {
			return haveReminder();
		}
		return !haveReminder();
	}
}
