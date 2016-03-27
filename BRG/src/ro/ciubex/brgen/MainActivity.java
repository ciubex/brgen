/**
 * This file is part of BRG application.
 * <p/>
 * Copyright (C) 2014 Claudiu Ciobotariu
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ro.ciubex.brgen;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ro.ciubex.brgen.adapter.SlideMenuListAdapter;
import ro.ciubex.brgen.fragment.AboutFragment;
import ro.ciubex.brgen.fragment.BaseFragment;
import ro.ciubex.brgen.fragment.BirthdayListFragment;
import ro.ciubex.brgen.fragment.ContactsListFragment;
import ro.ciubex.brgen.fragment.LicenseFragment;
import ro.ciubex.brgen.fragment.SettingsFragment;
import ro.ciubex.brgen.model.SlideMenuItem;
import ro.ciubex.brgen.util.Utilities;

/**
 * This is main activity class.
 *
 * @author Claudiu Ciobotariu
 *
 */
public class MainActivity extends AppCompatActivity {
    private MainApplication mApplication;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    public static final int FRG_CNT_LIST = 0;
    public static final int FRG_BRTH_LIST = 1;
    public static final int FRG_SETTINGS = 2;
    public static final int FRG_ABOUT = 3;
    public static final int FRG_LICENSE = 4;
    public static final int FORCE_RELOAD_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_CODE = 44;

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] mNavMenuTitles;
    private TypedArray mNavMenuIcons;

    private List<SlideMenuItem> mNavDrawerItems;
    private SlideMenuListAdapter mAdapter;
    private Fragment[] mFragments;
    private ActionBar mMainActionBar;
    private int mFragmentIdCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mApplication = (MainApplication) getApplication();
        applyApplicationTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Resources res = getResources();

        mTitle = mDrawerTitle = getTitle();

        // load slide menu items
        mNavMenuTitles = res.getStringArray(R.array.slide_menu_items);

        // nav drawer icons from resources
        mNavMenuIcons = res.obtainTypedArray(R.array.slide_menu_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        mNavDrawerItems = new ArrayList<SlideMenuItem>();

        // adding nav drawer items to array
        // Contact list
        mNavDrawerItems.add(new SlideMenuItem(mNavMenuTitles[0], mNavMenuIcons
                .getResourceId(0, -1)));
        // Birthdays list
        mNavDrawerItems.add(new SlideMenuItem(mNavMenuTitles[1], mNavMenuIcons
                .getResourceId(1, -1)));
        // Settings
        mNavDrawerItems.add(new SlideMenuItem(mNavMenuTitles[2], mNavMenuIcons
                .getResourceId(2, -1)));
        // About
        mNavDrawerItems.add(new SlideMenuItem(mNavMenuTitles[3], mNavMenuIcons
                .getResourceId(3, -1)));

        // Recycle the typed array
        mNavMenuIcons.recycle();

        mMainActionBar = getSupportActionBar();

        prepareFragments();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        mAdapter = new SlideMenuListAdapter(getApplicationContext(),
                mNavDrawerItems);
        mDrawerList.setAdapter(mAdapter);

        // enabling action bar app icon and behaving it as toggle button
        mMainActionBar.setDisplayHomeAsUpEnabled(true);
        mMainActionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, // nav menu toggle icon
                R.string.app_name, // nav drawer open - description for
                // accessibility
                R.string.app_name // nav drawer close - description for
                // accessibility
        ) {
            public void onDrawerClosed(View view) {
                mMainActionBar.setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                mMainActionBar.setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            mFragmentIdCurrent = FRG_CNT_LIST;
            // on first time display view for first nav item
            displayView(mFragmentIdCurrent, -1);
        }
    }

    /**
     * Apply application theme.
     */
    protected void applyApplicationTheme() {
        this.setTheme(mApplication.getApplicationTheme());
    }

    /**
     * Method used to initialize the application fragments.
     */
    private void prepareFragments() {
        mFragments = new Fragment[5];
        mFragments[FRG_CNT_LIST] = new ContactsListFragment();
        mFragments[FRG_BRTH_LIST] = new BirthdayListFragment();
        mFragments[FRG_SETTINGS] = new SettingsFragment();
        mFragments[FRG_ABOUT] = new AboutFragment();
        mFragments[FRG_LICENSE] = new LicenseFragment();
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
            displayView(position, -1);
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
            case R.id.action_update_reminders:
                ((ContactsListFragment) mFragments[FRG_CNT_LIST])
                        .updateAllReminders();
                return true;
            case R.id.action_sync:
                ((ContactsListFragment) mFragments[FRG_CNT_LIST]).syncReminders();
                return true;
            case R.id.action_reload:
                ((ContactsListFragment) mFragments[FRG_CNT_LIST])
                        .reloadContactList();
                return true;
            case R.id.action_settings:
                displayView(FRG_SETTINGS, -1);
                return true;
            case R.id.action_back:
                displayView(FRG_CNT_LIST, -1);
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
        menu.findItem(R.id.action_update_reminders).setVisible(
                mFragmentIdCurrent == FRG_CNT_LIST);
        menu.findItem(R.id.action_sync).setVisible(
                mFragmentIdCurrent == FRG_CNT_LIST);
        menu.findItem(R.id.action_reload).setVisible(
                mFragmentIdCurrent == FRG_CNT_LIST);
        menu.findItem(R.id.action_back).setVisible(
                mFragmentIdCurrent != FRG_CNT_LIST);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Displaying fragment view for selected nav drawer list item
     * */
    public void displayView(int position, int contentId) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        if (FRG_BRTH_LIST == position && !mApplication.isBirthdaysLoaded()) {
            position = mFragmentIdCurrent;
            mApplication.showMessageInfo(this, R.string.birthdays_not_loaded);
        }
        if (position > -1 && position < mFragments.length) {
            fragment = mFragments[position];
            mFragmentIdCurrent = position;
        } else {
            fragment = mFragments[FRG_CNT_LIST];
            mFragmentIdCurrent = FRG_CNT_LIST;
        }

        if (fragment != null) {
            if (fragment instanceof BaseFragment
                    && contentId > BaseFragment.NO_CONTENT_ID) {
                ((BaseFragment) fragment).setContentId(contentId);
            }
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();

            // update selected item and title, then close the drawer
            setTitle(mNavMenuTitles[position]);
            if (mFragmentIdCurrent != FRG_LICENSE) {
                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
            invalidateOptionsMenu();
            if (FRG_CNT_LIST == position && FORCE_RELOAD_CONTACTS == contentId) {
                ((ContactsListFragment) mFragments[FRG_CNT_LIST])
                        .reloadContactList();
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
        mMainActionBar.setTitle(mTitle);
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
     * @param newConfig The new device configuration.
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
            displayView(FRG_ABOUT, -1);
        } else if (mFragmentIdCurrent == FRG_CNT_LIST) {
            super.onBackPressed();
        } else {
            displayView(FRG_CNT_LIST, -1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForPermissions();
    }

    /**
     * Method used to check for application permissions.
     */
    @TargetApi(23)
    private void checkForPermissions() {
        if (mApplication.shouldAskPermissions()) {
            if (!mApplication.havePermissionsAsked()) {
                requestForPermissions(mApplication.getAllRequiredPermissions());
            }
        }
    }

    /**
     * Method used to request for application required permissions.
     */
    @TargetApi(23)
    public void requestForPermissions(String[] permissions) {
        if (!Utilities.isEmpty(permissions)) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback for the result from requesting permissions.
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (PERMISSIONS_REQUEST_CODE == requestCode) {
            mApplication.markPermissionsAsked();
            for (String permission : permissions) {
                mApplication.markPermissionAsked(permission);
            }
        }
    }
}
