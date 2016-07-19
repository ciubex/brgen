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
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.util.ApplicationPreferences;
import ro.ciubex.brgen.util.CalendarUtils;
import ro.ciubex.brgen.util.Utilities;

/**
 * This is main application class.
 *
 * @author Claudiu Ciobotariu
 *
 */
public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getName();
    private static Context mContext;
    private ApplicationPreferences mApplicationPreferences;
    private CalendarUtils mCalendarUtils;
    private List<Contact> mContacts;
    private List<Contact> mBirthdays;
    private boolean mContactsLoaded;
    private boolean mBirthdaysLoaded;
    private ProgressDialog mProgressDialog;
    private static int mSdkInt = 8;
    private SharedPreferences mSharedPreferences;
    private boolean mMustRestart;

    private static final String KEY_HAVE_PERMISSIONS_ASKED = "havePermissionsAsked";
    public static final String PERMISSION_FOR_READ_CONTACTS = "android.permission.READ_CONTACTS";
    public static final String PERMISSION_FOR_WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    public static final String PERMISSION_FOR_READ_CALENDAR = "android.permission.READ_CALENDAR";
    public static final String PERMISSION_FOR_WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";
    public static final String PERMISSION_FOR_READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String PERMISSION_FOR_WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static final String KEY_APP_THEME = "appTheme";

    public static final List<String> FUNCTIONAL_PERMISSIONS = Arrays.asList(
            PERMISSION_FOR_READ_EXTERNAL_STORAGE,
            PERMISSION_FOR_WRITE_EXTERNAL_STORAGE
    );

    public static final List<String> CONTACTS_PERMISSIONS = Arrays.asList(
            PERMISSION_FOR_READ_CONTACTS,
            PERMISSION_FOR_WRITE_CONTACTS
    );

    public static final List<String> CALENDAR_PERMISSIONS = Arrays.asList(
            PERMISSION_FOR_READ_CALENDAR,
            PERMISSION_FOR_WRITE_CALENDAR
    );

    /**
     * This method is invoked when the application is created.
     *
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        MainApplication.mContext = getApplicationContext();
        mSdkInt = android.os.Build.VERSION.SDK_INT;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mApplicationPreferences = new ApplicationPreferences(this);
        mContactsLoaded = false;
        mBirthdaysLoaded = false;
        mContacts = new ArrayList<Contact>();
        mBirthdays = new ArrayList<Contact>();
        mCalendarUtils = new CalendarUtils(this);
    }

    public static Context getAppContext() {
        return MainApplication.mContext;
    }

    /**
     * Get the contacts list.
     *
     * @return the contacts
     */
    public List<Contact> getContacts() {
        return mContacts;
    }

    /**
     * This list contain contacts with birthdays.
     *
     * @return the birthdays list.
     */
    public List<Contact> getBirthdays() {
        return mBirthdays;
    }

    /**
     * Obtain an array of contacts from the list.
     *
     * @return An array of contacts.
     */
    public Contact[] getContactsAsArray() {
        Contact[] contacts = new Contact[mContacts.size()];
        contacts = mContacts.toArray(contacts);
        return contacts;
    }

    /**
     * Retrieve default application locale
     *
     * @return Default locale used on application
     */
    public Locale getDefaultLocale() {
        return mApplicationPreferences.getDefaultLocale();
    }

    /**
     * Check the if the contact list is loaded.
     *
     * @return the contactsLoaded
     */
    public boolean isContactsLoaded() {
        return mContactsLoaded;
    }

    /**
     * Check if the birthdays list is loaded.
     *
     * @return the BirthdaysLoaded flag.
     */
    public boolean isBirthdaysLoaded() {
        return mBirthdaysLoaded;
    }

    /**
     * Obtain date format string.
     *
     * @return The date format string.
     */
    public String getDateFormat() {
        return mApplicationPreferences.getDateFormat();
    }

    /**
     * Obtain the date format for the birthday.
     *
     * @return
     */
    public String getDisplayDateFormat() {
        return mApplicationPreferences.getDisplayDateFormat();
    }

    /**
     * Format a provided calendar to a preferred formatted string.
     *
     * @param calendar
     *            The calendar to be formatted.
     * @return The formatted calendar string.
     */
    public String getFormattedCalendar(Calendar calendar) {
        return Utilities.getFormattedCalendar(getDefaultLocale(),
                getDisplayDateFormat(), calendar);
    }

    /**
     * Set the flag used to check if contacts list is loaded.
     *
     * @param contactsLoaded
     *            the contactsLoaded to set.
     */
    public void setContactsLoaded(boolean contactsLoaded) {
        this.mContactsLoaded = contactsLoaded;
    }

    /**
     * Set the flag used to check if birthdays list is loaded.
     *
     * @param birthdaysLoaded
     *            the birthdaysLoaded to set
     */
    public void setBirthdaysLoaded(boolean birthdaysLoaded) {
        this.mBirthdaysLoaded = birthdaysLoaded;
    }

    /**
     * This will show a progress dialog using a context and a message ID from
     * application string resources.
     *
     * @param context
     *            The context where should be displayed the progress dialog.
     * @param messageId
     *            The string resource id.
     */
    public void showProgressDialog(Context context, int messageId) {
        showProgressDialog(context, getString(messageId));
    }

    /**
     * This will show a progress dialog using a context and the message to be
     * showed on the progress dialog.
     *
     * @param context
     *            The context where should be displayed the progress dialog.
     * @param message
     *            The message displayed inside of progress dialog.
     */
    public void showProgressDialog(Context context, String message) {
        hideProgressDialog();
        mProgressDialog = ProgressDialog.show(context,
                getString(R.string.please_wait), message);
    }

    /**
     * Method used to hide the progress dialog.
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }

    /**
     * Method used to show the informations.
     *
     * @param context
     *            The context where should be displayed the message.
     * @param resourceMessageId
     *            The string resource id.
     */
    public void showMessageInfo(Context context, int resourceMessageId) {
        String message = getString(resourceMessageId);
        showMessageInfo(context, message);
    }

    /**
     * Method used to show formated informations.
     *
     * @param context
     *            The context where should be displayed the message.
     * @param resourceMessageId
     *            The string resource id.
     * @param formatArgs
     *            The arguments used on formated message.
     */
    public void showMessageInfo(Context context, int resourceMessageId,
                                Object... formatArgs) {
        String message = getString(resourceMessageId, formatArgs);
        showMessageInfo(context, message);
    }

    /**
     * This method is used to show on front of a context a toast message.
     *
     * @param context
     *            The context where should be showed the message.
     * @param message
     *            The message used to be displayed on the information box.
     */
    public void showMessageInfo(Context context, String message) {
        if (message != null && message.length() > 0) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method used to obtain the age based on a birthday calendar.
     *
     * @param birthday
     *            The birthday calendar.
     * @return The age as a string.
     */
    public String getAge(Calendar birthday) {
        Calendar now = Calendar.getInstance(getDefaultLocale());
        int age = now.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
        if (now.get(Calendar.DAY_OF_YEAR) < birthday.get(Calendar.DAY_OF_YEAR)) {
            age -= 1;
        }
        return getString(R.string.age, age);
    }

    /**
     * Obtain the application preferences.
     *
     * @return the applicationPreferences
     */
    public ApplicationPreferences getApplicationPreferences() {
        return mApplicationPreferences;
    }

    /**
     * Obtain the calendar utils.
     *
     * @return the calendarUtils
     */
    public CalendarUtils getCalendarUtils() {
        return mCalendarUtils;
    }

    /**
     * Check for pro version.
     *
     * @return True if pro version exist.
     */
    public boolean isProPresent() {
        PackageManager pm = getPackageManager();
        boolean success = false;
        try {
            success = (PackageManager.SIGNATURE_MATCH == pm.checkSignatures(
                    this.getPackageName(), "ro.ciubex.brgenpro"));
            Log.d(TAG, "isProPresent: " + success);
        } catch (Exception e) {
            Log.e(TAG, "isProPresent: " + e.getMessage(), e);
        }
        return success;
    }

    /**
     * Store a boolean value on the shared preferences.
     *
     * @param key   The shared preference key.
     * @param value The boolean value to be saved.
     */
    private void saveBooleanValue(String key, boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * Remove a shared preference.
     *
     * @param key The key of the shared preference to be removed.
     */
    private void removeSharedPreference(String key) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * Check if should be asked for permissions.
     *
     * @return True if should be asked for permissions.
     */
    public boolean shouldAskPermissions() {
        return mSdkInt > 22;
    }

    /**
     * Check if the permissions were asked.
     *
     * @return True if the permissions were asked.
     */
    public boolean havePermissionsAsked() {
        return mSharedPreferences.getBoolean(KEY_HAVE_PERMISSIONS_ASKED, false);
    }

    /**
     * Set the permission asked flag to true.
     */
    public void markPermissionsAsked() {
        saveBooleanValue(KEY_HAVE_PERMISSIONS_ASKED, true);
    }

    /**
     * Check if a permission was asked.
     *
     * @param permission The permission to be asked.
     * @return True if the permission was asked before.
     */
    public boolean isPermissionAsked(String permission) {
        return mSharedPreferences.getBoolean(permission, false);
    }

    /**
     * Mark a permission as asked.
     *
     * @param permission Permission to be marked as asked.
     */
    public void markPermissionAsked(String permission) {
        saveBooleanValue(permission, true);
    }

    /**
     * Remove the permission asked flag.
     *
     * @param permission The permission for which will be removed the asked flag.
     */
    public void removePermissionAskedMark(String permission) {
        removeSharedPreference(permission);
    }

    /**
     * Check if a permission is allowed.
     *
     * @param permission The permission to be checked.
     * @return True if the permission is allowed.
     */
    public boolean hasPermission(String permission) {
        if (shouldAskPermissions()) {
            return hasPermission23(permission);
        }
        return true;
    }

    /**
     * Check if a permission is allowed. (API 23)
     *
     * @param permission The permission to be checked.
     * @return True if the permission is allowed.
     */
    @TargetApi(23)
    private boolean hasPermission23(String permission) {
        return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Check if the application have functional permissions.
     *
     * @return True if all functional permissions are allowed.
     */
    public boolean haveFunctionalPermissions() {
        for (String permission : MainApplication.FUNCTIONAL_PERMISSIONS) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the application have contacts permissions.
     *
     * @return True if all functional contacts are allowed.
     */
    public boolean haveContactsPermissions() {
        for (String permission : MainApplication.CONTACTS_PERMISSIONS) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the application have calendar permissions.
     *
     * @return True if all calendar permissions are allowed.
     */
    public boolean haveCalendarPermissions() {
        for (String permission : MainApplication.CALENDAR_PERMISSIONS) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get all not granted permissions.
     */
    public String[] getNotGrantedPermissions() {
        List<String> permissions = new ArrayList<>();
        buildRequiredPermissions(permissions, MainApplication.CONTACTS_PERMISSIONS, true);
        buildRequiredPermissions(permissions, MainApplication.FUNCTIONAL_PERMISSIONS, true);
        buildRequiredPermissions(permissions, MainApplication.CALENDAR_PERMISSIONS, true);
        String[] array = null;
        if (!permissions.isEmpty()) {
            array = new String[permissions.size()];
            array = permissions.toArray(array);
        }
        return array;
    }

    /**
     * Get an array with all required permissions.
     *
     * @return Array with permissions to be requested.
     */
    public String[] getAllRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        buildRequiredPermissions(permissions, MainApplication.CONTACTS_PERMISSIONS, true);
        buildRequiredPermissions(permissions, MainApplication.FUNCTIONAL_PERMISSIONS, false);
        buildRequiredPermissions(permissions, MainApplication.CALENDAR_PERMISSIONS, true);
        String[] array = null;
        if (!permissions.isEmpty()) {
            array = new String[permissions.size()];
            array = permissions.toArray(array);
        }
        return array;
    }

    /**
     * Put on the permissions all required permissions which is missing and was not asked.
     *
     * @param permissions         List of permissions to be requested.
     * @param requiredPermissions List with all required permissions to be checked.
     */
    private void buildRequiredPermissions(List<String> permissions, List<String> requiredPermissions, boolean force) {
        for (String permission : requiredPermissions) {
            if ((force && !hasPermission(permission)) ||
                    (!isPermissionAsked(permission) && !hasPermission(permission))) {
                permissions.add(permission);
            }
        }
    }


    /**
     * Get the application theme.
     *
     * @return The application theme.
     */
    public int getApplicationTheme() {
        String theme = mSharedPreferences.getString(KEY_APP_THEME, "light");
        if ("dark".equals(theme)) {
            return R.style.AppThemeDark;
        }
        return R.style.AppThemeLight;
    }

    /**
     * Set the must restart flag.
     *
     * @param mustRestart The value to be set.
     */
    public void setMustRestart(boolean mustRestart) {
        this.mMustRestart = mustRestart;
    }

    /**
     * Check the must restart flag state.
     *
     * @return The must restart flag state.
     */
    public boolean isMustRestart() {
        return mMustRestart;
    }
}
