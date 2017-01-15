package whowhatwhere.model.criteria;

import java.util.Arrays;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.lan.Ethernet;

import whowhatwhere.model.ipsniffer.PacketDirection;

public class CriteriaPacketDirection implements Criteria<PcapPacket, Boolean>
{
	private PacketDirection direction;
	private byte[] ownMAC;
	
	
	public CriteriaPacketDirection(PacketDirection direction, byte[] ownMAC)
	{
		this.direction = direction;
		this.ownMAC = ownMAC;
	}

	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		if (direction == PacketDirection.ANY)
			return true;
		else
		{
			if (!itemToCheck.hasHeader(Ethernet.ID))
				return false;
			
			Ethernet eth = null;
			eth = itemToCheck.getHeader(eth);
			
			switch(direction)
			{
				case Incoming:	return Arrays.equals(ownMAC, eth.destination());
				case Outgoing:	return Arrays.equals(ownMAC, eth.source());
				default:		return false; //doesn't get here
			}
		}
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(Packet direction: " + direction.name() + ")";
	}
}
