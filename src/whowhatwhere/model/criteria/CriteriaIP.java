package whowhatwhere.model.criteria;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.network.Ip4;

import whowhatwhere.model.ipsniffer.IPSniffer;

public class CriteriaIP implements Criteria<PcapPacket, Boolean>
{
	String ipAddress;
	String mask;
	SubnetInfo subnetInfo;
	
	public CriteriaIP(String ipAddress, String mask)
	{
		this.ipAddress = ipAddress;
		this.mask = mask;
		
		SubnetUtils subnetUtils = new SubnetUtils(ipAddress, mask);
		subnetUtils.setInclusiveHostCount(true); //to allow one specific address with mask 255.255.255.255
		
		subnetInfo = subnetUtils.getInfo();
	}

	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		if (!itemToCheck.hasHeader(IPSniffer.IPv4_PROTOCOL))
			return false;
		
		Ip4 ipHeader = new Ip4();
		ipHeader = itemToCheck.getHeader(ipHeader);
		
		return subnetInfo.isInRange(ipHeader.sourceToInt());
	}

	@Override
	public String getCriteriaAsText()
	{
		String lowAddress = subnetInfo.getLowAddress(), highAddress = subnetInfo.getHighAddress();
				
		return lowAddress.equals(highAddress) ? "(IP == " + ipAddress + ")" : "(IP in " + lowAddress + ".." + highAddress + ")";  
	}
}
