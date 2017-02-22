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
package whowhatwhere.model.criteria;

public class OrCriteria<T> extends BinaryCriteriaOperator<T, Boolean> implements Criteria<T, Boolean>
{
	public OrCriteria(Criteria<T, Boolean> left, Criteria<T, Boolean> right)
	{
		super(left, right);
	}

	@Override
	public Boolean meetCriteria(T itemToCheck)
	{
		return left.meetCriteria(itemToCheck) || right.meetCriteria(itemToCheck);
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(" + left.getCriteriaAsText() + " OR " + right.getCriteriaAsText() + ")";
	}
}
