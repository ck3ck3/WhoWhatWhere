package mostusedips.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import mostusedips.model.ipsniffer.IPToMatch;
import javafx.scene.layout.AnchorPane;

public class WatchdogController implements Initializable
{
	@FXML
	private Button btnAddRow;
	@FXML
	private Button btnEditRow;
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
		columnIP.setCellValueFactory(new PropertyValueFactory<IPToMatch, String>("ip"));
		columnProtocol.setCellValueFactory(new PropertyValueFactory<IPToMatch, String>("protocol"));
		columnSrcPort.setCellValueFactory(new PropertyValueFactory<IPToMatch, String>("srcPort"));
		columnDstPort.setCellValueFactory(new PropertyValueFactory<IPToMatch, String>("dstPort"));

		tableEntries.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		EventHandler<KeyEvent> enterKeyEventHandler = new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent ke)
			{
				if (ke.getCode().equals(KeyCode.ENTER))
					btnClose.fire();
			}
		};
		
		paneRoot.setOnKeyPressed(enterKeyEventHandler);
		tableEntries.setOnKeyPressed(enterKeyEventHandler);
	}

	public Button getBtnAddRow()
	{
		return btnAddRow;
	}

	public Button getBtnEditRow()
	{
		return btnEditRow;
	}

	public Button getBtnRemoveRow()
	{
		return btnRemoveRow;
	}

	public Button getBtnClose()
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

	public TableView<IPToMatch> getTable()
	{
		return tableEntries;
	}
}
