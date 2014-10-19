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

import java.util.Comparator;

/**
 * This comparator is used to sort the contacts on the list by birthday.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class BirthdayComparator implements Comparator<Contact> {

	@Override
	public int compare(Contact o1, Contact o2) {
		if (o1.getBirthday() == null || o2.getBirthday() == null) {
			return ContactsComparator.nameCompare(o1, o2);
		}
		return 0;
	}

}
