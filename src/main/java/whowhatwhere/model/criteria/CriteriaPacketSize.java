package whowhatwhere.model.criteria;

import org.jnetpcap.packet.PcapPacket;

public class CriteriaPacketSize implements Criteria<PcapPacket, Boolean>
{
	private int size;
	private RelativeToValue sign;
	
	public CriteriaPacketSize(int size, RelativeToValue sign)
	{
		this.size = size;
		this.sign = sign;
	}

	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		int currPacketSize = itemToCheck.size();
		
		switch (sign)
		{
			case LESS_THAN:		return currPacketSize <= size;
			case EQUALS:		return currPacketSize == size;
			case GREATER_THAN:	return currPacketSize >= size;
			
			default:			return null; //never gets here
		}
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(Packet size " + sign.getSign() + " " + size + ")";
	}
}
