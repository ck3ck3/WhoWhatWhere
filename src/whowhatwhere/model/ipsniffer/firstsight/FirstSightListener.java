package whowhatwhere.model.ipsniffer.firstsight;

import org.jnetpcap.packet.PcapPacket;

public interface FirstSightListener
{
	/**
	 * @param packet - the packet that matched the rule
	 */
	public void firstSightOfIP(PcapPacket packet);
}
