package whowhatwhere.controller.appearancecounter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

public class ManageUserNotesController implements Initializable
{
	@FXML
	private TableView<UserNotesRowModel> tableEntries;
	@FXML
	private TableColumn<UserNotesRowModel, String> columnIP;
	@FXML
	private TableColumn<UserNotesRowModel, String> columnNotes;
	@FXML
	private Button btnClose;
	@FXML
	private Button btnAddRow;
	@FXML
	private Button btnRemoveRow;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		columnIP.setCellValueFactory(new PropertyValueFactory<UserNotesRowModel, String>("ipAddress"));
		columnIP.setCellFactory(TextFieldTableCell.forTableColumn());
		columnNotes.setCellValueFactory(new PropertyValueFactory<UserNotesRowModel, String>("notes"));
		columnNotes.setCellFactory(TextFieldTableCell.forTableColumn());
		
		tableEntries.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
	
	public TableView<UserNotesRowModel> getTable()
	{
		return tableEntries;
	}
	
	public Button getBtnAddRow()
	{
		return btnAddRow;
	}

	public Button getBtnRemoveRow()
	{
		return btnRemoveRow;
	}

	public Button getCloseButton()
	{
		return btnClose;
	}
	
	public TableColumn<UserNotesRowModel, String> getColumnIP()
	{
		return columnIP;
	}
	
	public TableColumn<UserNotesRowModel, String> getColumnNotes()
	{
		return columnNotes;
	}
}
