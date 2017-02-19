package whowhatwhere.model.networksniffer.watchdog;

import java.io.Serializable;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import whowhatwhere.controller.watchdog.NumberRange;
import whowhatwhere.controller.watchdog.NumberRangeValues;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.PacketDirection;
import whowhatwhere.model.networksniffer.SupportedProtocols;

public class PacketTypeToMatch implements Serializable
{
	private static final long serialVersionUID = 3254209424151921883L; //auto-generated, modify if changes to the class are not backwards-compatible

	public final static String packetDirection_EMPTY = "";
	public final static String IPAddress_EMPTY = "";
	public final static String netmask_EMPTY = "";
	public final static String userNotes_EMPTY = "";
	public final static String packetOrPort_EMPTY = "";
	public final static String protocol_EMPTY = "";
	public final static String message_EMPTY = "";
	public final static String outputMethod_default = OutputMethod.TTS.toString();

	transient private SimpleStringProperty messageText;
	private String messageTextValue;

	transient private SimpleStringProperty messageOutputMethod;
	private OutputMethod messageOutputMethodValue;

	transient private SimpleStringProperty packetDirection;
	private PacketDirection packetDirectionValue;

	transient private SimpleStringProperty ipAddress;
	private String ipAddressValue;

	private String netmaskValue;

	private String userNotesValue;
	private List<String> ipsFromUserNotes;

	transient private SimpleStringProperty packetSize;
	private NumberRangeValues packetSizeValues;

	transient private SimpleStringProperty protocol;
	private SupportedProtocols protocolValue;

	transient private SimpleStringProperty srcPort;
	private NumberRangeValues srcPortValues;

	transient private SimpleStringProperty dstPort;
	private NumberRangeValues dstPortValues;

	public PacketTypeToMatch(String ipAddress, String netmask, String userNotes, List<String> ipsFromUserNotes, PacketDirection packetDirection, SupportedProtocols protocol, NumberRangeValues srcPortValues, NumberRangeValues dstPortValues, 
			NumberRangeValues packetSizeValues, String message, OutputMethod outputMethod)
	{
		setNetmask(netmask); //needs to be set before IP
		setIpAddress(ipAddress);
		setUserNotes(userNotes, ipsFromUserNotes);
		setPacketDirection(packetDirection);
		setProtocol(protocol);
		setSrcPort(srcPortValues);
		setDstPort(dstPortValues);
		setPacketSize(packetSizeValues);
		setMessageText(message);
		setMessageOutputMethod(outputMethod);
	}

	public void initAfterSerialization()
	{
		setNetmask(netmaskValue); //needs to be set before IP
		setIpAddress(ipAddressValue);
		setUserNotes(userNotesValue, ipsFromUserNotes);
		setPacketDirection(packetDirectionValue);
		setProtocol(protocolValue);
		setSrcPort(srcPortValues);
		setDstPort(dstPortValues);
		setPacketSize(packetSizeValues);
		setMessageText(messageTextValue);
		setMessageOutputMethod(messageOutputMethodValue);
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

		this.messageTextValue = message;
	}

	public SimpleStringProperty messageOutputMethodProperty()
	{
		return messageOutputMethod;
	}

	public void setMessageOutputMethod(OutputMethod outputMethod)
	{
		if (this.messageOutputMethod == null)
			this.messageOutputMethod = new SimpleStringProperty(outputMethod.toString());
		else
			this.messageOutputMethod.setValue(outputMethod.toString());

		this.messageOutputMethodValue = outputMethod;
	}

	public SimpleStringProperty packetDirectionProperty()
	{
		return packetDirection;
	}

	public void setPacketDirection(PacketDirection packetDirection)
	{
		String stringVal = packetDirection == null ? packetDirection_EMPTY : packetDirection.toString();

		if (this.packetDirection == null)
			this.packetDirection = new SimpleStringProperty(stringVal);
		else
			this.packetDirection.setValue(stringVal);

		packetDirectionValue = packetDirection;
	}

	public SimpleStringProperty ipAddressProperty()
	{
		return ipAddress;
	}

	public void setIpAddress(String ip)
	{
		String stringVal = ip == null ? IPAddress_EMPTY : ip;

		if (!stringVal.equals(IPAddress_EMPTY) && netmaskValue != null && !netmaskValue.equals(netmask_EMPTY) && !netmaskValue.equals("255.255.255.255"))
			stringVal = NetworkSniffer.getSubnetRange(stringVal, netmaskValue);
		
		if (this.ipAddress == null)
			this.ipAddress = new SimpleStringProperty(stringVal);
		else
			this.ipAddress.setValue(stringVal);

		this.ipAddressValue = ip;
	}

	public SimpleStringProperty protocolProperty()
	{
		return protocol;
	}

	public void setProtocol(SupportedProtocols protocol)
	{
		String stringVal = protocol == null ? protocol_EMPTY : protocol.toString();

		if (this.protocol == null)
			this.protocol = new SimpleStringProperty(stringVal);
		else
			this.protocol.setValue(stringVal);

		this.protocolValue = protocol;
	}

	public Integer protocolAsInt()
	{
		return (protocol == null || protocol.getValue().isEmpty() || protocol.getValue().equals(protocol_EMPTY) ? null : protocolValue.getValue());
	}

	public void setNetmask(String netmask)
	{
		netmaskValue = netmask;
	}

	public void setUserNotes(String userNotes, List<String> ipsFromUserNotes)
	{
		userNotesValue = userNotes;
		this.ipsFromUserNotes = ipsFromUserNotes;
		
		if (userNotes != null && !userNotes.isEmpty())
		{
			int addressCount = ipsFromUserNotes.size();
			String stringVal = userNotes + " [" + addressCount + " address" + (addressCount > 1 ? "es]" : "]");
			
			if (this.ipAddress == null)
				this.ipAddress = new SimpleStringProperty(stringVal);
			else
				this.ipAddress.setValue(stringVal);
		}
	}

	public SimpleStringProperty packetSizeProperty()
	{
		return packetSize;
	}

	public void setPacketSize(NumberRangeValues packetSizeValues)
	{
		String newStringValue = packetSizeValues == null ? packetOrPort_EMPTY : NumberRange.numberRangeStringRepresentation(packetSizeValues.getRange(), packetSizeValues.getLeftValue(), packetSizeValues.getRightValue()) + " bytes";

		if (packetSize == null)
			packetSize = new SimpleStringProperty(newStringValue);
		else
			packetSize.set(newStringValue);
		
		this.packetSizeValues = packetSizeValues;
	}

	public SimpleStringProperty srcPortProperty()
	{
		return srcPort;
	}

	public void setSrcPort(NumberRangeValues srcPortValues)
	{
		String newStringValue = srcPortValues == null ? packetOrPort_EMPTY : NumberRange.numberRangeStringRepresentation(srcPortValues.getRange(), srcPortValues.getLeftValue(), srcPortValues.getRightValue());

		if (srcPort == null)
			srcPort = new SimpleStringProperty(newStringValue);
		else
			srcPort.set(newStringValue);
		
		this.srcPortValues = srcPortValues;
	}

	public SimpleStringProperty dstPortProperty()
	{
		return dstPort;
	}

	public void setDstPort(NumberRangeValues dstPortValues)
	{
		String newStringValue = dstPortValues == null ? packetOrPort_EMPTY : NumberRange.numberRangeStringRepresentation(dstPortValues.getRange(), dstPortValues.getLeftValue(), dstPortValues.getRightValue());

		if (dstPort == null)
			dstPort = new SimpleStringProperty(newStringValue);
		else
			dstPort.set(newStringValue);
		
		this.dstPortValues = dstPortValues;
	}

	public List<String> getIPsFromUserNotes()
	{
		return ipsFromUserNotes;
	}

	public void setIPsFromUserNotes(List<String> ipsFromUserNotes)
	{
		this.ipsFromUserNotes = ipsFromUserNotes;
	}

	public String getMessageTextValue()
	{
		return messageTextValue;
	}

	public OutputMethod getMessageOutputMethodValue()
	{
		return messageOutputMethodValue;
	}

	public PacketDirection getPacketDirectionValue()
	{
		return packetDirectionValue;
	}

	public String getIpAddressValue()
	{
		return ipAddressValue;
	}

	public String getNetmaskValue()
	{
		return netmaskValue;
	}

	public String getUserNotesValue()
	{
		return userNotesValue;
	}

	public SupportedProtocols getProtocolValue()
	{
		return protocolValue;
	}

	public NumberRangeValues getPacketSizeValues()
	{
		return packetSizeValues;
	}

	public NumberRangeValues getSrcPortValues()
	{
		return srcPortValues;
	}

	public NumberRangeValues getDstPortValues()
	{
		return dstPortValues;
	}
}
