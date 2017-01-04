package whowhatwhere.controller.appearancecounter;

import java.io.IOException;
import java.util.Properties;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import whowhatwhere.model.ipsniffer.IPSniffer;
import whowhatwhere.view.SecondaryFXMLScreen;

public class ManageUserNotesScreen extends SecondaryFXMLScreen
{
	private final static String emptyCellString = "(Click to edit)";
	
	private ManageUserNotesController userNotesController;
	private Properties propsNotes;
	private TableView<UserNotesRowModel> table;
	private ObservableList<UserNotesRowModel> entryList;
	private TableColumn<UserNotesRowModel, String> columnIP;
	private TableColumn<UserNotesRowModel, String> columnNotes;

	public ManageUserNotesScreen(String fxmlLocation, Stage stage, Scene scene, Properties propsNotes) throws IOException
	{
		super(fxmlLocation, stage, scene);
		
		userNotesController = getLoader().<ManageUserNotesController> getController();
		table = userNotesController.getTable();
		this.propsNotes = propsNotes;
		columnIP = userNotesController.getColumnIP();
		columnNotes = userNotesController.getColumnNotes();
		
		table.getSortOrder().add(columnIP);
		
		entryList = propertiesToObservableList(propsNotes);
		table.setItems(entryList);

		initButtonHandlers();
		setOnEditCommit();
	}

	private void setOnEditCommit()
	{
		columnIP.setOnEditCommit(rowModel -> 
		{
			String newContent = rowModel.getNewValue();
			String previousValue = rowModel.getOldValue();
			boolean isNewContentValid = IPSniffer.isValidIPv4(newContent);
			
			rowModel.getRowValue().setIpAddress(isNewContentValid ? newContent : previousValue);
			
			if (isNewContentValid)
			{
				propsNotes.remove(previousValue);
				propsNotes.put(newContent, rowModel.getRowValue().notesProperty().getValue());
			}
			else
				new Alert(AlertType.ERROR, "Please enter a valid IP address. If you want to delete this row, please select it and press the \"" + userNotesController.getBtnRemoveRow().getText() + "\" button.").showAndWait();
			
			table.refresh();
		});
		
		columnNotes.setOnEditCommit(rowModel -> 
		{
			String newContent = rowModel.getNewValue();
			boolean isNewContentValid = !newContent.isEmpty() && !newContent.equals(emptyCellString);
			
			rowModel.getRowValue().setNotes(isNewContentValid ? newContent : rowModel.getOldValue());
			
			if (isNewContentValid)
			{
				propsNotes.put(rowModel.getRowValue().ipAddressProperty().getValue(), newContent);
			}
			else
				new Alert(AlertType.ERROR, "Please enter a non-empty, non-default note. If you want to delete this row, please select it and press the \"" + userNotesController.getBtnRemoveRow().getText() + "\" button.").showAndWait();
			
			table.refresh();
		});		
	}
	
	public Button getCloseButton()
	{
		return userNotesController.getCloseButton();
	}
	
	private void initButtonHandlers()
	{
		userNotesController.getBtnAddRow().setOnAction(event -> table.getItems().add(new UserNotesRowModel(emptyCellString, emptyCellString)));
		userNotesController.getBtnRemoveRow().setOnAction(event ->
		{
			ObservableList<UserNotesRowModel> selectedItems = table.getSelectionModel().getSelectedItems();

			if (selectedItems.isEmpty())
			{
				new Alert(AlertType.ERROR, "No lines selected.").showAndWait();
				return;
			}
			
			for (UserNotesRowModel row : selectedItems)
				propsNotes.remove(row.ipAddressProperty().getValue());
			
			entryList.removeAll(selectedItems);
			
		});
		
		userNotesController.getCloseButton().setOnAction(event -> AppearanceCounterUI.saveUserNotes(propsNotes));
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
