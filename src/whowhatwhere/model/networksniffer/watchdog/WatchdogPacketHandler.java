package whowhatwhere.model.networksniffer.watchdog;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.PacketDirection;

public class WatchdogPacketHandler implements PcapPacketHandler<Void>
{
	private boolean isRepeated;
	private Integer cooldownInSecs;
	private boolean isCooldownPeriod = false;
	private ScheduledThreadPoolExecutor timer;
	private WatchdogListener listener;
	private NetworkSniffer sniffer;
	private byte[] ownMACAddress;
	
	private List<Criteria<PcapPacket, Boolean>> criteriaList;
	private Map<Criteria<PcapPacket, Boolean>, WatchdogMessage> criteriaToMsgMap = new HashMap<Criteria<PcapPacket, Boolean>, WatchdogMessage>();

	public WatchdogPacketHandler(List<PacketTypeToMatch> packetTypeList, boolean isRepeated, Integer cooldownInSecs, WatchdogListener listener, NetworkSniffer sniffer, byte[] ownMACAddress)
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

		criteriaList = convertPacketTypeToMatchListToCriteria(packetTypeList);
		
		if (criteriaList.size() == 0)
			throw new IllegalArgumentException("No criteria was set");
	}

	@Override
	public void nextPacket(PcapPacket packet, Void nothing)
	{
		if (!isCooldownPeriod)
		{
			for (Criteria<PcapPacket, Boolean> criteria : criteriaList)
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
					
					listener.watchdogFoundMatchingPacket(packet, criteriaToMsgMap.get(criteria));
				}
			}
		}
	}

	private List<Criteria<PcapPacket, Boolean>> convertPacketTypeToMatchListToCriteria(List<PacketTypeToMatch> packetTypeList)
	{
		List<Criteria<PcapPacket, Boolean>> criterias = new ArrayList<Criteria<PcapPacket, Boolean>>();
		
		for (PacketTypeToMatch item : packetTypeList)
		{
			Criteria<PcapPacket, Boolean> andCriteria = null;
			List<Criteria<PcapPacket, Boolean>> criteriasToAND = generateCriteriasToAND(item);
			
			if (criteriasToAND.size() > 0)
				andCriteria = criteriasToAND.get(0);
			
			for (int i = 1; i < criteriasToAND.size(); i++)
				andCriteria = new AndCriteria<PcapPacket>(andCriteria, criteriasToAND.get(i));
			
			if (criteriasToAND.size() > 0)
			{
				criterias.add(andCriteria);
				criteriaToMsgMap.put(andCriteria, new WatchdogMessage(item.messageTextProperty().get(), OutputMethod.stringToEnum(item.messageOutputMethodProperty().get())));
			}
		}
		
		return criterias;
	}
	
	private List<Criteria<PcapPacket, Boolean>> generateCriteriasToAND(PacketTypeToMatch item)
	{
		List<Criteria<PcapPacket, Boolean>> criteriasToAND = new ArrayList<Criteria<PcapPacket, Boolean>>();
		
		if (!item.packetDirectionProperty().get().equals(PacketTypeToMatch.packetDirection_ANY))
			criteriasToAND.add(new CriteriaPacketDirection(PacketDirection.stringToEnum(item.packetDirectionProperty().get()), ownMACAddress));
		
		if (!item.ipAddressProperty().get().equals(PacketTypeToMatch.IP_ANY))
			criteriasToAND.add(new CriteriaIP(item.ipAddressProperty().get(), item.netmaskProperty().get(), PacketDirection.stringToEnum(item.packetDirectionProperty().get())));
		
		if (!item.userNotesProperty().get().equals(PacketTypeToMatch.userNotes_ANY))
		{
			List<String> ipsFromUserNotes = item.getIPsFromUserNotes();
			int ipsToAdd = ipsFromUserNotes.size();
			
			if (ipsToAdd > 0)
			{
				CriteriaIP criteriaIP = new CriteriaIP(ipsFromUserNotes.get(0), "255.255.255.255", PacketDirection.stringToEnum(item.packetDirectionProperty().get()));
				Criteria<PcapPacket, Boolean> orBetweenIPs = criteriaIP;
					
				for (int i = 1; i < ipsToAdd; i++)
					orBetweenIPs = new OrCriteria<PcapPacket>(orBetweenIPs, new CriteriaIP(ipsFromUserNotes.get(i), "255.255.255.255", PacketDirection.stringToEnum(item.packetDirectionProperty().get())));
					
				criteriasToAND.add(orBetweenIPs);
			}
		}
		
		if (!item.packetSizeGreaterProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
			criteriasToAND.add(new CriteriaPacketSize(Integer.valueOf(item.packetSizeGreaterProperty().get()), RelativeToValue.GREATER_THAN));
		
		if (!item.packetSizeEqualsProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
			criteriasToAND.add(new CriteriaPacketSize(Integer.valueOf(item.packetSizeEqualsProperty().get()), RelativeToValue.EQUALS));
		
		if (!item.packetSizeSmallerProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
			criteriasToAND.add(new CriteriaPacketSize(Integer.valueOf(item.packetSizeSmallerProperty().get()), RelativeToValue.LESS_THAN));
		
		if (!item.protocolProperty().get().equals(PacketTypeToMatch.protocol_ANY))
			criteriasToAND.add(new CriteriaProtocol(item.protocolAsInt()));
		
		if (!item.srcPortGreaterProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
			criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.srcPortGreaterProperty().get()), RelativeToValue.GREATER_THAN, true));
		
		if (!item.srcPortEqualsProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
			criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.srcPortEqualsProperty().get()), RelativeToValue.EQUALS, true));
		
		if (!item.srcPortSmallerProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
			criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.srcPortSmallerProperty().get()), RelativeToValue.LESS_THAN, true));

		if (!item.dstPortGreaterProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
			criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.dstPortGreaterProperty().get()), RelativeToValue.GREATER_THAN, false));
		
		if (!item.dstPortEqualsProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
			criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.dstPortEqualsProperty().get()), RelativeToValue.EQUALS, false));
		
		if (!item.dstPortSmallerProperty().get().equals(PacketTypeToMatch.packetOrPort_ANY))
			criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.dstPortSmallerProperty().get()), RelativeToValue.LESS_THAN, false));
		
		return criteriasToAND;
	}
}
