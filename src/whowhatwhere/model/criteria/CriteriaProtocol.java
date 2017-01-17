package whowhatwhere.model.criteria;

import org.jnetpcap.packet.PcapPacket;

import whowhatwhere.model.networksniffer.NetworkSniffer;

public class CriteriaProtocol implements Criteria<PcapPacket, Boolean>
{
	int protocol;

	public CriteriaProtocol(int protocol)
	{
		this.protocol = protocol;
	}
	
	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		return itemToCheck.hasHeader(protocol);
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(Protocol == " + NetworkSniffer.intProtocolToString(protocol) + ")";
	}
}
