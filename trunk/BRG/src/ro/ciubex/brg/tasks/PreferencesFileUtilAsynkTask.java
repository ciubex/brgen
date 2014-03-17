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
package ro.ciubex.brg.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ciubex.brg.MainApplication;
import ro.ciubex.brg.R;
import ro.ciubex.brg.model.Constants;
import ro.ciubex.brg.util.Utilities;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;

/**
 * This class is used to export or import application preferences
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class PreferencesFileUtilAsynkTask extends
		AsyncTask<Void, Void, DefaultAsyncTaskResult> {
	private static Logger logger = Logger
			.getLogger(PreferencesFileUtilAsynkTask.class.getName());

	/**
	 * The listener should implement this interface
	 */
	public interface Responder {
		public Application getApplication();

		public void startFileAsynkTask(Operation operationType);

		public void endFileAsynkTask(Operation operationType,
				DefaultAsyncTaskResult result);
	}

	/** Define available operations type */
	public enum Operation {
		EXPORT, IMPORT
	}

	private Responder responder;
	private Operation operationType;
	private String externalFileName;

	/**
	 * The constructor of this task
	 * 
	 * @param responder
	 *            The listener of this task
	 * @param fileName
	 *            Full file name path of exported / imported preferences
	 * @param operationType
	 *            Type of operation
	 */
	public PreferencesFileUtilAsynkTask(Responder responder, String fileName,
			Operation operationType) {
		this.responder = responder;
		externalFileName = fileName != null ? fileName.trim() : "";
		this.operationType = operationType;
	}

	/**
	 * Method invoked when is started this task
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		responder.startFileAsynkTask(operationType);
	}

	/**
	 * Method invoked at the end of this task
	 * 
	 * @param result
	 *            The result of this task
	 */
	@Override
	protected void onPostExecute(DefaultAsyncTaskResult result) {
		super.onPostExecute(result);
		responder.endFileAsynkTask(operationType, result);
	}

	/**
	 * This is main task method, here should be processed all background
	 * operations
	 */
	@Override
	protected DefaultAsyncTaskResult doInBackground(Void... params) {
		DefaultAsyncTaskResult result = new DefaultAsyncTaskResult();
		if (externalFileName.length() > 0) {
			result.resultId = Constants.OK;
		} else {
			result.resultId = Constants.ERROR;
			result.resultMessage = responder.getApplication().getString(
					R.string.file_name_missing);
		}
		if (result.resultId == Constants.OK) {
			if (operationType == Operation.IMPORT) {
				importFromFile(result);
			} else {
				exportToFile(result);
			}
		}
		return result;
	}

	/**
	 * Check if parent folders exists. If not, create them. This method is
	 * invoked on exporting.
	 * 
	 * @param file
	 *            File which should be created with exported preferences
	 * @return True if parent folders exist or are created successfully
	 */
	private boolean createParentFolders(File file) {
		boolean result = false;
		if (file != null) {
			if (file.getParentFile().exists()) {
				result = true;
			} else {
				result = file.getParentFile().mkdirs();
			}
		}
		return result;
	}

	/**
	 * Method used to export all application preferences
	 * 
	 * @param result
	 *            Result of export operation
	 */
	private void exportToFile(DefaultAsyncTaskResult result) {
		MainApplication app = (MainApplication) responder.getApplication();
		result.resultMessage = app.getString(R.string.export_success,
				externalFileName);
		OutputStream fos = null;
		File outFile;
		try {
			outFile = new File(externalFileName);
			if (createParentFolders(outFile)) {
				fos = new FileOutputStream(outFile);
				SharedPreferences prefs = app.getApplicationPreferences()
						.getSharedPreferences();
				Map<String, ?> keys = prefs.getAll();
				String key, clazz, value;
				StringBuilder sb = new StringBuilder();
				for (Map.Entry<String, ?> entry : keys.entrySet()) {
					key = entry.getKey();
					clazz = entry.getValue().getClass().getName();
					value = String.valueOf(entry.getValue());
					sb.append(key).append(':').append(clazz).append(':')
							.append(value).append('\n');
				}
				String content = sb.toString();
				logger.log(Level.INFO, content);
				fos.write(content.getBytes());
				fos.flush();
			} else {
				result.resultId = Constants.ERROR;
				result.resultMessage = app.getString(
						R.string.create_folders_error, externalFileName);
			}
		} catch (IllegalArgumentException e) {
			result.resultId = Constants.ERROR;
			result.resultMessage = app.getString(R.string.export_exception,
					externalFileName, "IllegalArgumentException",
					e.getMessage());
		} catch (FileNotFoundException e) {
			result.resultId = Constants.ERROR;
			result.resultMessage = app.getString(R.string.export_exception,
					externalFileName, "FileNotFoundException", e.getMessage());
		} catch (IOException e) {
			result.resultId = Constants.ERROR;
			result.resultMessage = app.getString(R.string.export_exception,
					externalFileName, "IOException", e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					result.resultId = Constants.ERROR;
					result.resultMessage = app.getString(
							R.string.export_exception, externalFileName,
							"Closing IOException", e.getMessage());
				}
			}
		}
	}

	/**
	 * Method used to import application preferences
	 * 
	 * @param result
	 *            Result of import operation
	 */
	private void importFromFile(DefaultAsyncTaskResult result) {
		result.resultMessage = responder.getApplication().getString(
				R.string.import_success, externalFileName);
		MainApplication app = (MainApplication) responder.getApplication();
		FileInputStream inFile = null;
		try {
			File f = new File(externalFileName);
			if (f.exists()) {
				inFile = new FileInputStream(f);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inFile));
				SharedPreferences prefs = app.getApplicationPreferences()
						.getSharedPreferences();
				Editor editor = prefs.edit();
				String line;
				String[] arrLine;
				while ((line = reader.readLine()) != null) {
					arrLine = currentLine(line);
					if (arrLine != null) {
						storeCurrentLine(editor, arrLine);
					}
				}
				editor.commit();
			} else {
				result.resultId = Constants.ERROR;
				result.resultMessage = app.getString(
						R.string.import_file_not_exist, externalFileName);
			}
		} catch (FileNotFoundException e) {
			result.resultId = Constants.ERROR;
			result.resultMessage = app.getString(R.string.import_exception,
					externalFileName, "FileNotFoundException", e.getMessage());
		} catch (IOException e) {
			result.resultId = Constants.ERROR;
			result.resultMessage = app.getString(R.string.import_exception,
					externalFileName, "IOException", e.getMessage());
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (IOException e) {
					result.resultId = Constants.ERROR;
					result.resultMessage = app.getString(
							R.string.import_exception, externalFileName,
							"Closing IOException", e.getMessage());
				}
			}
		}
	}

	private void storeCurrentLine(Editor editor, String[] arrLine) {
		String key = arrLine[0], clazz = arrLine[1], value = arrLine[2];
		int intValue;
		float floatValue;
		long longValue;
		if ("java.lang.String".equals(clazz)) {
			editor.putString(key, value);
		} else if ("java.lang.Boolean".equals(clazz)) {
			editor.putBoolean(key, "true".equalsIgnoreCase(value));
		} else if ("java.lang.Integer".equals(clazz)) {
			intValue = Utilities.parseInt(value);
			editor.putInt(key, intValue);
		} else if ("java.lang.Float".equals(clazz)) {
			floatValue = Utilities.parseFloat(value);
			editor.putFloat(key, floatValue);
		} else if ("java.lang.Long".equals(clazz)) {
			longValue = Utilities.parseLong(value);
			editor.putLong(key, longValue);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(key).append('+').append(clazz).append('+').append(value)
				.append('\n');
		logger.log(Level.INFO, sb.toString());
	}

	private String[] currentLine(String line) {
		String arr[] = null;
		if (line != null) {
			String text = line.trim();
			if (text.length() > 0) {
				int idx1 = text.indexOf(':');
				int idx2 = text.indexOf(':', idx1 + 1);
				String key, clazz, value;
				if (idx1 > 0 && idx2 > 0) {
					key = text.substring(0, idx1);
					clazz = text.substring(idx1 + 1, idx2);
					value = text.substring(idx2 + 1);
					arr = new String[3];
					arr[0] = key;
					arr[1] = clazz;
					arr[2] = value;
				}
			}
		}
		return arr;
	}

}