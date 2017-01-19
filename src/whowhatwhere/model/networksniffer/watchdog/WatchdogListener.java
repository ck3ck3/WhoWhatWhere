package whowhatwhere.model.networksniffer.watchdog;

import org.jnetpcap.packet.PcapPacket;

public interface WatchdogListener
{
	/**
	 * @param packet - the packet that matched the rule
	 */
	public void watchdogFoundMatchingPacket(PcapPacket packet, WatchdogMessage message);
}
