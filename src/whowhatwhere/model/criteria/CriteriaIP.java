package whowhatwhere.model.criteria;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.network.Ip4;

import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.PacketDirection;

public class CriteriaIP implements Criteria<PcapPacket, Boolean>
{
	private String ipAddress;
	private PacketDirection direction;
	private SubnetInfo subnetInfo;
	
	public CriteriaIP(String ipAddress, String mask, PacketDirection direction)
	{
		this.ipAddress = ipAddress;
		this.direction = direction;
		
		SubnetUtils subnetUtils = new SubnetUtils(ipAddress, mask);
		subnetUtils.setInclusiveHostCount(true); //to allow one specific address with mask 255.255.255.255
		
		subnetInfo = subnetUtils.getInfo();
	}

	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		if (!itemToCheck.hasHeader(NetworkSniffer.IPv4_PROTOCOL))
			return false;
		
		Ip4 ipHeader = new Ip4();
		ipHeader = itemToCheck.getHeader(ipHeader);
		
		switch(direction)
		{
			case ANY:		return subnetInfo.isInRange(ipHeader.sourceToInt()) || subnetInfo.isInRange(ipHeader.destinationToInt());
			case Incoming:	return subnetInfo.isInRange(ipHeader.sourceToInt());
			case Outgoing:	return subnetInfo.isInRange(ipHeader.destinationToInt());
			default:		return null; //doesn't get here
		}
	}

	@Override
	public String getCriteriaAsText()
	{
		String lowAddress = subnetInfo.getLowAddress(), highAddress = subnetInfo.getHighAddress();
				
		return lowAddress.equals(highAddress) ? "(IP == " + ipAddress + ")" : "(IP in " + lowAddress + "-" + highAddress + ")";  
	}
}
