package mostusedips.model.ipsniffer;

import java.io.Serializable;
import java.util.HashMap;

import javafx.beans.property.SimpleStringProperty;

public class IPToMatch implements Serializable
{
	private static final long serialVersionUID = 4082672055903423251L; //auto-generated, modify if changes to the class are not backwards-compatible
	public final static String protocol_ANY = "ANY";
	public final static String port_ANY = "ANY";
	
	transient private SimpleStringProperty ip;
	private String ipToSerialize;
	
	transient private SimpleStringProperty protocol;
	private String protocolToSerialize;
	
	transient private SimpleStringProperty srcPort;
	private String srcPortToSerialize;
	
	transient private SimpleStringProperty dstPort;
	private String dstPortToSerialize;
	
	public IPToMatch(String ip, Integer protocol, String srcPort, String dstPort)
	{
		this(ip, (protocol != null ? IpSniffer.intProtocolToString(protocol) : protocol_ANY), srcPort, dstPort);		
	}
	
	public IPToMatch(String ip, String protocol, String srcPort, String dstPort)
	{
		init(ip, protocol, srcPort, dstPort);
	}
	
	public void init(String ip, String protocol, String srcPort, String dstPort)
	{
		setIP(ip);
		setProtocol(protocol);
		setSrcPort(srcPort);
		setDstPort(dstPort);
	}
	
	public void initAfterSerialization()
	{
		setIP(ipToSerialize);
		setProtocol(protocolToSerialize);
		setSrcPort(srcPortToSerialize);
		setDstPort(dstPortToSerialize);
	}
	
	public SimpleStringProperty ipProperty()
	{
		return ip;
	}

	public void setIP(String ip)
	{
		if (this.ip == null)
			this.ip = (ip != null ? new SimpleStringProperty(ip) : new SimpleStringProperty());
		else
			this.ip.setValue(ip);
		
		this.ipToSerialize = ip;
	}

	public SimpleStringProperty protocolProperty()
	{
		return protocol;
	}

	public void setProtocol(String protocol)
	{
		if (this.protocol == null)
			this.protocol = (protocol != null ? new SimpleStringProperty(protocol) : new SimpleStringProperty());
		else
			this.protocol.setValue(protocol);
		
		this.protocolToSerialize = protocol;
	}
	
	public Integer protocolAsInt()
	{
		return (protocol == null || protocol.getValue().isEmpty() || protocol.getValue().equals(protocol_ANY) ? null : IpSniffer.stringProtocolToInt(protocol.getValue()));
	}

	public SimpleStringProperty srcPortProperty()
	{
		return srcPort;
	}

	public void setSrcPort(String port)
	{
		if (this.srcPort == null)
			this.srcPort = (port != null ? new SimpleStringProperty(port) : new SimpleStringProperty());
		else
			this.srcPort.setValue(port);
		
		this.srcPortToSerialize = port;
	}
	
	public SimpleStringProperty dstPortProperty()
	{
		return dstPort;
	}

	public void setDstPort(String port)
	{
		if (this.dstPort == null)
			this.dstPort = (port != null ? new SimpleStringProperty(port) : new SimpleStringProperty());
		else
			this.dstPort.setValue(port);
		
		this.dstPortToSerialize = port;
	}
	
	public HashMap<String, String> getDataAsMap()
	{
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put("ip", ip.getValue());
		map.put("protocol", protocol.getValue());
		map.put("srcPort", srcPort.getValue());
		map.put("dstPort", dstPort.getValue());
		
		return map;
	}
}
