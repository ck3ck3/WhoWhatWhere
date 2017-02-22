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
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

public class CriteriaPort implements Criteria<PcapPacket, Boolean>
{
	private int portNumber;
	private RelativeToValue sign;
	private PortType portType;
	
	public enum PortType {SOURCE, DESTINATION}

	/**
	 * @param portNumber - the port number
	 * @param sign - less than (<), equals (==), greater than (>)
	 * @param portType - enum that specifies if it's a source port or a destination port
	 */
	public CriteriaPort(int portNumber, RelativeToValue sign, PortType portType)
	{
		this.portNumber = portNumber;
		this.sign = sign;
		this.portType = portType;
	}

	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		if (itemToCheck.hasHeader(Tcp.ID))
		{
			Tcp tcp = new Tcp();
			tcp = itemToCheck.getHeader(tcp);
			int portToCheck = portType == PortType.SOURCE ? tcp.source() : tcp.destination();

			return evaluate(portToCheck);
		}
		
		if (itemToCheck.hasHeader(Udp.ID))
		{
			Udp udp = new Udp();
			udp = itemToCheck.getHeader(udp);
			int portToCheck = portType == PortType.SOURCE ? udp.source() : udp.destination();

			return evaluate(portToCheck);
		}

		return false;
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(" + (portType == PortType.SOURCE ? "Source" : "Destination") + " port " + sign.getSign() + " " + portNumber + ")";
	}
	
	private Boolean evaluate(int portToCheck)
	{
		switch(sign)
		{
			case LESS_THAN:		return portToCheck <= portNumber;
			case EQUALS:		return portToCheck == portNumber;
			case GREATER_THAN:	return portToCheck >= portNumber;
			
			default:			return null; //never gets here
		}
	}
}
