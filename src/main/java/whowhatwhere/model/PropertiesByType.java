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
	
	public static boolean getBoolProperty(Properties props, String key) throws IllegalArgumentException
	{
		String value = getStringProperty(props, key);

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
	
	public static Integer getIntProperty(Properties props, String key) throws IllegalArgumentException
	{
		String value = getStringProperty(props, key);

		return Integer.valueOf(value);
	}
	
	public static Double getDoubleProperty(Properties props, String key) throws IllegalArgumentException
	{
		String value = getStringProperty(props, key);
		
		return Double.valueOf(value);
	}
	
	public static Double getDoubleProperty(Properties props, String key, Double defaultIfNoKey)
	{
		Double value;
		
		try
		{
			value = getDoubleProperty(props, key);
		}
		catch(IllegalArgumentException iae)
		{
			return defaultIfNoKey;
		}
		
		return value;
	}
	
	public static String getStringProperty(Properties props, String key) throws IllegalArgumentException
	{
		String value = props.getProperty(key);

		if (value == null)
			throw new IllegalArgumentException("The key \"" + key + "\" doesn't exist");
		
		return value;
	}
	
	public static String getStringProperty(Properties props, String key, String defaultIfNoKey)
	{
		String value;
		
		try
		{
			value = getStringProperty(props, key);
		}
		catch(IllegalArgumentException iae)
		{
			return defaultIfNoKey;
		}
		
		return value;
	}
}
