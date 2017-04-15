/*******************************************************************************
 * Who What Where
 * Copyright (C) 2017  ck3ck3
 * https://github.com/ck3ck3/WhoWhatWhere
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
 ******************************************************************************/
package whowhatwhere.model.startwithwindows;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import whowhatwhere.Main;

public class StartWithWindowsRegistryUtils
{
	private final static String registrySubKey = "\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
	private final static String registryValue = Main.appTitle; //IF THIS EVER CHANGES, TAKE CARE OF BACKWARDS COMPATABILITY
	
	/**
	 * @param add - if true, we want to add the key. If false - delete the key.
	 * @param forAllUsers - if true, set this for all users. If false - only for this user
	 * @throws IOException if there was a problem modifying the registry
	 */
	public static void setRegistryToStartWithWindows(boolean add, boolean forAllUsers) throws IOException
	{
		String scheduledTaskCommand = "schtasks /run /tn \\\"" +  Main.scheduledTaskName + "\\\"";
		String command = "reg " + (add ? "add " : "delete ") + (forAllUsers ? "HKLM" : "HKCU") +  registrySubKey + " /v \"" + registryValue + "\"" + (add ? (" /d \"" + scheduledTaskCommand + "\"") : "") + " /f";
		
		Runtime.getRuntime().exec(command);
		
		if (add && isSetToStartWithWindows(!forAllUsers)) //check if the other option was already set, if so, delete the other option from registry 
			setRegistryToStartWithWindows(false, !forAllUsers);
	}
	
	/**
	 * @param forAllUsers - if true, checks for all users (HKLM), if false checks for current user (HKCU)
	 * @return The value of the key if exists, null if it doesn't exist.
	 * @throws IOException if there was a problem querying the registry
	 */
	public static String getExecutableLocationToStartWithWindows(boolean forAllUsers) throws IOException
	{
		String regQueryOutput = IOUtils.toString(Runtime.getRuntime().exec("reg query " + (forAllUsers ? "HKLM" : "HKCU") + registrySubKey + " /v \"" + registryValue + "\"").getInputStream(), (String)null);
		
		if (!regQueryOutput.contains(registryValue)) //value doesn't exist
			return null;
		
		String taskQueryOutput = IOUtils.toString(Runtime.getRuntime().exec("cmd /u /c schtasks /query /xml /tn \"" + Main.scheduledTaskName + "\"").getInputStream()); //using cmd with /u for utf-16 LE
		
		String getValueAfter = "process call create \"";
		String valueEndsWith = "\"</Arguments>";
		int start = taskQueryOutput.indexOf(getValueAfter) + getValueAfter.length();
		int end = taskQueryOutput.indexOf(valueEndsWith);
		
		return taskQueryOutput.substring(start, end);
	}	
	/**
	 * @param forAllUsers - if true, checks for all users (HKLM), if false checks for current user (HKCU)
	 * @return true if set to start with Windows (for all users or just current one, depending on {@code forAllUsers}) 
	 * @throws IOException if there was a problem querying the registry
	 */
	public static boolean isSetToStartWithWindows(boolean forAllUsers) throws IOException
	{
		return getExecutableLocationToStartWithWindows(forAllUsers) != null; 
	}
}
