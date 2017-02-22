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

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.network.Ip4;

import whowhatwhere.model.networksniffer.PacketDirection;

public class CriteriaIP implements Criteria<PcapPacket, Boolean>
{
	private String ipAddress;
	private PacketDirection direction;
	private SubnetInfo subnetInfo;
	
	public CriteriaIP(String ipAddress, String mask, PacketDirection direction)
	{
		this.ipAddress = ipAddress;
		this.direction = direction;
		
		SubnetUtils subnetUtils = new SubnetUtils(ipAddress, mask);
		subnetUtils.setInclusiveHostCount(true); //to allow one specific address with mask 255.255.255.255
		
		subnetInfo = subnetUtils.getInfo();
	}

	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		if (!itemToCheck.hasHeader(Ip4.ID))
			return false;
		
		Ip4 ipHeader = new Ip4();
		ipHeader = itemToCheck.getHeader(ipHeader);
		
		if (direction == null) //direction wasn't set, so any direction
			return subnetInfo.isInRange(ipHeader.sourceToInt()) || subnetInfo.isInRange(ipHeader.destinationToInt());
		
		switch(direction)
		{
			case Incoming:	return subnetInfo.isInRange(ipHeader.sourceToInt());
			case Outgoing:	return subnetInfo.isInRange(ipHeader.destinationToInt());
			default:		return null; //doesn't get here
		}
	}

	@Override
	public String getCriteriaAsText()
	{
		String lowAddress = subnetInfo.getLowAddress(), highAddress = subnetInfo.getHighAddress();
				
		return lowAddress.equals(highAddress) ? "(IP == " + ipAddress + ")" : "(IP in " + lowAddress + "-" + highAddress + ")";  
	}
}
