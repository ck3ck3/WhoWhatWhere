package whowhatwhere.model.networksniffer.watchdog;

import java.io.Serializable;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import whowhatwhere.model.networksniffer.PacketDirection;
import whowhatwhere.model.networksniffer.SupportedProtocols;

public class PacketTypeToMatch implements Serializable
{
	private static final long serialVersionUID = -7741791299979590040L;	//auto-generated, modify if changes to the class are not backwards-compatible
	
	public final static String packetDirection_ANY = PacketDirection.ANY.toString();
	public final static String IP_ANY = "";
	public final static String netmask_ANY = "";
	public final static String userNotes_ANY = "";
	public final static String packetOrPort_ANY = "";
	public final static String protocol_ANY = "ANY";
	public final static String message_empty = "";
	public final static String outputMethod_default = OutputMethod.TTS.toString();
	
	transient private SimpleStringProperty messageText;
	private String messageTextToSerialize;
	
	transient private SimpleStringProperty messageOutputMethod;
	private OutputMethod messageOutputMethodToSerialize;
	
	transient private SimpleStringProperty packetDirection;
	private PacketDirection packetDirectionToSerialize;
	
	transient private SimpleStringProperty ipAddress;
	private String ipAddressToSerialize;
	
	transient private SimpleStringProperty netmask;
	private String netmaskToSerialize;
	
	transient private SimpleStringProperty userNotes;
	private String userNotesToSerialize;
	private List<String> ipsFromUserNotes;
	
	transient private SimpleStringProperty packetSizeSmaller;
	private String packetSizeSmallerToSerialize;
	
	transient private SimpleStringProperty packetSizeEquals;
	private String packetSizeEqualsToSerialize;
	
	transient private SimpleStringProperty packetSizeGreater;
	private String packetSizeGreaterToSerialize;
	
	transient private SimpleStringProperty protocol;
	private SupportedProtocols protocolToSerialize;
	
	transient private SimpleStringProperty srcPortSmaller;
	private String srcPortSmallerToSerialize;
	
	transient private SimpleStringProperty srcPortEquals;
	private String srcPortEqualsToSerialize;
	
	transient private SimpleStringProperty srcPortGreater;
	private String srcPortGreaterToSerialize;
	
	transient private SimpleStringProperty dstPortSmaller;
	private String dstPortSmallerToSerialize;
	
	transient private SimpleStringProperty dstPortEquals;
	private String dstPortEqualsToSerialize;
	
	transient private SimpleStringProperty dstPortGreater;
	private String dstPortGreaterToSerialize;
	

	public PacketTypeToMatch(String message, String outputMethod, String packetDirection, String ipAddress, String netmask, String userNotes, String packetSizeSmaller, String packetSizeEquals, String packetSizeGreater, String protocol, 
			String srcPortSmaller, String srcPortEquals, String srcPortGreater, String dstPortSmaller, String dstPortEquals, String dstPortGreater)
	{
		
		setMessageText(message);
		setMessageOutputMethod(outputMethod);
		setPacketDirection(packetDirection);
		setIpAddress(ipAddress);
		setNetmask(netmask);
		setUserNotes(userNotes);
		setPacketSizeSmaller(packetSizeSmaller);
		setPacketSizeEquals(packetSizeEquals);
		setPacketSizeGreater(packetSizeGreater);
		setProtocol(protocol);
		setSrcPortSmaller(srcPortSmaller);
		setSrcPortEquals(srcPortEquals);
		setSrcPortGreater(srcPortGreater);
		setDstPortSmaller(dstPortSmaller);
		setDstPortEquals(dstPortEquals);
		setDstPortGreater(dstPortGreater);
	}
	
	public void initAfterSerialization()
	{
		setMessageText(messageTextToSerialize);
		setMessageOutputMethod(messageOutputMethodToSerialize.toString());
		setPacketDirection(packetDirectionToSerialize.toString());
		setIpAddress(ipAddressToSerialize);
		setNetmask(netmaskToSerialize);
		setUserNotes(userNotesToSerialize);
		setPacketSizeSmaller(packetSizeSmallerToSerialize);
		setPacketSizeEquals(packetSizeEqualsToSerialize);
		setPacketSizeGreater(packetSizeGreaterToSerialize);
		setProtocol(protocolToSerialize == null ? protocol_ANY : protocolToSerialize.toString()); //since ANY isn't in the enum
		setSrcPortSmaller(srcPortSmallerToSerialize);
		setSrcPortEquals(srcPortEqualsToSerialize);
		setSrcPortGreater(srcPortGreaterToSerialize);
		setDstPortSmaller(dstPortSmallerToSerialize);
		setDstPortEquals(dstPortEqualsToSerialize);
		setDstPortGreater(dstPortGreaterToSerialize);
	}
	
	public SimpleStringProperty messageTextProperty()
	{
		return messageText;
	}

	public void setMessageText(String message)
	{
		if (this.messageText == null)
			this.messageText = new SimpleStringProperty(message);
		else
			this.messageText.setValue(message);
		
		this.messageTextToSerialize = message;
	}
	
	public SimpleStringProperty messageOutputMethodProperty()
	{
		return messageOutputMethod;
	}

	public void setMessageOutputMethod(String outputMethod)
	{
		if (this.messageOutputMethod == null)
			this.messageOutputMethod = new SimpleStringProperty(outputMethod);
		else
			this.messageOutputMethod.setValue(outputMethod);
		
		this.messageOutputMethodToSerialize = OutputMethod.stringToEnum(outputMethod);
	}
	
	public OutputMethod messageOutputMethodAsEnum()
	{
		return messageOutputMethodToSerialize;
	}
	
	public SimpleStringProperty packetDirectionProperty()
	{
		return packetDirection;
	}

	public void setPacketDirection(String packetDirection)
	{
		if (this.packetDirection == null)
			this.packetDirection = new SimpleStringProperty(packetDirection);
		else
			this.packetDirection.setValue(packetDirection);
		
		packetDirectionToSerialize = PacketDirection.stringToEnum(packetDirection);
	}
	
	public PacketDirection packetDirectoinAsEnum()
	{
		return packetDirectionToSerialize;
	}
	
	public SimpleStringProperty ipAddressProperty()
	{
		return ipAddress;
	}

	public void setIpAddress(String ip)
	{
		if (this.ipAddress == null)
			this.ipAddress = new SimpleStringProperty(ip);
		else
			this.ipAddress.setValue(ip);
		
		this.ipAddressToSerialize = ip;
	}

	public SimpleStringProperty protocolProperty()
	{
		return protocol;
	}

	public void setProtocol(String protocol)
	{
		if (this.protocol == null)
			this.protocol = new SimpleStringProperty(protocol);
		else
			this.protocol.setValue(protocol);
		
		this.protocolToSerialize = SupportedProtocols.stringToEnum(protocol);
	}
	
	public Integer protocolAsInt()
	{
		return (protocol == null || protocol.getValue().isEmpty() || protocol.getValue().equals(protocol_ANY) ? null : protocolToSerialize.getValue());
	}
	
	public SupportedProtocols protocolAsEnum()
	{
		return protocolToSerialize;
	}
	
	public boolean isSameValuesAs(PacketTypeToMatch otherEntry)
	{
		if (!packetDirection.get().equals(otherEntry.packetDirection.get()))
			return false;
		
		if (!ipAddress.get().equals(otherEntry.ipAddress.get()))
			return false;
		
		if (!netmask.get().equals(otherEntry.netmask.get()))
			return false;
		
		if (!userNotes.get().equals(otherEntry.userNotes.get()))
			return false;
		
		if (!packetSizeSmaller.get().equals(otherEntry.packetSizeSmaller.get()))
			return false;
		
		if (!packetSizeEquals.get().equals(otherEntry.packetSizeEquals.get()))
			return false;
		
		if (!packetSizeGreater.get().equals(otherEntry.packetSizeGreater.get()))
			return false;
		
		if (!protocol.get().equals(otherEntry.protocol.get()))
			return false;
		
		if (!srcPortSmaller.get().equals(otherEntry.srcPortSmaller.get()))
			return false;
		
		if (!dstPortEquals.get().equals(otherEntry.dstPortEquals.get()))
			return false;
		
		if (!srcPortGreater.get().equals(otherEntry.srcPortGreater.get()))
			return false;
		
		if (!dstPortSmaller.get().equals(otherEntry.dstPortSmaller.get()))
			return false;
		
		if (!dstPortEquals.get().equals(otherEntry.dstPortEquals.get()))
			return false;
		
		if (!dstPortGreater.get().equals(otherEntry.dstPortGreater.get()))
			return false;
		
		return true;
	}

	public SimpleStringProperty netmaskProperty()
	{
		return netmask;
	}

	public void setNetmask(String netmask)
	{
		if (this.netmask == null)
			this.netmask = new SimpleStringProperty(netmask);
		else
			this.netmask.setValue(netmask);
		
		netmaskToSerialize = netmask;
	}
	
	public SimpleStringProperty userNotesProperty()
	{
		return userNotes;
	}

	public void setUserNotes(String userNotes)
	{
		if (this.userNotes == null)
			this.userNotes = new SimpleStringProperty(userNotes);
		else
			this.userNotes.setValue(userNotes);
		
		userNotesToSerialize = userNotes;
	}
	
	public SimpleStringProperty packetSizeSmallerProperty()
	{
		return packetSizeSmaller;
	}

	public void setPacketSizeSmaller(String packetSizeSmaller)
	{
		if (this.packetSizeSmaller == null)
			this.packetSizeSmaller = new SimpleStringProperty(packetSizeSmaller);
		else
			this.packetSizeSmaller.setValue(packetSizeSmaller);
		
		packetSizeSmallerToSerialize = packetSizeSmaller;
	}
	
	public SimpleStringProperty packetSizeEqualsProperty()
	{
		return packetSizeEquals;
	}

	public void setPacketSizeEquals(String packetSizeEquals)
	{
		if (this.packetSizeEquals == null)
			this.packetSizeEquals = new SimpleStringProperty(packetSizeEquals);
		else
			this.packetSizeEquals.setValue(packetSizeEquals);
		
		packetSizeEqualsToSerialize = packetSizeEquals;
	}
	
	public SimpleStringProperty packetSizeGreaterProperty()
	{
		return packetSizeGreater;
	}

	public void setPacketSizeGreater(String packetSizeGreater)
	{
		if (this.packetSizeGreater == null)
			this.packetSizeGreater = new SimpleStringProperty(packetSizeGreater);
		else
			this.packetSizeGreater.setValue(packetSizeGreater);
		
		packetSizeGreaterToSerialize = packetSizeGreater;
	}
	
	public SimpleStringProperty srcPortSmallerProperty()
	{
		return srcPortSmaller;
	}

	public void setSrcPortSmaller(String srcPortSmaller)
	{
		if (this.srcPortSmaller == null)
			this.srcPortSmaller = new SimpleStringProperty(srcPortSmaller);
		else
			this.srcPortSmaller.setValue(srcPortSmaller);
		
		srcPortSmallerToSerialize = srcPortSmaller;
	}
	
	public SimpleStringProperty srcPortEqualsProperty()
	{
		return srcPortEquals;
	}

	public void setSrcPortEquals(String port)
	{
		if (this.srcPortEquals == null)
			this.srcPortEquals = new SimpleStringProperty(port);
		else
			this.srcPortEquals.setValue(port);
		
		this.srcPortEqualsToSerialize = port;
	}
	
	public SimpleStringProperty srcPortGreaterProperty()
	{
		return srcPortGreater;
	}

	public void setSrcPortGreater(String srcPortGreater)
	{
		if (this.srcPortGreater == null)
			this.srcPortGreater = new SimpleStringProperty(srcPortGreater);
		else
			this.srcPortGreater.setValue(srcPortGreater);
		
		srcPortGreaterToSerialize = srcPortGreater;
	}
	
	public SimpleStringProperty dstPortSmallerProperty()
	{
		return dstPortSmaller;
	}

	public void setDstPortSmaller(String dstPortSmaller)
	{
		if (this.dstPortSmaller == null)
			this.dstPortSmaller = new SimpleStringProperty(dstPortSmaller);
		else
			this.dstPortSmaller.setValue(dstPortSmaller);
		
		dstPortSmallerToSerialize = dstPortSmaller;
	}
	
	public SimpleStringProperty dstPortEqualsProperty()
	{
		return dstPortEquals;
	}

	public void setDstPortEquals(String port)
	{
		if (this.dstPortEquals == null)
			this.dstPortEquals = new SimpleStringProperty(port);
		else
			this.dstPortEquals.setValue(port);
		
		this.dstPortEqualsToSerialize = port;
	}
	
	public SimpleStringProperty dstPortGreaterProperty()
	{
		return dstPortGreater;
	}

	public void setDstPortGreater(String dstPortGreater)
	{
		if (this.dstPortGreater == null)
			this.dstPortGreater = new SimpleStringProperty(dstPortGreater);
		else
			this.dstPortGreater.setValue(dstPortGreater);
		
		this.dstPortGreaterToSerialize = dstPortGreater;
	}

	public List<String> getIPsFromUserNotes()
	{
		return ipsFromUserNotes;
	}

	public void setIPsFromUserNotes(List<String> ipsFromUserNotes)
	{
		this.ipsFromUserNotes = ipsFromUserNotes;
	}
}
