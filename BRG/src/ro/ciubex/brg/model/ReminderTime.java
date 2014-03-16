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
 * This model is used on the Application preferences to set and get reminder
 * start and end time, because the preference can not use a complex model only
 * strings, long, integer, float and boolean.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class ReminderTime {
	public int hour;
	public int minute;

	public ReminderTime() {
		this("00:00");
	}

	public ReminderTime(String time) {
		fromString(time);
	}

	public void fromString(String time) {
		String[] timeParts = time.split(":");
		if (timeParts.length == 2) {
			hour = Integer.parseInt(timeParts[0]);
			minute = Integer.parseInt(timeParts[1]);
		}
	}
}
