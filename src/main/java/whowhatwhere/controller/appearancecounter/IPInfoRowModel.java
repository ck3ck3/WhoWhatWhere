/*******************************************************************************
 * Who What Where
 * Copyright (C) 2017  ck3ck3
 * https://github.com/ck3ck3/WhoWhatWhere
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package whowhatwhere.controller.appearancecounter;

import java.util.Arrays;
import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class IPInfoRowModel
{
	private Integer rowID;

//	ANY NEW PROPERTY THAT IS ADDED, MUST ALSO BE ADDED TO AppearanceCounterUI.mapColumnNameToPropertyValue() method
	
	private SimpleIntegerProperty packetCount;
	private SimpleStringProperty ipAddress;
	private SimpleStringProperty notes;
	private SimpleStringProperty owner;
	private SimpleStringProperty ping;
	private SimpleStringProperty country;
	private SimpleStringProperty region;
	private SimpleStringProperty city;

	public IPInfoRowModel(Integer rowID, Integer packetCount, String ipAddress, String notes, String owner, String ping, String country, String region, String city)
	{
		this.rowID = rowID;
		
		this.packetCount = new SimpleIntegerProperty(packetCount);
		this.ipAddress = new SimpleStringProperty(ipAddress);
		this.notes = new SimpleStringProperty(notes);
		this.owner = new SimpleStringProperty(owner);
		this.ping = new SimpleStringProperty(ping);
		this.country = new SimpleStringProperty(country);
		this.region = new SimpleStringProperty(region);
		this.city = new SimpleStringProperty(city);
	}
	
	public List<String> getFullRowDataAsOrderedList() //the items must be set in the order in which they will appear in the table
	{
		return Arrays.asList(packetCount.getValue().toString(), ipAddress.getValue(), notes.getValue(), owner.getValue(), ping.getValue(), country.getValue(), region.getValue(), city.getValue());
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

	public SimpleStringProperty notesProperty()
	{
		return notes;
	}
	
	public void setNotes(String notes)
	{
		this.notes.setValue(notes);
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
