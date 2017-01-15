package whowhatwhere.model.ipsniffer.firstsight;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

import whowhatwhere.model.criteria.AndCriteria;
import whowhatwhere.model.criteria.Criteria;
import whowhatwhere.model.criteria.CriteriaIP;
import whowhatwhere.model.criteria.CriteriaPacketDirection;
import whowhatwhere.model.criteria.CriteriaPacketSize;
import whowhatwhere.model.criteria.CriteriaPort;
import whowhatwhere.model.criteria.CriteriaProtocol;
import whowhatwhere.model.criteria.OrCriteria;
import whowhatwhere.model.criteria.RelativeToValue;
import whowhatwhere.model.ipsniffer.IPSniffer;
import whowhatwhere.model.ipsniffer.PacketDirection;

public class FirstSightPacketHandler implements PcapPacketHandler<Void>
{
	private boolean isRepeated;
	private Integer cooldownInSecs;
	private boolean isCooldownPeriod = false;
	private ScheduledThreadPoolExecutor timer;
	private FirstSightListener listener;
	private IPSniffer sniffer;
	private byte[] ownMACAddress;
	
	Criteria<PcapPacket, Boolean> criteria;

	public FirstSightPacketHandler(List<PacketTypeToMatch> packetTypeList, boolean isRepeated, Integer cooldownInSecs, FirstSightListener listener, IPSniffer sniffer, byte[] ownMACAddress)
			throws IllegalArgumentException, UnknownHostException
	{
		this.isRepeated = isRepeated;
		this.cooldownInSecs = cooldownInSecs;
		this.listener = listener;
		this.sniffer = sniffer;
		this.ownMACAddress = ownMACAddress;
		
		if (isRepeated && cooldownInSecs == null)
			throw new IllegalArgumentException("A repeated task cannot have a null cooldownInSecs");
		
		timer = new ScheduledThreadPoolExecutor(1);

		convertIPToMatchListToCriteria(packetTypeList);
		
		if (criteria == null)
			throw new IllegalArgumentException("No criteria was set");
	}

	@Override
	public void nextPacket(PcapPacket packet, Void nothing)
	{
		if (!isCooldownPeriod && packet.hasHeader(IPSniffer.IPv4_PROTOCOL))
		{
			if (criteria.meetCriteria(packet))
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
				
				listener.firstSightOfIP(packet);
			}
		}
	}

	private void convertIPToMatchListToCriteria(List<PacketTypeToMatch> ipList)
	{
		List<Criteria<PcapPacket, Boolean>> criteriasToOR = new ArrayList<Criteria<PcapPacket, Boolean>>();
		
		for (PacketTypeToMatch item : ipList)
		{
			List<Criteria<PcapPacket, Boolean>> criteriasToAND = new ArrayList<Criteria<PcapPacket, Boolean>>();
			
			if (!item.packetDirectionProperty().get().equals(PacketTypeToMatch.packetDirection_ANY))
				criteriasToAND.add(new CriteriaPacketDirection(PacketDirection.valueOf(item.packetDirectionProperty().get()), ownMACAddress));
			
			if (!item.ipAddressProperty().get().equals(PacketTypeToMatch.IP_ANY))
				criteriasToAND.add(new CriteriaIP(item.ipAddressProperty().get(), item.netmaskProperty().get()));
			
			//TODO convert user notes to ip criteria
			
			if (!item.packetSizeSmallerProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
				criteriasToAND.add(new CriteriaPacketSize(Integer.valueOf(item.packetSizeSmallerProperty().get()), RelativeToValue.valueOf(item.packetSizeSmallerProperty().get())));
			
			if (!item.packetSizeEqualsProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
				criteriasToAND.add(new CriteriaPacketSize(Integer.valueOf(item.packetSizeEqualsProperty().get()), RelativeToValue.valueOf(item.packetSizeEqualsProperty().get())));
			
			if (!item.packetSizeGreaterProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
				criteriasToAND.add(new CriteriaPacketSize(Integer.valueOf(item.packetSizeGreaterProperty().get()), RelativeToValue.valueOf(item.packetSizeGreaterProperty().get())));
			
			if (!item.protocolProperty().get().equals(PacketTypeToMatch.protocol_ANY))
				criteriasToAND.add(new CriteriaProtocol(item.protocolAsInt()));
			
			if (!item.srcPortSmallerProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
				criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.srcPortSmallerProperty().get()), RelativeToValue.valueOf(item.srcPortSmallerProperty().get()), true));
			
			if (!item.srcPortEqualsProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
				criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.srcPortEqualsProperty().get()), RelativeToValue.valueOf(item.srcPortEqualsProperty().get()), true));
			
			if (!item.srcPortGreaterProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
				criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.srcPortGreaterProperty().get()), RelativeToValue.valueOf(item.srcPortGreaterProperty().get()), true));

			if (!item.dstPortSmallerProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
				criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.dstPortSmallerProperty().get()), RelativeToValue.valueOf(item.dstPortSmallerProperty().get()), false));
			
			if (!item.dstPortEqualsProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
				criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.dstPortEqualsProperty().get()), RelativeToValue.valueOf(item.dstPortEqualsProperty().get()), false));
			
			if (!item.dstPortGreaterProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
				criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.dstPortGreaterProperty().get()), RelativeToValue.valueOf(item.dstPortGreaterProperty().get()), false));
			
			Criteria<PcapPacket, Boolean> andCriteria = null;
			
			if (criteriasToAND.size() > 0)
				andCriteria = criteriasToAND.get(0);
			
			for (int i = 1; i < criteriasToAND.size(); i++)
				andCriteria = new AndCriteria<PcapPacket>(andCriteria, criteriasToAND.get(i));
			
			if (criteriasToAND.size() > 0)
				criteriasToOR.add(andCriteria);
		}
		
		Criteria<PcapPacket, Boolean> orCriteria = null;
		
		if (criteriasToOR.size() > 0)
			orCriteria = criteriasToOR.get(0);
		
		for (int i = 1; i < criteriasToOR.size(); i++)
			orCriteria = new OrCriteria<PcapPacket>(orCriteria, criteriasToOR.get(i));
		
		criteria = orCriteria;
	}
}
