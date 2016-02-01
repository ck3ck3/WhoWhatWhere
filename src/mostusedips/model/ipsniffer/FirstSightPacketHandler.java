package mostusedips.model.ipsniffer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

public class FirstSightPacketHandler implements PcapPacketHandler<Void>
{
	private FirstSightListener listener;
	private IpSniffer sniffer;
	private HashMap<Integer, IPToMatch> ipMap;
	//	private boolean found = false;

	public FirstSightPacketHandler(ArrayList<IPToMatch> ipList, FirstSightListener listener, IpSniffer sniffer)
	{
		this.listener = listener;
		this.sniffer = sniffer;
		ipMap = new HashMap<Integer, IPToMatch>();

		try
		{
			for (IPToMatch ipToMatch : ipList)
			{
				String ip = ipToMatch.getIP();
				
				if (ip == null)
					throw new IllegalArgumentException("IP address must be set");
				
				ipMap.put(IpSniffer.stringToIntIp(ip), ipToMatch);
			}
		}
		catch (UnknownHostException e) 
		{
		}
		
		if (ipMap.isEmpty())
			throw new IllegalArgumentException("No valid criteria was found");
	}

	@Override
	public void nextPacket(PcapPacket packet, Void nothing)
	{
		if (/* !found && */ packet.hasHeader(IpSniffer.IPv4_PROTOCOL))
		{
			IPToMatch ipInfo = getMatchingIP(packet);

			if (ipInfo != null)
			{
				//				found = true;
				listener.firstSightOfIP(ipInfo);
				sniffer.stopCapture();
			}
		}
	}

	private IPToMatch getMatchingIP(PcapPacket packet)
	{
		Ip4 ipHeader = new Ip4();
		ipHeader = packet.getHeader(ipHeader);
		int sourceInt = ipHeader.sourceToInt();

		IPToMatch ipToMatch = ipMap.get(sourceInt);
		
		if (ipToMatch != null)
		{
			Integer protocol = ipToMatch.getProtocol();
			Integer srcPort = ipToMatch.getSrcPort();
			Integer dstPort = ipToMatch.getDstPort();

			if (protocol != null && !packet.hasHeader(protocol)) //if a protocol was specified, but this packet doesn't have it
					return null;

			if (srcPort != null || dstPort != null)
			{
				if (packet.hasHeader(IpSniffer.TCP_PROTOCOL))
				{
					Tcp tcp = new Tcp();
					tcp = packet.getHeader(tcp);
					
					if (srcPort != null && srcPort != tcp.source())
						return null;

					if (dstPort != null && dstPort != tcp.destination())
						return null;
				}
				else
					if (packet.hasHeader(IpSniffer.UDP_PROTOCOL))
					{
						Udp udp = new Udp();
						udp = packet.getHeader(udp);

						if (srcPort != null && srcPort != udp.source())
							return null;

						if (dstPort != null && dstPort != udp.destination())
							return null;
					}
					else //port specified, but unable to check ports on this packet
						return null;
			}

			return ipToMatch;
		}
		else
			return null;
	}
}
