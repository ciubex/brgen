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

import java.util.Calendar;

import ro.ciubex.brgen.MainApplication;
import ro.ciubex.brgen.model.Contact;
import ro.ciubex.brgen.tasks.BirthdaySaveAsyncTask;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.DatePicker;

/**
 * @author Claudiu Ciobotariu
 * 
 */
public class DatePickerDialogFragment extends DialogFragment implements
		OnDateSetListener, BirthdaySaveAsyncTask.Responder {

	private MainApplication mApplication;
	private Contact mContact;
	private BaseAdapter mAdapter;
	private Calendar mOldBirthday;

	public DatePickerDialogFragment() {
	}

	/**
	 * Set main application.
	 * 
	 * @param application
	 *            The main application to set.
	 */
	public void setMainApplication(MainApplication application) {
		mApplication = application;
	}

	/**
	 * @param contact
	 *            the contact to set
	 */
	public void setContact(Contact contact) {
		this.mContact = contact;
	}

	/**
	 * @param adapter
	 *            the adapter to set
	 */
	public void setAdapter(BaseAdapter adapter) {
		this.mAdapter = adapter;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Calendar cal = mContact.getBirthday();
		if (cal == null) {
			cal = Calendar.getInstance(mApplication.getDefaultLocale());
		}
		return new DatePickerDialog(getActivity(), this,
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH));
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		mOldBirthday = mContact.getBirthday();
		Calendar newCalendar = Calendar.getInstance(mApplication.getDefaultLocale());
		newCalendar.set(Calendar.YEAR, year);
		newCalendar.set(Calendar.MONTH, monthOfYear);
		newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		mContact.setBirthday(newCalendar);
		new BirthdaySaveAsyncTask(mApplication, this, mContact).execute();
	}

	@Override
	public void saveProcessResult(boolean result) {
		if (result) {
			mAdapter.notifyDataSetChanged();
		} else {
			mContact.setBirthday(mOldBirthday);
		}
	}

}
