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
package ro.ciubex.brg.adapter;

import java.util.List;

import ro.ciubex.brg.R;
import ro.ciubex.brg.model.SlideMenuItem;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class define left slide menu list adapter.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class SlideMenuListAdapter extends BaseAdapter {
	private Context context;
	private List<SlideMenuItem> menuItems;

	public SlideMenuListAdapter(Context context, List<SlideMenuItem> menuItems) {
		this.context = context;
		this.menuItems = menuItems;
	}

	/**
	 * This is returning the menu items count.
	 * 
	 * @return The menu items count.
	 */
	@Override
	public int getCount() {
		return menuItems.size();
	}

	/**
	 * This method is returning a menu item from a specified position.
	 * 
	 * @param position
	 *            The specified position from the menu.
	 */
	@Override
	public Object getItem(int position) {
		return menuItems.get(position);
	}

	/**
	 * Get the row id associated with the specified position in the list. In
	 * this case the position is also the id.
	 * 
	 * @param position
	 *            The position in the list.
	 * @return The position in the list.
	 */
	@Override
	public long getItemId(int position) {
		return position;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.slide_menu_layout, null);
		}

		ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
		TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
		TextView txtCount = (TextView) convertView.findViewById(R.id.counter);

		imgIcon.setImageResource(menuItems.get(position).getIcon());
		txtTitle.setText(menuItems.get(position).getTitle());

		// displaying count
		// check whether it set visible or not
		if (menuItems.get(position).getCounterVisibility()) {
			txtCount.setText(menuItems.get(position).getCount());
		} else {
			// hide the counter view
			txtCount.setVisibility(View.GONE);
		}

		return convertView;
	}

}
