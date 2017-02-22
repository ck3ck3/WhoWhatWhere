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
import org.jnetpcap.protocol.network.Icmp;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

public enum SupportedProtocols
{
	ICMP(Icmp.ID), UDP(Udp.ID), TCP(Tcp.ID), HTTP(Http.ID);
	
	private int value;
	private static TreeBidiMap<String, SupportedProtocols> protocolBidiMap;
	
	static
	{
		protocolBidiMap = new TreeBidiMap<>();
		protocolBidiMap.put("ICMP", ICMP);
		protocolBidiMap.put("UDP", UDP);
		protocolBidiMap.put("TCP", TCP);
		protocolBidiMap.put("HTTP", HTTP);
	}
	
	private SupportedProtocols(int value)
	{
		this.value = value;
	}
	
	public int getValue()
	{
		return value;
	}
	
	@Override
	public String toString()
	{
		return protocolBidiMap.getKey(this);
	}
	
	public static SupportedProtocols stringToEnum(String str)
	{
		return protocolBidiMap.get(str);
	}
	
	public static String[] getSupportedProtocolsAsString()
	{
		String[] protocols = {ICMP.toString(), UDP.toString(), TCP.toString(), HTTP.toString()};
		
		return protocols;
	}
}
