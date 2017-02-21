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
