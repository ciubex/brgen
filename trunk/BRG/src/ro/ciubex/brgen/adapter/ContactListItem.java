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
 * @author Claudiu
 * 
 */
public class ContactListItem {
	private ContactItemType itemType;
	private String sectionLabel = "";
	private Contact contactModel;

	public ContactListItem() {
		itemType = ContactItemType.UNUSED;
	}

	public ContactListItem(String sectionLabel) {
		this.sectionLabel = sectionLabel;
		itemType = ContactItemType.SEPARATOR;
	}

	public ContactListItem(Contact contactModel) {
		this.contactModel = contactModel;
		itemType = ContactItemType.ITEM;
		char ch = contactModel.getContactName().charAt(0);
		sectionLabel = (Character.isDigit(ch) ? "-" : ""
				+ Character.toUpperCase(ch));
	}

	public ContactItemType getItemType() {
		return itemType;
	}

	public void setItemType(ContactItemType itemType) {
		this.itemType = itemType;
	}

	public String getSectionLabel() {
		return sectionLabel;
	}

	public Contact getContactModel() {
		return contactModel;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ContactListItem [").append(sectionLabel).append(", ")
				.append(contactModel).append("]");
		return builder.toString();
	}

}
