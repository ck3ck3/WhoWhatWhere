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

import org.apache.commons.collections4.bidimap.TreeBidiMap;

public enum PacketDirection
{
	Incoming, Outgoing;
	
	private static TreeBidiMap<PacketDirection, String> bidiMap = new TreeBidiMap<>();
	
	static
	{
		bidiMap.put	(Incoming, "Incoming");
		bidiMap.put(Outgoing, "Outgoing");
	}
	
	public static String[] getValuesAsStrings()
	{
		String[] directions = {bidiMap.get(Incoming), bidiMap.get(Outgoing)};
		
		return directions;		
	}
	
	public static PacketDirection stringToEnum(String str)
	{
		return bidiMap.getKey(str);
	}
	
	@Override
	public String toString()
	{
		return bidiMap.get(this);
	}
}
