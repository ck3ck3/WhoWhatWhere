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

import java.io.Serializable;

public class NumberRangeValues implements Serializable
{
	private static final long serialVersionUID = 7554403736198431309L; //auto-generated, modify if changes to the class are not backwards-compatible
	
	private NumberRange rangeEnum;
	private Integer leftValue;
	private Integer rightValue;
	
	public NumberRangeValues(NumberRange range, Integer left, Integer right)
	{
		rangeEnum = range;
		leftValue = left;
		rightValue = right;
	}

	public NumberRange getRange()
	{
		return rangeEnum;
	}

	public Integer getLeftValue()
	{
		return leftValue;
	}

	public Integer getRightValue()
	{
		return rightValue;
	}
}
