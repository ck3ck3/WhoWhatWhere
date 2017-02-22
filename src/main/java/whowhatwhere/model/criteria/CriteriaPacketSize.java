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

import org.jnetpcap.packet.PcapPacket;

public class CriteriaPacketSize implements Criteria<PcapPacket, Boolean>
{
	private int size;
	private RelativeToValue sign;
	
	public CriteriaPacketSize(int size, RelativeToValue sign)
	{
		this.size = size;
		this.sign = sign;
	}

	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		int currPacketSize = itemToCheck.size();
		
		switch (sign)
		{
			case LESS_THAN:		return currPacketSize <= size;
			case EQUALS:		return currPacketSize == size;
			case GREATER_THAN:	return currPacketSize >= size;
			
			default:			return null; //never gets here
		}
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(Packet size " + sign.getSign() + " " + size + ")";
	}
}
