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
package whowhatwhere.model.networksniffer.watchdog;

import org.apache.commons.collections4.bidimap.TreeBidiMap;

public enum OutputMethod
{
	TTS, POPUP, TTS_AND_POPUP;
	
	private static TreeBidiMap<OutputMethod, String> bidiMap = new TreeBidiMap<>();
	
	static
	{
		bidiMap.put(TTS, "Read out loud");
		bidiMap.put(POPUP, "Show a pop-up message");
		bidiMap.put(TTS_AND_POPUP, "Both");
	}
	
	public static String[] getValuesAsStrings()
	{
		String[] methods = {bidiMap.get(TTS), bidiMap.get(POPUP), bidiMap.get(TTS_AND_POPUP)};
		
		return methods;
	}
	
	public static OutputMethod stringToEnum(String str)
	{
		return bidiMap.getKey(str);
	}
	
	@Override
	public String toString()
	{
		return bidiMap.get(this);
	}
}
