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
package ro.ciubex.brg.model;

/**
 * This model is used to store on application preferences informations with
 * generated reminders: - contact id; - calendar event id with the reminder id.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class ContactEvent {
	public long contactId;
	public long eventId;
	public long reminderId;

	public ContactEvent() {
		contactId = -1;
		eventId = -1;
		reminderId = -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (contactId ^ (contactId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof ContactEvent))
			return false;
		ContactEvent other = (ContactEvent) obj;
		if (contactId != other.contactId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('[').append(contactId).append(':').append(eventId)
				.append(':').append(reminderId).append(']');
		return builder.toString();
	}

	public void fromString(String contactEvent) {
		String ce = contactEvent.substring(1, contactEvent.length() - 1);
		String arr[] = ce.split(":");
		if (arr.length == 3) {
			contactId = Integer.parseInt(arr[0]);
			eventId = Integer.parseInt(arr[1]);
			reminderId = Integer.parseInt(arr[2]);
		}
	}
}
