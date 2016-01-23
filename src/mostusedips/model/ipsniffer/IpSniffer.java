package mostusedips.model.ipsniffer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapAddr;
import org.jnetpcap.PcapIf;
import org.jnetpcap.protocol.network.Icmp;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

import mostusedips.Main;

public class IpSniffer
{
	private FilteredCounterPacketHandler packetHandler;
	private Pcap pcap;
	private CaptureStartListener captureStartListener = null;
	private HashMap<String, PcapIf> ipToDevice = new HashMap<String, PcapIf>();
	
	public static final int ICMP_PROTOCOL = Icmp.ID;
	public static final int UDP_PROTOCOL = Udp.ID;
	public static final int TCP_PROTOCOL = Tcp.ID;
	public static final int HTTP_PROTOCOL = Http.ID;

	private static int snaplen = 64 * 1024; // Capture all packets, no truncation
	private static int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
	private static int timeout = 10 * 1000; // 10 seconds in milliseconds
	
	private final static String Ipv4Prefix = "INET4:";
	private final static String DLLName = "jnetpcap";

	private static final Logger logger = Logger.getLogger(IpSniffer.class.getPackage().getName());
	
	private boolean dllLoaded = false;
	
	public IpSniffer(String dllX86Location, String dllX64Location)
	{
		dllLoaded = loadJnetpcapDll(dllX86Location, dllX64Location);
	}
	
	public boolean isDllLoaded()
	{
		return dllLoaded;
	}
	
	/**
	 * @return true if successfully loaded, false otherwise
	 */
	private boolean loadJnetpcapDll(String dllX86Location, String dllX64Location)
	{
		try
		{
			System.loadLibrary(DLLName); //expected to throw exception on first run only
		}
		catch (UnsatisfiedLinkError ule)
		{
			try
			{
				if (!tryLoadingDll(dllX86Location, DLLName, false)) //if loading the x86 version failed, no need to log the error yet. try loading the x64 version
					return tryLoadingDll(dllX64Location, DLLName, true); //if this fails, write a log entry
			}
			catch (IOException ioe)
			{
				logger.log(Level.SEVERE, "Unable to copy dll from resources", ioe);
				return false;
			}
		}

		return true;
	}

	/**
	 * @param copyDllFrom
	 *            - relative path from the resources dir to the dll to be copied
	 * @param libName
	 *            - the library's name (without the ".dll")
	 * @param logULE
	 *            - if true, caught UnsatisfiedLinkError will be logged.
	 * @return true if successfully loaded, false otherwise
	 */
	private boolean tryLoadingDll(String copyDllFrom, String libName, boolean logULE) throws IOException
	{
		try
		{
			String currDir = System.getProperty("user.dir");
			InputStream dll = Main.class.getResourceAsStream(copyDllFrom);

			if (dll == null)
				throw new IOException("Unable to find " + copyDllFrom + " in resources");

			FileOutputStream dstFile = new FileOutputStream(currDir + "/" + libName + ".dll");

			IOUtils.copy(dll, dstFile);
			IOUtils.closeQuietly(dstFile);

			System.loadLibrary(libName);
		}
		catch (UnsatisfiedLinkError ule)
		{
			if (logULE)
				logger.log(Level.SEVERE, "Unable to load " + copyDllFrom, ule);

			return false;
		}

		return true;
	}

	public ArrayList<DeviceIPAndDescription> getListOfDevices(StringBuilder errbuf)
	{
		List<PcapIf> alldevs = new ArrayList<PcapIf>();
		ArrayList<DeviceIPAndDescription> ipAndDescList = new ArrayList<DeviceIPAndDescription>();

		int r = Pcap.findAllDevs(alldevs, errbuf);

		if (r == Pcap.ERROR || alldevs.isEmpty())
		{
			logger.log(Level.SEVERE, "Can't read list of devices, error is " + errbuf.toString());
			return null;
		}
		
		for (PcapIf device : alldevs)
		{
			String description = (device.getDescription() != null) ? device.getDescription() : "No description available";
			String IP = null;
			for (PcapAddr pcapAddr : device.getAddresses())
			{
				String temp = pcapAddr.getAddr().toString();

				if (temp.contains(Ipv4Prefix))
				{
					IP = temp.replace(Ipv4Prefix, "");
					break;
				}
			}

			if (IP == null)
				continue;
			
			ipAndDescList.add(new DeviceIPAndDescription(IP, description));
			ipToDevice.put(IP, device);
		}
		
		return ipAndDescList;
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

	public void startCapture(String IP, ArrayList<Integer> protocolsToCapture, StringBuilder errbuf)
	{
		PcapIf device = ipToDevice.get(IP);
		
		if (device == null)
		{
			logger.log(Level.SEVERE, "Unable to find device with IP " + IP);
			return;
		}
		
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