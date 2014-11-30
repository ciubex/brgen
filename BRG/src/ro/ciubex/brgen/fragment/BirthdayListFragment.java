/**
 * 
 */
package ro.ciubex.brgen.fragment;

import ro.ciubex.brgen.MainActivity;
import ro.ciubex.brgen.R;
import ro.ciubex.brgen.adapter.BirthdayListAdapter;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.tasks.BirthdaySortAsyncTask;
import android.content.Intent;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class BirthdayListFragment extends ContactsListBaseFragment implements
		BirthdaySortAsyncTask.SortListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see ro.ciubex.brgen.fragment.BaseFragment#getFragmentResourceId()
	 */
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
		startBirthdaySortAsyncTask();
	}

	@Override
	protected void initListView() {
		super.initListView();
		mListView.setEmptyView(mFragmentView
				.findViewById(R.id.empty_bithday_list_view));
	}

	/**
	 * Start birthday sort asynchronous task.
	 */
	public void startBirthdaySortAsyncTask() {
		new BirthdaySortAsyncTask(mApplication, this).execute();
	}

	/**
	 * Method invoked when is started birthday sort asynchronous task.
	 */
	@Override
	public void sortStarted() {
		mApplication.showProgressDialog(mActivity, R.string.please_wait);
	}

	/**
	 * Method invoked when is finished birthday sort asynchronous task.
	 */
	@Override
	public void sortFinished() {
		initBirthdaysList();
		mApplication.hideProgressDialog();
		checkFilter();
	}

	/**
	 * Initialize the birthday list view.
	 */
	private void initBirthdaysList() {
		mAdapter = new BirthdayListAdapter(mApplication, this,
				mApplication.getBirthdays(), mApplication.getDefaultLocale());
		mListView.setAdapter(mAdapter);
		mListView.setFastScrollEnabled(mApplication.getApplicationPreferences()
				.isEnabledFastScroll());
	}

	/**
	 * This method is invoked when a child activity is finished and this
	 * activity is showed again
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_CONTACT_EDITOR) {
			startBirthdaySortAsyncTask();
		}
	}

	/**
	 * Invoked when a contact date is changed.
	 */
	@Override
	public void onDateChange(boolean modified, Contact contact) {
		startBirthdaySortAsyncTask();
	}

	/**
	 * Method invoked at the end of birthday removing.
	 */
	@Override
	public void removeProcessResult(boolean result, Contact contact) {
		super.removeProcessResult(result, contact);
		if (result) {
			startBirthdaySortAsyncTask();
		}
	}

	@Override
	protected void backFromContactEditor() {
		mActivity.displayView(MainActivity.FRG_CNT_LIST,
				MainActivity.FORCE_RELOAD_CONTACTS);
	}
}
