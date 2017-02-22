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
