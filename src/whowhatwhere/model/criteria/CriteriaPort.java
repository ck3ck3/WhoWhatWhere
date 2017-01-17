package whowhatwhere.model.criteria;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

import whowhatwhere.model.networksniffer.NetworkSniffer;

public class CriteriaPort implements Criteria<PcapPacket, Boolean>
{
	private int portNumber;
	private RelativeToValue sign;
	private boolean isSourcePort;

	/**
	 * @param portNumber - the port number
	 * @param sign - less than (<), equals (==), greater than (>)
	 * @param isSourcePort - true if it's the source port, false if it's the destination port
	 */
	public CriteriaPort(int portNumber, RelativeToValue sign, boolean isSourcePort)
	{
		this.portNumber = portNumber;
		this.sign = sign;
		this.isSourcePort = isSourcePort;
	}

	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		if (itemToCheck.hasHeader(NetworkSniffer.TCP_PROTOCOL))
		{
			Tcp tcp = new Tcp();
			tcp = itemToCheck.getHeader(tcp);
			int portToCheck = isSourcePort ? tcp.source() : tcp.destination();

			return evaluate(portToCheck);
		}
		
		if (itemToCheck.hasHeader(NetworkSniffer.UDP_PROTOCOL))
		{
			Udp udp = new Udp();
			udp = itemToCheck.getHeader(udp);
			int portToCheck = isSourcePort ? udp.source() : udp.destination();

			return evaluate(portToCheck);
		}

		return false;
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(" + (isSourcePort ? "Source" : "Destination") + " port " + sign.getSign() + " " + portNumber + ")";
	}
	
	private Boolean evaluate(int portToCheck)
	{
		switch(sign)
		{
			case LESS_THAN:		return portToCheck < portNumber;
			case EQUALS:		return portToCheck == portNumber;
			case GREATER_THAN:	return portToCheck > portNumber;
			
			default:			return null; //never gets here
		}
	}
}
