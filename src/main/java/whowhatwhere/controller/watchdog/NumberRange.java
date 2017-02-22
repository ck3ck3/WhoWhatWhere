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
package whowhatwhere.controller.watchdog;

public enum NumberRange
{
	EQUALS("Equals"), GREATER_THAN("At least"), LESS_THAN("At most"), RANGE("Between"); 
	
	private String value;
	
	private NumberRange(String value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return value;
	}

	public static String numberRangeStringRepresentation(NumberRange range, Number leftNum, Number rightNum)
	{
		switch(range)
		{
			case EQUALS: return leftNum.toString();
			case GREATER_THAN : return "\u2265 " + leftNum; // >=
			case LESS_THAN : return "\u2264 " + leftNum; // <=
			case RANGE: return leftNum + " - " + rightNum;
			default : return ""; //never gets here
		}
	}
}
