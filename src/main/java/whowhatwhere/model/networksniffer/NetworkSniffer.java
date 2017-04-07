/*******************************************************************************
 * Who What Where
 * Copyright (C) 2017  ck3ck3
 * https://github.com/ck3ck3/WhoWhatWhere
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package whowhatwhere.model.networksniffer;

import java.io.IOException;
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

import whowhatwhere.model.networksniffer.appearancecounter.AppearanceCounterPacketHandler;
import whowhatwhere.model.networksniffer.appearancecounter.AppearanceCounterResults;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;
import whowhatwhere.model.networksniffer.watchdog.WatchdogListener;
import whowhatwhere.model.networksniffer.watchdog.WatchdogPacketHandler;

public class NetworkSniffer
{
	private static final Logger logger = Logger.getLogger(NetworkSniffer.class.getPackage().getName());
	
	public final static int defaultPingTimeout = -1;
	public final static String pingError = "Error";
	public final static String pingTimeout = "Request timed out";

	private static int snaplen = 64 * 1024; // Capture all packets, no truncation
	private static int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
	private static int timeout = 1 * 1000; // 1 second in milliseconds

	private static final Pattern ipv4Pattern = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	private final static String Ipv4Prefix = "INET4:";

	private Pcap pcap;
	private List<NICInfo> ipAndDescList = new ArrayList<>();
	private Map<NICInfo, PcapIf> nicInfoToPcapIf = new HashMap<>();

	public NetworkSniffer() throws IllegalStateException
	{
		generateListOfDevicesWithIPs();
	}


	public List<NICInfo> getListOfDevicesWithIP()
	{
		return ipAndDescList;
	}

	private void generateListOfDevicesWithIPs()
	{
		List<PcapIf> deviceList = new ArrayList<>();
		StringBuilder errbuf = new StringBuilder();

		int r = Pcap.findAllDevs(deviceList, errbuf);

		if (r == Pcap.ERROR || deviceList.isEmpty())
		{
			logger.log(Level.SEVERE, "Can't read list of devices, error is " + errbuf.toString());
			return;
		}
		
		for (PcapIf device : deviceList)
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

			if (ip == null || ip.equals("[0.0.0.0]"))
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

			NICInfo nicInfo = new NICInfo(ip, hardwareAddress, description);
			ipAndDescList.add(nicInfo);
			nicInfoToPcapIf.put(nicInfo, device);
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

	public void startAppearanceCounterCapture(NICInfo device, List<Integer> protocolsToCapture, StringBuilder errbuf)
	{
		startAppearanceCounterCapture(device, protocolsToCapture, null, errbuf);
	}

	public AppearanceCounterResults startAppearanceCounterCapture(NICInfo device, List<Integer> protocolsToCapture, CaptureStartListener listener, StringBuilder errbuf)
	{
		String deviceIp = device.getIP();
		String ownAddress = deviceIp.substring(1, deviceIp.length() - 1);
		int ownIpInt;

		try
		{
			ownIpInt = stringToIntIp(ownAddress);
		}
		catch (UnknownHostException uhe)
		{
			logger.log(Level.SEVERE, "Unable convert own IP address " + ownAddress + " to integer. Unable to capture");
			return null;
		}

		AppearanceCounterPacketHandler filteredCounterPH = new AppearanceCounterPacketHandler(ownIpInt, protocolsToCapture, listener);

		startCapture(nicInfoToPcapIf.get(device), filteredCounterPH, errbuf);

		return new AppearanceCounterResults(filteredCounterPH);
	}

	public void startWatchdogCapture(NICInfo deviceInfo, List<PacketTypeToMatch> packetTypeList, boolean isRepeated, Integer cooldownInSecs, WatchdogListener listener, StringBuilder errbuf)
			throws IllegalArgumentException, UnknownHostException
	{
		WatchdogPacketHandler watchdogPH = new WatchdogPacketHandler(packetTypeList, isRepeated, cooldownInSecs, listener, this, deviceInfo.getMACAddress());

		startCapture(nicInfoToPcapIf.get(deviceInfo), watchdogPH, errbuf);
	}

	private void startCapture(PcapIf device, PcapPacketHandler<Void> packetHandler, StringBuilder errbuf)
	{
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
	 *            - timeout in ms. {@code defaultPingTimeout} for default timeout
	 * @return The string "X milliseconds" (where X is the ping result). If the
	 *         ping timed out, returns {@code pingTimeout}. If an error occurred, returns
	 *         {@code pingError}
	 */
	public static String pingAsString(String ip, int timeout)
	{
		String command = "ping -n 1 " + (timeout != defaultPingTimeout ? "-w " + timeout + " " : "") + ip;
		Process exec;

		try
		{
			exec = Runtime.getRuntime().exec(command);
			List<String> readLines = IOUtils.readLines(exec.getInputStream());
			String results = readLines.get(readLines.size() - 1);
			results = results.substring(results.lastIndexOf(' ')).trim();
			String ping;

			if (!results.contains("ms"))
				ping = pingTimeout;
			else
				ping = results.replace("ms", " milliseconds"); //replace ms to milliseconds for a more user-friendly string

			return ping;
		}
		catch (IOException e)
		{
			logger.log(Level.SEVERE, "Unable to execute ping command", e);
			return pingError;
		}
	}
}
