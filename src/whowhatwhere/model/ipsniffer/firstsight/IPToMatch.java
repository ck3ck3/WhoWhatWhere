package whowhatwhere.model.ipsniffer.firstsight;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.SimpleStringProperty;
import whowhatwhere.model.ipsniffer.IPSniffer;

public class IPToMatch implements Serializable
{
	private static final long serialVersionUID = 4082672055903423251L; //auto-generated, modify if changes to the class are not backwards-compatible
	public final static String protocol_ANY = "ANY";
	public final static String port_ANY = "ANY";
	
	transient private SimpleStringProperty ipAddress;
	private String ipToSerialize;
	
	transient private SimpleStringProperty protocol;
	private String protocolToSerialize;
	
	transient private SimpleStringProperty srcPort;
	private String srcPortToSerialize;
	
	transient private SimpleStringProperty dstPort;
	private String dstPortToSerialize;
	
	public IPToMatch(String ip, Integer protocol, String srcPort, String dstPort)
	{
		this(ip, (protocol != null ? IPSniffer.intProtocolToString(protocol) : protocol_ANY), srcPort, dstPort);		
	}
	
	public IPToMatch(String ip, String protocol, String srcPort, String dstPort)
	{
		init(ip, protocol, srcPort, dstPort);
	}
	
	public void init(String ip, String protocol, String srcPort, String dstPort)
	{
		setIpAddress(ip);
		setProtocol(protocol);
		setSrcPort(srcPort);
		setDstPort(dstPort);
	}
	
	public void initAfterSerialization()
	{
		setIpAddress(ipToSerialize);
		setProtocol(protocolToSerialize);
		setSrcPort(srcPortToSerialize);
		setDstPort(dstPortToSerialize);
	}
	
	public SimpleStringProperty ipAddressProperty()
	{
		return ipAddress;
	}

	public void setIpAddress(String ip)
	{
		if (this.ipAddress == null)
			this.ipAddress = (ip != null ? new SimpleStringProperty(ip) : new SimpleStringProperty());
		else
			this.ipAddress.setValue(ip);
		
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
		return (protocol == null || protocol.getValue().isEmpty() || protocol.getValue().equals(protocol_ANY) ? null : IPSniffer.stringProtocolToInt(protocol.getValue()));
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
	
	public Map<String, String> getDataAsMap()
	{
		Map<String, String> map = new HashMap<>();
		
		map.put("ip", ipAddress.getValue());
		map.put("protocol", protocol.getValue());
		map.put("srcPort", srcPort.getValue());
		map.put("dstPort", dstPort.getValue());
		
		return map;
	}
	
	public boolean isSameValuesAs(IPToMatch otherEntry)
	{
		if (!ipAddress.get().equals(otherEntry.ipAddress.get()))
			return false;
		
		if (!protocol.get().equals(otherEntry.protocol.get()))
			return false;
		
		if (!srcPort.get().equals(otherEntry.srcPort.get()))
			return false;
		
		if (!dstPort.get().equals(otherEntry.dstPort.get()))
			return false;
		
		return true;
	}
}
