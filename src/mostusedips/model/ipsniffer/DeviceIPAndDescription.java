package mostusedips.model.ipsniffer;

public class DeviceIPAndDescription
{
	private String ip;
	private String description;
	
	public DeviceIPAndDescription(String ip, String description)
	{
		this.ip = ip;
		this.description = description;
	}
	
	public String getIP()
	{
		return ip;
	}
	
	public String getDescription()
	{
		return description;
	}
}
