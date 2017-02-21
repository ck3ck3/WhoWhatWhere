package whowhatwhere.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import whowhatwhere.controller.appearancecounter.IPNotesRowModel;

public class ManageIPNotesController extends SecondaryFXMLWithCRUDTableController<IPNotesRowModel>
{
	@FXML
	private TableView<IPNotesRowModel> tableEntries;
	@FXML
	private TableColumn<IPNotesRowModel, String> columnIP;
	@FXML
	private TableColumn<IPNotesRowModel, String> columnNotes;
	@FXML
	private Button btnClose;
	@FXML
	private Button btnAddRow;
	@FXML
	private Button btnRemoveRow;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		columnIP.setCellValueFactory(new PropertyValueFactory<IPNotesRowModel, String>("ipAddress"));
		columnIP.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnNotes.setCellValueFactory(new PropertyValueFactory<IPNotesRowModel, String>("notes"));
		columnNotes.setCellFactory(TextFieldTableCell.forTableColumn());
		
		tableEntries.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		GUIController.setCommonGraphicOnLabeled(btnAddRow, GUIController.CommonGraphicImages.ADD);
		GUIController.setCommonGraphicOnLabeled(btnRemoveRow, GUIController.CommonGraphicImages.REMOVE);
		GUIController.setCommonGraphicOnLabeled(btnClose, GUIController.CommonGraphicImages.OK);
	}
	
	public TableView<IPNotesRowModel> getTable()
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
	
	public TableColumn<IPNotesRowModel, String> getColumnIP()
	{
		return columnIP;
	}
	
	public TableColumn<IPNotesRowModel, String> getColumnNotes()
	{
		return columnNotes;
	}
}
