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
package ro.ciubex.brg;

import java.util.ArrayList;
import java.util.List;

import ro.ciubex.brg.adapter.SlideMenuListAdapter;
import ro.ciubex.brg.fragment.AboutFragment;
import ro.ciubex.brg.fragment.ContactsListFragment;
import ro.ciubex.brg.fragment.LicenseFragment;
import ro.ciubex.brg.fragment.SettingsFragment;
import ro.ciubex.brg.model.Constants;
import ro.ciubex.brg.model.Contact;
import ro.ciubex.brg.model.SlideMenuItem;
import ro.ciubex.brg.tasks.DefaultAsyncTaskResult;
import ro.ciubex.brg.tasks.GenerateRemindersAsyncTask;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * This is main activity class.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class MainActivity extends Activity implements
		GenerateRemindersAsyncTask.Responder {
	private MainApplication mApplication;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	public static final int FRG_CNT_LIST = 0;
	public static final int FRG_SETTINGS = 1;
	public static final int FRG_ABOUT = 2;
	public static final int FRG_LICENSE = 3;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private List<SlideMenuItem> navDrawerItems;
	private SlideMenuListAdapter adapter;
	private Fragment[] fragments;
	private ActionBar mainActionBar;
	private int mFragmentIdCurrent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (MainApplication) getApplication();
		setContentView(R.layout.activity_main);

		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.slide_menu_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.slide_menu_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<SlideMenuItem>();

		// adding nav drawer items to array
		// Contact list
		navDrawerItems.add(new SlideMenuItem(navMenuTitles[0], navMenuIcons
				.getResourceId(0, -1)));
		// Settings
		navDrawerItems.add(new SlideMenuItem(navMenuTitles[1], navMenuIcons
				.getResourceId(1, -1)));
		// About
		navDrawerItems.add(new SlideMenuItem(navMenuTitles[2], navMenuIcons
				.getResourceId(2, -1)));

		// Recycle the typed array
		navMenuIcons.recycle();

		mainActionBar = getActionBar();

		prepareFragments();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new SlideMenuListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		mainActionBar.setDisplayHomeAsUpEnabled(true);
		mainActionBar.setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				mainActionBar.setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				mainActionBar.setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			mFragmentIdCurrent = FRG_CNT_LIST;
			// on first time display view for first nav item
			displayView(mFragmentIdCurrent);
		}
	}

	/**
	 * Method used to initialize the application fragments.
	 */
	private void prepareFragments() {
		fragments = new Fragment[4];
		fragments[FRG_CNT_LIST] = new ContactsListFragment();
		fragments[FRG_SETTINGS] = new SettingsFragment();
		fragments[FRG_ABOUT] = new AboutFragment();
		fragments[FRG_LICENSE] = new LicenseFragment();
	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * 
	 * @param menu
	 *            The options menu in which you place your items.
	 * @return True for the menu to be displayed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * This hook is called whenever an item in your options menu is selected.
	 * 
	 * @param item
	 *            The menu item that was selected.
	 * @return Return false to allow normal menu processing to proceed, true to
	 *         consume it here.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_generate:
			generateReminders(mApplication.getContactsAsArray());
			return true;
		case R.id.action_reload:
			((ContactsListFragment) fragments[FRG_CNT_LIST])
					.reloadContactList();
			return true;
		case R.id.action_settings:
			displayView(FRG_SETTINGS);
			return true;
		case R.id.action_exit:
			doExit();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Call this when the activity should be closed.
	 */
	private void doExit() {
		finish();
	}

	/**
	 * Prepare the Screen's standard options menu to be displayed.
	 * 
	 * @param menu
	 *            The options menu as last shown or first initialized by
	 *            onCreateOptionsMenu().
	 * @return Return true for the menu to be displayed.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_settings).setVisible(
				mFragmentIdCurrent == FRG_CNT_LIST);
		menu.findItem(R.id.action_generate).setVisible(
				mFragmentIdCurrent == FRG_CNT_LIST);
		menu.findItem(R.id.action_reload).setVisible(
				mFragmentIdCurrent == FRG_CNT_LIST);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Displaying fragment view for selected nav drawer list item
	 * */
	public void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		if (position > -1 && position < fragments.length) {
			fragment = fragments[position];
			mFragmentIdCurrent = position;
		} else {
			fragment = fragments[FRG_CNT_LIST];
			mFragmentIdCurrent = FRG_CNT_LIST;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			setTitle(navMenuTitles[position]);
			if (mFragmentIdCurrent != FRG_LICENSE) {
				mDrawerList.setItemChecked(position, true);
				mDrawerList.setSelection(position);
				mDrawerLayout.closeDrawer(mDrawerList);
			}
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	/**
	 * Change the title associated with this activity.
	 * 
	 * @param title
	 *            The activity title to be showed.
	 */
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		mainActionBar.setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	/**
	 * Called by the system when the device configuration changes while your
	 * activity is running.
	 * 
	 * @param The
	 *            new device configuration.
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * Called when the activity has detected the user's press of the back key.
	 */
	@Override
	public void onBackPressed() {
		if (mFragmentIdCurrent == FRG_LICENSE) {
			displayView(FRG_ABOUT);
		} else if (mFragmentIdCurrent == FRG_CNT_LIST) {
			super.onBackPressed();
		} else {
			displayView(FRG_CNT_LIST);
		}
	}

	/**
	 * Create thread to generate reminders, based on the selected contacts from
	 * the main list
	 */
	public void generateReminders(Contact... contacts) {
		if (mApplication.getApplicationPreferences().haveCalendarSelected()) {
			new GenerateRemindersAsyncTask(this, contacts).execute();
		} else {
			mApplication.showMessageError(this, R.string.select_a_calendar);
		}
	}

	/**
	 * Method invoked when is started the generate reminders thread
	 */
	@Override
	public void startGenerateReminders() {
		mApplication.showProgressDialog(this, R.string.generate_reminders);
	}

	/**
	 * Method invoked at the end of generate reminders thread
	 * 
	 * @param result
	 *            The process result
	 */
	@Override
	public void endGenerateReminders(DefaultAsyncTaskResult result) {
		mApplication.hideProgressDialog();
		if (Constants.OK == result.resultId) {
			mApplication.showMessageInfo(this, result.resultMessage);
		} else {
			mApplication.showMessageError(this, result.resultMessage);
		}
	}
}
