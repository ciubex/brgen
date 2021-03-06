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

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ro.ciubex.brgen.MainActivity;
import ro.ciubex.brgen.R;

/**
 * Define About activity.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class AboutFragment extends BaseFragment {
	private final static String TAG = AboutFragment.class.getName();
	private AlertDialog.Builder donate;

	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_about;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ro.ciubex.brgen.fragment.BaseFragment#initFragment()
	 */
	@Override
	protected void initFragment() {
		initTextView();
		initButtons();
	}

	/**
	 * Initialize the text view control.
	 */
	private void initTextView() {
		TextView tv = (TextView) mFragmentView.findViewById(R.id.aboutTextView);
		tv.setText(Html.fromHtml(prepareAboutText()));
		tv.setMovementMethod(LinkMovementMethod.getInstance());
	}

	/**
	 * Prepare the full about text adding the version number, stored on the
	 * Application manifest file.
	 * 
	 * @return The about text.
	 */
	private String prepareAboutText() {
		String version = "1.0";
		String aboutText = "";
		try {
			version = getActivity().getPackageManager().getPackageInfo(
					getActivity().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		aboutText = getString(R.string.about_text, version);
		return aboutText;
	}

	/**
	 * Method used to initialize the buttons. 
	 */
	private void initButtons() {
		Button bLicense = (Button) mFragmentView.findViewById(R.id.license);
		bLicense.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickLicense(LicenseFragment.LICENCE);
			}
		});
		
		Button bLicense20 = (Button) mFragmentView.findViewById(R.id.license20);
		bLicense20.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickLicense(LicenseFragment.LICENCE_2_0);
			}
		});

		Button bDonate = (Button) mFragmentView.findViewById(R.id.donate);
		if (mApplication.isProPresent()) {
			bDonate.setVisibility(View.GONE);
		} else {
			bDonate.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					onClickDonate();
				}
			});
		}
	}

	/**
	 * Method invoked when is pressed the license button.
	 */
	private void onClickLicense(int contentId) {
		((MainActivity)getActivity()).displayView(MainActivity.FRG_LICENSE, contentId);
	}

	/**
	 * Method invoked when is pressed the donate button.
	 */
	private void onClickDonate() {
		if (donate == null) {
			donate = new AlertDialog.Builder(getActivity())
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.donate_title)
					.setMessage(R.string.donate_message)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startBrowserWithPage(R.string.donate_url);
								}

							}).setNegativeButton(R.string.no, null);
		}
		donate.show();
	}

	/**
	 * Launch the default browser with a specified URL page.
	 * 
	 * @param urlResourceId
	 *            The URL resource id.
	 */
	private void startBrowserWithPage(int urlResourceId) {
		String url = mApplication.getString(urlResourceId);
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		try {
			startActivity(i);
		} catch (ActivityNotFoundException exception) {
		}
	}
}
