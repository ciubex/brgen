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

import ro.ciubex.brgen.MainActivity;
import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.R;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public abstract class BaseFragment extends Fragment {
	public static final int NO_CONTENT_ID = -1;
	protected MainApplication mApplication;
	protected MainActivity mActivity;
	protected View mFragmentView;
	protected int mContentId;
	protected DatePickerDialogFragment mDatePickerDlg;

	public BaseFragment() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (MainActivity) getActivity();
		mApplication = (MainApplication) mActivity.getApplication();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mFragmentView = inflater.inflate(getFragmentResourceId(), container,
				false);
		return mFragmentView;
	}

	protected abstract int getFragmentResourceId();

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initFragment();
	}

	protected void initFragment() {
		mDatePickerDlg = new DatePickerDialogFragment();
		mDatePickerDlg.setMainApplication(mApplication);
		mDatePickerDlg.setActivity(mActivity);
	}

	/**
	 * Method invoked when the used click on the OK dialog message.
	 * 
	 * @param messageId
	 *            The ID of the message to be identified on the caller activity.
	 * @param anObject
	 *            The object used by the caller activity.
	 */
	protected void onMessageOk(int messageId, Object anObject) {
	}

	/**
	 * Show to the user an error dialog message.
	 * 
	 * @param titleStringId
	 *            The resource string id used for the dialog title.
	 * @param messageId
	 *            The resource string id used for the dialog text.
	 */
	protected void showMessageError(int titleStringId, int messageId) {
		showMessageDialog(titleStringId, getString(messageId),
				android.R.drawable.ic_dialog_alert, 0, null);
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
		showMessageDialog(titleStringId, message,
				android.R.drawable.ic_dialog_alert, 0, null);
	}

	/**
	 * Show to the user an information dialog message.
	 * 
	 * @param titleStringId
	 *            The resource string id used for the dialog title.
	 * @param message
	 *            The message from dialog text.
	 */
	protected void showMessageInfo(int titleStringId, String message) {
		showMessageDialog(titleStringId, message,
				android.R.drawable.ic_dialog_info, 0, null);
	}

	/**
	 * This method should be used to show a dialog message to the user.
	 * 
	 * @param titleStringId
	 *            The resource string id used for the dialog title.
	 * @param message
	 *            The message used for the dialog text.
	 * @param iconId
	 *            The dialog icon ID.
	 * @param messageId
	 *            The ID of the message to be identified on the caller activity.
	 * @param anObject
	 *            The object used by the caller activity.
	 */
	private void showMessageDialog(int titleStringId, String message,
			int iconId, final int messageId, final Object anObject) {
		new AlertDialog.Builder(mActivity)
				.setIcon(iconId)
				.setTitle(getString(titleStringId))
				.setMessage(message)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								onMessageOk(messageId, anObject);
							}
						}).show();
	}

	/**
	 * This method should be used to show a confirmation dialog message to the
	 * user.
	 * 
	 * @param titleStringId
	 *            The resource string id used for the confirmation dialog title.
	 * @param message
	 *            The message used for the confirmation dialog text.
	 * @param confirmationId
	 *            The ID of the message to be identified on the caller activity.
	 * @param anObject
	 *            The object used by the caller activity.
	 */
	public void showConfirmationDialog(int titleStringId, String message,
			final int confirmationId, final Object anObject) {
		new AlertDialog.Builder(mActivity)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(titleStringId)
				.setMessage(message)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								onConfirmationOk(confirmationId, anObject);
							}

						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								onConfirmationNo(confirmationId, anObject);
							}
						}).show();
	}

	/**
	 * Method invoked when the used click on the OK confirmation dialog message.
	 * 
	 * @param confirmationId
	 *            The ID of the message to be identified on the caller activity.
	 * @param anObject
	 *            The object used by the caller activity.
	 */
	protected void onConfirmationOk(int confirmationId, Object anObject) {

	}

	/**
	 * Method invoked when the used click on the NO confirmation dialog message.
	 * 
	 * @param confirmationId
	 *            The ID of the message to be identified on the caller activity.
	 * @param anObject
	 *            The object used by the caller activity.
	 */
	protected void onConfirmationNo(int confirmationId, Object anObject) {

	}

	/**
	 * @param mContentId the mContentId to set
	 */
	public void setContentId(int contentId) {
		this.mContentId = contentId;
	}
}
