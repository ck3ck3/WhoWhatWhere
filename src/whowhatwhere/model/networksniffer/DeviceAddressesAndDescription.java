package whowhatwhere.model.networksniffer;

public class DeviceAddressesAndDescription
{
	private String ip;
	private byte[] macAddress;
	private String description;
	
	public DeviceAddressesAndDescription(String ip, byte[] macAddress, String description)
	{
		this.ip = ip;
		this.macAddress = macAddress;
		this.description = description;
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
}
