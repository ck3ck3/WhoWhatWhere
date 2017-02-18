package whowhatwhere.controller.watchdog;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.PopupWindow.AnchorLocation;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.GUIController;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;

public class WatchdogController implements Initializable
{
	private final static String addImageLocation = "/buttonGraphics/Add.png";
	private final static String editImageLocation = "/buttonGraphics/Edit.png";
	private final static String removeImageLocation = "/buttonGraphics/Delete.png";
	private final static String upImageLocation = "/buttonGraphics/Up.png";
	private final static String downImageLocation = "/buttonGraphics/Down.png";
	private final static String loadImageLocation = "/buttonGraphics/Load.png";
	private final static String saveImageLocation = "/buttonGraphics/Save.png";
	
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
	@FXML
	private CheckBox chkboxHotkey;
	@FXML
	private HBox paneHotkeyConfig;
	@FXML
	private Button btnConfigureHotkey;
	@FXML
	private Label labelCurrHotkey;
	@FXML
	private Button btnStart;
	@FXML
	private Button btnStop;
	@FXML
	private RadioButton radioStopAfterMatch;
	@FXML
	private ToggleGroup tglStopOrContinue;
	@FXML
	private RadioButton radioKeepLooking;
	@FXML
	private AnchorPane paneCooldown;
	@FXML
	private NumberTextField numFieldCooldown;
	@FXML
	private AnchorPane paneConfig;
	@FXML
	private Label labelCooldownSeconds;
	@FXML
	private Label labelTableHeader;
	@FXML
	private AnchorPane paneTableAndControls;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		setColumnCellFactories();
		setButtonGraphics();
		
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		numFieldCooldown.setMinValue(WatchdogUI.minCooldownValue);
		numFieldCooldown.setMaxValue(WatchdogUI.maxCooldownValue);
		
		GUIController.setTooltipGraphic(labelCooldownSeconds);
		Tooltip cooldownTooltip = new Tooltip("In order to avoid getting flooded with messages, matches that occur during a cooldown period will be ignored and not issue a notification.\nMinimal cooldown period is " + WatchdogUI.minCooldownValue + " seconds.");
		cooldownTooltip.setWrapText(true);
		cooldownTooltip.setMaxWidth(350);
		cooldownTooltip.setAnchorLocation(AnchorLocation.WINDOW_TOP_RIGHT);
		labelCooldownSeconds.setTooltip(cooldownTooltip);
		
		GUIController.setTooltipGraphic(labelTableHeader);
		Tooltip headerTooltip = new Tooltip("Watchdog inspects network traffic in the background and issues a user-customized notification when a packet matches the conditions specified in an entry. The entries are checked in the order that they appear. If a packet matches an entry, the remaining entries will not be checked.");
		headerTooltip.setWrapText(true);
		headerTooltip.setMaxWidth(420);
		labelTableHeader.setTooltip(headerTooltip);
		
	}

	private void setButtonGraphics()
	{
		GUIController.setGraphicForLabeledControl(btnAddRow, addImageLocation, ContentDisplay.LEFT);
		GUIController.setGraphicForLabeledControl(btnEditRow, editImageLocation, ContentDisplay.LEFT);
		GUIController.setGraphicForLabeledControl(btnRemoveRow, removeImageLocation, ContentDisplay.LEFT);
		GUIController.setGraphicForLabeledControl(btnMoveUp, upImageLocation, ContentDisplay.LEFT);
		GUIController.setGraphicForLabeledControl(btnMoveDown, downImageLocation, ContentDisplay.LEFT);
		GUIController.setGraphicForLabeledControl(btnSavePreset, saveImageLocation, ContentDisplay.LEFT);
		GUIController.setGraphicForLabeledControl(menubtnLoadPreset, loadImageLocation, ContentDisplay.LEFT);
		
		GUIController.setConfigureHotkeyGraphic(btnConfigureHotkey);
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
	
	public CheckBox getChkboxHotkey()
	{
		return chkboxHotkey;
	}

	public HBox getPaneHotkeyConfig()
	{
		return paneHotkeyConfig;
	}

	public Button getBtnConfigureHotkey()
	{
		return btnConfigureHotkey;
	}

	public Label getLabelCurrHotkey()
	{
		return labelCurrHotkey;
	}

	public Button getBtnStart()
	{
		return btnStart;
	}

	public Button getBtnStop()
	{
		return btnStop;
	}

	public RadioButton getRadioStopAfterMatch()
	{
		return radioStopAfterMatch;
	}

	public RadioButton getRadioKeepLooking()
	{
		return radioKeepLooking;
	}

	public AnchorPane getPaneCooldown()
	{
		return paneCooldown;
	}

	public NumberTextField getNumFieldCooldown()
	{
		return numFieldCooldown;
	}

	public AnchorPane getPaneConfig()
	{
		return paneConfig;
	}

	public AnchorPane getPaneTableAndControls()
	{
		return paneTableAndControls;
	}
}
