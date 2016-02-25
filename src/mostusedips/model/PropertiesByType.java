package mostusedips.model;

import java.util.Properties;

public class PropertiesByType
{
	public static boolean getBoolProperty(Properties props, String key)
	{
		String value = props.getProperty(key);

		if (value == null)
			throw new IllegalArgumentException("The key \"" + key + "\" doesn't exist");

		return value.equals("true");

	}

	public static Integer getIntProperty(Properties props, String key)
	{
		String value = props.getProperty(key);

		if (value == null)
			throw new IllegalArgumentException("The key \"" + key + "\" doesn't exist");

		return Integer.valueOf(value);
	}
}
