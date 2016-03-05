package mostusedips.model.ipsniffer.appearancecounter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;

import mostusedips.model.ipsniffer.CaptureStartListener;
import mostusedips.model.ipsniffer.IPSniffer;

public class AppearanceCounterPacketHandler implements PcapPacketHandler<Void>
{

	private Map<Integer, Integer> ipToAmountOfRepeats = new HashMap<>();
	private int ownIpInt;
	private List<Integer> protocolsToCount = new ArrayList<>();
	private boolean isFirstPacket = true;
	CaptureStartListener captureStartListener;

	public AppearanceCounterPacketHandler(int ownIpInt, List<Integer> listOfProtocls)
	{
		this(ownIpInt, listOfProtocls, null);
	}
	
	public AppearanceCounterPacketHandler(int ownIpInt, List<Integer> listOfProtocls, CaptureStartListener listener)
	{
		this.ownIpInt = ownIpInt;
		this.protocolsToCount = listOfProtocls;
		this.captureStartListener = listener;
	}

	public List<IpAppearancesCounter> getListOfIpAppearances()
	{
		List<IpAppearancesCounter> list = new ArrayList<>();
		String strKey;

		for (Integer key : ipToAmountOfRepeats.keySet())
		{
			try
			{
				strKey = IPSniffer.intToIpString(key);
			}
			catch (UnknownHostException uhe)
			{
				continue; //ignore this packet
			}

			list.add(new IpAppearancesCounter(strKey, ipToAmountOfRepeats.get(key)));
		}

		Collections.sort(list);

		return list;
	}

	@Override
	public void nextPacket(PcapPacket packet, Void nothing)
	{
		if (isFirstPacket)
		{
			if (captureStartListener != null) //notify on first packet captured
				captureStartListener.captureStartedNotification();
		
			isFirstPacket = false;
		}
		
		if (packet.hasHeader(Ip4.ID) && (protocolsToCount.isEmpty() || isSelectedProtocol(packet))) //only if there's an IP layer, and if any filter is selected, filter it 
		{
			Ip4 ipHeader = new Ip4();
			ipHeader = packet.getHeader(ipHeader);

			int sourceInt = ipHeader.sourceToInt();
			int destInt = ipHeader.destinationToInt();

			int key;

			if (ownIpInt == sourceInt)
				key = destInt;
			else
				if (ownIpInt == destInt)
					key = sourceInt;
				else //not relevant to us
					return;

			Integer repeats = ipToAmountOfRepeats.get(key);

			if (repeats == null) //first time we see this ip
				ipToAmountOfRepeats.put(key, 1);
			else
				ipToAmountOfRepeats.put(key, repeats + 1);
		}
	}

	private boolean isSelectedProtocol(PcapPacket packet)
	{
		for (Integer protocolId : protocolsToCount)
			if (packet.hasHeader(protocolId))
				return true;

		return false;
	}

	public void setCaptureStartListener(CaptureStartListener captureStartListener)
	{
		this.captureStartListener = captureStartListener;
	}
}
