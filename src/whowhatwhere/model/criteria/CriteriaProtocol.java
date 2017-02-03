package whowhatwhere.model.criteria;

import org.jnetpcap.packet.PcapPacket;

import whowhatwhere.model.networksniffer.SupportedProtocols;

public class CriteriaProtocol implements Criteria<PcapPacket, Boolean>
{
	SupportedProtocols protocol;

	public CriteriaProtocol(SupportedProtocols protocol)
	{
		this.protocol = protocol;
	}
	
	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		return itemToCheck.hasHeader(protocol.getValue());
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(Protocol == " + protocol.toString() + ")";
	}
}
