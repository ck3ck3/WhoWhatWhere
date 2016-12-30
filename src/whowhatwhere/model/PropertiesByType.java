package whowhatwhere.model;

import java.util.Properties;

public class PropertiesByType
{
	public static boolean getBoolProperty(Properties props, String key, boolean defaultIfNoKey)
	{
		boolean value;
		
		try
		{
			value = getBoolProperty(props, key);
		}
		catch(IllegalArgumentException iae)
		{
			return defaultIfNoKey;
		}
		
		return value;
	}
	
	public static boolean getBoolProperty(Properties props, String key)
	{
		String value = getProperty(props, key);

		return value.equals("true");
	}

	public static Integer getIntProperty(Properties props, String key, Integer defaultIfNoKey)
	{
		Integer value;
		
		try
		{
			value = getIntProperty(props, key);
		}
		catch(IllegalArgumentException iae)
		{
			return defaultIfNoKey;
		}
		
		return value;
	}
	
	public static Integer getIntProperty(Properties props, String key)
	{
		String value = getProperty(props, key);

		return Integer.valueOf(value);
	}
	
	private static String getProperty(Properties props, String key)
	{
		String value = props.getProperty(key);

		if (value == null)
			throw new IllegalArgumentException("The key \"" + key + "\" doesn't exist");
		
		return value;
	}
}
