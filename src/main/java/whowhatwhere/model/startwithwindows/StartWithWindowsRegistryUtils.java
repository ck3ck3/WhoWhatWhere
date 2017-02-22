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
	private final static String registryValue = Main.getAppName(); //IF THIS EVER CHANGES, TAKE CARE OF BACKWARDS COMPATABILITY
	
	/**
	 * @param add - if true, we want to add the key. If false - delete the key.
	 * @param isForAllUsers - if true, set this for all users. If false - only for this user
	 * @throws IOException if there was a problem modifying the registry
	 */
	public static void setRegistryToStartWithWindows(boolean add, boolean forAllUsers) throws IOException
	{
		String currentRunLocation = System.getProperty("user.dir") + "\\" + Main.getExecutablefilename();		
		String command = "reg " + (add ? "add " : "delete ") + (forAllUsers ? "HKLM" : "HKCU") +  registrySubKey + " /v \"" + registryValue + "\"" + (add ? (" /d " + currentRunLocation) : "") + " /f";
		
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
		String result = IOUtils.toString(Runtime.getRuntime().exec("reg query " + (forAllUsers ? "HKLM" : "HKCU") + registrySubKey + " /v \"" + registryValue + "\"").getInputStream(), (String)null);
		
		if (!result.contains(registryValue))
			return null;
		
		return result.substring(result.trim().lastIndexOf(' ')).trim();
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
