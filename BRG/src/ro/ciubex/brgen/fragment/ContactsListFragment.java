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
import ro.ciubex.brgen.adapter.OnListItemClickListener;
import ro.ciubex.brgen.adapter.StickyListAdapter;
import ro.ciubex.brgen.model.Constants;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.tasks.BirthdayRemoveAsyncTask;
import ro.ciubex.brgen.tasks.BirthdaysLoaderAsyncTask;
import ro.ciubex.brgen.tasks.ContactImageLoaderAsyncTask;
import ro.ciubex.brgen.tasks.DefaultAsyncTaskResult;
import ro.ciubex.brgen.tasks.UpdateRemindersAsyncTask;
import ro.ciubex.brgen.tasks.LoadContactsAsyncTask;
import ro.ciubex.brgen.tasks.LoadPhonesContactAsyncTask;
import ro.ciubex.brgen.tasks.SyncRemindersAsyncTask;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.EditText;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class ContactsListFragment extends BaseFragment implements
		LoadContactsAsyncTask.Responder, BirthdayRemoveAsyncTask.Responder,
		UpdateRemindersAsyncTask.Responder, SyncRemindersAsyncTask.Responder,
		LoadPhonesContactAsyncTask.Responder, OnListItemClickListener {
	private final static String TAG = ContactsListFragment.class.getName();
	private EditText mFilterBox = null;
	private CheckedTextView mCheckedAll;
	private StickyListHeadersListView mListView = null;
	private StickyListAdapter mAdapter;
	private DatePickerDialogFragment mDatePickerDlg;

	private static final int REQUEST_CODE_CONTACT_EDITOR = 1;
	private static final int CONFIRMATION_REMOVE_BIRTHDAY = 0;
	private static final int CONFIRMATION_UPDATE_REMINDERS = 1;
	private static final int CONFIRMATION_SYNC_REMINDERS = 2;
	private static final int SELECT_CALL_CONTACT = 0;
	private static final int SELECT_SEND_SMS = 1;

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
		mDatePickerDlg.setActivity(mActivity);
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
		mListView = (StickyListHeadersListView) mFragmentView
				.findViewById(R.id.contacts_list);
		mListView
				.setEmptyView(mFragmentView.findViewById(R.id.empty_list_view));

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				final Contact contact = mAdapter.getItem(position);
				if (contact != null) {
					showItemDialogMenu(contact.getContactName(), contact
							.haveBirthday() ? R.array.contact_item
							: R.array.contact_item_no_birthday, position);
				}
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
			new ContactImageLoaderAsyncTask(mApplication, mAdapter).execute();
		} else {
			showMessageError(R.string.error_occurred, result.resultMessage);
		}
	}

	/**
	 * Method used to create the list view.
	 */
	private void preparePhoneContactsList() {
		mAdapter = new StickyListAdapter(mApplication, this,
				mApplication.getContacts(), mApplication.getDefaultLocale());
		mListView.setAdapter(mAdapter);
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
	 * @param title
	 *            The title of the popup menu.
	 * @param itemsId
	 *            The menu resource id to be displayed.
	 * @param contactPosition
	 *            The contact position where was made the long click
	 */
	private void showItemDialogMenu(String title, final int itemsId,
			final int contactPosition) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(title);
		builder.setItems(itemsId, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				onItemDialogMenuClick(itemsId, contactPosition, which);
			}
		});
		builder.create().show();
	}

	/**
	 * Based on the chosen above menu item call proper action.
	 * 
	 * @param itemsId
	 *            The menu resource id.
	 * @param contactPosition
	 *            The contact position.
	 * @param which
	 *            The menu item position.
	 */
	private void onItemDialogMenuClick(int itemsId, int contactPosition,
			int which) {
		switch (itemsId) {
		case R.array.contact_item:
			onClickItemWithBirthday(contactPosition, which);
			break;
		case R.array.contact_item_no_birthday:
			onClickItemNoBirthday(contactPosition, which);
			break;
		case R.array.contact_picture:
			onClickItemPicture(contactPosition, which);
			break;
		}
	}

	/**
	 * Click on a contact with birthday information.
	 * 
	 * @param contactPosition
	 *            The contact position.
	 * @param which
	 *            Which selected menu item.
	 */
	private void onClickItemWithBirthday(int contactPosition, int which) {
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
			updateReminder(contactPosition);
			break;
		case 4:
			removeReminder(contactPosition);
			break;
		}
	}

	/**
	 * Click on a contact without birthday information.
	 * 
	 * @param contactPosition
	 *            The contact position.
	 * @param which
	 *            Which selected menu item.
	 */
	private void onClickItemNoBirthday(int contactPosition, int which) {
		switch (which) {
		case 0:
			editContactBirthday(contactPosition);
			break;
		case 1:
			editContact(contactPosition);
			break;
		}
	}

	/**
	 * Click on a contact picture.
	 * 
	 * @param contactPosition
	 *            The contact position.
	 * @param which
	 *            Which selected menu item.
	 */
	private void onClickItemPicture(int contactPosition, int which) {
		switch (which) {
		case 0:
			onSelectContact(contactPosition, SELECT_CALL_CONTACT);
			break;
		case 1:
			onSelectContact(contactPosition, SELECT_SEND_SMS);
			break;
		}
	}

	/**
	 * Edit the contact birthday.
	 * 
	 * @param contactPosition
	 *            The contact position on the list.
	 */
	private void editContactBirthday(int contactPosition) {
		Contact contact = mAdapter.getItem(contactPosition);
		if (contact != null) {
			mDatePickerDlg.setContact(contact);
			mDatePickerDlg.setAdapter(mAdapter);
			mDatePickerDlg.show(getFragmentManager().beginTransaction(),
					"date_dialog");
		}
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
				showConfirmationDialog(R.string.remove_contact_birthday_title,
						mApplication.getString(
								R.string.remove_contact_birthday_question,
								contact.getContactName()),
						CONFIRMATION_REMOVE_BIRTHDAY, contact);
			} else {
				showMessageError(
						R.string.attention,
						getString(R.string.no_birthday_for,
								contact.getContactName()));
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
	 * Update the reminder for selected contact.
	 * 
	 * @param contactPosition
	 */
	private void updateReminder(int contactPosition) {
		Contact contact = mAdapter.getItem(contactPosition);
		if (contact != null) {
			if (contact.haveBirthday()) {
				if (mApplication.getApplicationPreferences()
						.haveCalendarSelected()) {
					new UpdateRemindersAsyncTask(this, mApplication,
							mAdapter, contact).execute();
				} else {
					showMessageError(R.string.attention,
							R.string.select_a_calendar);
				}
			} else {
				showMessageError(
						R.string.attention,
						getString(R.string.no_birthday_for,
								contact.getContactName()));
			}
		}
	}

	/**
	 * Method used to remove generated reminder for the selected contact.
	 * 
	 * @param contactPosition
	 *            Position of selected contact.
	 */
	private void removeReminder(int contactPosition) {
		Contact contact = mAdapter.getItem(contactPosition);
		if (contact != null) {
			if (mApplication.getApplicationPreferences().haveCalendarSelected()) {
				contact.setChecked(false);
				new UpdateRemindersAsyncTask(this, mApplication, mAdapter,
						contact).execute();
			} else {
				showMessageError(R.string.attention, R.string.select_a_calendar);
			}
		}
	}

	/**
	 * Method invoked when is started the update reminders thread
	 */
	@Override
	public void startUpdateReminders() {
		mApplication.showProgressDialog(mActivity, R.string.update_reminders);
	}

	/**
	 * Method invoked at the end of update reminders thread
	 * 
	 * @param result
	 *            The process result
	 */
	@Override
	public void endUpdateReminders(DefaultAsyncTaskResult result) {
		mApplication.hideProgressDialog();
		if (Constants.OK == result.resultId) {
			mApplication.showMessageInfo(mActivity, result.resultMessage);
		} else {
			showMessageError(R.string.attention, result.resultMessage);
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
		case CONFIRMATION_UPDATE_REMINDERS:
			new UpdateRemindersAsyncTask(this, mApplication, mAdapter,
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
			showMessageError(R.string.error_occurred, mApplication.getString(
					R.string.contact_birthday_not_removed,
					contact.getContactName()));
		}
	}

	/**
	 * Update reminders for all contacts.
	 */
	public void updateAllReminders() {
		if (mApplication.getApplicationPreferences().haveCalendarSelected()) {
			showConfirmationDialog(R.string.update_reminders_title,
					mApplication
							.getString(R.string.update_reminders_question),
					CONFIRMATION_UPDATE_REMINDERS, null);
		} else {
			showMessageError(R.string.attention, R.string.select_a_calendar);
		}
	}

	/**
	 * Synchronize local contacts with stored reminders.
	 */
	public void syncReminders() {
		if (mApplication.getContacts().isEmpty()) {
			showMessageError(R.string.attention, R.string.no_contacts);
		} else if (mApplication.getApplicationPreferences()
				.haveCalendarSelected()) {
			showConfirmationDialog(
					R.string.sync_confirmation_title,
					mApplication.getString(R.string.sync_confirmation_question),
					CONFIRMATION_SYNC_REMINDERS, null);
		} else {
			showMessageError(R.string.attention, R.string.select_a_calendar);
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

	/**
	 * Method invoked when the used chose the "Call" or "Send SMS" menu item.
	 * 
	 * @param contactPosition
	 *            The contact position.
	 * @param taskId
	 *            The ID of the task, call or send SMS.
	 */
	private void onSelectContact(int contactPosition, int taskId) {
		Contact contact = (Contact) mAdapter.getItem(contactPosition);
		if (contact != null) {
			if (contact.havePhoneNumbers()) {
				onSelectContact(contact, taskId);
			} else {
				new LoadPhonesContactAsyncTask(this, contact, taskId).execute();
			}
		}
	}

	/**
	 * Method used to prepare the contact and the number for the action: SMS or
	 * Call.
	 * 
	 * @param contact
	 *            The chosen contact.
	 * @param taskId
	 *            The task ID: SMS or Call.
	 */
	private void onSelectContact(Contact contact, int taskId) {
		String[] arr = contact.getPhoneNumbers();
		if (arr.length == 1) {
			onSelectContact(contact, arr[0], taskId);
		} else {
			choseContactPhone(contact, taskId);
		}
	}

	/**
	 * This method will display a list of phone numbers for chosen contact.
	 * 
	 * @param contact
	 *            The chosen contact.
	 * @param taskId
	 *            The task ID: SMS or Call.
	 */
	private void choseContactPhone(final Contact contact, final int taskId) {
		new AlertDialog.Builder(mActivity)
				.setTitle(R.string.phone_select)
				.setItems(contact.getPhoneNumbers(),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								onSelectContact(contact,
										contact.getPhoneNumbers()[which],
										taskId);
							}
						}).create().show();
	}

	/**
	 * Method used to launch proper action based on the task id.
	 * 
	 * @param contact
	 *            The chosen contact.
	 * @param phoneNumber
	 *            The phone number.
	 * @param taskId
	 *            Task ID: SMS or Call.
	 */
	private void onSelectContact(Contact contact, String phoneNumber, int taskId) {
		switch (taskId) {
		case SELECT_CALL_CONTACT:
			doContactCall(contact, phoneNumber);
			break;
		case SELECT_SEND_SMS:
			doContactSendSMS(contact, phoneNumber);
			break;
		}
	}

	/**
	 * Call a contact using provided number.
	 * 
	 * @param contact
	 *            The contact to be calling.
	 * @param number
	 *            The contact phone number.
	 */
	private void doContactCall(Contact contact, String number) {
		Uri uri = Uri.parse("tel:" + number);
		try {
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(uri);
			startActivity(intent);
		} catch (Exception e) {
			showMessageError(R.string.error_occurred,
					getString(R.string.phone_error_call, number, e));
			Log.e(TAG, e.getMessage(), e);
		}
	}

	/**
	 * Send a SMS message to the selected contact.
	 * 
	 * @param contact
	 *            The selected contact.
	 * @param number
	 *            The number of selected contact.
	 */
	private void doContactSendSMS(Contact contact, String number) {
		Uri uri = Uri.parse("sms:" + number);
		String message = mApplication.getApplicationPreferences()
				.getSMSMessage(contact.getContactName());
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setType("text/plain");
			intent.setData(uri);
			intent.putExtra(Intent.EXTRA_TEXT, message);
			intent.putExtra("sms_body", message);
			startActivity(intent);
		} catch (Exception e) {
			showMessageError(R.string.error_occurred,
					getString(R.string.phone_error_sms, number, e));
			Log.e(TAG, e.getMessage(), e);
		}
	}

	/**
	 * Method invoked when the phone contact loader is started.
	 */
	@Override
	public void startLoadPhonesContact() {
		mApplication.showProgressDialog(mActivity, R.string.loading_wait);
	}

	/**
	 * Method invoked at the end of the phone contact loader.
	 */
	@Override
	public void endLoadPhonesContact(DefaultAsyncTaskResult result) {
		mApplication.hideProgressDialog();
		if (result.resultId != Constants.OK && result.resultMessage != null) {
			mApplication.showMessageInfo(mActivity, result.resultMessage);
		} else {
			Contact contact = (Contact) result.object;
			onSelectContact(contact, result.taskId);
		}
	}

	/**
	 * Method invoked when the contact image is clicked.
	 */
	@Override
	public void onContactImageClick(int position) {
		Contact contact = (Contact) mAdapter.getItem(position);
		if (contact != null) {
			showItemDialogMenu(contact.getContactName(),
					R.array.contact_picture, position);
		}
	}

	/**
	 * Method invoked when the check box image is clicked.
	 */
	@Override
	public void onCheckBoxClick(int position) {
		Contact contact = (Contact) mAdapter.getItem(position);
		if (contact != null && contact.haveBirthday()) {
			contact.setChecked(!contact.isChecked());
			mAdapter.notifyDataSetChanged();
		}
	}
}
