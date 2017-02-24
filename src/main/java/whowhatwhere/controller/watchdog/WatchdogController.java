/*******************************************************************************
 * Who What Where
 * Copyright (C) 2017  ck3ck3
 * https://github.com/ck3ck3/WhoWhatWhere
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package whowhatwhere.controller.watchdog;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.util.Callback;
import numbertextfield.NumberTextField;
import whowhatwhere.controller.GUIController;
import whowhatwhere.model.networksniffer.watchdog.PacketTypeToMatch;
import javafx.scene.control.ScrollPane;

public class WatchdogController implements Initializable
{
	private final static String startWatchdogImageLocation = "/buttonGraphics/watchdogIcon.png";

	@FXML
	private Button btnAddRow;
	@FXML
	private Button btnEditRow;
	@FXML
	private Button btnRemoveRow;
	@FXML
	private Button btnClose;
	@FXML
	private MenuButton menubtnLoadRuleList;
	@FXML
	private Button btnSaveRuleList;
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
	@FXML
	private Label labelRuleList;
	@FXML
	private ScrollPane scrollPane;
	

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		setColumnCellFactories();
		setGraphics();
		setButtonsDisableStates();

		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		numFieldCooldown.setMinValue(WatchdogUI.minCooldownValue);
		numFieldCooldown.setMaxValue(WatchdogUI.maxCooldownValue);
		
		//show scrollbars only when needed. When not needed, allow to stretch GUI (AnchorPane behavior)
		scrollPane.fitToWidthProperty().bind(scrollPane.widthProperty().greaterThan(scrollPane.getPrefWidth()));
		scrollPane.fitToHeightProperty().bind(scrollPane.heightProperty().greaterThan(scrollPane.getPrefHeight()));
	}

	private void setButtonsDisableStates()
	{
		BooleanBinding noRowSelected = table.getSelectionModel().selectedItemProperty().isNull();
		
		btnRemoveRow.disableProperty().bind(noRowSelected);
		btnMoveUp.disableProperty().bind(noRowSelected);
		btnMoveDown.disableProperty().bind(noRowSelected);

		btnEditRow.setDisable(true); //init to disable since no item is selected on startup
		table.getSelectionModel().getSelectedIndices().addListener((ListChangeListener<Integer>) change -> btnEditRow.setDisable(change.getList().size() != 1));
		
		btnSaveRuleList.setDisable(true); //init to disable since the list starts empty
		table.getItems().addListener((ListChangeListener<PacketTypeToMatch>) change -> btnSaveRuleList.setDisable(change.getList().size() == 0));
	}

	private void setGraphics()
	{
		GUIController.setCommonGraphicOnLabeled(labelTableHeader, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip headerTooltip = new Tooltip("Watchdog inspects network traffic and issues a user-customized notification when a packet matches the conditions specified in a rule.");
		headerTooltip.setWrapText(true);
		headerTooltip.setMaxWidth(420);
		labelTableHeader.setTooltip(headerTooltip);
		
		GUIController.setCommonGraphicOnLabeled(labelRuleList, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip ruleListTooltip = new Tooltip("The rules are checked in the order that they appear. If a packet matches a rule, the remaining rules will not be checked.");
		ruleListTooltip.setWrapText(true);
		ruleListTooltip.setMaxWidth(400);
		labelRuleList.setTooltip(ruleListTooltip);
		
		GUIController.setCommonGraphicOnLabeled(labelCooldownSeconds, GUIController.CommonGraphicImages.TOOLTIP);
		Tooltip cooldownTooltip = new Tooltip("In order to avoid getting flooded with messages, matches that occur during a cooldown period will be ignored and not issue a notification.\nMinimal cooldown period is "
				+ WatchdogUI.minCooldownValue + " seconds.");
		cooldownTooltip.setWrapText(true);
		cooldownTooltip.setMaxWidth(350);
		cooldownTooltip.setAnchorLocation(AnchorLocation.WINDOW_TOP_RIGHT);
		labelCooldownSeconds.setTooltip(cooldownTooltip);
		
		GUIController.setCommonGraphicOnLabeled(btnAddRow, GUIController.CommonGraphicImages.ADD);
		GUIController.setCommonGraphicOnLabeled(btnEditRow, GUIController.CommonGraphicImages.EDIT);
		GUIController.setCommonGraphicOnLabeled(btnRemoveRow, GUIController.CommonGraphicImages.REMOVE);
		GUIController.setCommonGraphicOnLabeled(btnMoveUp, GUIController.CommonGraphicImages.UP);
		GUIController.setCommonGraphicOnLabeled(btnMoveDown, GUIController.CommonGraphicImages.DOWN);
		GUIController.setCommonGraphicOnLabeled(btnSaveRuleList, GUIController.CommonGraphicImages.SAVE);
		GUIController.setCommonGraphicOnLabeled(menubtnLoadRuleList, GUIController.CommonGraphicImages.LOAD);

		GUIController.setCommonGraphicOnLabeled(btnConfigureHotkey, GUIController.CommonGraphicImages.HOTKEY);

		GUIController.setCommonGraphicOnLabeled(btnStop, GUIController.CommonGraphicImages.STOP);
		GUIController.setGraphicForLabeledControl(btnStart, startWatchdogImageLocation, ContentDisplay.LEFT);
		btnStart.setGraphicTextGap(6);
	}

	private void setColumnCellFactories()
	{
		columnMsgText.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("messageText"));
		columnMsgOutputMethod.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("messageOutputMethod"));
		columnPacketDirection.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("packetDirection"));
		columnIP.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("ipAddress"));
		columnProtocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
		columnSrcPort.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("srcPort"));
		columnDstPort.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("dstPort"));
		columnPacketSize.setCellValueFactory(new PropertyValueFactory<PacketTypeToMatch, String>("packetSize"));

		columnIP.setCellFactory(new Callback<TableColumn<PacketTypeToMatch, String>, TableCell<PacketTypeToMatch, String>>()
		{
			@Override
			public TableCell<PacketTypeToMatch, String> call(TableColumn<PacketTypeToMatch, String> param)
			{
				return new TableCell<PacketTypeToMatch, String>()
				{
					@Override
					protected void updateItem(String item, boolean empty)
					{
						super.updateItem(item, empty);

						setText(item);
						setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
					}
				};
			}
		});
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

	public MenuButton getMenuBtnLoadRuleList()
	{
		return menubtnLoadRuleList;
	}

	public Button getBtnSaveRuleList()
	{
		return btnSaveRuleList;
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
