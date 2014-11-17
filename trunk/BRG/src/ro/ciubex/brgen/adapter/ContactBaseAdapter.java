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
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is a custom base adapter for contact list.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public abstract class ContactBaseAdapter extends BaseAdapter implements
		StickyListHeadersAdapter {
	protected MainApplication mApplication;
	protected Resources mResources;
	protected Context mContext;
	protected LayoutInflater mInflater;
	protected List<ContactListItem> mItems;
	protected List<Contact> mContacts;
	protected Map<String, Integer> mIndexes;
	protected String[] mSections;
	protected Filter mFilter;
	protected Locale mLocale;
	protected OnListItemClickListener mItemClickListener;

	public ContactBaseAdapter(MainApplication application,
			OnListItemClickListener itemClickListener, List<Contact> contacts,
			Locale locale) {
		mContext = (Context) application;
		mApplication = application;
		mResources = mContext.getResources();
		mItemClickListener = itemClickListener;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mContacts = contacts;
		this.mLocale = locale;
		mIndexes = new HashMap<String, Integer>();
		mItems = new ArrayList<ContactListItem>();
		initSections();
		initItems(contacts);
	}

	/**
	 * Used to prepare all item views on this adapter
	 * 
	 * @param contacts
	 *            A list of contact models
	 */
	protected void initItems(final List<Contact> contacts) {
		for (Contact contact : contacts) {
			add(contact);
		}
		initIndexes();
	}

	/**
	 * Initialize sections.
	 */
	abstract protected void initSections();

	/**
	 * Add a contact item to the adapter item list
	 * 
	 * @param item
	 *            The contact model to be added to the adapter item list
	 */
	abstract public void add(Contact item);

	/**
	 * Initialize the list indexes
	 */
	abstract public void initIndexes();

	/**
	 * Get all contacts loaded on the adapter
	 * 
	 * @return List of contact models
	 */
	public List<Contact> getContacts() {
		return mContacts;
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
		if (position < 0 || position >= mItems.size()) {
			return null;
		}
		return mItems.get(position).getContactModel();
	}

	/**
	 * Get the row id associated with the specified position in the list. In
	 * this case the position is also the id.
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Used to obtain the adapter filter
	 * 
	 * @return Adapter customized filter
	 */
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ContactListFilter(this, mLocale);
		}
		return mFilter;
	}

	/**
	 * Initialize the separator item view
	 * 
	 * @param view
	 *            The view used to obtain the text view used to display the
	 *            separator character.
	 * @return The separator view
	 */
	protected SeparatorViewHolder initSeparatorView(View view) {
		SeparatorViewHolder viewHolder = new SeparatorViewHolder();
		viewHolder.separatorItem = (TextView) view
				.findViewById(R.id.separatorItem);
		return viewHolder;
	}

	/**
	 * Initialize the normal item view, with the contact photo, name, birthday
	 * and check mark.
	 * 
	 * @param view
	 *            The view used to obtain all child views.
	 * @param position
	 *            The item position.
	 * @return Contact item view.
	 */
	protected ItemViewHolder initItemView(View view, final int position) {
		ItemViewHolder viewHolder = new ItemViewHolder();
		viewHolder.firstItemText = (TextView) view
				.findViewById(R.id.firstItemText);
		viewHolder.secondItemText = (TextView) view
				.findViewById(R.id.secondItemText);
		viewHolder.thirdItemText = (TextView) view
				.findViewById(R.id.thirdItemText);
		viewHolder.picture = (ImageView) view.findViewById(R.id.contactImage);
		viewHolder.imageCheckBox = (ImageView) view
				.findViewById(R.id.image_check);
		return viewHolder;
	}

	/**
	 * Prepare the separator view with the section separator character
	 * 
	 * @param viewHolder
	 *            The view holder of this section separator
	 * @param sectionLabel
	 *            The item position in list view
	 */
	protected void prepareSeparatorView(SeparatorViewHolder viewHolder,
			int position) {
		viewHolder.separatorItem
				.setText(mItems.get(position).getSectionLabel());
	}

	/**
	 * Prepare the normal item with the contact details
	 * 
	 * @param viewHolder
	 *            The view holder
	 * @param position
	 *            The item position in list view
	 */
	protected void prepareItemView(ItemViewHolder viewHolder, final int position) {
		final Contact contact = mItems.get(position).getContactModel();
		if (contact != null) {
			int resId;
			if (contact.havePicture()) {
				viewHolder.picture.setImageBitmap(contact.getThumbnail());
			} else {
				viewHolder.picture.setImageResource(R.drawable.contact_image);
			}
			viewHolder.firstItemText.setText(contact.getContactName());
			if (contact.haveBirthday()) {
				viewHolder.secondItemText.setText(mApplication.getAge(contact
						.getBirthday()));
				viewHolder.thirdItemText.setText(mApplication
						.getFormattedCalendar(contact.getBirthday()));
				resId = contact.isChecked() ? contact.isModified() ? R.drawable.checkbox_checked_modified
						: R.drawable.checkbox_checked
						: contact.isModified() ? R.drawable.checkbox_unchecked_modified
								: R.drawable.checkbox_unchecked;
				viewHolder.imageCheckBox.setImageResource(resId);
			} else {
				resId = R.drawable.checkbox_empty;
				viewHolder.imageCheckBox.setImageResource(resId);
				viewHolder.secondItemText.setText("");
				if (!contact.isLoadedBirthday()) {
					viewHolder.thirdItemText.setText(R.string.loading_wait);
				} else {
					viewHolder.thirdItemText.setText("");
				}
			}
			viewHolder.picture.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mItemClickListener.onContactImageClick(position);
				}
			});
			viewHolder.imageCheckBox
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							mItemClickListener.onCheckBoxClick(position);
						}
					});
		}
	}

	/**
	 * View holder for contact items within the list.
	 */
	static class ItemViewHolder {
		ImageView picture;
		TextView firstItemText;
		TextView secondItemText;
		TextView thirdItemText;
		ImageView imageCheckBox;
	}

	/**
	 * View holder for contact items within the list.
	 */
	static class SeparatorViewHolder {
		TextView separatorItem;
	}
}
