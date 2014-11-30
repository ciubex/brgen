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
package ro.ciubex.brgen.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ro.ciubex.brgen.adapter.ContactBaseAdapter;
import ro.ciubex.brgen.model.Contact;
import android.widget.Filter;

/**
 * This is the contact list filter. A customized filter for the application
 * adapter.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class ContactListFilter extends Filter {

	private ContactBaseAdapter adapter;
	private Locale locale;

	public ContactListFilter(ContactBaseAdapter adapter, Locale locale) {
		this.adapter = adapter;
		this.locale = locale;
	}

	/**
	 * Method used to filter the data according to the constraint.
	 * 
	 * @param constraint
	 *            The specified constraint.
	 * @return The results of the filtering operation.
	 */
	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		FilterResults results = new FilterResults();
		results.values = null;
		results.count = -1;
		List<Contact> contacts = adapter.getContacts();
		int originalSize = contacts != null ? contacts.size() : 0;
		if (constraint.length() > 0) {
			String filter = constraint.toString().trim();
			if (filter.length() > 0) {
				List<Contact> filterList = new ArrayList<Contact>();
				filter = filter.toLowerCase(locale);
				String contactName;
				for (Contact contact : contacts) {
					contactName = contact.getContactName().toLowerCase(locale);
					if (contactName.indexOf(filter) > -1) {
						filterList.add(contact);
					}
				}
				results.values = filterList;
				results.count = filterList.size();
			}
		} else if (originalSize > 0 && originalSize != adapter.getCount()) {
			List<Contact> filterList = new ArrayList<Contact>(contacts);
			results.values = filterList;
			results.count = filterList.size();
		}
		return results;
	}

	/**
	 * Method used to invoke the UI thread to publish the filtering results in
	 * the user interface.
	 * 
	 * @param constraint
	 *            The specified constraint.
	 * @param results
	 *            The results of the filtering operation.
	 */
	@Override
	protected void publishResults(CharSequence constraint, FilterResults results) {
		if (results.count > -1) {
			@SuppressWarnings("unchecked")
			List<Contact> filterList = (List<Contact>) results.values;
				if (filterList != null) {
				adapter.clear();
				for (Contact contact : filterList) {
					adapter.add(contact);
				}
				adapter.initIndexes();
				adapter.notifyDataSetChanged();
			} else {
				adapter.notifyDataSetInvalidated();
			}
		} else {
			adapter.notifyDataSetInvalidated();
		}
	}
}
