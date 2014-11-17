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
		Calendar c1 = o1.getBirthday();
		Calendar c2 = o2.getBirthday();
		if (c1 != null && c2 != null) {
			int m1 = c1.get(Calendar.MONTH);
			int m2 = c2.get(Calendar.MONTH);
			int d1 = c1.get(Calendar.DAY_OF_MONTH);
			int d2 = c2.get(Calendar.DAY_OF_MONTH);
			if (m1 == m2) {
				if (d1 == d2) {
					return ContactsComparator.nameCompare(o1, o2);
				}
				return d1 - d2;
			} else {
				return m1 - m2;
			}
		}
		return ContactsComparator.nameCompare(o1, o2);
	}

}
