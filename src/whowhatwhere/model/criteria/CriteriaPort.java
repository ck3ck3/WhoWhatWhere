package whowhatwhere.model.criteria;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

import whowhatwhere.model.ipsniffer.IPSniffer;

public class CriteriaPort implements Criteria<PcapPacket, Boolean>
{
	private int portNumber;
	private boolean isSourcePort;

	/**
	 * @param portNumber - the port number
	 * @param isSourcePort - true if it's the source port, false if it's the destination port
	 */
	public CriteriaPort(int portNumber, boolean isSourcePort)
	{
		this.portNumber = portNumber;
		this.isSourcePort = isSourcePort;
	}

	@Override
	public Boolean meetCriteria(PcapPacket itemToCheck)
	{
		if (itemToCheck.hasHeader(IPSniffer.TCP_PROTOCOL))
		{
			Tcp tcp = new Tcp();
			tcp = itemToCheck.getHeader(tcp);
			int portToCheck = isSourcePort ? tcp.source() : tcp.destination();

			return portNumber == portToCheck;
		}
		
		if (itemToCheck.hasHeader(IPSniffer.UDP_PROTOCOL))
		{
			Udp udp = new Udp();
			udp = itemToCheck.getHeader(udp);
			int portToCheck = isSourcePort ? udp.source() : udp.destination();

			return portNumber == portToCheck;
		}

		return false;
	}

	@Override
	public String getCriteriaAsText()
	{
		return "(Port == " + portNumber + ")";
	}
}
