package whowhatwhere.controller.appearancecounter;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import whowhatwhere.controller.SecondaryFXMLWithCRUDTableController;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.view.SecondaryFXMLWithCRUDTableScreen;

public class ManageUserNotesScreen extends SecondaryFXMLWithCRUDTableScreen<UserNotesRowModel>
{
	private ManageUserNotesController userNotesController;
	private Properties propsNotes;
	private TableColumn<UserNotesRowModel, String> columnIP;
	private TableColumn<UserNotesRowModel, String> columnNotes;
	
	private final static String emptyCellString = "(Click to edit)";

	public ManageUserNotesScreen(String fxmlLocation, Stage stage, Scene scene, Properties propsNotes) throws IOException
	{
		super(fxmlLocation, stage, scene);

		this.propsNotes = propsNotes;
		userNotesController = (ManageUserNotesController) controller;
		columnIP = userNotesController.getColumnIP();
		columnNotes = userNotesController.getColumnNotes();
		
		table.getSortOrder().add(columnIP);
		initGUI();
	}
	
	@Override
	protected SecondaryFXMLWithCRUDTableController<UserNotesRowModel> initController()
	{
		return getLoader().<ManageUserNotesController> getController();
	}

	@Override
	protected ObservableList<UserNotesRowModel> getInitialTableItems()
	{
		return propertiesToObservableList(propsNotes);
	}

	@Override
	protected void setOnEditCommit()
	{
		columnIP.setOnEditCommit(rowModel -> 
		{
			String newContent = rowModel.getNewValue();
			String previousValue = rowModel.getOldValue();
			boolean isNewContentValid = isValidIPValue(newContent);
			UserNotesRowModel rowValue = rowModel.getRowValue();
			boolean ipAlreadyExists = propsNotes.containsKey(newContent);
			
			if (ipAlreadyExists)
			{
				new Alert(AlertType.ERROR, "This IP address already has a note.").showAndWait();
				rowValue.setIpAddress(previousValue);
			}
			else
			{
				rowValue.setIpAddress(isNewContentValid ? newContent : previousValue);
				
				if (isNewContentValid)
				{
					if (isValidNotesValue(rowValue.notesProperty().getValue()))
					{
						propsNotes.remove(previousValue);
						propsNotes.put(newContent, rowValue.notesProperty().getValue());
					}
				}
				else
					new Alert(AlertType.ERROR, "Please enter a valid IP address. If you want to delete this row, please select it and press the \"" + userNotesController.getBtnRemoveRow().getText() + "\" button.").showAndWait();
			}
			
			table.refresh();
		});
		
		columnNotes.setOnEditCommit(rowModel -> 
		{
			String newContent = rowModel.getNewValue();
			boolean isNewContentValid = isValidNotesValue(newContent);
			UserNotesRowModel rowValue = rowModel.getRowValue();
			
			rowValue.setNotes(isNewContentValid ? newContent : rowModel.getOldValue());
			
			if (isNewContentValid)
			{
				String ipAddressValue = rowValue.ipAddressProperty().getValue();
				
				if (isValidIPValue(ipAddressValue))
					propsNotes.put(ipAddressValue, newContent);
			}
			else
				new Alert(AlertType.ERROR, "Please enter a non-empty, non-default note. If you want to delete this row, please select it and press the \"" + userNotesController.getBtnRemoveRow().getText() + "\" button.").showAndWait();
			
			table.refresh();
		});		
	}
	
	private boolean isValidIPValue(String ip)
	{
		return NetworkSniffer.isValidIPv4(ip);
	}
	
	private boolean isValidNotesValue(String notes)
	{
		return !notes.isEmpty() && !notes.equals(emptyCellString);
	}

	@Override
	protected UserNotesRowModel newEmptyTableRow()
	{
		return new UserNotesRowModel(emptyCellString, emptyCellString);
	}

	@Override
	protected String filterRowsToDelete(ObservableList<UserNotesRowModel> initialSelectionOfRowsToDelete)
	{
		return null;
	}

	@Override
	protected void performForDeleteRows(List<UserNotesRowModel> listOfDeletedRows)
	{
		for (UserNotesRowModel row : listOfDeletedRows)
			propsNotes.remove(row.ipAddressProperty().getValue());
	}

	@Override
	protected void performOnCloseButton() throws IllegalArgumentException
	{
		for (UserNotesRowModel item : table.getItems())
			if (item.ipAddressProperty().get().equals(emptyCellString) || item.notesProperty().get().equals(emptyCellString))
			{
				new Alert(AlertType.ERROR, "At least one row is missing an IP address or note (or both). Either fill the missing data or delete the row.").showAndWait();
				throw new IllegalArgumentException();
			}
		
		AppearanceCounterUI.saveUserNotes(propsNotes);
	}
	
	private ObservableList<UserNotesRowModel> propertiesToObservableList(Properties props)
	{
		ObservableList<UserNotesRowModel> list = FXCollections.observableArrayList();
		
		for (Object ipObj : props.keySet())
		{
			String ip = (String)ipObj;
			list.add(new UserNotesRowModel(ip, props.getProperty(ip)));
		}
		
		return list;
	}
}
