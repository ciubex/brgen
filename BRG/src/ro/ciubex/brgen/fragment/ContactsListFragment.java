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
import ro.ciubex.brgen.model.Constants;
import ro.ciubex.brgen.tasks.BirthdaysLoaderAsyncTask;
import ro.ciubex.brgen.tasks.ContactImageLoaderAsyncTask;
import ro.ciubex.brgen.tasks.DefaultAsyncTaskResult;
import ro.ciubex.brgen.tasks.LoadContactsAsyncTask;
import ro.ciubex.brgen.tasks.SyncRemindersAsyncTask;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class ContactsListFragment extends ContactsListBaseFragment implements
		LoadContactsAsyncTask.Responder, SyncRemindersAsyncTask.Responder {

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
		super.initFragment();
		initFilterBox();
		initListView();
		prepareCheckedAll();
		if (mApplication.isContactsLoaded()) {
			preparePhoneContactsList();
		} else {
			loadContactListView();
		}
	}

	@Override
	protected void initListView() {
		super.initListView();
		mListView
				.setEmptyView(mFragmentView.findViewById(R.id.empty_list_view));
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
		mAdapter = new ContactListAdapter(mApplication, this,
				mApplication.getContacts(), mApplication.getDefaultLocale());
		mListView.setAdapter(mAdapter);
		mListView.setFastScrollEnabled(mApplication.getApplicationPreferences()
				.isEnabledFastScroll());
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
		case CONFIRMATION_SYNC_REMINDERS:
			new SyncRemindersAsyncTask(this, mApplication, mAdapter).execute();
			break;
		default:
			super.onConfirmationOk(confirmationId, anObject);
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

	@Override
	protected void backFromContactEditor() {
		reloadContactList();
	}
}
