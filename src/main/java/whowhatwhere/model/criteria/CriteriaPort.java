package whowhatwhere.model.criteria;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

public class CriteriaPort implements Criteria<PcapPacket, Boolean>
{
	private int portNumber;
	private RelativeToValue sign;
	private PortType portType;
	
	public enum PortType {SOURCE, DESTINATION}

	/**
	 * @param portNumber - the port number
	 * @param sign - less than (<), equals (==), greater than (>)
	 * @param portType - enum that specifies if it's a source port or a destination port
	 */
	public CriteriaPort(int portNumber, RelativeToValue sign, PortType portType)
	{
		this.portNumber = portNumber;
		this.sign = sign;
		this.portType = portType;
	}

	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		if (itemToCheck.hasHeader(Tcp.ID))
		{
			Tcp tcp = new Tcp();
			tcp = itemToCheck.getHeader(tcp);
			int portToCheck = portType == PortType.SOURCE ? tcp.source() : tcp.destination();

			return evaluate(portToCheck);
		}
		
		if (itemToCheck.hasHeader(Udp.ID))
		{
			Udp udp = new Udp();
			udp = itemToCheck.getHeader(udp);
			int portToCheck = portType == PortType.SOURCE ? udp.source() : udp.destination();

			return evaluate(portToCheck);
		}

		return false;
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(" + (portType == PortType.SOURCE ? "Source" : "Destination") + " port " + sign.getSign() + " " + portNumber + ")";
	}
	
	private Boolean evaluate(int portToCheck)
	{
		switch(sign)
		{
			case LESS_THAN:		return portToCheck <= portNumber;
			case EQUALS:		return portToCheck == portNumber;
			case GREATER_THAN:	return portToCheck >= portNumber;
			
			default:			return null; //never gets here
		}
	}
}
