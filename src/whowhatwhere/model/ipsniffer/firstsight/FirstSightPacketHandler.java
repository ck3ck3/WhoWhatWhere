package whowhatwhere.model.ipsniffer.firstsight;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

import whowhatwhere.model.ipsniffer.IPSniffer;

public class FirstSightPacketHandler implements PcapPacketHandler<Void>
{
	private boolean isRepeated;
	private Integer cooldownInSecs;
	private boolean isCooldownPeriod = false;
	private ScheduledThreadPoolExecutor timer;
	private FirstSightListener listener;
	private IPSniffer sniffer;
	private Map<Integer, IPToMatch> ipMap;

	public FirstSightPacketHandler(List<IPToMatch> ipList, boolean isRepeated, Integer cooldownInSecs, FirstSightListener listener, IPSniffer sniffer)
			throws IllegalArgumentException, UnknownHostException
	{
		this.isRepeated = isRepeated;
		this.cooldownInSecs = cooldownInSecs;
		this.listener = listener;
		this.sniffer = sniffer;
		ipMap = new HashMap<>();
		
		if (isRepeated && cooldownInSecs == null)
			throw new IllegalArgumentException("A repeated task cannot have a null cooldownInSecs");
		
		timer = new ScheduledThreadPoolExecutor(1);

		for (IPToMatch ipToMatch : ipList)
		{
			String ip = ipToMatch.ipAddressProperty().getValue();

			if (ip == null)
				throw new IllegalArgumentException("IP address must be set");

			ipMap.put(IPSniffer.stringToIntIp(ip), ipToMatch);
		}

		if (ipMap.isEmpty())
			throw new IllegalArgumentException("No valid criteria was found");
	}

	@Override
	public void nextPacket(PcapPacket packet, Void nothing)
	{
		if (!isCooldownPeriod && packet.hasHeader(IPSniffer.IPv4_PROTOCOL))
		{
			IPToMatch ipInfo = getMatchingIP(packet);

			if (ipInfo != null)
			{
				if (isRepeated)
				{
					isCooldownPeriod = true;
					
					timer.schedule(() ->
					{
						isCooldownPeriod = false;			
					}, cooldownInSecs, TimeUnit.SECONDS);
				}
				else
					sniffer.stopCapture();
				
				listener.firstSightOfIP(ipInfo);
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
			Integer protocol = ipToMatch.protocolAsInt();
			String srcPort = ipToMatch.srcPortProperty().getValue();
			String dstPort = ipToMatch.dstPortProperty().getValue();

			if (protocol != null && !packet.hasHeader(protocol)) //if a protocol was specified, but this packet doesn't have it
				return null;

			boolean checkSrcPort = (srcPort != null && !srcPort.equals(IPToMatch.port_ANY));
			boolean checkDstPort = (dstPort != null && !dstPort.equals(IPToMatch.port_ANY));

			if (checkSrcPort || checkDstPort)
			{
				int intSrcPort = Integer.valueOf(srcPort);
				int intdstPort = Integer.valueOf(dstPort);

				if (packet.hasHeader(IPSniffer.TCP_PROTOCOL))
				{
					Tcp tcp = new Tcp();
					tcp = packet.getHeader(tcp);

					if (checkSrcPort && intSrcPort != tcp.source())
						return null;

					if (checkDstPort && intdstPort != tcp.destination())
						return null;
				}
				else
					if (packet.hasHeader(IPSniffer.UDP_PROTOCOL))
					{
						Udp udp = new Udp();
						udp = packet.getHeader(udp);

						if (checkSrcPort && intSrcPort != udp.source())
							return null;

						if (checkDstPort && intdstPort != udp.destination())
							return null;
					}
					else //port specified, but unable to check ports on this packet
						return null;
			}

			//			if (ipHeader.length() > 300)
			return ipToMatch;
			//			else
			//				return null;
		}
		else
			return null;
	}
}
