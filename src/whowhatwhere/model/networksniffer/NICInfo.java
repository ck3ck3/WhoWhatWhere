package whowhatwhere.model.networksniffer;

public class NICInfo
{
	private String ip;
	private byte[] macAddress;
	private String description;

	public NICInfo()
	{
	}

	public NICInfo(String ip, byte[] macAddress, String description)
	{
		this.ip = ip;
		this.macAddress = macAddress;
		this.description = description;
	}

	public void copyNICInfo(NICInfo other)
	{
		ip = other.ip;
		macAddress = other.macAddress;
		description = other.description;
	}

	public String getIP()
	{
		return ip;
	}

	public byte[] getMACAddress()
	{
		return macAddress;
	}

	public String getDescription()
	{
		return description;
	}

	@Override
	public String toString()
	{
		String hexString = javax.xml.bind.DatatypeConverter.printHexBinary(macAddress);
		String macAddressString = hexString.substring(0, 2) + ":" + hexString.substring(2, 4) + ":" + hexString.substring(4, 6) + ":" + hexString.substring(6, 8) + ":" + hexString.substring(8, 10)
				+ ":" + hexString.substring(10, 12);

		return description + " " + ip + " " + macAddressString;
	}
}
