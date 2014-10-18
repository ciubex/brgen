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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.R;
import ro.ciubex.brgen.list.ContactListFilter;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.tasks.ContactImageLoaderAsyncTask;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * This adaptor is used to handle the contact list view
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class ContactListAdapter extends BaseAdapter implements SectionIndexer {
	private MainApplication mApplication;
	private LayoutInflater mInflater;
	private Filter mFilter;
	private Locale mLocale;
	private List<Contact> mContacts;
	private List<ContactListItem> mItems;
	private String mSectionsChars = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private String[] mSections;
	private Map<String, Integer> mIndexes;

	/**
	 * Define item views type
	 */
	public enum ITEM_TYPE {
		ITEM, SEPARATOR, UNUSED
	};

	public ContactListAdapter(MainApplication application,
			List<Contact> contacts, Locale locale) {
		Context context = (Context) application;
		mApplication = application;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mContacts = contacts;
		this.mLocale = locale;
		mItems = new ArrayList<ContactListAdapter.ContactListItem>();
		mIndexes = new HashMap<String, Integer>();
		initListView(contacts);
	}

	/**
	 * Used to prepare all item views on this adapter
	 * 
	 * @param contacts
	 *            A list of contact models
	 */
	private void initListView(final List<Contact> contacts) {
		for (Contact contact : contacts) {
			add(contact);
		}
		initIndexes();
	}

	/**
	 * Initialize the list indexes
	 */
	public void initIndexes() {
		if (!mIndexes.isEmpty()) {
			mIndexes.clear();
		}
		if (mItems.isEmpty()) {
			mSections = new String[1];
			char key = mSectionsChars.charAt(0);
			mIndexes.put("" + key, 0);
			mSections[0] = "" + key;
		} else {
			int s, i, idx = 0;
			int size = mSectionsChars.length();
			int count = mItems.size();
			char key;
			char sectionChar;
			mSections = new String[size];
			for (s = 0; s < size; s++) {
				key = mSectionsChars.charAt(s);
				for (i = idx; i < count; i++) {
					sectionChar = mItems.get(i).sectionChar;
					if (key == sectionChar) {
						idx = i;
						break;
					}
				}
				mIndexes.put("" + key, idx);
				mSections[s] = "" + key;
			}
		}
	}

	/**
	 * Used to clear the list of items
	 */
	public void clear() {
		mItems.clear();
	}

	/**
	 * Used to obtain items list size
	 * 
	 * @return Items size
	 */
	@Override
	public int getCount() {
		return mItems.size();
	}

	/**
	 * Retrieve the contact model from the items list at specified position
	 * 
	 * @param position
	 *            The position in list for retrieve contact
	 * @return The contact model object a specified position
	 */
	@Override
	public Contact getItem(int position) {
		return mItems.get(position).contactModel;
	}

	/**
	 * Get the row id associated with the specified position in the list. In
	 * this case the position is also the id.
	 */
	@Override
	public long getItemId(int pos) {
		return pos;
	}

	/**
	 * Get a View that displays the data at the specified position in the data
	 * set.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param view
	 *            The old view to reuse, if possible.
	 * @param parent
	 *            The parent that this view will eventually be attached to.
	 * @return A View corresponding to the data at the specified position.
	 */
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ContactListItem item = mItems.get(position);
		ContactsViewHolder viewHolder = null;
		if (view != null) {
			viewHolder = (ContactsViewHolder) view.getTag();
		}
		if (view == null || viewHolder == null
				|| viewHolder.itemType != item.itemType) {
			switch (item.itemType) {
			case ITEM:
				view = mInflater.inflate(R.layout.contact_item_layout, parent,
						false);
				viewHolder = initItemView(view);
				view.setTag(viewHolder);
				break;
			case SEPARATOR:
				view = mInflater.inflate(R.layout.contact_separator_layout,
						parent, false);
				viewHolder = initSeparatorView(view);
				view.setTag(viewHolder);
				break;
			case UNUSED:
				break;
			}
		}
		if (viewHolder != null) {
			switch (item.itemType) {
			case ITEM:
				prepareItemView(viewHolder, item.contactModel);
				break;
			case SEPARATOR:
				prepareSeparatorView(viewHolder, "" + item.sectionChar);
				break;
			case UNUSED:
				break;
			}
		}
		return view;
	}

	/**
	 * Initialize the separator item view
	 * 
	 * @param view
	 *            The view used to obtain the text view used to display the
	 *            separator character
	 * @return The separator view
	 */
	private ContactsViewHolder initSeparatorView(View view) {
		ContactsViewHolder viewHolder = new ContactsViewHolder();
		viewHolder.itemType = ITEM_TYPE.SEPARATOR;
		viewHolder.firstItemText = (TextView) view
				.findViewById(R.id.separatorItem);
		return viewHolder;
	}

	/**
	 * Initialize the normal item view, with the contact photo, name, birthday
	 * and check mark.
	 * 
	 * @param view
	 *            The view used to obtain all child views.
	 * @return Contact item view.
	 */
	private ContactsViewHolder initItemView(View view) {
		ContactsViewHolder viewHolder = new ContactsViewHolder();
		viewHolder.itemType = ITEM_TYPE.ITEM;
		viewHolder.picture = (ImageView) view.findViewById(R.id.contactImage);
		viewHolder.firstItemText = (TextView) view
				.findViewById(R.id.firstItemText);
		viewHolder.secondItemText = (TextView) view
				.findViewById(R.id.secondItemText);
		viewHolder.thirdItemText = (TextView) view
				.findViewById(R.id.thirdItemText);
		viewHolder.imageCheckBox = (ImageView) view
				.findViewById(R.id.image_check);
		return viewHolder;
	}

	/**
	 * Prepare the separator view with the section separator character
	 * 
	 * @param viewHolder
	 *            The view holder of this section separator
	 * @param sectionChar
	 *            The section separator character
	 */
	private void prepareSeparatorView(ContactsViewHolder viewHolder,
			String sectionChar) {
		viewHolder.firstItemText.setText(sectionChar);
	}

	/**
	 * Prepare the normal item with the contact details
	 * 
	 * @param viewHolder
	 *            The view holder
	 * @param contact
	 *            The contact model used to extrat contact details
	 */
	private void prepareItemView(ContactsViewHolder viewHolder, Contact contact) {
		if (contact != null) {
			if (!contact.isPictureLoading()) {
				new ContactImageLoaderAsyncTask(mApplication, this)
						.execute(contact);
			}
			if (contact.havePicture()) {
				viewHolder.picture.setImageBitmap(contact.getPicture());
			} else {
				viewHolder.picture.setImageResource(R.drawable.contact_image);
			}
			viewHolder.firstItemText.setText(contact.getContactName());
			if (contact.haveBirthday()) {
				viewHolder.secondItemText.setText(mApplication.getAge(contact
						.getBirthday()));
				viewHolder.thirdItemText.setText(mApplication
						.getFormattedCalendar(contact.getBirthday()));
				viewHolder.imageCheckBox
						.setImageResource(contact.isChecked() ? R.drawable.checkbox_checked
								: R.drawable.checkbox_unchecked);
			} else {
				viewHolder.imageCheckBox
						.setImageResource(R.drawable.checkbox_empty);
				viewHolder.secondItemText.setText("");
				if (!contact.isLoadedBirthday()) {
					viewHolder.thirdItemText.setText(R.string.loading_wait);
				} else {
					viewHolder.thirdItemText.setText("");
				}
			}
		}
	}

	/**
	 * Used to obtain the position for a specified section
	 * 
	 * @param section
	 *            Specified section
	 * @return The section for specified position
	 */
	@Override
	public int getPositionForSection(int section) {
		String key = mSections[section];
		int position = 0;
		if (mIndexes.containsKey(key)) {
			position = mIndexes.get(key);
		}
		return position;
	}

	/**
	 * Used to obtain the section for a specified position
	 * 
	 * @param position
	 *            The specified position
	 * @return The section for specified position
	 */
	@Override
	public int getSectionForPosition(int position) {
		int index = 0;
		ContactListItem item = mItems.get(position);
		String key = "" + item.sectionChar;
		if (mIndexes.containsKey(key)) {
			index = mIndexes.get(key);
		}
		return index;
	}

	/**
	 * Retrieve all sections
	 * 
	 * @return The sections array
	 */
	@Override
	public Object[] getSections() {
		return mSections;
	}

	/**
	 * Add a contact item to the adapter item list
	 * 
	 * @param item
	 *            The contact model to be added to the adapter item list
	 */
	public void add(Contact item) {
		mItems.add(prepareAdd(item));
	}

	/**
	 * Prepare the items list based on added contact model
	 * 
	 * @param item
	 *            The contact model to be added to the adapter item list
	 * @return A contact list item
	 */
	private ContactListItem prepareAdd(Contact item) {
		ContactListItem cItem = new ContactListItem(item);
		if (mItems.isEmpty()) {
			mItems.add(new ContactListItem(cItem.sectionChar));
		} else {
			ContactListItem lastItem = mItems.get(mItems.size() - 1);
			if (lastItem == null || lastItem.sectionChar != cItem.sectionChar) {
				mItems.add(new ContactListItem(cItem.sectionChar));
			}
		}
		return cItem;
	}

	/**
	 * Used to obtain the adapter filter
	 * 
	 * @return Adapter customized filter
	 */
	public Filter getFilter() {
		if (mFilter == null)
			mFilter = new ContactListFilter(this, mLocale);
		return mFilter;
	}

	/**
	 * Get all contacts loaded on the adapter
	 * 
	 * @return List of contact models
	 */
	public List<Contact> getContacts() {
		return mContacts;
	}

	/**
	 * View holder for contact items within the list.
	 * 
	 */
	static class ContactsViewHolder {
		ITEM_TYPE itemType;
		ImageView picture;
		TextView firstItemText;
		TextView secondItemText;
		TextView thirdItemText;
		ImageView imageCheckBox;
	}

	/**
	 * Static class with contact list items holders.
	 */
	static class ContactListItem {
		ITEM_TYPE itemType;
		char sectionChar;
		Contact contactModel;

		public ContactListItem() {
			itemType = ITEM_TYPE.UNUSED;
		}

		public ContactListItem(char sectionChar) {
			this.sectionChar = sectionChar;
			itemType = ITEM_TYPE.SEPARATOR;
		}

		public ContactListItem(Contact contactModel) {
			this.contactModel = contactModel;
			itemType = ITEM_TYPE.ITEM;
			char ch = contactModel.getContactName().charAt(0);
			sectionChar = (Character.isDigit(ch) ? '#' : Character
					.toUpperCase(ch));
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ContactListItem [").append(sectionChar)
					.append(", ").append(contactModel).append("]");
			return builder.toString();
		}
	}
}
