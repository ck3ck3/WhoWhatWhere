package mostusedips.model.ipsniffer;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

import mostusedips.Main;

public class IpSniffer
{
	private FilteredCounterPacketHandler packetHandler;
	private Pcap pcap;
	private CaptureStartListener captureStartListener = null;

	private static int snaplen = 64 * 1024; // Capture all packets, no truncation
	private static int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
	private static int timeout = 10 * 1000; // 10 seconds in milliseconds

	private static final Logger logger = Logger.getLogger(Main.getAppName());

	public List<PcapIf> getListOfDevices(StringBuilder errbuf)
	{
		List<PcapIf> alldevs = new ArrayList<PcapIf>();

		int r = Pcap.findAllDevs(alldevs, errbuf);

		if (r == Pcap.ERROR || alldevs.isEmpty())
		{
			logger.log(Level.SEVERE, "Can't read list of devices, error is " + errbuf.toString());
			return null;
		}

		return alldevs;
	}

	public static String intToIpString(int intIp) throws UnknownHostException
	{
		byte[] bytes = BigInteger.valueOf(intIp).toByteArray();

		return InetAddress.getByAddress(bytes).getHostAddress();
	}

	public static int stringToIntIp(String str) throws UnknownHostException
	{
		InetAddress bar = InetAddress.getByName(str);
		return ByteBuffer.wrap(bar.getAddress()).getInt();
	}

	public void startCapture(PcapIf device, ArrayList<Integer> protocolsToCapture, StringBuilder errbuf)
	{
		pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);

		if (pcap == null)
		{
			logger.log(Level.SEVERE, "Error while opening device for capture: " + errbuf.toString());
			return;
		}

		String ownAddress = device.getAddresses().get(0).getAddr().toString();
		ownAddress = ownAddress.substring(7, ownAddress.length() - 1);

		int ownIpInt;

		try
		{
			ownIpInt = stringToIntIp(ownAddress);
		}
		catch (UnknownHostException uhe)
		{
			logger.log(Level.SEVERE, "Unable convert own IP address " + ownAddress + " to integer. Debug info: " + device.getAddresses().get(0).getAddr() + "\nUnable to capture");
			return;
		}

		packetHandler = new FilteredCounterPacketHandler(ownIpInt, protocolsToCapture);

		if (captureStartListener != null)
			packetHandler.setCaptureStartListener(captureStartListener);

		pcap.loop(Pcap.LOOP_INFINITE, packetHandler, null);

	}

	public void stopCapture()
	{
		pcap.breakloop();
	}

	public ArrayList<IpAppearancesCounter> getResults()
	{
		return packetHandler.getListOfIpAppearances();
	}

	public void cleanup()
	{
		if (pcap != null)
		{
			stopCapture();
			pcap.close();
		}
	}

	public void setCapureStartListener(CaptureStartListener listener)
	{
		captureStartListener = listener;
	}
}