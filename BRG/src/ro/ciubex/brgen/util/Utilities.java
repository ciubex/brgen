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
package ro.ciubex.brgen.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.database.Cursor;
import android.util.Log;

/**
 * This contain commons utilities methods.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class Utilities {
	private final static String TAG = Utilities.class.getName();

	/**
	 * This method fill format a string by adding a zero on the from if the
	 * number is less than ten.
	 * 
	 * @param c
	 *            The number to be formated.
	 * @return The formated number.
	 */
	public static String numberPadding(int c) {
		if (c > 9)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	/**
	 * Parse a string to an integer. If the string could not be formated zero
	 * will be returned.
	 * 
	 * @param value
	 *            The string value to be parsed.
	 * @return Parsed integer.
	 */
	public static int parseInt(String value) {
		return parseInt(value, 0);
	}

	/**
	 * Parse a string to an integer. If the string could not be formated will be
	 * returned the defaultValue.
	 * 
	 * @param value
	 *            The string value to be parsed.
	 * @param defaultValue
	 *            Default value returned if the string could not be parsed.
	 * @return Parsed integer.
	 */
	public static int parseInt(String value, int defaultValue) {
		int i = defaultValue;
		try {
			i = Integer.parseInt(value);
		} catch (NumberFormatException e) {
		}
		return i;
	}

	/**
	 * Parse a string to a float number. If the string could not be parsed will
	 * be returned the value zero.
	 * 
	 * @param value
	 *            The string to be parsed.
	 * @return Parsed float.
	 */
	public static float parseFloat(String value) {
		float f = 0;
		try {
			f = Float.parseFloat(value);
		} catch (NumberFormatException e) {
		}
		return f;
	}

	/**
	 * Parse a string to a long number. If the string could not be parsed will
	 * be returned the value zero.
	 * 
	 * @param value
	 *            The string to be parsed.
	 * @return Parsed long.
	 */
	public static long parseLong(String value) {
		long l = 0;
		try {
			l = Long.parseLong(value);
		} catch (NumberFormatException e) {
		}
		return l;
	}

	/**
	 * This is used to parse a birthday date from a string to a calendar object.
	 * 
	 * @param birthday
	 *            The birthday date string.
	 * @return Resulted calendar object.
	 */
	public static Calendar parseCalendarString(Locale locale,
			String dateFormat, String calendar) {
		Calendar c = Calendar.getInstance(locale);
		if (calendar != null && calendar.length() > 0) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, locale);
				Date d = sdf.parse(calendar);
				c.setTimeInMillis(d.getTime());
			} catch (Exception ex) {
			}
		}
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c;
	}

	/**
	 * Format a calendar object to a human readable string.
	 * 
	 * @param locale
	 *            The locale used for the formatting.
	 * @param dateFormat
	 *            The date format used.
	 * @param calendar
	 *            The calendar object to be formated.
	 * @return The formated string for the provided calendar.
	 */
	public static String getFormattedCalendar(Locale locale, String dateFormat,
			Calendar calendar) {
		String res = null;
		if (calendar != null) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, locale);
				res = sdf.format(calendar.getTime());
			} catch (Exception ex) {
			}
		}
		return res;
	}

	/**
	 * Convert a string into a list for strings. The elements should be comma
	 * separated.
	 * 
	 * @param string
	 *            The string array with comma separated elements.
	 * @return A list with strings.
	 */
	public static List<String> getStringList(String string) {
		String[] array = string.split(",");
		List<String> list = new ArrayList<String>(array.length);
		for (String item : array) {
			list.add(item);
		}
		return list;
	}

	/**
	 * Close a DB cursor.
	 * 
	 * @param cursor
	 *            The DB cursor to be closed.
	 */
	public static void closeCursor(Cursor cursor) {
		try {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
}
