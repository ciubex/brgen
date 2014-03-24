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
	private long id;
	private long eventId;
	private long reminderId;
	private boolean checked;
	private String contactName;
	private Calendar birthday;
	private boolean loadingBirthday;
	private Bitmap picture;
	private boolean pictureLoaded;
	private boolean pictureLoading;

	public Contact() {
		eventId = -1;
		reminderId = -1;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getEventId() {
		return eventId;
	}

	public boolean haveEvent() {
		return eventId > -1;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public long getReminderId() {
		return reminderId;
	}

	public boolean haveReminder() {
		return reminderId > -1;
	}

	public void setReminderId(long reminderId) {
		this.reminderId = reminderId;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public Calendar getBirthday() {
		return birthday;
	}

	public void setBirthday(Calendar birthday) {
		this.birthday = birthday;
	}

	@Override
	public int compareTo(Contact another) {
		int n1 = contactName != null ? contactName.length() : 0;
		int n2 = another.contactName != null ? another.contactName.length() : 0;
		int min = Math.min(n1, n2);
		for (int i = 0; i < min; i++) {
			char c1 = contactName.charAt(i);
			char c2 = another.contactName.charAt(i);
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
		return birthday != null;
	}

	public boolean isLoadingBirthday() {
		return loadingBirthday;
	}

	public void setLoadingBirthday(boolean loadingBirthday) {
		this.loadingBirthday = loadingBirthday;
	}

	/**
	 * @return the picture
	 */
	public Bitmap getPicture() {
		return picture;
	}

	/**
	 * @param picture
	 *            the picture to set
	 */
	public void setPicture(Bitmap picture) {
		this.picture = picture;
	}

	public boolean havePicture() {
		return (picture != null) ? (picture.getWidth() > 0 && picture
				.getHeight() > 0) : false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ContactModel [");
		builder.append(contactName);
		builder.append(", ");
		builder.append(id);
		builder.append(", ");
		builder.append(birthday);
		builder.append("]");
		return builder.toString();
	}

	public boolean isPictureLoaded() {
		return pictureLoaded;
	}

	public void setPictureLoaded(boolean flag) {
		this.pictureLoaded = flag;
	}

	/**
	 * @return the pictureLoading
	 */
	public boolean isPictureLoading() {
		return pictureLoading;
	}

	/**
	 * @param pictureLoading
	 *            the pictureLoading to set
	 */
	public void setPictureLoading(boolean pictureLoading) {
		this.pictureLoading = pictureLoading;
	}
}
