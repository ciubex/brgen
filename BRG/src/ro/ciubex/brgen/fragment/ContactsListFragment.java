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

import ro.ciubex.brgen.R;
import ro.ciubex.brgen.adapter.ContactListAdapter;
import ro.ciubex.brgen.list.ContactListView;
import ro.ciubex.brgen.model.Constants;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.tasks.BirthdayRemoveAsyncTask;
import ro.ciubex.brgen.tasks.BirthdaysLoaderAsyncTask;
import ro.ciubex.brgen.tasks.DefaultAsyncTaskResult;
import ro.ciubex.brgen.tasks.GenerateRemindersAsyncTask;
import ro.ciubex.brgen.tasks.LoadContactsAsyncTask;
import ro.ciubex.brgen.tasks.SyncRemindersAsyncTask;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class ContactsListFragment extends BaseFragment implements
		LoadContactsAsyncTask.Responder, BirthdayRemoveAsyncTask.Responder,
		GenerateRemindersAsyncTask.Responder, SyncRemindersAsyncTask.Responder {
	private EditText mFilterBox = null;
	private CheckedTextView mCheckedAll;
	private ListView mListView = null;
	private ContactListAdapter mAdapter;
	private DatePickerDialogFragment mDatePickerDlg;

	private static final int REQUEST_CODE_CONTACT_EDITOR = 1;
	private static final int CONFIRMATION_REMOVE_BIRTHDAY = 0;
	private static final int CONFIRMATION_GEN_REMINDERS = 1;
	private static final int CONFIRMATION_SYNC_REMINDERS = 2;

	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_contacts_list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ro.ciubex.brgen.fragment.BaseFragment#initFragment()
	 */
	@Override
	protected void initFragment() {
		mDatePickerDlg = new DatePickerDialogFragment();
		mDatePickerDlg.setMainApplication(mApplication);
		initFilterBox();
		initListView();
		prepareCheckedAll();
		if (mApplication.isContactsLoaded()) {
			preparePhoneContactsList();
		} else {
			loadContactListView();
		}
	}

	/**
	 * Method used to initialize the filter box.
	 */
	private void initFilterBox() {
		mFilterBox = (EditText) mFragmentView
				.findViewById(R.id.contacts_filter_box);
		mFilterBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				applyFilter(s);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	/**
	 * Method used to initialize the list view.
	 */
	private void initListView() {
		mListView = (ContactListView) mFragmentView
				.findViewById(R.id.contacts_list);
		mListView
				.setEmptyView(mFragmentView.findViewById(R.id.empty_list_view));
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				if (position > -1 && position < mAdapter.getCount()) {
					Contact contact = mAdapter.getItem(position);
					if (contact != null && contact.haveBirthday()) {
						contact.setChecked(!contact.isChecked());
						mAdapter.notifyDataSetChanged();
					}
				}
			}
		});
		mListView.setLongClickable(true);
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {
				boolean isProcessed = false;
				if (position > -1 && position < mAdapter.getCount()) {
					Contact contact = mAdapter.getItem(position);
					if (contact != null) {
						isProcessed = true;
						showItemDialogMenu(position);
					}
				}
				return isProcessed;
			}
		});
	}

	/**
	 * Prepare Select/unselect all checkbox
	 */
	private void prepareCheckedAll() {
		mCheckedAll = (CheckedTextView) mFragmentView
				.findViewById(R.id.check_box_all);
		mCheckedAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				clickOnCheckedAll((CheckedTextView) view);
			}
		});
	}

	/**
	 * Method invoked when the Select/unselect all check box is clicked Also on
	 * this method is changed the label for the check box
	 * 
	 * @param checkView
	 *            The check box control
	 */
	private void clickOnCheckedAll(CheckedTextView checkView) {
		boolean flag = toggleCheckedTextView(checkView);
		checkView.setText(flag ? R.string.unselect_all : R.string.select_all);
		toggleSelectedItems(flag);
	}

	/**
	 * This is a generic method used to invert the checking state of a check box
	 * 
	 * @param checkView
	 *            The check box control for which should be changed the state.
	 * @return The new boolean state of check box
	 */
	private boolean toggleCheckedTextView(CheckedTextView checkView) {
		boolean checked = !checkView.isChecked();
		checkCheckedTextView(checkView, checked);
		return checked;
	}

	/**
	 * This is a generic method used to make a check box to be checked or
	 * unchecked
	 * 
	 * @param checkView
	 *            The check box control for which should be changed the state.
	 * @param flag
	 *            The new state of the check box.
	 * @return The new boolean state of check box
	 */
	private boolean checkCheckedTextView(CheckedTextView checkView, boolean flag) {
		checkView.setChecked(flag);
		return flag;
	}

	/**
	 * This method is used to check or uncheck all selected contacts and its
	 * corresponded item view from main list
	 * 
	 * @param flag
	 *            The boolean state of the items
	 */
	private void toggleSelectedItems(boolean flag) {
		for (int position = 0; position < mAdapter.getCount(); position++) {
			Contact contact = mAdapter.getItem(position);
			if (contact != null && contact.haveBirthday()) {
				contact.setChecked(flag);
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * This method is invoked when the filter is edited
	 * 
	 * @param charSequence
	 *            The char sequence from the filter
	 */
	private void applyFilter(CharSequence charSequence) {
		mApplication.showProgressDialog(mActivity, R.string.filtering);
		mAdapter.getFilter().filter(charSequence);
		mApplication.hideProgressDialog();
	}

	public void reloadContactList() {
		loadContactListView();
	}

	/**
	 * Prepare thread used to load the contacts to the list view.
	 */
	private void loadContactListView() {
		new LoadContactsAsyncTask(this, mApplication.getContacts()).execute();
	}

	/**
	 * 
	 */
	@Override
	public void startLoadContacts() {
		mApplication.showProgressDialog(mActivity, R.string.please_wait);
	}

	/**
	 * 
	 */
	@Override
	public void endLoadContacts(DefaultAsyncTaskResult result) {
		preparePhoneContactsList();
		mApplication.setContactsLoaded(true);
		mApplication.hideProgressDialog();
		if (Constants.OK == result.resultId) {
			mApplication.showMessageInfo(mActivity, result.resultMessage);
			new BirthdaysLoaderAsyncTask(mApplication, mAdapter).execute();
		} else {
			showMessageDialog(R.string.information, result.resultMessage, 0,
					null);
		}
	}

	/**
	 * Method used to create the list view.
	 */
	private void preparePhoneContactsList() {
		mAdapter = new ContactListAdapter(mApplication,
				mApplication.getContacts(), mApplication.getDefaultLocale());
		mListView.removeAllViewsInLayout();
		mListView.setAdapter(mAdapter);
		mListView.invalidateViews();
		mListView.scrollBy(0, 0);
		mListView.setFastScrollEnabled(mApplication.getApplicationPreferences()
				.isEnabledFastScroll());
	}

	@Override
	public Application getApplication() {
		return mApplication;
	}

	/**
	 * This method show the popup menu when the user do a long click on a list
	 * item
	 * 
	 * @param contactPosition
	 *            The contact position where was made the long click
	 */
	private void showItemDialogMenu(final int contactPosition) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.contact_item_title);
		builder.setItems(R.array.contact_item,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							editContactBirthday(contactPosition);
							break;
						case 1:
							removeContactBirthday(contactPosition);
							break;
						case 2:
							editContact(contactPosition);
							break;
						case 3:
							regenerateReminder(contactPosition);
							break;
						}
					}
				});
		builder.create().show();
	}

	/**
	 * Edit the contact birthday.
	 * 
	 * @param contactPosition
	 *            The contact position on the list.
	 */
	private void editContactBirthday(int contactPosition) {
		Contact contact = mAdapter.getItem(contactPosition);
		mDatePickerDlg.setContact(contact);
		mDatePickerDlg.setAdapter(mAdapter);
		mDatePickerDlg.show(getFragmentManager().beginTransaction(),
				"date_dialog");
	}

	/**
	 * Remove the contact birthday.
	 * 
	 * @param contactPosition
	 *            The contact position on the list.
	 */
	private void removeContactBirthday(int contactPosition) {
		Contact contact = mAdapter.getItem(contactPosition);
		if (contact != null) {
			if (contact.haveBirthday()) {
				showRemoveContactBirthdayConfirmation(contact);
			} else {
				mApplication.showMessageError(mActivity,
						R.string.no_birthday_for, contact.getContactName());
			}
		}
	}

	/**
	 * Start to edit the contact associated on the specified position.
	 * 
	 * @param contactPosition
	 *            The contact position on the list.
	 */
	private void editContact(int contactPosition) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Contact contact = mAdapter.getItem(contactPosition);
		if (contact != null) {
			Uri lookUpUri = Uri
					.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, ""
							+ contact.getId());
			intent.setData(lookUpUri);
			startActivityForResult(intent, REQUEST_CODE_CONTACT_EDITOR);
		}
	}

	/**
	 * (Re)Generate the reminder for selected contact.
	 * 
	 * @param contactPosition
	 */
	private void regenerateReminder(int contactPosition) {
		Contact contact = mAdapter.getItem(contactPosition);
		if (contact != null) {
			if (contact.haveBirthday()) {
				contact.setChecked(true);
				new GenerateRemindersAsyncTask(this, mApplication, contact)
						.execute();
			} else {
				mApplication.showMessageError(mActivity,
						R.string.no_birthday_for, contact.getContactName());
			}
		}
	}

	/**
	 * Method invoked when is started the generate reminders thread
	 */
	@Override
	public void startGenerateReminders() {
		mApplication.showProgressDialog(mActivity, R.string.generate_reminders);
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
			mApplication.showMessageInfo(mActivity, result.resultMessage);
		} else {
			mApplication.showMessageError(mActivity, result.resultMessage);
		}
	}

	/**
	 * This method is invoked when a child activity is finished and this
	 * activity is showed again
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_CONTACT_EDITOR) {
			loadContactListView();
		}
	}

	/**
	 * Show a confirmation dialog of removing birthday information from selected
	 * contact
	 * 
	 * @param contact
	 *            Selected contact
	 */
	private void showRemoveContactBirthdayConfirmation(final Contact contact) {
		showConfirmationDialog(R.string.remove_contact_birthday_title,
				mApplication.getString(
						R.string.remove_contact_birthday_question,
						contact.getContactName()),
				CONFIRMATION_REMOVE_BIRTHDAY, contact);
	}

	/**
	 * Method invoked when the used click on the OK confirmation dialog message.
	 * 
	 * @param confirmationId
	 *            The ID of the message to be identified on the caller activity.
	 * @param anObject
	 *            The object used by the caller activity.
	 */
	@Override
	protected void onConfirmationOk(int confirmationId, Object anObject) {
		switch (confirmationId) {
		case CONFIRMATION_REMOVE_BIRTHDAY:
			doRemoveContactBirthday((Contact) anObject);
			break;
		case CONFIRMATION_GEN_REMINDERS:
			new GenerateRemindersAsyncTask(this, mApplication,
					mApplication.getContactsAsArray()).execute();
			break;
		case CONFIRMATION_SYNC_REMINDERS:
			new SyncRemindersAsyncTask(this, mApplication, mAdapter).execute();
			break;
		}
	}

	/**
	 * Create the thread used to remove contact birthday
	 * 
	 * @param contact
	 *            Selected contact from which should be removed the birthday
	 *            information
	 */
	private void doRemoveContactBirthday(Contact contact) {
		new BirthdayRemoveAsyncTask(mApplication, this, contact).execute();
	}

	/**
	 * Method invoked at the end of birthday removing.
	 */
	@Override
	public void removeProcessResult(boolean result, Contact contact) {
		if (result) {
			mAdapter.notifyDataSetChanged();
		} else {
			showMessageDialog(R.string.error_occurred, mApplication.getString(
					R.string.contact_birthday_not_removed,
					contact.getContactName()), 0, null);
		}
	}

	/**
	 * Save reminders for selected contacts.
	 */
	public void saveReminders() {
		if (mApplication.getApplicationPreferences().haveCalendarSelected()) {
			showConfirmationDialog(R.string.generate_reminders_title,
					mApplication
							.getString(R.string.generate_reminders_question),
					CONFIRMATION_GEN_REMINDERS, null);
		} else {
			mApplication
					.showMessageError(mActivity, R.string.select_a_calendar);
		}
	}

	/**
	 * Synchronize local contacts with stored reminders.
	 */
	public void syncReminders() {
		if (mApplication.getContacts().isEmpty()) {
			mApplication.showMessageError(mActivity, R.string.no_contacts);
		} else {
			showConfirmationDialog(
					R.string.sync_confirmation_title,
					mApplication.getString(R.string.sync_confirmation_question),
					CONFIRMATION_SYNC_REMINDERS, null);
		}
	}

	/**
	 * Method invoked when the synchronization is started.
	 */
	@Override
	public void startSyncReminders() {
		mApplication.showProgressDialog(mActivity, R.string.sync_in_progress);
	}

	/**
	 * Method invoked when the synchronization is ended.
	 */
	@Override
	public void endSyncReminders(DefaultAsyncTaskResult result) {
		if (Constants.OK == result.resultId) {
			mAdapter.notifyDataSetChanged();
		}
		mApplication.hideProgressDialog();
		if (result.resultMessage != null) {
			mApplication.showMessageInfo(mActivity, result.resultMessage);
		}
	}
}
