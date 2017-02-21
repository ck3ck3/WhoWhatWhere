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
import whowhatwhere.controller.appearancecounter.IPNotesRowModel;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.view.secondaryfxmlscreen.SecondaryFXMLWithCRUDTableScreen;

public class ManageIPNotesScreen extends SecondaryFXMLWithCRUDTableScreen<IPNotesRowModel>
{
	private ManageIPNotesController ipNotesController;
	private IPNotes ipNotes;
	private TableColumn<IPNotesRowModel, String> columnIP;
	private TableColumn<IPNotesRowModel, String> columnNotes;
	
	private IPNotesRowModel rowBeingEdited;
	private boolean editedCellWasEmpty;
	
	private final static String emptyCellString = "(Click to edit)";

	public ManageIPNotesScreen(String fxmlLocation, Stage stage, Scene scene, IPNotes ipNot) throws IOException
	{
		super(fxmlLocation, stage, scene);

		this.ipNotes = ipNot;
		ipNotesController = (ManageIPNotesController) controller;
		columnIP = ipNotesController.getColumnIP();
		columnNotes = ipNotesController.getColumnNotes();
		
		table.getSortOrder().add(columnIP);
		initGUI();
	}
	
	@Override
	protected SecondaryFXMLWithCRUDTableController<IPNotesRowModel> initController()
	{
		return getLoader().<ManageIPNotesController> getController();
	}

	@Override
	protected ObservableList<IPNotesRowModel> getInitialTableItems()
	{
		ObservableList<IPNotesRowModel> list = FXCollections.observableArrayList();
		
		for (Object ipObj : ipNotes.getIPSet())
		{
			String ip = (String)ipObj;
			list.add(new IPNotesRowModel(ip, ipNotes.getIPNote(ip)));
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
			IPNotesRowModel rowValue = rowModel.getRowValue();
			boolean ipAlreadyExists = ipNotes.containsIP(newContent);
			
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
						ipNotes.removeIPNote(previousValue);
						ipNotes.addIPNote(newContent, rowValue.notesProperty().getValue());
					}
				}
				else
				{
					new Alert(AlertType.ERROR, "Please enter a valid IP address. If you want to delete this row, please select it and press the \"" + ipNotesController.getBtnRemoveRow().getText() + "\" button.").showAndWait();
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
			IPNotesRowModel rowValue = rowModel.getRowValue();
			
			rowValue.setNotes(isNewContentValid ? newContent : rowModel.getOldValue());
			
			if (isNewContentValid)
			{
				String ipAddressValue = rowValue.ipAddressProperty().getValue();
				
				if (isValidIPValue(ipAddressValue))
					ipNotes.addIPNote(ipAddressValue, newContent);
			}
			else
				new Alert(AlertType.ERROR, "Please enter a non-empty, non-default note. If you want to delete this row, please select it and press the \"" + ipNotesController.getBtnRemoveRow().getText() + "\" button.").showAndWait();
			
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
	protected IPNotesRowModel newEmptyTableRow()
	{
		return new IPNotesRowModel(emptyCellString, emptyCellString);
	}

	@Override
	protected String filterRowsToDelete(ObservableList<IPNotesRowModel> initialSelectionOfRowsToDelete)
	{
		return null;
	}

	@Override
	protected void performForDeleteRows(List<IPNotesRowModel> listOfDeletedRows)
	{
		for (IPNotesRowModel row : listOfDeletedRows)
			ipNotes.removeIPNote(row.ipAddressProperty().getValue());
	}

	@Override
	protected void performOnCloseButton() throws IllegalArgumentException
	{
		for (IPNotesRowModel item : table.getItems())
			if (item.ipAddressProperty().get().equals(emptyCellString) || item.notesProperty().get().equals(emptyCellString))
			{
				new Alert(AlertType.ERROR, "At least one row is missing an IP address or note (or both). Either fill the missing data or delete the row.").showAndWait();
				throw new IllegalArgumentException();
			}
		
		ipNotes.saveIPNotes();
	}
}
