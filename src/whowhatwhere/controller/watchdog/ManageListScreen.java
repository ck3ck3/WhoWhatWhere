package whowhatwhere.controller.watchdog;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.filefilter.WildcardFileFilter;

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
import whowhatwhere.model.ipsniffer.IPSniffer;
import whowhatwhere.model.ipsniffer.firstsight.IPToMatch;
import whowhatwhere.view.SecondaryFXMLWithCRUDTableScreen;

public class ManageListScreen extends SecondaryFXMLWithCRUDTableScreen<IPToMatch>
{
	private ManageListController watchdogListController;
	private ObservableList<IPToMatch> entryList;
	private TextField textToSay;
	private TableColumn<IPToMatch, String> columnIP;
	private TableColumn<IPToMatch, String> columnProtocol;
	private TableColumn<IPToMatch, String> columnSrcPort;
	private TableColumn<IPToMatch, String> columnDstPort;
	
	private final static String emptyCellString = "(Click to edit)";
	

	public ManageListScreen(String fxmlLocation, Stage stage, Scene scene, WatchdogUI uiController) throws IOException
	{
		super(fxmlLocation, stage, scene);
		
		watchdogListController = (ManageListController) controller;
		entryList = uiController.getEntryList();
		textToSay = uiController.getTextMessage();
		
		columnIP = watchdogListController.getColumnIP();
		columnProtocol = watchdogListController.getColumnProtocol();
		columnSrcPort = watchdogListController.getColumnSrcPort();
		columnDstPort = watchdogListController.getColumnDstPort();

		initPresetButtonHandlers();
		initGUI();
	}
	

	@Override
	protected SecondaryFXMLWithCRUDTableController<IPToMatch> initController()
	{
		return getLoader().<ManageListController> getController();
	}

	@Override
	protected ObservableList<IPToMatch> getInitialTableItems()
	{
		return entryList;
	}

	@Override
	protected void setOnEditCommit()
	{
		columnIP.setOnEditCommit(rowModel -> 
		{
			String newContent = rowModel.getNewValue();
			String previousValue = rowModel.getOldValue();
			boolean isNewContentValid = isValidIPValue(newContent);
			IPToMatch rowValue = rowModel.getRowValue();
			
			rowValue.setIpAddress(isNewContentValid ? newContent : previousValue);
			
			if (!isNewContentValid)
				new Alert(AlertType.ERROR, "Please enter a valid IP address. If you want to delete this row, please select it and press the \"" + watchdogListController.getBtnRemoveRow().getText() + "\" button.").showAndWait();
			
			table.refresh();
		});
		
		columnProtocol.setOnEditCommit(rowModel -> rowModel.getRowValue().setProtocol(rowModel.getNewValue()));
			
		columnSrcPort.setOnEditCommit(getPortOnEditCommit(true));
		
		columnDstPort.setOnEditCommit(getPortOnEditCommit(false));
	}
	
	private EventHandler<CellEditEvent<IPToMatch, String>> getPortOnEditCommit(boolean srcPort)
	{
		return rowModel -> 
		{
			String newContent = rowModel.getNewValue();
			String previousValue = rowModel.getOldValue();
			boolean isNewContentValid = isValidPortValue(newContent);
			IPToMatch rowValue = rowModel.getRowValue();
			
			if (srcPort)
				rowValue.setSrcPort(isNewContentValid ? newContent : previousValue);
			else
				rowValue.setDstPort(isNewContentValid ? newContent : previousValue);
			
			if (!isNewContentValid)
				new Alert(AlertType.ERROR, "Port numbers must be between 1-65535. Please enter a valid port number or \"" + IPToMatch.protocol_ANY + "\" to check any port.").showAndWait();
			
			table.refresh();
		};
	}
	
	private boolean isValidIPValue(String ip)
	{
		return IPSniffer.isValidIPv4(ip);
	}
	
	private boolean isValidPortValue(String port)
	{
		if (port.equals(IPToMatch.port_ANY))
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

	@Override
	protected IPToMatch newEmptyTableRow()
	{
		return new IPToMatch(emptyCellString, IPToMatch.protocol_ANY, IPToMatch.port_ANY, IPToMatch.port_ANY);
	}

	@Override
	protected String filterRowsToDelete(ObservableList<IPToMatch> initialSelectionOfRowsToDelete)
	{
		return null;
	}

	@Override
	protected void performForDeleteRows(List<IPToMatch> listOfDeletedRows) {}

	@Override
	protected void performOnCloseButton() throws IllegalArgumentException 
	{
		boolean invalidLines = false;
		
		for (IPToMatch row : entryList)
		{
			if (!isValidIPValue(row.ipAddressProperty().getValue()))
			{
				invalidLines = true;
				break;
			}
		}
		
		if (invalidLines)
		{
			new Alert(AlertType.ERROR, "At least one row's IP column wasn't set. Please set a valid IP address in it or remove that row.").showAndWait();
			throw new IllegalArgumentException(); //don't close the window
		}
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
	
	public static MenuItem createMenuItem(ObservableList<IPToMatch> list, TextField textToSay, String filename)
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
