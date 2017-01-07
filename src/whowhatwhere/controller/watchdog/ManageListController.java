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
import whowhatwhere.model.ipsniffer.IPSniffer;
import whowhatwhere.model.ipsniffer.firstsight.IPToMatch;

public class ManageListController extends SecondaryFXMLWithCRUDTableController<IPToMatch>
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
	private TableView<IPToMatch> tableEntries;
	@FXML
	private TableColumn<IPToMatch, String> columnIP;
	@FXML
	private TableColumn<IPToMatch, String> columnProtocol;
	@FXML
	private TableColumn<IPToMatch, String> columnSrcPort;
	@FXML
	private TableColumn<IPToMatch, String> columnDstPort;
	@FXML
	private AnchorPane paneRoot;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		columnIP.setCellValueFactory(new PropertyValueFactory<IPToMatch, String>("ipAddress"));
		columnIP.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnProtocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));

		String[] protocols = new String[IPSniffer.supportedProtocols.length + 1];
		protocols[0] = IPToMatch.protocol_ANY;
		System.arraycopy(IPSniffer.supportedProtocols, 0, protocols, 1, IPSniffer.supportedProtocols.length); //add the rest of the protocols
		
		columnProtocol.setCellFactory(ComboBoxTableCell.forTableColumn(protocols));
		
		columnSrcPort.setCellValueFactory(new PropertyValueFactory<>("srcPort"));
		columnSrcPort.setCellFactory(TextFieldTableCell.forTableColumn());
		
		columnDstPort.setCellValueFactory(new PropertyValueFactory<>("dstPort"));
		columnDstPort.setCellFactory(TextFieldTableCell.forTableColumn());

		tableEntries.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		EventHandler<KeyEvent> enterKeyEventHandler = ke ->
		{
			if (ke.getCode().equals(KeyCode.ENTER))
				btnClose.fire();
		};
		
		paneRoot.setOnKeyPressed(enterKeyEventHandler);
		tableEntries.setOnKeyPressed(enterKeyEventHandler);
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
	public TableView<IPToMatch> getTable()
	{
		return tableEntries;
	}

	public TableColumn<IPToMatch, String> getColumnIP()
	{
		return columnIP;
	}
	
	public TableColumn<IPToMatch, String> getColumnProtocol()
	{
		return columnProtocol;
	}
	
	public TableColumn<IPToMatch, String> getColumnSrcPort()
	{
		return columnSrcPort;
	}
	
	public TableColumn<IPToMatch, String> getColumnDstPort()
	{
		return columnDstPort;
	}
}
