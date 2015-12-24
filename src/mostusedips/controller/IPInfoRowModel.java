package mostusedips.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class IPInfoRowModel
{
	private Integer rowID;

	private Integer packetCount;
	private String ipAddress;
	private String owner;
	private String ping;
	private String country;
	private String region;
	private String city;

	public IPInfoRowModel(Integer rowID, Integer packetCount, String ipAddress, String owner, String ping, String country, String region, String city)
	{
		this.rowID = rowID;
		this.setPacketCount(new SimpleIntegerProperty(packetCount));
		this.setIpAddress(new SimpleStringProperty(ipAddress));
		this.setOwner(new SimpleStringProperty(owner));
		this.setPing(new SimpleStringProperty(ping));
		this.setCountry(new SimpleStringProperty(country));
		this.setRegion(new SimpleStringProperty(region));
		this.setCity(new SimpleStringProperty(city));
	}

	public Integer getPacketCount()
	{
		return packetCount;
	}

	public void setPacketCount(SimpleIntegerProperty packetCount)
	{
		this.packetCount = packetCount.get();
	}

	public String getIpAddress()
	{
		return ipAddress;
	}

	public void setIpAddress(SimpleStringProperty ipAddress)
	{
		this.ipAddress = ipAddress.get();
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(SimpleStringProperty owner)
	{
		this.owner = owner.get();
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(SimpleStringProperty country)
	{
		this.country = country.get();
	}

	public String getRegion()
	{
		return region;
	}

	public void setRegion(SimpleStringProperty region)
	{
		this.region = region.get();
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(SimpleStringProperty city)
	{
		this.city = city.get();
	}

	public Integer getRowID()
	{
		return rowID;
	}

	public void setRowID(Integer rowID)
	{
		this.rowID = rowID;
	}

	public String getPing()
	{
		return ping;
	}

	public void setPing(SimpleStringProperty ping)
	{
		this.ping = ping.get();
	}

}
