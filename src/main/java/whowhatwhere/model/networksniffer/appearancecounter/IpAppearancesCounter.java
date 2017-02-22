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
package whowhatwhere.model.networksniffer.appearancecounter;

public class IpAppearancesCounter implements Comparable<IpAppearancesCounter>
{
	private String ip;
	private int amountOfAppearances;

	public IpAppearancesCounter()
	{
	}

	public IpAppearancesCounter(String ip, int amount)
	{
		this.ip = ip;
		amountOfAppearances = amount;
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public int getAmountOfAppearances()
	{
		return amountOfAppearances;
	}

	public void setAmountOfAppearances(int amountOfAppearances)
	{
		this.amountOfAppearances = amountOfAppearances;
	}

	@Override
	public int compareTo(IpAppearancesCounter o) //REVERSE ORDER, bigger numbers first
	{
		return o.getAmountOfAppearances() - this.amountOfAppearances;
	}
}
