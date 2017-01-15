package whowhatwhere.controller.watchdog;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.net.util.SubnetUtils;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import whowhatwhere.controller.SecondaryFXMLWithCRUDTableController;
import whowhatwhere.model.criteria.RelativeToValue;
import whowhatwhere.model.ipsniffer.IPSniffer;
import whowhatwhere.model.ipsniffer.firstsight.PacketTypeToMatch;
import whowhatwhere.view.SecondaryFXMLWithCRUDTableScreen;

public class ManageListScreen extends SecondaryFXMLWithCRUDTableScreen<PacketTypeToMatch>
{
	private ManageListController watchdogListController;
	private ObservableList<PacketTypeToMatch> entryList;
	private TextField textToSay;
	private TableColumn<PacketTypeToMatch, String> columnPacketDirection;
	private TableColumn<PacketTypeToMatch, String> columnIP;
	private TableColumn<PacketTypeToMatch, String> columnNetmask;
	private TableColumn<PacketTypeToMatch, String> columnUserNotes;
//	private TableColumn<IPToMatch, String> columnPacketSize;
	private TableColumn<PacketTypeToMatch, String> columnPacketSizeSmaller;
	private TableColumn<PacketTypeToMatch, String> columnPacketSizeEquals;
	private TableColumn<PacketTypeToMatch, String> columnPacketSizeGreater;
	private TableColumn<PacketTypeToMatch, String> columnProtocol;
	private TableColumn<PacketTypeToMatch, String> columnSrcPortSmaller;
	private TableColumn<PacketTypeToMatch, String> columnSrcPortEquals;
	private TableColumn<PacketTypeToMatch, String> columnSrcPortGreater;
//	private TableColumn<IPToMatch, String> columnSrcPort;
//	private TableColumn<IPToMatch, String> columnDstPort;
	private TableColumn<PacketTypeToMatch, String> columnDstPortSmaller;
	private TableColumn<PacketTypeToMatch, String> columnDstPortEquals;
	private TableColumn<PacketTypeToMatch, String> columnDstPortGreater;	
	private Map<String, List<String>> userNotesToIPListMap;
	
	private final PacketTypeToMatch emptyRow = newEmptyTableRow();
	

	public ManageListScreen(String fxmlLocation, Stage stage, Scene scene, WatchdogUI uiController) throws IOException
	{
		super(fxmlLocation, stage, scene);
		
		watchdogListController = (ManageListController) controller;
		entryList = uiController.getEntryList();
		textToSay = uiController.getTextMessage();
		userNotesToIPListMap = uiController.getUserNotesReverseMap();
		watchdogListController.setUserNotesComboValues(userNotesToIPListMap.keySet().toArray());
		
		columnPacketDirection = watchdogListController.getColumnPacketDirection();
		columnIP = watchdogListController.getColumnIP();
		columnNetmask = watchdogListController.getColumnNetmask();
		columnUserNotes = watchdogListController.getColumnUserNotes();
		columnPacketSizeSmaller = watchdogListController.getColumnPacketSizeSmaller();
		columnPacketSizeEquals = watchdogListController.getColumnPacketSizeEquals();
		columnPacketSizeGreater = watchdogListController.getColumnPacketSizeGreater();
		columnProtocol = watchdogListController.getColumnProtocol();
		columnSrcPortSmaller = watchdogListController.getColumnSrcPortSmaller();
		columnSrcPortEquals = watchdogListController.getColumnSrcPortEquals();
		columnSrcPortGreater = watchdogListController.getColumnSrcPortGreater();
		columnDstPortSmaller = watchdogListController.getColumnDstPortSmaller();
		columnDstPortEquals = watchdogListController.getColumnDstPortEquals();
		columnDstPortGreater = watchdogListController.getColumnDstPortGreater();

		initPresetButtonHandlers();
		initGUI();
	}

	@Override
	protected SecondaryFXMLWithCRUDTableController<PacketTypeToMatch> initController()
	{
		return getLoader().<ManageListController> getController();
	}

	@Override
	protected ObservableList<PacketTypeToMatch> getInitialTableItems()
	{
		return entryList;
	}

	@Override
	protected void setOnEditCommit()
	{
		columnPacketDirection.setOnEditCommit(rowModel -> rowModel.getRowValue().setPacketDirection(rowModel.getNewValue()));
		
		columnIP.setOnEditCommit(rowModel -> 
		{
			String newContent = rowModel.getNewValue();
			String previousValue = rowModel.getOldValue();
			boolean isNewContentValid = isValidIPValue(newContent);
			PacketTypeToMatch rowValue = rowModel.getRowValue();
			
			rowValue.setIpAddress(isNewContentValid ? newContent : previousValue);
			
			if (isNewContentValid && rowValue.netmaskProperty().get().equals(PacketTypeToMatch.netmask_ANY)) //if the ip is valid and no netmask is set, set netmask for specific ip
				rowValue.setNetmask("255.255.255.255");
				
			
			if (!isNewContentValid)
				new Alert(AlertType.ERROR, "Please enter a valid IP address.").showAndWait();
			
			table.refresh();
		});
		
		columnNetmask.setOnEditCancel(rowModel -> 
		{
			PacketTypeToMatch rowValue = rowModel.getRowValue();
			String ipAddress = rowValue.ipAddressProperty().get();
			boolean isNewContentValid = !ipAddress.equals(PacketTypeToMatch.IP_ANY); //first check, was an ip entered?
			String newContent = rowModel.getNewValue();
			String previousValue = rowModel.getOldValue();
			
			if (!isNewContentValid)
				new Alert(AlertType.ERROR, "Please enter an IP address first.").showAndWait();
			
			if (isNewContentValid)
			{
				try
				{
					 new SubnetUtils(ipAddress, newContent); //second check, is the subnet valid
				}
				catch(IllegalArgumentException iae)
				{
					isNewContentValid = false;
					new Alert(AlertType.ERROR, "Invalid netmask.").showAndWait();
				}
			}
			
			rowValue.setNetmask(isNewContentValid ? newContent : previousValue);
			table.refresh();
		});
		
		columnUserNotes.setOnEditCommit(rowModel -> rowModel.getRowValue().setUserNotes(rowModel.getNewValue()));
		
		columnPacketSizeSmaller.setOnEditCommit(getPacketSizeOnEditCommit(RelativeToValue.LESS_THAN));
		columnPacketSizeEquals.setOnEditCommit(getPacketSizeOnEditCommit(RelativeToValue.EQUALS));
		columnPacketSizeGreater.setOnEditCommit(getPacketSizeOnEditCommit(RelativeToValue.GREATER_THAN));
		
		columnProtocol.setOnEditCommit(rowModel -> rowModel.getRowValue().setProtocol(rowModel.getNewValue()));
			
		columnSrcPortSmaller.setOnEditCommit(getPortOnEditCommit(true, RelativeToValue.LESS_THAN));
		columnSrcPortEquals.setOnEditCommit(getPortOnEditCommit(true, RelativeToValue.EQUALS));
		columnSrcPortGreater.setOnEditCommit(getPortOnEditCommit(true, RelativeToValue.GREATER_THAN));
		
		columnDstPortSmaller.setOnEditCommit(getPortOnEditCommit(false, RelativeToValue.LESS_THAN));
		columnDstPortEquals.setOnEditCommit(getPortOnEditCommit(false, RelativeToValue.EQUALS));
		columnDstPortGreater.setOnEditCommit(getPortOnEditCommit(false, RelativeToValue.GREATER_THAN));
	}
	
	/**
	 * @param isSrcPort - true if it's for source port, false if it's for destination port
	 * @param sign - the sign of the column (<, =, >)
	 * @return an EventHandler to be used for OnEditCommit
	 */
	private EventHandler<CellEditEvent<PacketTypeToMatch, String>> getPortOnEditCommit(boolean isSrcPort, RelativeToValue sign)
	{
		return rowModel -> 
		{
			String newContent = rowModel.getNewValue();
			String previousValue = rowModel.getOldValue();
			boolean isNewContentValid = isValidPortValue(newContent);
			PacketTypeToMatch rowValue = rowModel.getRowValue();
			
			if (isSrcPort)
			{
				switch(sign)
				{
					case LESS_THAN:		rowValue.setSrcPortSmaller(isNewContentValid ? newContent : previousValue);	break;
					case EQUALS:		rowValue.setSrcPortEquals(isNewContentValid ? newContent : previousValue);	break;
					case GREATER_THAN:	rowValue.setSrcPortGreater(isNewContentValid ? newContent : previousValue);	break;
				}
			}
			else
			{
				switch(sign)
				{
					case LESS_THAN:		rowValue.setDstPortSmaller(isNewContentValid ? newContent : previousValue);	break;
					case EQUALS:		rowValue.setDstPortEquals(isNewContentValid ? newContent : previousValue);	break;
					case GREATER_THAN:	rowValue.setDstPortGreater(isNewContentValid ? newContent : previousValue);	break;
				}
			}
			
			if (!isNewContentValid)
				new Alert(AlertType.ERROR, "Port numbers must be between 1-65535. Please enter a valid port number or leave this field empty.").showAndWait();
			
			table.refresh();
		};
	}
	
	/**
	 * @param sign - the sign of the column (<, =, >)
	 * @return an EventHandler to be used for OnEditCommit
	 */
	private EventHandler<CellEditEvent<PacketTypeToMatch, String>> getPacketSizeOnEditCommit(RelativeToValue sign)
	{
		return rowModel -> 
		{
			String newContent = rowModel.getNewValue();
			String previousValue = rowModel.getOldValue();
			boolean isNewContentValid = isValidPacketSizeValue(newContent);
			PacketTypeToMatch rowValue = rowModel.getRowValue();
			
			switch(sign)
			{
				case LESS_THAN:		rowValue.setPacketSizeSmaller(isNewContentValid ? newContent : previousValue);	break;
				case EQUALS:		rowValue.setPacketSizeEquals(isNewContentValid ? newContent : previousValue);	break;
				case GREATER_THAN:	rowValue.setPacketSizeGreater(isNewContentValid ? newContent : previousValue);	break;
			}
			
			if (!isNewContentValid)
				new Alert(AlertType.ERROR, "Packet size must be a number >= 0. Please enter a valid packet size or leave this field empty.").showAndWait();
			
			table.refresh();
		};
	}
	
	private boolean isValidIPValue(String ip)
	{
		return IPSniffer.isValidIPv4(ip);
	}
	
	private boolean isValidPortValue(String port)
	{
		if (port.equals(PacketTypeToMatch.packetOrPort_ANY))
			return true;
		
		int value;
		
		try
		{
			value = Integer.parseInt(port);
		}
		catch(NumberFormatException nfe)
		{
			return false;
		}
		
		return value >= 1 && value <= 65535;
	}
	
	private boolean isValidPacketSizeValue(String size)
	{
		if (size.equals(PacketTypeToMatch.packetOrPort_ANY))
			return true;
		
		int value;
		
		try
		{
			value = Integer.parseInt(size);
		}
		catch(NumberFormatException nfe)
		{
			return false;
		}
		
		return value >= 0;
	}

	@Override
	protected PacketTypeToMatch newEmptyTableRow()
	{
		return new PacketTypeToMatch(PacketTypeToMatch.packetDirection_ANY, PacketTypeToMatch.IP_ANY, PacketTypeToMatch.netmask_ANY, PacketTypeToMatch.userNotes_ANY, 
				PacketTypeToMatch.packetOrPort_ANY, PacketTypeToMatch.packetOrPort_ANY, PacketTypeToMatch.packetOrPort_ANY, PacketTypeToMatch.protocol_ANY, 
				PacketTypeToMatch.packetOrPort_ANY, PacketTypeToMatch.packetOrPort_ANY, PacketTypeToMatch.packetOrPort_ANY, PacketTypeToMatch.packetOrPort_ANY, PacketTypeToMatch.packetOrPort_ANY, PacketTypeToMatch.packetOrPort_ANY);
	}

	@Override
	protected String filterRowsToDelete(ObservableList<PacketTypeToMatch> initialSelectionOfRowsToDelete)
	{
		return null;
	}

	@Override
	protected void performForDeleteRows(List<PacketTypeToMatch> listOfDeletedRows) {}

	@Override
	protected void performOnCloseButton() throws IllegalArgumentException 
	{
		boolean invalidLines = false;
		
		for (PacketTypeToMatch row : entryList)
		{
			if (isOnlyUnsetValues(row))
			{
				invalidLines = true;
				break;
			}
		}
		
		if (invalidLines)
		{
			new Alert(AlertType.ERROR, "At least one row's values are all un-set. Please set a value in at least one column.").showAndWait();
			throw new IllegalArgumentException(); //don't close the window
		}
	}
	
	private boolean isOnlyUnsetValues(PacketTypeToMatch entry)
	{
		return emptyRow.isSameValuesAs(entry);
	}
	
	private void initPresetButtonHandlers()
	{
		watchdogListController.getBtnSavePreset().setOnAction(event ->
		{
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Save Preset");
			dialog.setHeaderText("Save this preset for future use");
			dialog.setContentText("Please enter preset name:");

			Optional<String> result = dialog.showAndWait();

			result.ifPresent(filename -> 
			{
				try
				{
					WatchdogUI.saveListToFile(entryList, textToSay.getText(), filename + WatchdogUI.presetExtension);
				}
				catch (IOException ioe)
				{
					new Alert(AlertType.ERROR, "Unable to save preset: " + ioe.getMessage()).showAndWait();
					return;
				}

				MenuItem menuItem = ManageListScreen.createMenuItem(entryList, textToSay, filename);
				
				ObservableList<MenuItem> items = watchdogListController.getMenuBtnLoadPreset().getItems();
				
				if (items.get(0).isDisable()) //it only contains the disabled "none found " item, remove it before adding new one
					items.clear();
					
				items.add(menuItem);
			});
		});

		initLoadPresetButton();
	}
	
	private void initLoadPresetButton()
	{
		ObservableList<MenuItem> items = watchdogListController.getMenuBtnLoadPreset().getItems();
		items.clear();

		File dir = new File(System.getProperty("user.dir"));
		FileFilter fileFilter = new WildcardFileFilter("*" + WatchdogUI.presetExtension);
		File[] files = dir.listFiles(fileFilter);

		for (File file : files)
			items.add(createMenuItem(entryList, textToSay, file.getName().replace(WatchdogUI.presetExtension, "")));
		
		if (items.isEmpty())
		{
			MenuItem none = new MenuItem("No presets found");
			
			none.setDisable(true);
			items.add(none);
		}
	}
	
	public static MenuItem createMenuItem(ObservableList<PacketTypeToMatch> list, TextField textToSay, String filename)
	{
		MenuItem menuItem = new MenuItem(filename);

		menuItem.setOnAction(event ->
		{
			try
			{
				WatchdogUI.loadListFromFile(list, textToSay, filename + WatchdogUI.presetExtension);
			}
			catch (ClassNotFoundException | IOException e)
			{
				new Alert(AlertType.ERROR, "Unable to load preset: " + e.getMessage()).showAndWait();
			}
		});
		
		return menuItem;
	}
}
