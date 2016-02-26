package mostusedips.model.ipsniffer.appearancecounter;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;

import mostusedips.model.ipsniffer.CaptureStartListener;
import mostusedips.model.ipsniffer.IPSniffer;

public class AppearanceCounterPacketHandler implements PcapPacketHandler<Void>
{

	private HashMap<Integer, Integer> ipToAmountOfRepeats = new HashMap<Integer, Integer>();
	private int ownIpInt;
	private ArrayList<Integer> protocolsToCount = new ArrayList<Integer>();
	private boolean isFirstPacket = true;
	CaptureStartListener captureStartListener;

	public AppearanceCounterPacketHandler(int ownIpInt, ArrayList<Integer> listOfProtocls)
	{
		this(ownIpInt, listOfProtocls, null);
	}
	
	public AppearanceCounterPacketHandler(int ownIpInt, ArrayList<Integer> listOfProtocls, CaptureStartListener listener)
	{
		this.ownIpInt = ownIpInt;
		this.protocolsToCount = listOfProtocls;
		this.captureStartListener = listener;
	}

	public ArrayList<IpAppearancesCounter> getListOfIpAppearances()
	{
		ArrayList<IpAppearancesCounter> list = new ArrayList<IpAppearancesCounter>();
		String strKey;

		for (Integer key : ipToAmountOfRepeats.keySet())
		{
			try
			{
				strKey = IPSniffer.intToIpString(key.intValue());
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

		if (isFirstPacket && captureStartListener != null) //notify on first packet captured
		{
			isFirstPacket = false;
			captureStartListener.captureStartedNotification();
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
			if (packet.hasHeader(protocolId.intValue()))
				return true;

		return false;
	}

	public void setCaptureStartListener(CaptureStartListener captureStartListener)
	{
		this.captureStartListener = captureStartListener;
	}
}
