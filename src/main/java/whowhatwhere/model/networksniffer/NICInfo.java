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
package whowhatwhere.model.networksniffer;

import java.util.Arrays;

public class NICInfo
{
	private String ip;
	private byte[] macAddress;
	private String description;

	public NICInfo()
	{
	}

	public NICInfo(String ip, byte[] macAddress, String description)
	{
		this.ip = ip;
		this.macAddress = macAddress;
		this.description = description;
	}

	public void copyNICInfo(NICInfo other)
	{
		ip = other.ip;
		macAddress = other.macAddress;
		description = other.description;
	}

	public String getIP()
	{
		return ip;
	}

	public byte[] getMACAddress()
	{
		return macAddress;
	}

	public String getDescription()
	{
		return description;
	}

	@Override
	public String toString()
	{
		String hexString = javax.xml.bind.DatatypeConverter.printHexBinary(macAddress);
		String macAddressString = hexString.substring(0, 2) + ":" + hexString.substring(2, 4) + ":" + hexString.substring(4, 6) + ":" + hexString.substring(6, 8) + ":" + hexString.substring(8, 10)
				+ ":" + hexString.substring(10, 12);

		return description + " " + ip + " " + macAddressString;
	}

	@Override
	public int hashCode() //hashcode and equals are overriden so this class can be used as a key in a map
	{
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + Arrays.hashCode(macAddress);
		
		return result;
	}

	@Override
	public boolean equals(Object obj) //hashcode and equals are overriden so this class can be used as a key in a map
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof NICInfo))
			return false;
		
		NICInfo other = (NICInfo) obj;
		if (description == null)
		{
			if (other.description != null)
				return false;
		}
		else
			if (!description.equals(other.description))
				return false;
		
		if (ip == null)
		{
			if (other.ip != null)
				return false;
		}
		else
			if (!ip.equals(other.ip))
				return false;
		
		if (!Arrays.equals(macAddress, other.macAddress))
			return false;
		
		return true;
	}
}
