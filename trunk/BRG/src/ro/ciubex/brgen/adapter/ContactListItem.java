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
package ro.ciubex.brgen.adapter;

import ro.ciubex.brgen.model.Contact;

/**
 * This is the contact list item class, used to prepare section label.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class ContactListItem {
	private String mSectionLabel = "";
	private Contact mContactModel;

	public ContactListItem(Contact contactModel) {
		this.mContactModel = contactModel;
	}

	/**
	 * Set the section label.
	 * 
	 * @param sectionLabel
	 *            the mSectionLabel to set
	 */
	public void setSectionLabel(String sectionLabel) {
		this.mSectionLabel = sectionLabel;
	}

	public String getSectionLabel() {
		return mSectionLabel;
	}

	public Contact getContactModel() {
		return mContactModel;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ContactListItem [").append(mSectionLabel).append(", ")
				.append(mContactModel).append("]");
		return builder.toString();
	}

}
