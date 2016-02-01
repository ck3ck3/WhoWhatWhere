package mostusedips.model.ipsniffer;

public class IPToMatch
{
	private String ip;
	private Integer protocol;
	private Integer srcPort;
	private Integer dstPort;
	
	public IPToMatch(String ip, Integer protocol, Integer srcPort, Integer dstPort)
	{
		this.ip = ip;
		this.protocol = protocol;
		this.srcPort = srcPort;
		this.dstPort = dstPort;
	}

	public String getIP()
	{
		return ip;
	}

	public void setIP(String ip)
	{
		this.ip = ip;
	}

	public Integer getProtocol()
	{
		return protocol;
	}

	public void setProtocol(Integer protocol)
	{
		this.protocol = protocol;
	}

	public Integer getSrcPort()
	{
		return srcPort;
	}

	public void setSrcPort(Integer port)
	{
		this.srcPort = port;
	}
	
	public Integer getDstPort()
	{
		return dstPort;
	}

	public void setDstPort(Integer port)
	{
		this.dstPort = port;
	}
}
