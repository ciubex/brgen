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
package ro.ciubex.brg.fragment;

import ro.ciubex.brg.MainActivity;
import ro.ciubex.brg.MainApplication;
import ro.ciubex.brg.R;
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
	protected MainApplication mApplication;
	protected MainActivity mActivity;
	protected View mFragmentView;

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
	 * This method should be used to show a dialog message to the user.
	 * 
	 * @param titleStringId
	 *            The resource string id used for the confirmation dialog title.
	 * @param message
	 *            The message used for the confirmation dialog text.
	 * @param messageId
	 *            The ID of the message to be identified on the caller activity.
	 * @param anObject
	 *            The object used by the caller activity.
	 */
	public void showMessageDialog(int titleStringId, String message,
			final int messageId, final Object anObject) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
		alertDialog.setIcon(android.R.drawable.ic_dialog_info);
		alertDialog.setTitle(getString(titleStringId));
		alertDialog.setMessage(message);
		alertDialog.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						onMessageOk(messageId, anObject);
					}
				});
		AlertDialog alert = alertDialog.create();
		alert.show();
	}
}
