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

import java.util.List;
import java.util.Locale;

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.R;
import ro.ciubex.brgen.model.Contact;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * This is the contact list adapter used on the contact list view.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class ContactListAdapter extends ContactBaseAdapter implements
		StickyListHeadersAdapter {

	public ContactListAdapter(MainApplication application,
			OnListItemClickListener itemClickListener, List<Contact> contacts,
			Locale locale) {
		super(application, itemClickListener, contacts, locale);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ro.ciubex.brgen.adapter.ContactBaseAdapter#initSections()
	 */
	@Override
	protected void initSections() {
		mSections = mResources.getStringArray(R.array.sections_alphabetically);
	}

	@Override
	public void add(Contact item) {
		ContactListItem itm = new ContactListItem(item);
		char ch = item.getContactName().charAt(0);
		if (Character.isDigit(ch)) {
			itm.setSectionLabel("-");
		} else {
			itm.setSectionLabel("" + Character.toUpperCase(ch));
		}
		mItems.add(itm);
	}

	/**
	 * Initialize the list indexes
	 */
	@Override
	public void initIndexes() {
		if (!mIndexes.isEmpty()) {
			mIndexes.clear();
		}
		int size = mSections.length;
		if (mItems.isEmpty()) {
			mIndexes.put(mSections[0], 0);
		} else {
			int s, i, idx = 0;
			int count = mItems.size();
			String key;
			String sectionLabel;
			for (s = 0; s < size; s++) {
				key = mSections[s];
				for (i = idx; i < count; i++) {
					sectionLabel = mItems.get(i).getSectionLabel();
					if (key.equals(sectionLabel)) {
						idx = i;
						break;
					}
				}
				mIndexes.put(key, idx);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ItemViewHolder itmViewHolder = null;
		if (view == null || view.getTag() == null) {
			view = mInflater.inflate(R.layout.contact_item_layout, parent,
					false);
			itmViewHolder = initItemView(view, position);
			view.setTag(itmViewHolder);
		} else {
			itmViewHolder = (ItemViewHolder) view.getTag();
		}
		prepareItemView(itmViewHolder, position);
		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * se.emilsjolander.stickylistheaders.StickyListHeadersAdapter#getHeaderView
	 * (int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getHeaderView(int position, View view, ViewGroup parent) {
		SeparatorViewHolder sepViewHolder = null;
		if (view == null || view.getTag() == null) {
			view = mInflater.inflate(R.layout.contact_separator_layout, parent,
					false);
			sepViewHolder = initSeparatorView(view);
			view.setTag(sepViewHolder);
		} else {
			sepViewHolder = (SeparatorViewHolder) view.getTag();
		}
		prepareSeparatorView(sepViewHolder, position);
		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * se.emilsjolander.stickylistheaders.StickyListHeadersAdapter#getHeaderId
	 * (int)
	 */
	@Override
	public long getHeaderId(int position) {
		int index = 0;
		ContactListItem item = mItems.get(position);
		String key = item.getSectionLabel();
		if (mIndexes.containsKey(key)) {
			index = mIndexes.get(key);
		}
		return index;
	}

}
