/**
 * 
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
 * @author Claudiu Ciobotariu
 * 
 */
public class StickyListAdapter extends ContactBaseAdapter implements
		StickyListHeadersAdapter {

	public StickyListAdapter(MainApplication application,
			OnListItemClickListener itemClickListener, List<Contact> contacts,
			Locale locale) {
		super(application, itemClickListener, contacts, locale);
	}

	@Override
	public void add(Contact item) {
		ContactListItem itm = new ContactListItem(item);
		mItems.add(itm);
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
