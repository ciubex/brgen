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
package ro.ciubex.brgen.tasks;

/**
 * Default result model used on asynchronous tasks to store process results:
 * task ID, result ID (OK or ERROR) and result message string.
 * 
 * @author Claudiu Ciobotariu
 * 
 */
public class DefaultAsyncTaskResult {
	public int taskId;
	public int resultId;
	public Object object;
	public String resultMessage;
}
