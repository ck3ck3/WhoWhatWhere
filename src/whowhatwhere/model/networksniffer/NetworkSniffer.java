package whowhatwhere.model.networksniffer;

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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapAddr;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacketHandler;

import whowhatwhere.Main;
import whowhatwhere.model.networksniffer.appearancecounter.AppearanceCounterPacketHandler;
import whowhatwhere.model.networksniffer.appearancecounter.AppearanceCounterResults;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;
import whowhatwhere.model.networksniffer.watchdog.WatchdogListener;
import whowhatwhere.model.networksniffer.watchdog.WatchdogPacketHandler;

public class NetworkSniffer
{
	private static final Logger logger = Logger.getLogger(NetworkSniffer.class.getPackage().getName());
	private static boolean initSuccessful;
	private static List<String> errorList = new ArrayList<String>();

	static
	{
		initSuccessful = loadJnetpcapDll(Main.jnetpcapDLLx86Location, Main.jnetpcapDLLx64Location);

		if (!initSuccessful)
			logger.log(Level.SEVERE, "Unable to load jnetpcap.dll.");
	}

	private static int snaplen = 64 * 1024; // Capture all packets, no truncation
	private static int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
	private static int timeout = 1 * 1000; // 1 second in milliseconds

	private static final Pattern ipv4Pattern = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	private final static String Ipv4Prefix = "INET4:";
	private final static String DLLName = "jnetpcap";

	private Pcap pcap;
	private Map<String, PcapIf> ipToDevice = new HashMap<>();
	private List<NICInfo> ipAndDescList = new ArrayList<>();

	public NetworkSniffer() throws IllegalStateException
	{
		if (!initSuccessful)
			throw new IllegalStateException("Unable to load jnetpcap library. These errors were reported:\n" + String.join("\n", errorList));

		generateListOfDevices();
	}

	/**
	 * @return true if successfully loaded, false otherwise
	 */
	private static boolean loadJnetpcapDll(String dllX86Location, String dllX64Location)
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
	private static boolean tryLoadingDll(String copyDllFrom, String libName, boolean logULE) throws IOException
	{
		try
		{
			String currDir = System.getProperty("user.dir");
			InputStream dll = NetworkSniffer.class.getResourceAsStream(copyDllFrom);

			if (dll == null)
				throw new IOException("Unable to find " + copyDllFrom + " in resources");

			FileOutputStream dstFile = new FileOutputStream(currDir + "/" + libName + ".dll");

			IOUtils.copy(dll, dstFile);
			IOUtils.closeQuietly(dstFile);

			System.loadLibrary(libName);
		}
		catch (UnsatisfiedLinkError ule)
		{
			errorList.add("Trying to load " + copyDllFrom + " : " + ule.getMessage());

			if (logULE)
				logger.log(Level.SEVERE, String.join("\n", errorList));

			return false;
		}

		return true;
	}

	public List<NICInfo> getListOfDevices()
	{
		return ipAndDescList;
	}

	private void generateListOfDevices()
	{
		List<PcapIf> alldevs = new ArrayList<>();
		StringBuilder errbuf = new StringBuilder();

		int r = Pcap.findAllDevs(alldevs, errbuf);

		if (r == Pcap.ERROR || alldevs.isEmpty())
		{
			logger.log(Level.SEVERE, "Can't read list of devices, error is " + errbuf.toString());
			return;
		}

		for (PcapIf device : alldevs)
		{
			String description = (device.getDescription() != null) ? device.getDescription() : "No description available";
			String ip = null;
			for (PcapAddr pcapAddr : device.getAddresses())
			{
				String temp = pcapAddr.getAddr().toString();

				if (temp.contains(Ipv4Prefix))
				{
					ip = temp.replace(Ipv4Prefix, "");
					break;
				}
			}

			if (ip == null)
				continue;

			byte[] hardwareAddress;

			try
			{
				hardwareAddress = device.getHardwareAddress();
			}
			catch (IOException ioe)
			{
				hardwareAddress = new byte[8];
			}

			ipAndDescList.add(new NICInfo(ip, hardwareAddress, description));
			ipToDevice.put(ip, device);
		}
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

	public static boolean isValidIPv4(String ip)
	{
		return ipv4Pattern.matcher(ip).matches();
	}

	public static boolean isValidNetmask(String ip, String netmask)
	{
		try
		{
			new SubnetUtils(ip, netmask);
		}
		catch(IllegalArgumentException iae)
		{
			return false;
		}
		
		return true;
	}
	
	public static String getSubnetRange(String ip, String netmask)
	{
		try
		{
			SubnetUtils subnetUtils = new SubnetUtils(ip, netmask);
			subnetUtils.setInclusiveHostCount(true); //to allow one specific address with mask 255.255.255.255
			
			return subnetUtils.getInfo().getLowAddress() + " - " + subnetUtils.getInfo().getHighAddress();
		}
		catch(IllegalArgumentException iae)
		{
			return null;
		}
	}

	public void startAppearanceCounterCapture(String deviceIp, List<Integer> protocolsToCapture, StringBuilder errbuf)
	{
		startAppearanceCounterCapture(deviceIp, protocolsToCapture, null, errbuf);
	}

	public AppearanceCounterResults startAppearanceCounterCapture(String deviceIp, List<Integer> protocolsToCapture, CaptureStartListener listener, StringBuilder errbuf)
	{
		String ownAddress = deviceIp.substring(1, deviceIp.length() - 1);
		int ownIpInt;

		try
		{
			ownIpInt = stringToIntIp(ownAddress);
		}
		catch (UnknownHostException uhe)
		{
			logger.log(Level.SEVERE, "Unable convert own IP address " + ownAddress + " to integer. Debug info: " + ipToDevice.get(deviceIp).getAddresses().get(0).getAddr() + "\nUnable to capture");
			return null;
		}

		AppearanceCounterPacketHandler filteredCounterPH = new AppearanceCounterPacketHandler(ownIpInt, protocolsToCapture, listener);

		startCapture(deviceIp, filteredCounterPH, errbuf);

		return new AppearanceCounterResults(filteredCounterPH);
	}

	public void startWatchdogCapture(NICInfo deviceInfo, List<PacketTypeToMatch> packetTypeList, boolean isRepeated, Integer cooldownInSecs, WatchdogListener listener, StringBuilder errbuf)
			throws IllegalArgumentException, UnknownHostException
	{
		WatchdogPacketHandler watchdogPH = new WatchdogPacketHandler(packetTypeList, isRepeated, cooldownInSecs, listener, this, deviceInfo.getMACAddress());

		startCapture(deviceInfo.getIP(), watchdogPH, errbuf);
	}

	private void startCapture(String deviceIp, PcapPacketHandler<Void> packetHandler, StringBuilder errbuf)
	{
		PcapIf device = ipToDevice.get(deviceIp);

		if (device == null)
		{
			logger.log(Level.SEVERE, "Unable to find device with IP " + deviceIp);
			return;
		}

		pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);

		if (pcap == null)
		{
			logger.log(Level.SEVERE, "Error while opening device for capture: " + errbuf.toString());
			return;
		}

		pcap.loop(Pcap.LOOP_INFINITE, packetHandler, null);
	}

	public void stopCapture()
	{
		if (pcap != null)
			pcap.breakloop();
	}

	public void cleanup()
	{
		if (pcap != null)
		{
			stopCapture();
			pcap.close();
		}
	}

	/**
	 * 
	 * @param ip
	 *            - IP to ping
	 * @param timeout
	 *            - timeout in ms. -1 for default timeout
	 * @return The string "X milliseconds" (where X is the ping result). If the
	 *         ping timed out, returns "Timeout". If an error occurred, returns
	 *         "Error"
	 */
	public static String pingAsString(String ip, int timeout)
	{
		String command = "ping -n 1 " + (timeout > 0 ? "-w " + timeout + " " : "") + ip;
		String errorString = "Error";
		Process exec;

		try
		{
			exec = Runtime.getRuntime().exec(command);
			List<String> readLines = IOUtils.readLines(exec.getInputStream());
			String results = readLines.get(readLines.size() - 1);
			results = results.substring(results.lastIndexOf(' ')).trim();
			String ping;

			if (!results.contains("ms"))
				ping = "Timeout";
			else
				ping = results.replace("ms", " milliseconds"); //replace ms to milliseconds for a more user-friendly string

			return ping;
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to execute ping (failed to load Ping (command) screen)", e);
			return errorString;
		}
	}
}