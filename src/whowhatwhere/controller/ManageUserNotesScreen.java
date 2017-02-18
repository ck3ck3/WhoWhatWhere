package whowhatwhere.controller;

import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import whowhatwhere.controller.appearancecounter.UserNotesRowModel;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLWithCRUDTableScreen;

public class ManageUserNotesScreen extends SecondaryFXMLWithCRUDTableScreen<UserNotesRowModel>
{
	private ManageUserNotesController userNotesController;
	private UserNotes userNotes;
	private TableColumn<UserNotesRowModel, String> columnIP;
	private TableColumn<UserNotesRowModel, String> columnNotes;
	
	private UserNotesRowModel rowBeingEdited;
	private boolean editedCellWasEmpty;
	
	private final static String emptyCellString = "(Click to edit)";

	public ManageUserNotesScreen(String fxmlLocation, Stage stage, Scene scene, UserNotes userNotes) throws IOException
	{
		super(fxmlLocation, stage, scene);

		this.userNotes = userNotes;
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
		ObservableList<UserNotesRowModel> list = FXCollections.observableArrayList();
		
		for (Object ipObj : userNotes.getIPSet())
		{
			String ip = (String)ipObj;
			list.add(new UserNotesRowModel(ip, userNotes.getUserNote(ip)));
		}
		
		return list;
	}
	
	@Override
	protected void setOnEditHandlers()
	{
		columnIP.setOnEditStart(rowModel ->
		{
			editedCellWasEmpty = rowModel.getOldValue().equals(emptyCellString);
			if (editedCellWasEmpty)
			{
				rowModel.getRowValue().setIpAddress("");
				rowBeingEdited = rowModel.getRowValue();
			}
		});
		columnIP.setOnEditCancel(rowModel -> 
		{
			if (editedCellWasEmpty)
				rowBeingEdited.setIpAddress(emptyCellString);
		});
		columnIP.setOnEditCommit(rowModel -> 
		{
			String newContent = rowModel.getNewValue();
			String previousValue = rowModel.getOldValue();
			boolean isNewContentValid = isValidIPValue(newContent);
			UserNotesRowModel rowValue = rowModel.getRowValue();
			boolean ipAlreadyExists = userNotes.containsIP(newContent);
			
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
						userNotes.removeUserNote(previousValue);
						userNotes.addUserNote(newContent, rowValue.notesProperty().getValue());
					}
				}
				else
				{
					new Alert(AlertType.ERROR, "Please enter a valid IP address. If you want to delete this row, please select it and press the \"" + userNotesController.getBtnRemoveRow().getText() + "\" button.").showAndWait();
					rowValue.setIpAddress(emptyCellString);
				}
			}
			
			table.refresh();
		});
		
		columnNotes.setOnEditStart(rowModel ->
		{
			editedCellWasEmpty = rowModel.getOldValue().equals(emptyCellString);
			if (editedCellWasEmpty)
			{
				rowModel.getRowValue().setNotes("");
				rowBeingEdited = rowModel.getRowValue();
			}
		});
		columnNotes.setOnEditCancel(rowModel -> 
		{
			if (editedCellWasEmpty)
				rowBeingEdited.setNotes(emptyCellString);
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
					userNotes.addUserNote(ipAddressValue, newContent);
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
			userNotes.removeUserNote(row.ipAddressProperty().getValue());
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
		
		userNotes.saveUserNotes();
	}
}
