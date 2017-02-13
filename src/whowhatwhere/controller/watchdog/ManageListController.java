package whowhatwhere.controller.watchdog;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;

public class ManageListController implements Initializable
{
	public enum RowMovementDirection {UP, DOWN}
	
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
	private TableView<PacketTypeToMatch> table;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnMsgText;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnMsgOutputMethod;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnPacketDirection;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnIP;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnUserNotes;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnPacketSize;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnProtocol;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnSrcPort;
	@FXML
	private TableColumn<PacketTypeToMatch, String> columnDstPort;
	@FXML
	private AnchorPane paneRoot;
	@FXML
	private Button btnMoveUp;
	@FXML
	private Button btnMoveDown;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		setColumnCellFactories();
		
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		btnMoveUp.setOnAction(generateRowMovementButtonHandlers(RowMovementDirection.UP));
		btnMoveDown.setOnAction(generateRowMovementButtonHandlers(RowMovementDirection.DOWN));
	}

	/**
	 * @param movementDirection - enum to specify if the row movement is up or down
	 * @return an EventHandler for the row movement buttons
	 */
	private EventHandler<ActionEvent> generateRowMovementButtonHandlers(RowMovementDirection movementDirection)
	{
		int moveToPosition = movementDirection == RowMovementDirection.UP ? -1 : 1;
		
		return event ->
		{
			ObservableList<PacketTypeToMatch> items = table.getItems();
			TableViewSelectionModel<PacketTypeToMatch> selectionModel = table.getSelectionModel();
			List<Integer> modifiableIndices = new ArrayList<Integer>(selectionModel.getSelectedIndices());
			int[] reSelectRows = new int[modifiableIndices.size()];
			int i = 0;
			
			if (movementDirection == RowMovementDirection.DOWN) //if we are moving down, we should start from the last index and go backwards
				Collections.reverse(modifiableIndices);
			
			for (Integer selectedIndex : modifiableIndices)
			{
				if (selectedIndex == (movementDirection == RowMovementDirection.UP ? 0 : items.size() - 1)) //if it's the first or last row (depending on movement direction), don't do anything
				{
					reSelectRows[i++] = selectedIndex;
					continue;
				}
				
				PacketTypeToMatch itemToReplace = items.set(selectedIndex + moveToPosition, items.get(selectedIndex));
				items.set(selectedIndex, itemToReplace);
				reSelectRows[i++] = selectedIndex + moveToPosition;
			}
			
			selectionModel.clearSelection();
			selectionModel.selectIndices(reSelectRows[0], reSelectRows);
			table.refresh();
		};		
	}

	private void setColumnCellFactories()
	{
		columnMsgText.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("messageText"));
		columnMsgOutputMethod.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("messageOutputMethod"));
		columnPacketDirection.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("packetDirection"));
		columnIP.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("ipAddress"));
		columnUserNotes.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("userNotes"));
		columnProtocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
		columnSrcPort.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("srcPort"));
		columnDstPort.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("dstPort"));
		columnPacketSize.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("packetSize"));
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

	public TableView<PacketTypeToMatch> getTable()
	{
		return table;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnPacketDirection()
	{
		return columnPacketDirection;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnIP()
	{
		return columnIP;
	}

	public TableColumn<PacketTypeToMatch, String> getColumnUserNotes()
	{
		return columnUserNotes;
	}
		
	public TableColumn<PacketTypeToMatch, String> getColumnProtocol()
	{
		return columnProtocol;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnMsgText()
	{
		return columnMsgText;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnMsgOutputMethod()
	{
		return columnMsgOutputMethod;
	}
	
	public TableColumn<PacketTypeToMatch, String> getColumnPacketSize()
	{
		return columnPacketSize;
	}

	public TableColumn<PacketTypeToMatch, String> getColumnSrcPort()
	{
		return columnSrcPort;
	}

	public TableColumn<PacketTypeToMatch, String> getColumnDstPort()
	{
		return columnDstPort;
	}

	public Button getBtnMoveUp()
	{
		return btnMoveUp;
	}
	
	public Button getBtnMoveDown()
	{
		return btnMoveDown;
	}
}
