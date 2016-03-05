package whowhatwhere.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class IPInfoRowModel
{
	private Integer rowID;

	private SimpleIntegerProperty packetCount;
	private SimpleStringProperty ipAddress;
	private SimpleStringProperty owner;
	private SimpleStringProperty ping;
	private SimpleStringProperty country;
	private SimpleStringProperty region;
	private SimpleStringProperty city;

	public IPInfoRowModel(Integer rowID, Integer packetCount, String ipAddress, String owner, String ping, String country, String region, String city)
	{
		this.rowID = new Integer(rowID);
		
		this.packetCount = new SimpleIntegerProperty(packetCount);
		this.ipAddress = new SimpleStringProperty(ipAddress);
		this.owner = new SimpleStringProperty(owner);
		this.ping = new SimpleStringProperty(ping);
		this.country = new SimpleStringProperty(country);
		this.region = new SimpleStringProperty(region);
		this.city = new SimpleStringProperty(city);
	}

	public SimpleIntegerProperty packetCountProperty()
	{
		return packetCount;
	}

	public void setPacketCount(Integer packetCount)
	{
		this.packetCount.setValue(packetCount);
	}

	public SimpleStringProperty ipAddressProperty()
	{
		return ipAddress;
	}

	public void setIpAddress(String ipAddress)
	{
		this.ipAddress.setValue(ipAddress);
	}

	public SimpleStringProperty ownerProperty()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner.setValue(owner);
	}

	public SimpleStringProperty countryProperty()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country.setValue(country);
	}

	public SimpleStringProperty regionProperty()
	{
		return region;
	}

	public void setRegion(String region)
	{
		this.region.setValue(region);
	}

	public SimpleStringProperty cityProperty()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city.setValue(city);
	}

	public Integer getRowID()
	{
		return rowID;
	}

	public void setRowID(Integer rowID)
	{
		this.rowID = rowID;
	}

	public SimpleStringProperty pingProperty()
	{
		return ping;
	}

	public void setPing(String ping)
	{
		this.ping.setValue(ping);
	}

}
