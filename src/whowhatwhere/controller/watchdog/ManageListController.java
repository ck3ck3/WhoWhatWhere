package whowhatwhere.controller.watchdog;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import whowhatwhere.controller.SecondaryFXMLWithCRUDTableController;
import whowhatwhere.model.networksniffer.NetworkSniffer;
import whowhatwhere.model.networksniffer.PacketDirection;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;

public class ManageListController extends SecondaryFXMLWithCRUDTableController<PacketTypeToMatch>
{
	@FXML
	private Button btnAddRow;
	@FXML
	private Button btnRemoveRow;
	@FXML
	private Button btnClose;
	@FXML
	private MenuButton menubtnLoadPreset;
	@FXML
	private Button btnSavePreset;
	@FXML
	private TableView<PacketTypeToMatch> tableEntries;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnPacketDirection;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnIP;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnNetmask;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnUserNotes;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnPacketSize;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnPacketSizeSmaller;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnPacketSizeEquals;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnPacketSizeGreater;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnProtocol;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnSrcPortSmaller;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnSrcPortEquals;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnSrcPortGreater;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnSrcPort;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnDstPort;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnDstPortSmaller;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnDstPortEquals;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnDstPortGreater;
	@FXML
	private AnchorPane paneRoot;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		columnPacketDirection.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("packetDirection"));
		columnPacketDirection.setCellFactory(ComboBoxTableCell.forTableColumn(PacketDirection.getPacketDirectionStrings()));
		
		columnIP.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("ipAddress"));
		columnIP.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnNetmask.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("netmask"));
		columnNetmask.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnUserNotes.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("userNotes")); //CellFactory combo and values will be set from setUserNotesComboValues(String[] values)
				
		columnPacketSizeSmaller.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("packetSizeSmaller"));
		columnPacketSizeSmaller.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnPacketSizeEquals.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("packetSizeEquals"));
		columnPacketSizeEquals.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnPacketSizeGreater.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("packetSizeGreater"));
		columnPacketSizeGreater.setCellFactory(TextFieldTableCell.forTableColumn());

		columnProtocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
		String[] protocols = new String[NetworkSniffer.supportedProtocols.length + 1];
		protocols[0] = PacketTypeToMatch.protocol_ANY;
		System.arraycopy(NetworkSniffer.supportedProtocols, 0, protocols, 1, NetworkSniffer.supportedProtocols.length); //add the rest of the protocols
		columnProtocol.setCellFactory(ComboBoxTableCell.forTableColumn(protocols));
		
		columnSrcPortSmaller.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("srcPortSmaller"));
		columnSrcPortSmaller.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnSrcPortEquals.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("srcPortEquals"));
		columnSrcPortEquals.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnSrcPortGreater.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("srcPortGreater"));
		columnSrcPortGreater.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnDstPortSmaller.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("dstPortSmaller"));
		columnDstPortSmaller.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnDstPortEquals.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("dstPortEquals"));
		columnDstPortEquals.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnDstPortGreater.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("dstPortGreater"));
		columnDstPortGreater.setCellFactory(TextFieldTableCell.forTableColumn());

		tableEntries.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		EventHandler<KeyEvent> enterKeyEventHandler = ke ->
		{
			if (ke.getCode().equals(KeyCode.ENTER))
				btnClose.fire();
		};

		paneRoot.setOnKeyPressed(enterKeyEventHandler);
		tableEntries.setOnKeyPressed(enterKeyEventHandler);
	}
	
	public void setUserNotesComboValues(Object[] values)
	{
		String[] notes = new String[values.length + 1];
		notes[0] = PacketTypeToMatch.userNotes_ANY;
		System.arraycopy(values, 0, notes, 1, values.length); //add the actual notes
		columnUserNotes.setCellFactory(ComboBoxTableCell.forTableColumn(notes));
	}

	@Override
	public Button getBtnAddRow()
	{
		return btnAddRow;
	}

	@Override
	public Button getBtnRemoveRow()
	{
		return btnRemoveRow;
	}

	@Override
	public Button getCloseButton()
	{
		return btnClose;
	}

	public MenuButton getMenuBtnLoadPreset()
	{
		return menubtnLoadPreset;
	}

	public Button getBtnSavePreset()
	{
		return btnSavePreset;
	}

	@Override
	public TableView<PacketTypeToMatch> getTable()
	{
		return tableEntries;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnPacketDirection()
	{
		return columnPacketDirection;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnIP()
	{
		return columnIP;
	}

	public TableColumn<PacketTypeToMatch, String> getColumnNetmask()
	{
		return columnNetmask;
	}

	public TableColumn<PacketTypeToMatch, String> getColumnUserNotes()
	{
		return columnUserNotes;
	}
		
	public TableColumn<PacketTypeToMatch, String> getColumnPacketSizeSmaller()
	{
		return columnPacketSizeSmaller;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnPacketSizeEquals()
	{
		return columnPacketSizeEquals;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnPacketSizeGreater()
	{
		return columnPacketSizeGreater;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnProtocol()
	{
		return columnProtocol;
	}

	public TableColumn<PacketTypeToMatch, String> getColumnSrcPortSmaller()
	{
		return columnSrcPortSmaller;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnSrcPortEquals()
	{
		return columnSrcPortEquals;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnSrcPortGreater()
	{
		return columnSrcPortGreater;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnDstPortSmaller()
	{
		return columnDstPortSmaller;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnDstPortEquals()
	{
		return columnDstPortEquals;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnDstPortGreater()
	{
		return columnDstPortGreater;
	}
}
