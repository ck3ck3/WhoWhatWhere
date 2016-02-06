package mostusedips.model.ipsniffer;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class IPToMatch
{
	public final static String protocol_ANY = "ANY";
	public final static String port_ANY = "0";
	
	private final SimpleStringProperty ip;
	private final SimpleStringProperty protocol;
	private final SimpleIntegerProperty srcPort;
	private final SimpleIntegerProperty dstPort;
	
	public IPToMatch(String ip, Integer protocol, Integer srcPort, Integer dstPort)
	{
		this(ip, (protocol != null ? IpSniffer.intProtocolToString(protocol) : protocol_ANY), srcPort, dstPort);		
	}
	
	public IPToMatch(String ip, String protocol, Integer srcPort, Integer dstPort)
	{
		this.ip = (ip != null ? new SimpleStringProperty(ip) : new SimpleStringProperty());
		this.protocol = (protocol != null ? new SimpleStringProperty(protocol) : new SimpleStringProperty());
		this.srcPort = (srcPort != null ? new SimpleIntegerProperty(srcPort) : new SimpleIntegerProperty());
		this.dstPort = (dstPort != null ? new SimpleIntegerProperty(dstPort) : new SimpleIntegerProperty());
	}
	
	public void init(String ip, String protocol, Integer srcPort, Integer dstPort)
	{
		this.ip.setValue(ip);
		this.protocol.setValue(protocol);
		this.srcPort.setValue(srcPort);
		this.dstPort.setValue(dstPort);
	}
	
	public SimpleStringProperty ipProperty()
	{
		return ip;
	}

	public void setIP(String ip)
	{
		this.ip.setValue(ip);
	}

	public SimpleStringProperty protocolProperty()
	{
		return protocol;
	}

	public void setProtocol(String protocol)
	{
		this.protocol.setValue(protocol);
	}
	
	public Integer protocolAsInt()
	{
		return (protocol == null || protocol.getValue().isEmpty() || protocol.getValue().equals(protocol_ANY) ? null : IpSniffer.stringProtocolToInt(protocol.getValue()));
	}

	public SimpleIntegerProperty srcPortProperty()
	{
		return srcPort;
	}

	public void setSrcPort(Integer port)
	{
		this.srcPort.setValue(port);
	}
	
	public SimpleIntegerProperty dstPortProperty()
	{
		return dstPort;
	}

	public void setDstPort(Integer port)
	{
		this.dstPort.setValue(port);
	}
}
