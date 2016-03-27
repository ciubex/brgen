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
package ro.ciubex.brgen.fragment;

import java.util.List;

import ro.ciubex.brgen.MainActivity;
import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.R;
import ro.ciubex.brgen.form.CustomEditTextPreference;
import ro.ciubex.brgen.model.Constants;
import ro.ciubex.brgen.model.GoogleCalendar;
import ro.ciubex.brgen.tasks.DefaultAsyncTaskResult;
import ro.ciubex.brgen.tasks.PreferencesFileUtilAsynkTask;
import ro.ciubex.brgen.util.CalendarUtils;
import ro.ciubex.brgen.util.Utilities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Birthday application preferences
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener, CustomEditTextPreference.Listener,
		PreferencesFileUtilAsynkTask.Responder {
	private MainApplication mApplication;
	private MainActivity mActivity;
	private Preference mAppTheme;
	private ListPreference mCalendarList;
	private PreferenceCategory mOtherSettings;
	private CustomEditTextPreference preferencesBackup;
	private CustomEditTextPreference preferencesRestore;
	private static final int PREF_BACKUP = 1;
	private static final int PREF_RESTORE = 2;

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_preferences);
		mActivity = (MainActivity) getActivity();
		mApplication = (MainApplication) mActivity.getApplication();
		mAppTheme = findPreference(MainApplication.KEY_APP_THEME);
		mCalendarList = (ListPreference) findPreference("calendarList");
		mOtherSettings = (PreferenceCategory) findPreference("otherSettings");
		prepareAllCustomEditTextPreference();
		preparePreferencesReset();
		prepareRequestPermissions();
		initPreferencesByPermissions();
		populateAvailableCalendars();
	}

	/**
	 * Prepare some custom preferences to not be stored. The preference backup
	 * and restore are actually used as buttons.
	 */
	private void prepareAllCustomEditTextPreference() {
		String backupPath = getBackupPath();
		preferencesBackup = (CustomEditTextPreference) findPreference("preferencesBackup");
		preferencesBackup.setResultListener(this, PREF_BACKUP);
		preferencesBackup.setPersistent(false);
		preferencesBackup.setText(backupPath);
		preferencesRestore = (CustomEditTextPreference) findPreference("preferencesRestore");
		preferencesRestore.setResultListener(this, PREF_RESTORE);
		preferencesRestore.setPersistent(false);
		preferencesRestore.setText(backupPath);
	}

	/**
	 * Prepare reset preference handler
	 */
	private void preparePreferencesReset() {
		Preference preferencesReset = findPreference("preferencesReset");
		preferencesReset
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						return onPreferencesReset();
					}
				});
	}

	private void prepareRequestPermissions() {
		Preference requestPermissions = findPreference("requestPermissions");
		requestPermissions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				onRequestPermissions();
				return true;
			}
		});
	}

	/**
	 * Remove the permission request preference if should not be asked for permissions.
	 */
	private void initPreferencesByPermissions() {
		if (!mApplication.shouldAskPermissions()) {
			Preference requestPermissions = findPreference("requestPermissions");
			if (requestPermissions != null) {
				mOtherSettings.removePreference(requestPermissions);
			}
		}
	}

	/**
	 * Prepare all informations when the activity is resuming
	 */
	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		prepareSummaries();
	}

	/**
	 * Method used to populate calendar list.
	 */
	private void populateAvailableCalendars() {
		CalendarUtils cu = mApplication.getCalendarUtils();
		if (cu.isCalendarSupported()) {
			List<GoogleCalendar> calendars = cu.getCalendars();
			int i = 0, count = calendars.size();
			String[] entries = new String[count];
			String[] values = new String[count];
			for (GoogleCalendar calendar : calendars) {
				entries[i] = calendar.getName();
				values[i] = String.valueOf(calendar.getId());
				i++;
			}
			mCalendarList.setEntries(entries);
			mCalendarList.setEntryValues(values);
			mCalendarList.setDefaultValue(values[0]);
		}
	}

	/**
	 * Unregister the preference changes when the activity is on pause
	 */
	@Override
	public void onPause() {
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	/**
	 * Prepare preferences summaries
	 */
	private void prepareSummaries() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mApplication);
		EditTextPreference editText = (EditTextPreference) findPreference("displayDateFormat");
		editText.setSummary(sp.getString("displayDateFormat",
				String.valueOf(editText.getSummary())));
		editText = (EditTextPreference) findPreference("dateFormat");
		editText.setSummary(sp.getString("dateFormat",
				String.valueOf(editText.getSummary())));
		editText = (EditTextPreference) findPreference("reminderTitleFormat");
		editText.setSummary(sp.getString("reminderTitleFormat",
				String.valueOf(editText.getSummary())));
		editText = (EditTextPreference) findPreference("reminderDescriptionFormat");
		editText.setSummary(sp.getString("reminderDescriptionFormat",
				String.valueOf(editText.getSummary())));

		String label = MainApplication.getAppContext().getString(R.string.app_theme_title_param,
				getSelectedThemeLabel());
		mAppTheme.setTitle(label);

		String value = (String) mCalendarList.getValue();
		if (!"none".equals(value)) {
			try {
				Long.parseLong(value);
				mCalendarList.setSummary(mCalendarList.getEntry());
			} catch (NumberFormatException exception) {
			}
		}

		ListPreference listPref = (ListPreference) findPreference("reminderType");
		value = (String) listPref.getEntry();
		listPref.setSummary(value);

		listPref = (ListPreference) findPreference("reminderBefore");
		value = (String) listPref.getEntry();
		listPref.setSummary(value);
	}

	/**
	 * Get the application theme label.
	 *
	 * @return The application theme label.
	 */
	private String getSelectedThemeLabel() {
		String[] labels = MainApplication.getAppContext().getResources().
				getStringArray(R.array.app_theme_labels);
		int themeId = mApplication.getApplicationTheme();
		if (R.style.AppThemeDark == themeId) {
			return labels[1];
		}
		return labels[0];
	}

	/**
	 * This method is invoked when a preference is changed
	 * 
	 * @param sp
	 *            The shared preference
	 * @param key
	 *            Key of changed preference
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		// Update summary value
		Preference obj = findPreference(key);
		if (MainApplication.KEY_APP_THEME.equals(key)) {
			showRestartActivityMessage();
			prepareSummaries();
		} else if (obj instanceof EditTextPreference) {
			EditTextPreference pref = (EditTextPreference) obj;
			pref.setSummary(sp.getString(key, pref.getText()));
		} else if (obj instanceof ListPreference) {
			ListPreference pref = (ListPreference) obj;
			pref.setSummary((String) pref.getEntry());
		}
	}

	/**
	 * Show to the user an alert message.
	 */
	private void showRestartActivityMessage() {
		new AlertDialog.Builder(mActivity)
				.setTitle(R.string.app_name)
				.setMessage(R.string.must_restart_application)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,
										int whichButton) {
						restartApplication();
					}
				}).show();
	}

	/**
	 * Mark the application to be restarted.
	 */
	private void restartApplication() {
		Intent intent = mActivity.getIntent();
		mActivity.finish();
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/**
	 * Persist into preferences the full file and path of saved or loaded
	 * preferences
	 * 
	 * @param backupPath
	 *            The full file and path of saved or loaded preferences
	 */
	private void storeBackupPath(String backupPath) {
		mApplication.getApplicationPreferences().setBackupPath(backupPath);
	}

	/**
	 * Obtain the full file and path of saved or loaded preferences
	 * 
	 * @return The full file and path of saved or loaded preferences
	 */
	private String getBackupPath() {
		return mApplication.getApplicationPreferences().getBackupPath();
	}

	/**
	 * Method invoked when is pressed the positive (OK) button from a preference
	 * edit dialog
	 * 
	 * @param resultId
	 *            The id of pressed preference: PREF_RESTORE or PREF_BACKUP
	 * @param value
	 *            The full file and path of saved or loaded preferences
	 */
	@Override
	public void onPositiveResult(int resultId, String value) {
		if (resultId > 0) {
			storeBackupPath(value);
		}
		if (resultId == PREF_RESTORE) {
			preferencesBackup.setText(value);
			onRestorePreferences(value);
		} else if (resultId == PREF_BACKUP) {
			preferencesRestore.setText(value);
			onBackupPreferences(value);
		}
	}

	/**
	 * Method invoked when is pressed the negative (Cancel) button from a
	 * preference edit dialog
	 * 
	 * @param resultId
	 *            The id of pressed preference: PREF_RESTORE or PREF_BACKUP
	 * @param value
	 *            The full file and path of saved or loaded preferences
	 */
	@Override
	public void onNegativeResult(int resultId, String value) {

	}

	/**
	 * Method invoked when is pressed the restore preference This will launch a
	 * PreferencesFileUtilAsynkTask task to import preferences
	 * 
	 * @param backupPath
	 *            The full file and path from where should be loaded preferences
	 */
	private void onRestorePreferences(String backupPath) {
		new PreferencesFileUtilAsynkTask(this, backupPath,
				PreferencesFileUtilAsynkTask.Operation.IMPORT).execute();
	}

	/**
	 * Method invoked when is pressed the back-up preference This will launch
	 * PreferencesFileUtilAsynkTask task to export preferences
	 * 
	 * @param backupPath
	 *            The full file and path where should be stored preferences
	 */
	private void onBackupPreferences(String backupPath) {
		new PreferencesFileUtilAsynkTask(this, backupPath,
				PreferencesFileUtilAsynkTask.Operation.EXPORT).execute();
	}

	/**
	 * Obtain main application.
	 * 
	 * @return Main application.
	 */
	@Override
	public Application getApplication() {
		return mApplication;
	}

	/**
	 * Method invoked when is started PreferencesFileUtilAsynkTask task
	 * 
	 * @param operationType
	 *            The operation type: import or export
	 */
	@Override
	public void startFileAsynkTask(
			PreferencesFileUtilAsynkTask.Operation operationType) {
		if (operationType == PreferencesFileUtilAsynkTask.Operation.IMPORT) {
			mApplication.showProgressDialog(mActivity, R.string.import_started);
		} else {
			mApplication.showProgressDialog(mActivity, R.string.export_started);
		}
	}

	/**
	 * Method invoked when is ended PreferencesFileUtilAsynkTask task
	 * 
	 * @param operationType
	 *            The operation type: import or export
	 * @param result
	 *            The process result
	 */
	@Override
	public void endFileAsynkTask(
			PreferencesFileUtilAsynkTask.Operation operationType,
			DefaultAsyncTaskResult result) {
		mApplication.hideProgressDialog();
		if (result.resultId == Constants.OK) {
			mApplication.showMessageInfo(mActivity, result.resultMessage);
			if (operationType == PreferencesFileUtilAsynkTask.Operation.IMPORT) {
				// restartPreferencesActivity();
			}
		} else {
			showMessageError(R.string.attention, result.resultMessage);
		}
	}

	/**
	 * Method invoked to show a dialog to confirm preferences reset
	 * 
	 * @return Always true
	 */
	private boolean onPreferencesReset() {
		new AlertDialog.Builder(mActivity)
				.setTitle(R.string.app_name)
				.setMessage(R.string.reset_settings_question)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int whichButton) {
								doPreferencesReset();
							}
						}).setNegativeButton(R.string.no, null).show();
		return true;
	}

	/**
	 * Method invoked to reset the preferences to default values
	 */
	private void doPreferencesReset() {
		// Get this application SharedPreferences editor
		SharedPreferences prefs = mApplication.getApplicationPreferences()
				.getSharedPreferences();
		SharedPreferences.Editor preferencesEditor = prefs.edit();
		// Clear all the saved preference values.
		preferencesEditor.clear();
		// Commit all changes.
		preferencesEditor.commit();
		// Read the default values and set them as the current values.
		mApplication.getApplicationPreferences().setDefaultValues(
				R.xml.settings_preferences, true);

		// restartPreferencesActivity();
	}

	/**
	 * Show to the user an error dialog message.
	 * 
	 * @param titleStringId
	 *            The resource string id used for the dialog title.
	 * @param message
	 *            The message from dialog text.
	 */
	protected void showMessageError(int titleStringId, String message) {
		new AlertDialog.Builder(mActivity)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getString(titleStringId))
				.setMessage(message)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) {
								// ignored
							}
						}).show();
	}

	/**
	 * Method invoked when was pressed the request permission preference.
	 */
	private void onRequestPermissions() {
		new AlertDialog.Builder(mActivity)
				.setTitle(R.string.app_name)
				.setMessage(R.string.request_permissions_confirmation)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
												int whichButton) {
								doRequestPermissions();
							}
						}).setNegativeButton(R.string.no, null).show();
	}

	/**
	 * Check if is necessary to request permissions.
	 */
	private void doRequestPermissions() {
		String[] permissions = mApplication.getNotGrantedPermissions();
		if (Utilities.isEmpty(permissions)) {
			new AlertDialog.Builder(mActivity)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.app_name)
					.setMessage(R.string.request_permissions_ok)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int which) {
									// ignored
								}
							}).show();
		} else {
			mActivity.requestForPermissions(permissions);
		}
	}
}
