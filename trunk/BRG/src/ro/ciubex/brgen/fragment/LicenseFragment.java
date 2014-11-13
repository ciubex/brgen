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

import java.io.IOException;
import java.io.InputStream;

import ro.ciubex.brgen.MainActivity;
import ro.ciubex.brgen.R;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This is the license fragment class.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class LicenseFragment extends BaseFragment {
	public static final int LICENCE = 0;
	public static final int LICENCE_2_0 = 1;
	private final static String TAG = LicenseFragment.class.getName();
	private TextView licenseTxt;
	private Button okButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ro.ciubex.brgen.fragment.BaseFragment#getFragmentResourceId()
	 */
	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_license;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ro.ciubex.brgen.fragment.BaseFragment#initFragment()
	 */
	@Override
	protected void initFragment() {
		okButton = (Button) mFragmentView.findViewById(R.id.okButton);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickOk();
			}
		});

		licenseTxt = (TextView) mFragmentView.findViewById(R.id.licenseTxt);
	}

	/**
	 * Invoked when is pressed the OK button.
	 */
	private void onClickOk() {
		mActivity.displayView(MainActivity.FRG_ABOUT, -1);
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		String fileName = "LICENSE.TXT";
		if (mContentId == LICENCE_2_0) {
			fileName = "LICENSE-2.0.TXT";
		}
		licenseTxt.setText(getStreamText(fileName));
		super.onResume();
	}

	/**
	 * In this method is loaded the license text
	 * 
	 * @param fileName
	 *            File name with the license text
	 * @return The license text
	 */
	private String getStreamText(String fileName) {
		AssetManager assetManager = mActivity.getAssets();
		StringBuilder sb = new StringBuilder();
		InputStream in = null;
		try {
			in = assetManager.open(fileName);
			if (in != null && in.available() > 0) {
				char c;
				while (in.available() > 0) {
					c = (char) in.read();
					sb.append(c);
				}
			}
		} catch (IOException e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		return sb.toString();
	}
}
