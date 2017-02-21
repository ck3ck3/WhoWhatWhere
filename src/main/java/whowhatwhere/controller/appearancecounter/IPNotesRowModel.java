package whowhatwhere.controller.appearancecounter;

import javafx.beans.property.SimpleStringProperty;

public class IPNotesRowModel
{
	private SimpleStringProperty ipAddress;
	private SimpleStringProperty notes;
	
	public IPNotesRowModel(String ipAddress, String notes)
	{
		this.ipAddress = new SimpleStringProperty(ipAddress);
		this.notes = new SimpleStringProperty(notes);
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
}
