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

import java.util.Arrays;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.lan.Ethernet;

import whowhatwhere.model.networksniffer.PacketDirection;

public class CriteriaPacketDirection implements Criteria<PcapPacket, Boolean>
{
	private PacketDirection direction;
	private byte[] ownMAC;

	public CriteriaPacketDirection(PacketDirection direction, byte[] ownMAC)
	{
		this.direction = direction;
		this.ownMAC = ownMAC;
	}

	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		if (!itemToCheck.hasHeader(Ethernet.ID))
			return false;

		Ethernet eth = new Ethernet();
		eth = itemToCheck.getHeader(eth);

		switch (direction)
		{
			case Incoming:
				return Arrays.equals(ownMAC, eth.destination());
			case Outgoing:
				return Arrays.equals(ownMAC, eth.source());
			default:
				return false; //doesn't get here
		}
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(Packet direction: " + direction.name() + ")";
	}
}
