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

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.R;
import ro.ciubex.brgen.model.Contact;
import android.view.View;
import android.view.ViewGroup;

/**
 * This is the birthday list adapter, used on the birthday fragment view.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class BirthdayListAdapter extends ContactBaseAdapter {
	private Calendar mNow;
	private int mNowMonth;
	private int mNextMonth;
	private int mNowDay;
	private int mNextDay;
	private int mThisYear;
	private int mNextYear;
	private String mLabelToday;
	private String mLabelTomorrow;
	private String mLabelThisMonth;
	private String mLabelNextMonth;

	/**
	 * @param application
	 * @param itemClickListener
	 * @param contacts
	 * @param locale
	 */
	public BirthdayListAdapter(MainApplication application,
			OnListItemClickListener itemClickListener, List<Contact> contacts,
			Locale locale) {
		super(application, itemClickListener, contacts, locale);
	}

	@Override
	protected void initSections() {
		mSections = mResources.getStringArray(R.array.sections_months);
		mLabelToday = mResources.getString(R.string.today);
		mLabelTomorrow = mResources.getString(R.string.tomorrow);
		mLabelThisMonth = mResources.getString(R.string.this_month);
		mLabelNextMonth = mResources.getString(R.string.next_month);
	}

	/**
	 * Used to prepare all item views on this adapter
	 * 
	 * @param contacts
	 *            A list of contact models
	 */
	@Override
	protected void initItems(final List<Contact> contacts) {
		mNow = Calendar.getInstance(mLocale);
		mNowMonth = mNow.get(Calendar.MONTH);
		mNowDay = mNow.get(Calendar.DAY_OF_MONTH);
		mThisYear = mNow.get(Calendar.YEAR);
		mNextYear = mThisYear + 1;
		mNow.add(Calendar.DAY_OF_YEAR, 1);
		mNextDay = mNow.get(Calendar.DAY_OF_MONTH);
		mNow.set(Calendar.MONTH, mNowMonth);
		mNow.add(Calendar.MONTH, 1);
		mNextMonth = mNow.get(Calendar.MONTH);
		super.initItems(contacts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ro.ciubex.brgen.adapter.ContactBaseAdapter#add(ro.ciubex.brgen.model.
	 * Contact)
	 */
	@Override
	public void add(Contact item) {
		ContactListItem itm = new ContactListItem(item);
		if (item.haveBirthday()) {
			Calendar c = item.getBirthday();
			String label;
			int calM = c.get(Calendar.MONTH);
			int calD = c.get(Calendar.DAY_OF_MONTH);
			if (mNowDay == calD && mNowMonth == calM) {
				label = mLabelToday;
			} else if (mNextDay == calD
					&& (mNowMonth == calM || mNextMonth == calM)) {
				label = mLabelTomorrow;
			} else if (mNowMonth == calM) {
				label = mLabelThisMonth;
			} else if (mNextMonth == calM) {
				label = mLabelNextMonth;
			} else {
				label = getMonthLabel(calM);
			}
			itm.setSectionLabel(label);
		}
		mItems.add(itm);
	}

	/**
	 * Get the month separator label, based on current month.
	 * 
	 * @param month
	 *            Contact birthday month.
	 * @return Month and current year or next year.
	 */
	private String getMonthLabel(int month) {
		return mSections[month]
				+ " - "
				+ (month > mNowMonth && month < Calendar.UNDECIMBER ? mThisYear
						: mNextYear);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ro.ciubex.brgen.adapter.ContactBaseAdapter#initIndexes()
	 */
	@Override
	public void initIndexes() {
		if (!mIndexes.isEmpty()) {
			mIndexes.clear();
		}
		if (mItems.isEmpty()) {
			mIndexes.put(mLabelThisMonth, 0);
		} else {
			String sectionLabel = null;
			int pos = 0;
			for (ContactListItem item : mItems) {
				if (!item.getSectionLabel().equals(sectionLabel)) {
					sectionLabel = item.getSectionLabel();
					mIndexes.put(sectionLabel, pos);
				}
				pos++;
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
