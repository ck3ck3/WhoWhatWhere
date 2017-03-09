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
package whowhatwhere.controller.visualtrace;

import javafx.beans.property.SimpleStringProperty;

public class TraceLineInfo
{
	public enum Columns 
	{
		LABEL("label"), HOP("hopNumber"), PINGS("pingResults"), HOSTNAME("hostname"), IP("ipAddress"), LOCATION("location");
	
		private String columnName;
		
		private Columns(String columnName)
		{
			this.columnName = columnName;
		}
		
		public String getColumnName()
		{
			return columnName;
		}
	}
	
	private String label;
	
	private SimpleStringProperty hopNumber;
	private SimpleStringProperty pingResults;
	private SimpleStringProperty hostname;
	private SimpleStringProperty ipAddress;
	private SimpleStringProperty location;
	
	public TraceLineInfo()
	{
		this.label = "";
		this.hopNumber = new SimpleStringProperty();
		this.pingResults = new SimpleStringProperty();
		this.hostname = new SimpleStringProperty();
		this.ipAddress = new SimpleStringProperty();
		this.location = new SimpleStringProperty();
	}
	
	public TraceLineInfo(String label, String hopNumber, String pingResults, String hostname, String ipAddress, String location)
	{
		this.label = label;
		this.hopNumber = new SimpleStringProperty(hopNumber);
		this.pingResults = new SimpleStringProperty(pingResults);
		this.hostname = new SimpleStringProperty(hostname);
		this.ipAddress = new SimpleStringProperty(ipAddress);
		this.location = new SimpleStringProperty(location);
	}
	
	public void setColumnValue(Columns column, String value)
	{
		switch(column)
		{
			case LABEL:				setLabel(value);			break;
			case HOP:				setHopNumber(value);		break;
			case LOCATION:			setLocation(value);			break;
			case HOSTNAME:			setHostname(value);			break;
			case IP:				setIpAddress(value);		break;
			case PINGS:				setPingresults(value);		break;
		}
	}
	
	public String getLabel()
	{
		return label;
	}

	public SimpleStringProperty hopNumberProperty()
	{
		return hopNumber;
	}

	public SimpleStringProperty pingResultsProperty()
	{
		return pingResults;
	}

	public SimpleStringProperty hostnameProperty()
	{
		return hostname;
	}

	public SimpleStringProperty ipAddressProperty()
	{
		return ipAddress;
	}

	public SimpleStringProperty locationProperty()
	{
		return location;
	}
	
	public void setLabel(String label)
	{
		this.label = label;
	}

	public void setHopNumber(String hopNumber)
	{
		this.hopNumber.set(hopNumber);
	}

	public void setPingresults(String pingresults)
	{
		this.pingResults.set(pingresults);
	}

	public void setHostname(String hostname)
	{
		this.hostname.set(hostname);
	}

	public void setIpAddress(String ipAddress)
	{
		this.ipAddress.set(ipAddress);
	}

	public void setLocation(String location)
	{
		this.location.set(location);
	}
}
