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
import whowhatwhere.model.criteria.CriteriaPort;
import whowhatwhere.model.criteria.CriteriaProtocol;
import whowhatwhere.model.criteria.OrCriteria;
import whowhatwhere.model.ipsniffer.IPSniffer;

public class FirstSightPacketHandler implements PcapPacketHandler<Void>
{
	private boolean isRepeated;
	private Integer cooldownInSecs;
	private boolean isCooldownPeriod = false;
	private ScheduledThreadPoolExecutor timer;
	private FirstSightListener listener;
	private IPSniffer sniffer;
	
	Criteria<PcapPacket, Boolean> criteria;

	public FirstSightPacketHandler(List<IPToMatch> ipList, boolean isRepeated, Integer cooldownInSecs, FirstSightListener listener, IPSniffer sniffer)
			throws IllegalArgumentException, UnknownHostException
	{
		this.isRepeated = isRepeated;
		this.cooldownInSecs = cooldownInSecs;
		this.listener = listener;
		this.sniffer = sniffer;
		
		if (isRepeated && cooldownInSecs == null)
			throw new IllegalArgumentException("A repeated task cannot have a null cooldownInSecs");
		
		timer = new ScheduledThreadPoolExecutor(1);

		convertIPToMatchListToCriteria(ipList);
		
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

	private void convertIPToMatchListToCriteria(List<IPToMatch> ipList)
	{
		List<Criteria<PcapPacket, Boolean>> criteriasToOR = new ArrayList<Criteria<PcapPacket, Boolean>>();
		
		for (IPToMatch item : ipList)
		{
			List<Criteria<PcapPacket, Boolean>> criteriasToAND = new ArrayList<Criteria<PcapPacket, Boolean>>();
			
			//right now ip is mandatory (and default mask), otherwise we'd check for it
			criteriasToAND.add(new CriteriaIP(item.ipAddressProperty().get(), "255.255.255.255"));
			
			if (!item.protocolProperty().get().equals(IPToMatch.protocol_ANY))
				criteriasToAND.add(new CriteriaProtocol(item.protocolAsInt()));
			
			if (!item.srcPortProperty().get().equals(IPToMatch.port_ANY))
				criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.srcPortProperty().get()), true));
			
			if (!item.dstPortProperty().get().equals(IPToMatch.port_ANY))
				criteriasToAND.add(new CriteriaPort(Integer.valueOf(item.dstPortProperty().get()), false));
			
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
