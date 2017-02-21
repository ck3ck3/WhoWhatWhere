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
